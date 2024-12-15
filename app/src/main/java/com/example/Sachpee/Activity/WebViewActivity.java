package com.example.Sachpee.Activity;

import android.os.Bundle;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.Sachpee.R;
//wwebviewmap
public class WebViewActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);

        // Nhận URL từ Intent và load vào WebView
        String mapUrl = getIntent().getStringExtra("MAP_URL");
        webView.loadUrl(mapUrl);
    }
}
