package checkers.ui;

import android.content.Context;
import checkers.game.Difficulty;
import checkers.game.Side;
import timber.log.Timber;

public final class Settings {
    private static final String PREFS = "checkers_prefs";
    private static final String KEY_DIFFICULTY = "difficulty";
    private static final String KEY_SIDE = "human_side";

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

    public static Side getHumanSide(Context context) {
        String name = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_SIDE, Side.WHITE.name());
        try {
            return Side.valueOf(name);
        } catch (IllegalArgumentException e) {
            Timber.w("Unknown stored side '%s', using WHITE", name);
            return Side.WHITE;
        }
    }

    public static void setHumanSide(Context context, Side side) {
        Timber.i("Player side saved: %s", side);
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_SIDE, side.name())
                .apply();
    }

    public static void setDifficulty(Context context, Difficulty difficulty) {
        Timber.i("Difficulty saved: %s", difficulty);
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_DIFFICULTY, difficulty.name())
                .apply();
    }
}
