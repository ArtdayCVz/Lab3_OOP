package com.example.checkers.ui;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import com.example.checkers.R;
import timber.log.Timber;

public final class LanguageManager {

    public enum AppLanguage {
        SYSTEM("", R.string.language_system),
        ENGLISH("en", R.string.language_english),
        UKRAINIAN("uk", R.string.language_ukrainian);

        public final String tag;
        @StringRes
        public final int label;

        AppLanguage(String tag, @StringRes int label) {
            this.tag = tag;
            this.label = label;
        }
    }

    private LanguageManager() {
    }

    public static AppLanguage current() {
        String tags = AppCompatDelegate.getApplicationLocales().toLanguageTags();
        for (AppLanguage language : AppLanguage.values()) {
            if (language != AppLanguage.SYSTEM && tags.startsWith(language.tag)) return language;
        }
        return AppLanguage.SYSTEM;
    }

    public static void apply(AppLanguage language) {
        Timber.i("Language changed to '%s'", language.tag.isEmpty() ? "system" : language.tag);
        LocaleListCompat locales = language == AppLanguage.SYSTEM
                ? LocaleListCompat.getEmptyLocaleList()
                : LocaleListCompat.forLanguageTags(language.tag);
        AppCompatDelegate.setApplicationLocales(locales);
    }
}
