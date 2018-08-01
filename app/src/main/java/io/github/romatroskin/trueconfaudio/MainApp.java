package io.github.romatroskin.trueconfaudio;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

import mortar.MortarScope;
import timber.log.Timber;

public class MainApp extends Application {
    final private static String ROOT_SCOPE_NAME = "TrueConfAudioRoot";

    private MortarScope rootScope;

    @Override
    public void onCreate() {
        super.onCreate();

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }

        LeakCanary.install(this);

        if(BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        MainGlobalState.get(getApplicationContext()).inject(this);
    }

    @Override
    public Object getSystemService(String name) {
        if (rootScope == null) {
            rootScope = MortarScope.buildRootScope().build(ROOT_SCOPE_NAME);
        }

        return rootScope.hasService(name)
                ? rootScope.getService(name)
                : super.getSystemService(name);
    }
}
