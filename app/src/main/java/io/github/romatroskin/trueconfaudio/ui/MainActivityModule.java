package io.github.romatroskin.trueconfaudio.ui;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;

import dagger.Module;
import dagger.Provides;
import io.reactivex.subjects.PublishSubject;

@Module
final class MainActivityModule {
    private MainActivity activity;
    MainActivityModule(MainActivity activity) {
        this.activity = activity;
    }

    @Provides @ActivityScope
    MainActivity provideActivity() {
        return activity;
    }

    @Provides @ActivityScope
    InputMethodManager provideInputManager(Context context) {
        return (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Provides @ActivityScope
    PublishSubject<RequestPermissionsResult> provideRequestPermissionsResult(MainActivity activity) {
        return activity.getRequestPermissionsResults();
    }
}
