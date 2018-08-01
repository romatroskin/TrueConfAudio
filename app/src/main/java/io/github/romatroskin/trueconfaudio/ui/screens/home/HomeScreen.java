package io.github.romatroskin.trueconfaudio.ui.screens.home;

import java.io.FileNotFoundException;

import io.github.romatroskin.trueconfaudio.R;
import io.github.romatroskin.trueconfaudio.ui.screens.IPresenter;
import io.github.romatroskin.trueconfaudio.ui.screens.IScreen;
import io.github.romatroskin.trueconfaudio.ui.screens.IView;
import io.github.romatroskin.trueconfaudio.ui.screens.Screen;

@Screen(title = R.string.app_name, layout = R.layout.home_screen)
public class HomeScreen implements IScreen {
    interface View extends IView {
        void setTime(int seconds);
        void onRecordComplete();
        void onError(String message);
    }

    interface Presenter extends IPresenter {
        void record(String filename) throws FileNotFoundException;
        void play(String filename) throws FileNotFoundException;
    }
}
