package com.example.home_assignment;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.camera.view.PreviewView;
import io.flutter.plugin.platform.PlatformView;
import io.flutter.plugin.platform.PlatformViewFactory;

public class CameraXPreviewFactory extends PlatformViewFactory {

    public CameraXPreviewFactory() {
        super(null);
    }

    @NonNull
    @Override
    public PlatformView create(Context context, int viewId, Object args) {
        return new CameraXPreview(context);
    }
}

class CameraXPreview implements PlatformView {
    private final PreviewView previewView;

    CameraXPreview(Context context) {
        previewView = new PreviewView(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        previewView.setLayoutParams(params);
    }

    @Override
    public View getView() {
        return previewView;
    }

    @Override
    public void dispose() {
    }
}

