package com.example.checkers.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import com.example.checkers.R;
import com.example.checkers.game.Difficulty;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.util.Arrays;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private GameViewModel viewModel;
    private BoardView boardView;
    private MaterialCardView statusCard;
    private TextView statusText;
    private LinearProgressIndicator thinkingIndicator;
    private TextView computerScoreText;
    private TextView playerScoreText;
    private TextView difficultyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Timber.d("MainActivity created");

        viewModel = new ViewModelProvider(this).get(GameViewModel.class);

        boardView = findViewById(R.id.boardView);
        statusCard = findViewById(R.id.statusCard);
        statusText = findViewById(R.id.statusText);
        thinkingIndicator = findViewById(R.id.thinkingIndicator);
        computerScoreText = findViewById(R.id.computerScoreText);
        playerScoreText = findViewById(R.id.playerScoreText);
        difficultyButton = findViewById(R.id.difficultyButton);

        boardView.setOnSquareTapListener(viewModel::onSquareTapped);
        findViewById(R.id.newGameButton).setOnClickListener(v -> viewModel.newGame());
        difficultyButton.setOnClickListener(v -> showDifficultyDialog());
        findViewById(R.id.languageButton).setOnClickListener(v -> showLanguageDialog());

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
        difficultyButton.setText(getString(R.string.button_difficulty,
                getString(difficultyLabel(viewModel.getDifficulty()))));
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

    @StringRes
    private static int difficultyLabel(Difficulty difficulty) {
        switch (difficulty) {
            case EASY:
                return R.string.difficulty_easy;
            case MEDIUM:
                return R.string.difficulty_medium;
            default:
                return R.string.difficulty_hard;
        }
    }

    private void showDifficultyDialog() {
        final Difficulty[] levels = Difficulty.values();
        String[] labels = new String[levels.length];
        for (int i = 0; i < levels.length; i++) labels[i] = getString(difficultyLabel(levels[i]));
        int checked = Arrays.asList(levels).indexOf(viewModel.getDifficulty());

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.difficulty_title)
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    viewModel.setDifficulty(levels[which]);
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showLanguageDialog() {
        final LanguageManager.AppLanguage[] languages = LanguageManager.AppLanguage.values();
        String[] labels = new String[languages.length];
        for (int i = 0; i < languages.length; i++) labels[i] = getString(languages[i].label);
        int checked = Arrays.asList(languages).indexOf(LanguageManager.current());

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.language_title)
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    dialog.dismiss();
                    LanguageManager.apply(languages[which]);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
