package com.activednc.webappmaker;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebHistoryItem;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.activednc.webappmaker.utils.AlertDialogFactory;
import com.activednc.webappmaker.utils.MediaHelper;
import com.activednc.webappmaker.utils.WebUrlDelegator;
import com.activednc.webappmaker.utils.WebViewDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.webview)
    WebView webView;

    private boolean homePressed = false;

    private WebUrlDelegator webUrlDelegator;

    private static final int REQUEST_CODE_FILECHOOSER = 0;
    private static final int REQUEST_CODE_FILECHOOSER5 = 1;

    /** WebView의 파일 업로드 메시지 콜백. */
    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mUploadMessages;
    private String imageFilePath = null;
    private String videoFilePath = null;

    private MediaHelper mediaHelper;
    private ArrayList<String> mList_UploadFile = new ArrayList<String>();
    public ArrayList<Uri> mList_UploadUri = new ArrayList<Uri>();

    private WebViewDialog webviewDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(this);
        }

        this.mediaHelper = new MediaHelper(getPackageManager());

        webUrlDelegator = new WebUrlDelegator(this);
        initWebView();
        webView.loadUrl(getString(R.string.main_url));
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().startSync();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().stopSync();
        }
    }

    private void initWebView(){
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setGeolocationEnabled(true);
        webSettings.setSupportMultipleWindows(true);

        webView.setWebViewClient(this.webViewClient);
        webView.setWebChromeClient(this.webChromeClient);
    }

    private WebViewClient webViewClient = new WebViewClient(){
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            if(!webUrlDelegator.delegateUrl(url)){
                view.loadUrl(url);
            }

            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                CookieSyncManager.getInstance().sync();
            }

            if(homePressed){
                homePressed = false;
                view.clearHistory();
            }
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            Dialog dialog = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                dialog = AlertDialogFactory.createConfirmDialog(MainActivity.this, getString(R.string.notice), error.getDescription().toString(), null);
            }else{
                dialog = AlertDialogFactory.createConfirmDialog(MainActivity.this, getString(R.string.notice), getString(R.string.error_message_network), null);
            }
            dialog.show();
        }

        @Override
        public void onFormResubmission(WebView view, Message dontResend, Message resend) {
            resend.sendToTarget();
        }
    };

    private WebChromeClient webChromeClient = new WebChromeClient(){

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            callback.invoke(origin, true, false);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
            // TODO Auto-generated method stub
            new AlertDialog.Builder(view.getContext()).setTitle(R.string.notice).setMessage(message)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            result.confirm();
                        }
                    }).setCancelable(false).create().show();
            return true;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
            // TODO Auto-generated method stub
            new AlertDialog.Builder(view.getContext()).setTitle(R.string.notice).setMessage(message)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            result.confirm();
                        }
                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    result.cancel();
                }
            }).setCancelable(false).create().show();
            return true;
        }

        // For Android 5.0+
        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {

            mUploadMessages = filePathCallback;
            startActivityForResult(mediaHelper.getPickIntent(mList_UploadUri, "Upload Chooser"), REQUEST_CODE_FILECHOOSER5);

            return true;
        }

        /**
         * Open file chooser. For Android > 4.1
         *
         * @param uploadFile
         *            the upload file
         * @param acceptType
         *            the accept type
         * @param capture
         *            the capture
         */
        public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String capture) {
            openFileChooser(uploadFile, acceptType);
        }

        /**
         * Open file chooser. For Android 3.0+
         *
         * @param uploadFile
         *            the upload file
         * @param acceptType
         *            the accept type
         */
        public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType) {
            mUploadMessage = uploadFile;
            try {
                Intent chooser = null;
                if(acceptType.contains("video/*")){
                    chooser = Intent.createChooser(createVideoCaptureIntent(), "Upload Chooser");

                    // Set camera intent to file chooser
                    chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS
                            , new Parcelable[] { createImageOpenableIntent(), createImageCaptureIntent(), createVideoOpenableIntent() });
                }else{
                    chooser = Intent.createChooser(createImageCaptureIntent(), "Upload Chooser");

                    // Set camera intent to file chooser
                    chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS
                            , new Parcelable[] { createImageOpenableIntent() });
                }
                // On select image call onActivityResult method of activity
                startActivityForResult(chooser, REQUEST_CODE_FILECHOOSER);

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), R.string.failed_file_upload, Toast.LENGTH_LONG)
                        .show();
                mUploadMessage.onReceiveValue(null);
            }
        }

        /**
         * Open file chooser. For Android < 3.0
         *
         * @param uploadFile
         *            the upload msg
         */
        public void openFileChooser(ValueCallback<Uri> uploadFile) {
            openFileChooser(uploadFile, "");
        }

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog,
                                      boolean isUserGesture, Message resultMsg) {

            // Webview에서 window를 open하는 경우. 팝업으로 새로운 window를 보여준다.
            WebView childView = new WebView(MainActivity.this);
            childView.getSettings().setJavaScriptEnabled(true);
            childView.setWebChromeClient(this);
            childView.setWebViewClient(new WebViewClient());

            childView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

            if (webviewDialog == null) {
                webviewDialog = new WebViewDialog(MainActivity.this,
                        childView);
                webviewDialog
                        .setUserActionListener(new WebViewDialog.UserActionListener() {

                            @Override
                            public void onClose() {

                                webviewDialog = null;
                            }
                        });

                webviewDialog.show();
            } else {
                webviewDialog.addWebView(childView);
            }

            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(childView);
            resultMsg.sendToTarget();

            return true;
        }

        @Override
        public void onCloseWindow(WebView window) {

            if (webviewDialog != null) {
                webviewDialog.removeWebView(window);
            }

            super.onCloseWindow(window);
        }
    };

    public void uploadReset() {
        this.mUploadMessage = null;
        this.mUploadMessages = null;
        this.imageFilePath = null;
        this.videoFilePath = null;
    }

    private Uri getFileUri(String filePath){
        Uri result = null;
        if(filePath != null){
            File file = new File(filePath);
            if (file.exists()) {
                result = Uri.fromFile(file);
            }
        }
        return result;
    }

    public void putUploadFile(final String position, Uri fileUri) {

        if(fileUri == null) {
            return;
        }

        final String filePath = fileUri.getPath();
        File file = new File(filePath);
        if(file != null && file.exists() == true) {
            mList_UploadFile.add(filePath);
        }
    }

    private Uri getActivityResultData(Intent intent, int resultCode) {

        if(resultCode != RESULT_OK) {
            return null;
        }

        Uri result = (intent == null) ? null : intent.getData();
        if(result == null) {
            for(Uri uri : mList_UploadUri) {
                if(existFile(uri) == true) {
                    result = uri;
                    return result;
                }
            }
        }

        return result;
    }

    private boolean existFile(Uri uri) {

        final String fileName = uri.getPath();
        File file = new File(fileName);
        if(file.exists() == true && file.length() > 0) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(uri);
            sendBroadcast(mediaScanIntent);
            return true;
        } else {
            return false;
        }
    }

    public Intent createImageOpenableIntent() {
        // Create and return a chooser with the default OPENABLE
        // actions including the camera, camcorder and sound
        // recorder where available.
        Intent imageIntent = new Intent(Intent.ACTION_PICK);
        imageIntent.setType("image/*");
        return imageIntent;
    }

    public Intent createVideoOpenableIntent() {
        // Create and return a chooser with the default OPENABLE
        // actions including the camera, camcorder and sound
        // recorder where available.
        Intent videoIntent = new Intent(Intent.ACTION_PICK);
        videoIntent.setType("video/*");
        return videoIntent;
    }

    public Intent createImageCaptureIntent() {
        File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                MediaHelper.CAPTURE_DIR);

        if (!imageStorageDir.exists()) {
            imageStorageDir.mkdirs();
        }

        imageFilePath = imageStorageDir + File.separator + "IMG_" + String.valueOf(System.currentTimeMillis())
                + ".jpg";
        // Create camera captured image file path and name
        File file = new File(imageFilePath);

        Uri imageFileUri = Uri.fromFile(file);

        // Camera capture image intent
        Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);
        return captureIntent;
    }

    public Intent createVideoCaptureIntent() {
        File videoStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                MediaHelper.CAPTURE_DIR);

        if (!videoStorageDir.exists()) {
            // Create AndroidExampleFolder at sdcard
            videoStorageDir.mkdirs();
        }

        videoFilePath = videoStorageDir + File.separator + "MOV_" + String.valueOf(System.currentTimeMillis())
                + ".mp4";
        // Create camera captured image file path and name
        File file = new File(videoFilePath);

        Uri videoFileUri = Uri.fromFile(file);

        // Camera capture image intent
        Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE);

        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoFileUri);
        return captureIntent;
    }

    @OnClick({R.id.homeBtn, R.id.prevBtn, R.id.nextBtn, R.id.refreshBtn})
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.homeBtn:
                homePressed = true;
                webView.loadUrl(getString(R.string.main_url));
                break;
            case R.id.prevBtn:
                webView.goBack();
                break;
            case R.id.nextBtn:
                webView.goForward();
                break;
            case R.id.refreshBtn:
                webView.reload();
                break;
        }
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        switch (requestCode) {
            case REQUEST_CODE_FILECHOOSER5 : {
                if(mUploadMessages == null) {
                    return;
                }

                Uri result = getActivityResultData(data, resultCode);
                if(result == null) {
                    mUploadMessages.onReceiveValue(null);
                } else {
                    mUploadMessages.onReceiveValue(new Uri[]{result});
                }
                uploadReset();
                mList_UploadUri.clear();

                break;
            }
            case REQUEST_CODE_FILECHOOSER:{
                if (null ==  mUploadMessage) {
                    return;

                }

                Uri result=null;

                try{
                    if (resultCode != RESULT_OK) {

                        result = null;

                    } else {

                        // retrieve from the private variable if the intent is null
                        if(data == null){

                            result = getFileUri(imageFilePath);

                            if(result == null){
                                result = getFileUri(videoFilePath);
                            }

                            if(result != null){
                                sendBroadcast(
                                        new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, result));
                            }
                        }else{
                            result = data.getData();
                        }
                    }
                }
                catch(Exception e)
                {
                    Toast.makeText(getApplicationContext(), R.string.file_selection_failed,
                            Toast.LENGTH_LONG).show();
                }

                mUploadMessage.onReceiveValue(result);
                uploadReset();
                mList_UploadUri.clear();
                break;
            }
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }
}
