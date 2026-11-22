package checkers.ui;

import android.content.Context;
import checkers.game.Difficulty;
import timber.log.Timber;

public final class Settings {
    private static final String PREFS = "checkers_prefs";
    private static final String KEY_DIFFICULTY = "difficulty";

    private Settings() {
    }

    public static Difficulty getDifficulty(Context context) {
        String name = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_DIFFICULTY, Difficulty.MEDIUM.name());
        try {
            return Difficulty.valueOf(name);
        } catch (IllegalArgumentException e) {
            Timber.w("Unknown stored difficulty '%s', using MEDIUM", name);
            return Difficulty.MEDIUM;
        }
    }

    public static void setDifficulty(Context context, Difficulty difficulty) {
        Timber.i("Difficulty saved: %s", difficulty);
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_DIFFICULTY, difficulty.name())
                .apply();
    }
}
