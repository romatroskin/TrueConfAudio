package io.github.romatroskin.trueconfaudio;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
final class MainAppModule {
    private Context context;
    MainAppModule(Context context) {
        this.context = context;
    }

    @Provides @Singleton
    Context provideContext() {
        return context;
    }
}
