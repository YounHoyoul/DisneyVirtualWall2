package com.tcs.disneyvirtualwall2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ProductWebViewActivity extends Activity {
	private WebView mWebView;
    
	private static String [] IMAGE_URL = new String[]
		{
			"http://27.50.87.100/~thecreat/wp-content/uploads/2013/01/disney_sample_1_portrait.png",
			"http://27.50.87.100/~thecreat/wp-content/uploads/2013/01/disney_sample_2_portrait.png",
			"http://27.50.87.100/~thecreat/wp-content/uploads/2013/01/disney_sample_3_portrait.png",
			"http://27.50.87.100/~thecreat/wp-content/uploads/2013/01/disney_sample_4_portrait.png",
			"http://27.50.87.100/~thecreat/wp-content/uploads/2013/01/disney_new_sample_1.png",
			"http://27.50.87.100/~thecreat/wp-content/uploads/2013/01/disney_new_sample_2.png",
			"http://27.50.87.100/~thecreat/wp-content/uploads/2013/01/disney_new_sample_3.png",
			"http://27.50.87.100/~thecreat/wp-content/uploads/2013/01/disney_new_sample_4.png",
			"http://27.50.87.100/~thecreat/wp-content/uploads/2013/01/sample_t.png",
			"http://27.50.87.100/~thecreat/wp-content/uploads/2013/01/sample_g.png",
		};
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview_main);
         
        setLayout();
         
        mWebView.getSettings().setJavaScriptEnabled(true); 
        
        Intent intent = getIntent();
        int index = intent.getIntExtra("image_index", 0);
        mWebView.loadUrl(IMAGE_URL[index]);
        mWebView.setWebViewClient(new WebViewClientClass());  
         
    }
     
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) { 
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) { 
            mWebView.goBack(); 
            return true; 
        } 
        return super.onKeyDown(keyCode, event);
    }
     
    private class WebViewClientClass extends WebViewClient { 
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) { 
            view.loadUrl(url); 
            return true; 
        } 
    }

    private void setLayout(){
        mWebView = (WebView) findViewById(R.id.webview);
    }
}
