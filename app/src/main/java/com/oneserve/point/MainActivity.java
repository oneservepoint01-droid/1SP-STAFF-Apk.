package com.oneserve.point;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends Activity {
    private static final int FILE_CHOOSER_REQUEST = 4101;
    private static final int MEDIA_PERMISSION_REQUEST = 4102;

    private WebView webView;
    private ValueCallback<Uri[]> uploadCallback;
    private WebChromeClient.FileChooserParams pendingChooserParams;
    private Uri pendingCameraUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.rgb(16, 43, 80));
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);

        FrameLayout container = new FrameLayout(this);
        webView = new WebView(this);
        container.addView(webView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        setContentView(container);

        WebView.setWebContentsDebuggingEnabled(false);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setSupportMultipleWindows(false);
        settings.setMediaPlaybackRequiresUserGesture(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);

        final WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();

        webView.setWebViewClient(new WebViewClientCompat() {
            @Override
            public android.webkit.WebResourceResponse shouldInterceptRequest(
                    WebView view, WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
                if ("http".equals(scheme) || "https".equals(scheme)) {
                    return false;
                }
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                } catch (ActivityNotFoundException ignored) {
                    Toast.makeText(MainActivity.this, "இந்த இணைப்பைத் திறக்க முடியவில்லை", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view,
                    ValueCallback<Uri[]> filePathCallback,
                    FileChooserParams fileChooserParams) {
                if (uploadCallback != null) {
                    uploadCallback.onReceiveValue(null);
                }
                uploadCallback = filePathCallback;
                pendingChooserParams = fileChooserParams;
                pendingCameraUri = null;

                if (needsImagePermission(fileChooserParams)) {
                    String[] missing = getMissingMediaPermissions();
                    if (missing.length > 0) {
                        requestPermissions(missing, MEDIA_PERMISSION_REQUEST);
                        return true;
                    }
                }
                launchDocumentPicker();
                return true;
            }
        });

        webView.setBackgroundColor(Color.WHITE);
        webView.loadUrl("https://appassets.androidplatform.net/assets/index.html");
    }

    private boolean needsImagePermission(WebChromeClient.FileChooserParams params) {
        if (params == null || params.getAcceptTypes() == null) return false;
        for (String accept : params.getAcceptTypes()) {
            if (accept != null && (accept.contains("image") || accept.contains(".jpg")
                    || accept.contains(".jpeg") || accept.contains(".png"))) {
                return true;
            }
        }
        return false;
    }

    private String[] getMissingMediaPermissions() {
        ArrayList<String> missing = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= 34) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                missing.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                    != PackageManager.PERMISSION_GRANTED) {
                missing.add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED);
            }
        } else if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                missing.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else if (Build.VERSION.SDK_INT <= 32) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                missing.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
        return missing.toArray(new String[0]);
    }

    private void launchDocumentPicker() {
        if (uploadCallback == null || pendingChooserParams == null) return;

        try {
            Intent pickIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            pickIntent.addCategory(Intent.CATEGORY_OPENABLE);

            String[] mimeTypes = buildMimeTypes(pendingChooserParams.getAcceptTypes());
            if (mimeTypes.length == 1) {
                pickIntent.setType(mimeTypes[0]);
            } else {
                pickIntent.setType("*/*");
                pickIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            }
            pickIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,
                    pendingChooserParams.getMode() == WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE);

            Intent chooser = Intent.createChooser(pickIntent, "ONESERVE POINT - Select document");

            if (pendingChooserParams.isCaptureEnabled() && acceptsImages(mimeTypes)) {
                Intent cameraIntent = createCameraIntent();
                if (cameraIntent != null) {
                    chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{cameraIntent});
                }
            }
            startActivityForResult(chooser, FILE_CHOOSER_REQUEST);
        } catch (Exception e) {
            finishFileChooser(null);
            Toast.makeText(this, "File picker திறக்க முடியவில்லை. மீண்டும் முயற்சிக்கவும்.", Toast.LENGTH_LONG).show();
        }
    }

    private String[] buildMimeTypes(String[] acceptTypes) {
        Set<String> types = new LinkedHashSet<>();
        if (acceptTypes != null) {
            for (String raw : acceptTypes) {
                if (raw == null) continue;
                for (String part : raw.split(",")) {
                    String value = part.trim().toLowerCase(Locale.ROOT);
                    if (value.isEmpty()) continue;
                    if (value.startsWith(".")) {
                        if (value.equals(".pdf")) types.add("application/pdf");
                        else if (value.equals(".jpg") || value.equals(".jpeg")) types.add("image/jpeg");
                        else if (value.equals(".png")) types.add("image/png");
                        else if (value.equals(".webp")) types.add("image/webp");
                    } else if (value.contains("/")) {
                        types.add(value);
                    }
                }
            }
        }
        if (types.isEmpty()) types.add("*/*");
        return types.toArray(new String[0]);
    }

    private boolean acceptsImages(String[] types) {
        for (String type : types) {
            if (type.startsWith("image/") || type.equals("*/*")) return true;
        }
        return false;
    }

    private Intent createCameraIntent() {
        try {
            File folder = new File(getCacheDir(), "Pictures");
            if (!folder.exists() && !folder.mkdirs()) return null;
            String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            File photo = File.createTempFile("OSP_" + stamp + "_", ".jpg", folder);
            pendingCameraUri = FileProvider.getUriForFile(
                    this, getPackageName() + ".provider", photo);

            Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            camera.putExtra(MediaStore.EXTRA_OUTPUT, pendingCameraUri);
            camera.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            camera.setClipData(android.content.ClipData.newUri(getContentResolver(), "camera-photo", pendingCameraUri));
            return camera;
        } catch (IOException | IllegalArgumentException e) {
            pendingCameraUri = null;
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != FILE_CHOOSER_REQUEST) return;

        ArrayList<Uri> selected = new ArrayList<>();
        if (resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    if (uri != null) selected.add(uri);
                }
            } else if (data.getData() != null) {
                selected.add(data.getData());
            }
        } else if (resultCode == RESULT_OK && pendingCameraUri != null) {
            selected.add(pendingCameraUri);
        }

        if (selected.isEmpty() && resultCode == RESULT_OK && pendingCameraUri != null) {
            selected.add(pendingCameraUri);
        }
        Uri[] result = selected.isEmpty() ? null : selected.toArray(new Uri[0]);
        finishFileChooser(result);
    }

    private void finishFileChooser(Uri[] result) {
        if (uploadCallback != null) {
            uploadCallback.onReceiveValue(result);
            uploadCallback = null;
        }
        pendingChooserParams = null;
        pendingCameraUri = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MEDIA_PERMISSION_REQUEST) {
            // Continue to the Android document picker even if permission is denied.
            // SAF grants access only to the file(s) the user selects.
            launchDocumentPicker();
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (uploadCallback != null) {
            uploadCallback.onReceiveValue(null);
            uploadCallback = null;
        }
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
