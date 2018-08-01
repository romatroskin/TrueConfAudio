package io.github.romatroskin.trueconfaudio.ui;

import android.view.inputmethod.InputMethodManager;

import dagger.Component;
import io.github.romatroskin.trueconfaudio.MainAppComponent;

@ActivityScope
@Component(dependencies = MainAppComponent.class, modules = MainActivityModule.class)
public interface MainActivityComponent {
    void inject(MainActivity activity);

    MainActivity activity();
    InputMethodManager inputManager();
}
