package io.github.romatroskin.trueconfaudio.ui.screens.home;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import io.github.romatroskin.trueconfaudio.ui.MainActivity;
import io.github.romatroskin.trueconfaudio.ui.MainActivityComponent;
import io.github.romatroskin.trueconfaudio.ui.RequestPermissionsResult;
import io.github.romatroskin.trueconfaudio.ui.screens.BaseView;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

public class HomeView extends BaseView implements HomeScreen.View {
    private final static int RECORD_AUDIO_PERMISSIONS_REQUEST = 1337;

    @BindView(R.id.tv_time) TextView timeText;
    @BindView(R.id.ib_play) ImageButton playButton;
    @BindView(R.id.ib_record) ImageButton recordButton;

    @Inject HomePresenter presenter;
    @Inject MainActivity activity;
    @Inject PublishSubject<RequestPermissionsResult> permissions;
    private File outputFile;
    private Disposable permissionsSub;

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
        permissionsSub = permissions.subscribe(requestPermissionsResult -> {
            if(requestPermissionsResult.getRequestCode() == RECORD_AUDIO_PERMISSIONS_REQUEST) {
                for(int res : requestPermissionsResult.getGrantResults()) {
                    if(res != PackageManager.PERMISSION_GRANTED) {
                        onError("RECORD_AUDIO permission should be granted");
                        recordButton.setEnabled(false);
                    }
                }
            }
        });

        if(ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[] { Manifest.permission.RECORD_AUDIO },
                    RECORD_AUDIO_PERMISSIONS_REQUEST);
        }


        playButton.setEnabled(false);
        presenter.onLoad();
    }

    @Override
    public void onDestroy() {
        presenter.onSave();
        permissionsSub.dispose();
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
            onError("Error creating temporary file.");
            e.printStackTrace();
        }
    }

    @OnClick(R.id.ib_play)
    void play() {
        if(outputFile != null && outputFile.exists()) {
            try {
                presenter.play(outputFile.getCanonicalPath());
            } catch (IOException e) {
                onError("Error reading temporary file.");
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

    @Override
    public void onError(String message) {
        Snackbar.make(this, message, Snackbar.LENGTH_LONG);
    }
}
