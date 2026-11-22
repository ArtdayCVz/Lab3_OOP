package checkers.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import checkers.R;
import checkers.game.Difficulty;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.Arrays;
import timber.log.Timber;

public class MenuActivity extends AppCompatActivity {

    private TextView difficultyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        Timber.d("MenuActivity created");

        difficultyButton = findViewById(R.id.difficultyButton);

        findViewById(R.id.playButton).setOnClickListener(v -> {
            Timber.i("Play pressed");
            startActivity(new Intent(this, GameActivity.class));
        });
        difficultyButton.setOnClickListener(v -> showDifficultyDialog());
        findViewById(R.id.languageButton).setOnClickListener(v -> showLanguageDialog());
        findViewById(R.id.rulesButton).setOnClickListener(v -> showRulesDialog());
        findViewById(R.id.exitButton).setOnClickListener(v -> {
            Timber.i("Exit pressed");
            finishAffinity();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDifficultyButton();
    }

    private void updateDifficultyButton() {
        difficultyButton.setText(getString(R.string.button_difficulty,
                getString(difficultyLabel(Settings.getDifficulty(this)))));
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
        int checked = Arrays.asList(levels).indexOf(Settings.getDifficulty(this));

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.difficulty_title)
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    Settings.setDifficulty(this, levels[which]);
                    updateDifficultyButton();
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

    private void showRulesDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.rules_title)
                .setMessage(R.string.rules_text)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
