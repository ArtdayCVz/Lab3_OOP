package checkers;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import timber.log.Timber;

public class CheckersApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.Tree tree = BuildConfig.DEBUG ? new Timber.DebugTree() : new ReleaseTree();
        Timber.plant(tree);
        Timber.i("Application started, version %s", BuildConfig.VERSION_NAME);
    }

    private static final class ReleaseTree extends Timber.Tree {
        @Override
        protected boolean isLoggable(@Nullable String tag, int priority) {
            return priority >= Log.WARN;
        }

        @Override
        protected void log(int priority, @Nullable String tag, @NonNull String message, @Nullable Throwable t) {
            String text = t != null ? message + "\n" + Log.getStackTraceString(t) : message;
            Log.println(priority, tag != null ? tag : "Checkers", text);
        }
    }
}
