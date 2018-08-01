package io.github.romatroskin.trueconfaudio.ui.screens.home;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.github.romatroskin.trueconfaudio.R;
import io.github.romatroskin.trueconfaudio.ui.MainActivityComponent;
import io.github.romatroskin.trueconfaudio.ui.screens.BaseView;

public class HomeView extends BaseView implements HomeScreen.View {

    @BindView(R.id.tv_time) TextView timeText;
    @BindView(R.id.ib_play) ImageButton playButton;
    @BindView(R.id.ib_record) ImageButton recordButton;

    @Inject HomePresenter presenter;
    private File outputFile;

    public HomeView(Context context) {
        super(context);
    }

    public HomeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HomeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected View inflateView(Context context) {
        return LayoutInflater.from(context).inflate(R.layout.home_view, this, false);
    }

    @Override
    public void inject(MainActivityComponent mainActivityComponent) {
        DaggerHomeViewComponent.builder().mainActivityComponent(mainActivityComponent)
                .homeViewModule(new HomeViewModule(this)).build().inject(this);
    }

    @Override
    public void onShow() {
        playButton.setEnabled(false);
        presenter.onLoad();
    }

    @Override
    public void onDestroy() {
        presenter.onSave();
    }

    @OnClick(R.id.ib_record)
    void record() {
        try {
            outputFile = File.createTempFile(
                    "test_record", "pcm",
                    getContext().getCacheDir()
            );
            recordButton.setEnabled(false);
            presenter.record(outputFile.getCanonicalPath());
        } catch (IOException e) {
            recordButton.setEnabled(true);
            e.printStackTrace();
        }
    }

    @OnClick(R.id.ib_play)
    void play() {
        if(outputFile != null && outputFile.exists()) {
            try {
                presenter.play(outputFile.getCanonicalPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setTime(int seconds) {
        timeText.setText(String.format(Locale.getDefault(), "00:%02d", seconds));
    }

    @Override
    public void onRecordComplete() {
        recordButton.setEnabled(true);
        playButton.setEnabled(true);
    }
}
