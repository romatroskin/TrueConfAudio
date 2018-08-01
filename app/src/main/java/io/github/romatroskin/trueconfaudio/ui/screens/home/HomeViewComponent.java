package io.github.romatroskin.trueconfaudio.ui.screens.home;

import dagger.Component;
import io.github.romatroskin.trueconfaudio.ui.MainActivityComponent;
import io.github.romatroskin.trueconfaudio.ui.screens.ViewScope;

@ViewScope
@Component(dependencies = MainActivityComponent.class, modules = HomeViewModule.class)
interface HomeViewComponent {
    void inject(HomeView view);
}
