package com.tcs.disneyvirtualwall2;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.core.Mat;

import com.tcs.disneyvirtualwall2.youtube.PlayerViewDemoActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.ImageView;

public class MainActivity extends Activity implements CvCameraViewListener {

	private static final String TAG = "OCVSample::Activity";

    private CameraBridgeViewBase mOpenCvCameraView;
    //private boolean              mIsJavaCamera = true;
    //private MenuItem             mItemSwitchCamera = null;
    
    private MatchImageUtil 		mMatchImageUtil = null;
    
    private static boolean isRunThread = false;
    
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mMatchImageUtil = new MatchImageUtil(this);
        
        Intent intent = new Intent(MainActivity.this,PlayerViewDemoActivity.class);
		intent.putExtra("video_uri", "3dnxG6fxXi8");
    	startActivity(intent);
    }

    @Override
    public void onPause()
    {
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        super.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    	
    }

    public void onCameraViewStopped() {
    	
    }

    public Mat onCameraFrame(Mat inputFrame) {
    	Log.v(TAG,"onCameraFrame");
    	
    	FindObjectThread mFindObjectThread = new FindObjectThread();
    	
    	if(!mFindObjectThread.isRun()){
    		mFindObjectThread.setInputFrame(inputFrame);
    		mFindObjectThread.start();
    	}
    	
        return inputFrame;
    }
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    final Handler handler = new Handler() {
        public void handleMessage(Message msg)
        {
			String message = null;
			
			if(msg.what == -1 || msg.what >= 1000){
				message = "수행 실패";
			}else{
				message = "수행 완료 - " + msg.what;
				
		    	ImageView imageTrain = (ImageView)findViewById(R.id.target);
		    	imageTrain.setImageBitmap(BitmapFactory.decodeResource(getResources(), MatchImageUtil.mResources2[msg.what]));
		    	
		    	if(msg.what == 7){
  	     			Intent intent = new Intent(MainActivity.this,PlayerViewDemoActivity.class);
  	     			intent.putExtra("video_uri", "ZRlCulV7r-I");
  	     	    	startActivity(intent);
		    	}
		    	
		    	// 메시지 출력
	            // Toast.makeText(Sample1Java.this, message, Toast.LENGTH_SHORT).show();
			}
        }
    };
    
    private class FindObjectThread extends Thread implements Runnable{

    	private boolean isRun = false;
    	private Mat mInputFrame = null;
    	
		public boolean isRun() {
			isRun = isRunThread;
			return isRun;
		}

		public Mat getInputFrame() {
			return mInputFrame;
		}

		public void setInputFrame(Mat mInputFrame) {
			this.mInputFrame = mInputFrame;
		}

		public void setRun(boolean isRun) {
			this.isRun = isRun;
			isRunThread = isRun;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try{
				if(getInputFrame() != null){
					setRun(true);
					int ndx = mMatchImageUtil.findObject(mInputFrame);
	                handler.sendEmptyMessage(ndx);
				}
            }catch(Exception e){
                // 작업이 실패 시
                handler.sendEmptyMessage(1000);
            }
			
			mInputFrame = null;
			
			setRun(false);	
		}
    }

}
