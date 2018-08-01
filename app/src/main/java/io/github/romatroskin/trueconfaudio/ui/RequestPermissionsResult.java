package io.github.romatroskin.trueconfaudio.ui;

import android.support.annotation.NonNull;

public class RequestPermissionsResult {
    private final int requestCode;
    private final String[] permissions;
    private final int[] grantResults;

    public RequestPermissionsResult(int requestCode,
                                    @NonNull String[] permissions,
                                    @NonNull int[] grantResults) {
        this.requestCode = requestCode;
        this.permissions = permissions;
        this.grantResults = grantResults;
    }
}
