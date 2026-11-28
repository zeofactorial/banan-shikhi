package com.rakib.banan;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private WebView web;
    private volatile boolean volCaret = false;

    public class Bridge {
        @JavascriptInterface
        public void setVol(boolean on) { volCaret = on; }

        @JavascriptInterface
        public void share(final String text) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("application/vnd.android.package-archive");
                        i.putExtra(Intent.EXTRA_STREAM, Uri.parse("content://com.rakib.banan.apk/BanglaBooster.apk"));
                        i.putExtra(Intent.EXTRA_TEXT, text);
                        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(Intent.createChooser(i, "অ্যাপ শেয়ার করো"));
                    } catch (Exception e) {
                        Intent t = new Intent(Intent.ACTION_SEND);
                        t.setType("text/plain");
                        t.putExtra(Intent.EXTRA_TEXT, text);
                        startActivity(Intent.createChooser(t, "শেয়ার করো"));
                    }
                }
            });
        }

        @JavascriptInterface
        public void mail(final String to) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + to));
                        i.putExtra(Intent.EXTRA_SUBJECT, "Bangla Booster");
                        startActivity(Intent.createChooser(i, "ইমেইল পাঠাও"));
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(MainActivity.this, "ইমেইল অ্যাপ পাওয়া যায়নি: " + to, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        web = new WebView(this);
        setContentView(web);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setTextZoom(100);
        web.addJavascriptInterface(new Bridge(), "Android");
        web.setWebViewClient(new WebViewClient());
        web.loadUrl("file:///android_asset/index.html");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (volCaret && (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            final int d = (keyCode == KeyEvent.KEYCODE_VOLUME_UP) ? 1 : -1;
            web.post(new Runnable() {
                @Override
                public void run() {
                    web.evaluateJavascript("window.moveCaret&&window.moveCaret(" + d + ")", null);
                }
            });
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (volCaret && (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        web.evaluateJavascript("(window.appBack?window.appBack():false)", value -> {
            if (!"true".equals(value)) finish();
        });
    }
}
