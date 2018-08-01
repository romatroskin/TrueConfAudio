package io.github.romatroskin.trueconfaudio.ui.screens;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import butterknife.ButterKnife;
import io.github.romatroskin.trueconfaudio.ui.MainActivityComponent;

@ViewScope
public abstract class BaseView extends FrameLayout {
    public BaseView(Context context) {
        this(context, null);
    }

    public BaseView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.addView(inflateView(context));
    }

    protected abstract View inflateView(Context context);

    public abstract void inject(MainActivityComponent mainActivityComponent);

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.onShow();
    }

    @Override
    public void onDetachedFromWindow() {
        this.onDestroy();
        super.onDetachedFromWindow();
    }

    protected abstract void onShow();
    protected abstract void onDestroy();
}
