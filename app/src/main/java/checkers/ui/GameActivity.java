package checkers.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import checkers.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import timber.log.Timber;

public class GameActivity extends AppCompatActivity {

    private GameViewModel viewModel;
    private BoardView boardView;
    private MaterialCardView statusCard;
    private TextView statusText;
    private LinearProgressIndicator thinkingIndicator;
    private TextView computerScoreText;
    private TextView playerScoreText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Timber.d("GameActivity created");

        viewModel = new ViewModelProvider(this).get(GameViewModel.class);
        viewModel.setDifficulty(Settings.getDifficulty(this));

        boardView = findViewById(R.id.boardView);
        statusCard = findViewById(R.id.statusCard);
        statusText = findViewById(R.id.statusText);
        thinkingIndicator = findViewById(R.id.thinkingIndicator);
        computerScoreText = findViewById(R.id.computerScoreText);
        playerScoreText = findViewById(R.id.playerScoreText);

        boardView.setOnSquareTapListener(viewModel::onSquareTapped);
        findViewById(R.id.newGameButton).setOnClickListener(v -> viewModel.newGame());
        findViewById(R.id.menuButton).setOnClickListener(v -> finish());

        viewModel.getUi().observe(this, this::render);
    }

    private void render(GameUiState ui) {
        boardView.render(ui.board, ui.selectedPath, ui.targets, ui.hintPieces, ui.lastMovePath);

        statusText.setText(statusText(ui.status));
        statusCard.setCardBackgroundColor(ContextCompat.getColor(this, statusColor(ui.status)));
        thinkingIndicator.setVisibility(ui.status == Status.COMPUTER_THINKING ? View.VISIBLE : View.INVISIBLE);

        playerScoreText.setText(
                getResources().getQuantityString(R.plurals.pieces_you, ui.humanPieces, ui.humanPieces));
        computerScoreText.setText(
                getResources().getQuantityString(R.plurals.pieces_computer, ui.computerPieces, ui.computerPieces));
    }

    @StringRes
    private static int statusText(Status status) {
        switch (status) {
            case YOUR_TURN:
                return R.string.status_your_turn;
            case MUST_CAPTURE:
                return R.string.status_must_capture;
            case COMPUTER_THINKING:
                return R.string.status_thinking;
            case YOU_WON:
                return R.string.status_you_won;
            case COMPUTER_WON:
                return R.string.status_computer_won;
            default:
                return R.string.status_draw;
        }
    }

    @ColorRes
    private static int statusColor(Status status) {
        switch (status) {
            case YOUR_TURN:
                return R.color.status_your_turn;
            case MUST_CAPTURE:
                return R.color.status_must_capture;
            case COMPUTER_THINKING:
                return R.color.status_thinking;
            case YOU_WON:
                return R.color.status_win;
            case COMPUTER_WON:
                return R.color.status_lose;
            default:
                return R.color.status_draw;
        }
    }
}
