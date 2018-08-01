package io.github.romatroskin.trueconfaudio;

import android.content.Context;

public class MainGlobalState {
    private static MainGlobalState sInstance;
    private final MainAppComponent component;

    private MainGlobalState(Context context) {
        this.component = DaggerMainAppComponent.builder()
                .mainAppModule(new MainAppModule(context))
                .build();
    }

    public static MainAppComponent get(Context context) {
        if(sInstance == null) {
            sInstance = new MainGlobalState(context);
        }

        return sInstance.get();
    }

    public MainAppComponent get() {
        return this.component;
    }
}
