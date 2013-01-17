package com.tcs.disneyvirtualwall2;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;

public class SplashViewActivity extends Activity {
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_main);
        
        Handler handler = new Handler () {
            @Override
            public void handleMessage(Message msg)
            {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                finish();
            }
        };
        
        handler.sendEmptyMessageDelayed(0, 3000);
        // TODO Auto-generated method stub
    }
    
	public boolean onKeyDown(int keyCode, KeyEvent event) {
    	Log.w("[onKeyDown]", "keyCode: "+Integer.toString(keyCode));
    	if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
    		return false;
    	}
    	return true;
    }
}
