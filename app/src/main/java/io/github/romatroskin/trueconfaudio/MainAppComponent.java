package io.github.romatroskin.trueconfaudio;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = { MainAppModule.class })
public interface MainAppComponent {
    Context context();

    void inject(Application app);
}
