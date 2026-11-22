package checkers.ui;

import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import checkers.game.ComputerPlayer;
import checkers.game.Difficulty;
import checkers.game.GameState;
import checkers.game.Move;
import checkers.game.Outcome;
import checkers.game.Pos;
import checkers.game.Side;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import timber.log.Timber;

public class GameViewModel extends ViewModel {
    private static final long MIN_THINK_MS = 400L;

    private Side human = Side.WHITE;
    private Side computer = Side.BLACK;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final MutableLiveData<GameUiState> ui = new MutableLiveData<>();

    private GameState state = GameState.initial();
    private Difficulty difficulty = Difficulty.MEDIUM;
    private List<Pos> path = Collections.emptyList();
    private Move lastMove;
    private boolean computerThinking;
    private int generation;

    public GameViewModel() {
        Timber.i("New game started (difficulty=%s)", difficulty);
        publish();
    }

    public LiveData<GameUiState> getUi() {
        return ui;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void newGame() {
        restart();
    }

    public void setHumanSide(Side side) {
        if (side == human) return;
        Timber.i("Player side changed: %s -> %s", human, side);
        human = side;
        computer = side.opponent();
        restart();
    }

    private void restart() {
        generation++;
        computerThinking = false;
        state = GameState.initial();
        path = Collections.emptyList();
        lastMove = null;
        Timber.i("New game started (player=%s, difficulty=%s)", human, difficulty);
        publish();
        if (state.getTurn() == computer) startComputerTurn();
    }

    public void setDifficulty(Difficulty value) {
        if (value == difficulty) return;
        Timber.i("Difficulty changed: %s -> %s", difficulty, value);
        difficulty = value;
        publish();
    }

    public void onSquareTapped(Pos pos) {
        if (state.getTurn() != human || state.getOutcome() != Outcome.ONGOING || computerThinking) {
            Timber.d("Tap on %s ignored (not the player's turn)", pos);
            return;
        }

        if (!path.isEmpty()) {
            List<Move> candidates = candidates();
            if (nextSquares(candidates).contains(pos)) {
                path = append(path, pos);
                Move finished = null;
                for (Move move : candidates) {
                    if (move.getPath().equals(path)) {
                        finished = move;
                        break;
                    }
                }
                if (finished != null) {
                    playHuman(finished);
                } else {
                    publish();
                }
                return;
            }
        }

        boolean canMove = false;
        for (Move move : state.getLegalMoves()) {
            if (move.getFrom().equals(pos)) {
                canMove = true;
                break;
            }
        }
        if (canMove) {
            path = Collections.singletonList(pos);
            Timber.d("Piece selected: %s", pos);
        } else {
            path = Collections.emptyList();
            Timber.d("Tap on %s: no piece of the player can move from here", pos);
        }
        publish();
    }

    private void playHuman(Move move) {
        Timber.i("Player move: %s", move);
        state = state.play(move);
        lastMove = move;
        path = Collections.emptyList();
        publish();
        afterMove();
    }

    private void afterMove() {
        Outcome outcome = state.getOutcome();
        if (outcome == Outcome.ONGOING) {
            if (state.getTurn() == computer) startComputerTurn();
        } else {
            Timber.i("Game over: %s", outcome);
        }
    }

    private void startComputerTurn() {
        computerThinking = true;
        publish();

        final int gen = generation;
        final GameState snapshot = state;
        final Difficulty level = difficulty;
        executor.execute(() -> {
            long startedAt = System.currentTimeMillis();
            ComputerPlayer player = new ComputerPlayer(level);
            Move move = null;
            try {
                move = player.chooseMove(snapshot);
            } catch (RuntimeException e) {
                Timber.e(e, "Computer failed to choose a move");
            }
            final long elapsed = System.currentTimeMillis() - startedAt;
            if (elapsed < MIN_THINK_MS) {
                try {
                    Thread.sleep(MIN_THINK_MS - elapsed);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            final Move chosen = move;
            final long nodes = player.getNodes();
            mainHandler.post(() -> finishComputerTurn(gen, chosen, nodes, elapsed));
        });
    }

    private void finishComputerTurn(int gen, Move move, long nodes, long elapsedMs) {
        if (gen != generation) {
            Timber.d("Discarding computer move from an abandoned game");
            return;
        }
        computerThinking = false;
        if (move != null) {
            Timber.i("Computer move: %s (%d positions, %d ms)", move, nodes, elapsedMs);
            state = state.play(move);
            lastMove = move;
        }
        publish();
        if (state.getOutcome() != Outcome.ONGOING) {
            Timber.i("Game over: %s", state.getOutcome());
        }
    }

    private void publish() {
        ui.setValue(buildUi());
    }

    private GameUiState buildUi() {
        List<Move> moves = state.getLegalMoves();
        Set<Pos> targets = path.isEmpty() ? Collections.<Pos>emptySet() : nextSquares(candidates());
        boolean mustCapture = state.getTurn() == human && !moves.isEmpty() && moves.get(0).isCapture();

        Set<Pos> hints = new LinkedHashSet<>();
        if (mustCapture && path.isEmpty()) {
            for (Move move : moves) hints.add(move.getFrom());
        }
        return new GameUiState(
                state.getBoard(),
                statusOf(mustCapture),
                path,
                targets,
                hints,
                lastMove != null ? lastMove.getPath() : Collections.<Pos>emptyList(),
                state.getBoard().count(human),
                state.getBoard().count(computer),
                human);
    }

    private Status statusOf(boolean mustCapture) {
        switch (state.getOutcome()) {
            case WHITE_WINS:
                return human == Side.WHITE ? Status.YOU_WON : Status.COMPUTER_WON;
            case BLACK_WINS:
                return human == Side.BLACK ? Status.YOU_WON : Status.COMPUTER_WON;
            case DRAW:
                return Status.DRAW;
            default:
                if (computerThinking || state.getTurn() == computer) return Status.COMPUTER_THINKING;
                return mustCapture ? Status.MUST_CAPTURE : Status.YOUR_TURN;
        }
    }

    private List<Move> candidates() {
        List<Move> result = new ArrayList<>();
        for (Move move : state.getLegalMoves()) {
            List<Pos> movePath = move.getPath();
            if (movePath.size() >= path.size() && movePath.subList(0, path.size()).equals(path)) {
                result.add(move);
            }
        }
        return result;
    }

    private Set<Pos> nextSquares(List<Move> candidates) {
        Set<Pos> result = new LinkedHashSet<>();
        for (Move move : candidates) {
            if (move.getPath().size() > path.size()) result.add(move.getPath().get(path.size()));
        }
        return result;
    }

    private static List<Pos> append(List<Pos> list, Pos pos) {
        List<Pos> copy = new ArrayList<>(list);
        copy.add(pos);
        return copy;
    }

    @Override
    protected void onCleared() {
        executor.shutdownNow();
        mainHandler.removeCallbacksAndMessages(null);
        Timber.d("GameViewModel cleared");
    }
}
