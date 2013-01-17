package com.tcs.disneyvirtualwall2;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import com.tcs.disneyvirtualwall2.youtube.PlayerViewDemoActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity implements CvCameraViewListener, OnClickListener {

	private static final String TAG = "DISNEYVIRTUALWALL::Activity";
	private static final boolean D = false;
	
    private CameraBridgeViewBase 	mOpenCvCameraView = null;
    private MatchImageUtil 			mMatchImageUtil = null;
    //private Camera 				mCamera = null;
    
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:{
                    if(D) Log.i(TAG, "OpenCV loaded successfully");
                    
                    mOpenCvCameraView.enableView();
                    
                    Handler postHandler = new Handler();
                    postHandler.postDelayed(new Runnable(){
            			@Override
            			public void run() {
            				// TODO Auto-generated method stub
            				mMatchImageUtil.loadCachedFiles();
            				mProgressDialog.dismiss();
            			}
                    	
                    }, 50);
                    
                } break;
                default:{
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public MainActivity() {
        if(D) Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        if(D) Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);
        startActivity(new Intent(this, SplashViewActivity.class));
        
        //mCamera = openFrontFacingCamera();
        
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.java_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        if(!D){
        	android.view.ViewGroup.LayoutParams params = mOpenCvCameraView.getLayoutParams();
        	params.width = LayoutParams.FILL_PARENT;
        	params.height = LayoutParams.FILL_PARENT;
        	mOpenCvCameraView.setLayoutParams(params);
        }
        mOpenCvCameraView.setCvCameraViewListener(this);
        mMatchImageUtil = new MatchImageUtil(this);
        
        mOpenCvCameraView.setOnClickListener(this);
        
        //{
        //	Intent intent = new Intent(MainActivity.this,PlayerViewDemoActivity.class);
		//	intent.putExtra("video_uri", "3dnxG6fxXi8");
    	//	startActivity(intent);
        //}
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
        createProgressDialog();
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
    	//Log.v(TAG,"onCameraFrame");
    	
    	if(!isRunThread && !mIsFound){
	    	FindObjectThread mFindObjectThread = new FindObjectThread();
    		mFindObjectThread.setInputFrame(inputFrame);
    		mFindObjectThread.start();
	    }
    	
    	inputFrame = drawLines(inputFrame);
    	
        return inputFrame;
    }
    
    private static Mat mCheckIcon = null;
    
    private Mat drawLines(Mat inputFrame){
    	
    	if(mIsFound){
    	
	    	int row = inputFrame.rows();
	    	int col = inputFrame.cols();
	    	
	    	//Core.line(inputFrame, new Point(10,10), new Point(10,row-10), new Scalar(0, 255, 0, 255), 3);
	    	//Core.line(inputFrame, new Point(10,10), new Point(col-10,10), new Scalar(0, 255, 0, 255), 3);
	    	
	    	//Core.line(inputFrame, new Point(10,row-10), new Point(col-10,row-10), new Scalar(0, 255, 0, 255), 3);
	    	//Core.line(inputFrame, new Point(col-10,10), new Point(col-10,row-10), new Scalar(0, 255, 0, 255), 3);
	    	
	    	/*
	    	if(mCheckIcon == null){
	    		mCheckIcon = new Mat();
	    		Bitmap input2 = MatchImageUtil.scaleAndTrun(BitmapFactory.decodeResource(getResources(), R.drawable.check_icon));
  	       		Utils.bitmapToMat(input2, mCheckIcon);
	    	}
	    	
	    	mCheckIcon.copyTo(inputFrame);
	    	*/
	    	
	    	int radius = (row>col?col:row)/2-(row>col?col:row)/2/3;
	    	Core.circle(inputFrame, new Point(col/2,row/2), radius, new Scalar(0, 255, 0, 255), 15);
	    	
    	}
    	
    	return inputFrame;
    }
    
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v.getId() == R.id.java_surface_view){
			try{
				//mCamera = openFrontFacingCamera();
				//mCamera.autoFocus(null);
				//mCamera.release();
			}catch(Exception e){}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		mLastFindIndex = -1;
		mIsFound = false;
	}

	///////////////////////////////////////////////////////////////////////////
	private Camera openFrontFacingCamera() {
		
	    int cameraCount = 0;
	    Camera cam = null;
	    Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
	    cameraCount = Camera.getNumberOfCameras();
	    for ( int camIdx = 0; camIdx < cameraCount; camIdx++ ) {
	        Camera.getCameraInfo( camIdx, cameraInfo );
	        //if ( cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT  ) {
	        if ( cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK  ) {
	            try {
	                cam = Camera.open( camIdx );
	            } catch (RuntimeException e) {
	                Log.e(TAG, "Camera failed to open: " + e.getLocalizedMessage());
	            }
	        }
	    }

	    return cam;
	}

	///////////////////////////////////////////////////////////////////////////
	private ProgressDialog mProgressDialog;
	private void createProgressDialog(){
		mProgressDialog = ProgressDialog.show(MainActivity.this, "", "Loading...",true,false);
	}
	
    ///////////////////////////////////////////////////////////////////////////
    private static int mLastFindIndex = -1;
    private static boolean mIsFound = false;
    private static boolean isRunThread = false;
    private Handler mHandler = new Handler();
    private Handler mHandler2 = new Handler();
    
	final Handler handler = new Handler() {
        public void handleMessage(Message msg)
        {
			String message = null;
			
			if(msg.what == -1 || msg.what >= 1000){
				message = "Fail to find";
			}else{
				message = "Found - " + msg.what;
				
				if(mLastFindIndex != msg.what){
					
					if(D){
						ImageView imageTrain = (ImageView)findViewById(R.id.target);
						imageTrain.setImageBitmap(BitmapFactory.decodeResource(getResources(), MatchImageUtil.mResources[msg.what]));
					}else{
						// 메시지 출력
			            //Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();

			            if(msg.what != 7){
				            
			            	final int msgindex = msg.what;
			            	
				            mHandler.postDelayed(new Runnable(){
				            	@Override     
				            	public void run(){
				            		Intent intent = new Intent(MainActivity.this,ProductWebViewActivity.class);
						            intent.putExtra("image_index", msgindex);
						            //MainActivity.this.startActivity(intent);
						            startActivityForResult(intent, 0);
				            	}
				            }, 1200);
				            
			            }else{
			            	
		  	     	    	mHandler.postDelayed(new Runnable(){
				            	@Override     
				            	public void run(){
				            		Intent intent = new Intent(MainActivity.this,PlayerViewDemoActivity.class);
				  	     			intent.putExtra("video_uri", "ZRlCulV7r-I");
				  	     	    	//startActivity(intent);
				  	     	    	startActivityForResult(intent, 0);
				            	}
				            }, 1200);
				    	}
			            
					}
					
					mHandler2.postDelayed(new Runnable(){
		            	@Override     
		            	public void run(){
		            		mIsFound = false;
		            	}
		            }, 800);
					
					mMatchImageUtil.clearHit();
					mIsFound = true;
			    	mLastFindIndex = msg.what;
			    	
				}
			}
        }
    };
    
    private class FindObjectThread extends Thread implements Runnable{

    	private Mat mInputFrame = null;

		public Mat getInputFrame() {
			return mInputFrame;
		}

		public void setInputFrame(Mat mInputFrame) {
			this.mInputFrame = mInputFrame;
		}

		public void setRun(boolean isRun) {
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
