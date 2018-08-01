package io.github.romatroskin.trueconfaudio.ui.screens.home;

import dagger.Module;
import dagger.Provides;
import io.github.romatroskin.trueconfaudio.ui.screens.ViewScope;

@Module
class HomeViewModule {
    private final HomeView view;
    HomeViewModule(HomeView view) {
        this.view = view;
    }

    @Provides @ViewScope
    HomeView provideView() {
        return this.view;
    }
}
