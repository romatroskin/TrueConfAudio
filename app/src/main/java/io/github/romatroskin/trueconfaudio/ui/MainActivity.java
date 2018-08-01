package io.github.romatroskin.trueconfaudio.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import flow.Direction;
import flow.Flow;
import flow.KeyChanger;
import flow.KeyDispatcher;
import flow.State;
import flow.TraversalCallback;
import io.github.romatroskin.trueconfaudio.MainGlobalState;
import io.github.romatroskin.trueconfaudio.R;
import io.github.romatroskin.trueconfaudio.ui.screens.BaseView;
import io.github.romatroskin.trueconfaudio.ui.screens.Screen;
import io.github.romatroskin.trueconfaudio.ui.screens.home.HomeScreen;
import io.reactivex.subjects.PublishSubject;
import mortar.MortarScope;
import mortar.bundler.BundleServiceRunner;

import static mortar.MortarScope.buildChild;
import static mortar.MortarScope.findChild;

public class MainActivity extends AppCompatActivity {
    private final static String SERVICE_NAME = MainActivity.class.getName();
    private final static int RECORD_AUDIO_PERMISSIONS_REQUEST = 1337;

    @BindView(R.id.main_content) FrameLayout content;
    @BindView(R.id.main_toolbar) Toolbar toolbar;

    @Inject InputMethodManager inputManager;

    private MainActivityComponent component;

    private PublishSubject<RequestPermissionsResult> requestPermissionsResults;

    public MainActivityComponent component() {
        if (this.component == null) {
            this.component = DaggerMainActivityComponent.builder()
                    .mainAppComponent(MainGlobalState.get(getApplication()))
                    .mainActivityModule(new MainActivityModule(this))
                    .build();
        }

        return this.component;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        newBase = Flow.configure(newBase, this).defaultKey(new HomeScreen())
                .dispatcher(KeyDispatcher.configure(this, new ScreenChanger()).build())
                .install();

        super.attachBaseContext(newBase);
    }

    @Override
    public Object getSystemService(@NonNull String name) {
        MortarScope activityScope = findChild(getApplicationContext(), getScopeName());

        if (activityScope == null) {
            activityScope = buildChild(getApplicationContext())
                    .withService(BundleServiceRunner.SERVICE_NAME, new BundleServiceRunner())
                    .withService(MainActivity.SERVICE_NAME, component())
                    .build(getScopeName());
        }

        return activityScope.hasService(name)
                ? activityScope.getService(name)
                : super.getSystemService(name);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.RECORD_AUDIO },
                    RECORD_AUDIO_PERMISSIONS_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        requestPermissionsResults.onNext(
                new RequestPermissionsResult(requestCode, permissions, grantResults)
        );
    }

    public PublishSubject<RequestPermissionsResult> getRequestPermissionsResults() {
        return requestPermissionsResults;
    }

    private final class ScreenChanger implements KeyChanger {
        @Override
        public void changeKey(@Nullable State outgoingState, @NonNull State incomingState,
                              @NonNull Direction direction, @NonNull Map<Object, Context> incomingContexts,
                              @NonNull TraversalCallback callback) {
            final View focusedView = getCurrentFocus();
            if (focusedView != null) {
                inputManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
            }

            if (!incomingState.equals(outgoingState)) {
                if (outgoingState != null) {
                    outgoingState.save(content.getChildAt(0));
                    content.removeAllViews();
                }

                Object key = incomingState.getKey();
                final Context context = incomingContexts.get(key);
                final Class<?> keyClazz = key.getClass();
                if (keyClazz.isAnnotationPresent(Screen.class)) {
                    final Screen screen = keyClazz.getAnnotation(Screen.class);

                    toolbar.setTitle(screen.title());

                    final BaseView incomingView = (BaseView) LayoutInflater
                            .from(context).inflate(screen.layout(), content, false);
                    incomingView.inject(component);
                    incomingState.restore(incomingView);
                    content.addView(incomingView);
                }
            }

            callback.onTraversalCompleted();
        }
    }

    private String getScopeName() {
        return getClass().getName();
    }
}
