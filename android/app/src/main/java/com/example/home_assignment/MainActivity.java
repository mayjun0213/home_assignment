package com.example.home_assignment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.extensions.ExtensionsManager;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import com.google.common.util.concurrent.ListenableFuture;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;

import android.util.Log;
import android.widget.Toast;
import androidx.camera.view.PreviewView;
import java.io.File;
import java.util.concurrent.ExecutionException;

public class MainActivity extends FlutterActivity {
    private static final String CHANNEL = "com.example.home_assignment/camera";
    private ImageCapture imageCapture;
    private boolean isFlashOn = false;
    private Camera camera;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);
        }
    }
    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        // Create MethodChannel
        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
                .setMethodCallHandler((call, result) -> {
                    switch (call.method) {
                        case "openCamera":
                            int viewId = call.argument("viewId");  // Get viewId from Flutter
                            openCamera(viewId);
                            result.success(null);
                            break;
                        case "takePicture":
                            takePicture(result);
                            break;
                        case "toggleFlash":
                            toggleFlash(result);
                            break;
                        default:
                            result.notImplemented();
                            break;
                    }
                });

        // Register the PlatformViewFactory for CameraX preview
        flutterEngine
                .getPlatformViewsController()
                .getRegistry()
                .registerViewFactory("CameraXPreview", new CameraXPreviewFactory());
    }

    private void openCamera(int viewId) {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(getApplicationContext());
        cameraProviderFuture.addListener(() -> {
            try {
                final ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                final ListenableFuture<ExtensionsManager> extensionsManagerFuture =
                        ExtensionsManager.getInstanceAsync(getApplicationContext(), cameraProvider);
                extensionsManagerFuture.addListener(() -> {
                    final CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                    imageCapture = new ImageCapture.Builder().build();
                    final Preview preview = new Preview.Builder().build();
                    PreviewView previewView = (PreviewView) getFlutterEngine()
                            .getPlatformViewsController()
                            .getPlatformViewById(viewId);  // Get the PlatformView by viewId
                    if (previewView == null) {
                        Log.e("CameraX", "PreviewView not found for viewId: " + viewId);
                        return;
                    }
                    preview.setSurfaceProvider(previewView.getSurfaceProvider());

                    cameraProvider.unbindAll();
                    camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
                }, ContextCompat.getMainExecutor(this));

            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePicture(MethodChannel.Result result) {
        if (imageCapture == null) {
            result.error("IMAGE_CAPTURE_NOT_READY", "Image capture is not ready", null);
            return;
        }

        File photoFile = new File(getExternalFilesDir(null), System.currentTimeMillis() + ".jpg");

        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                result.success(photoFile.getAbsolutePath());// Send file path to Flutter
                Log.d("CameraX", "Image saved successfully");
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                result.error("IMAGE_CAPTURE_FAILED", exception.getMessage(), null);
                Log.e("CameraX", "Error capturing image: " + exception.getMessage());
            }
        });
    }

    private void toggleFlash(MethodChannel.Result result){
        if (camera == null) {
            result.error("CAMERA_NOT_INITIALIZED", "Camera is not ready for toggling flash", null);
            return;
        }

        try {
            isFlashOn = !isFlashOn;
            camera.getCameraControl().enableTorch(isFlashOn);
            result.success(isFlashOn);  // Return the flash status
        } catch (Exception e) {
            result.error("FLASH_ERROR", "Failed to toggle flash", null);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //open camera
        } else {
            Toast.makeText(this, "Camera permission is required to use this app", Toast.LENGTH_SHORT).show();
        }
    }
}
