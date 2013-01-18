package com.tcs.disneyvirtualwall2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

import com.tcs.disneyvirtualwall2.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class MatchImageUtil {
	private static final String TAG = "DISNEYVIRTUALWALL::Activity";
	private static final boolean D = false;
	
	private static final int BOUNDARY = 35;
	private static final double THRESHOLD = 75.0;
	private static final double REVERSE_THRESHOLD = 25.0;
	private static final int [] DEDUCT = new int[]{25,45,60,70};
	private static final boolean CACHED = true;
	
	private static Mat mSceneDescriptors = null;
	
	private static HashMap<String, String> mCachedMap = new HashMap<String,String>();
	
	public static int [] mResources = new int[]{
		R.drawable.disney_sample_1_landscape_80,
		R.drawable.disney_sample_2_landscape_80,
		R.drawable.disney_sample_3_landscape_80,
		R.drawable.disney_sample_4_landscape_80,
		
		//R.drawable.disney_sample_1_landscape_40,
		//R.drawable.disney_sample_2_landscape_40,
		//R.drawable.disney_sample_3_landscape_40,
		//R.drawable.disney_sample_4_landscape_40,
		
		R.drawable.disney_new_sample_1,
		R.drawable.disney_new_sample_2,
		R.drawable.disney_new_sample_3,
		R.drawable.disney_new_sample_4
		
		//R.drawable.sample_t,
		//R.drawable.sample_g
	};
	
	private Activity mctx = null;
	
	public MatchImageUtil(Activity ctx){
		mctx = ctx;
	}
	
	private Mat preProcess(Mat sceneMat){
		
		Mat mIntermediateMat = new Mat();
		
    	int row = sceneMat.rows();
    	int col = sceneMat.cols();
    	
    	int radius = (row>col?col:row)/2*4/5;
    	//Core.circle(inputFrame, new Point(col/2,row/2), radius, new Scalar(0, 255, 0, 255), 15);
    	Mat subArea = sceneMat.submat(row/2-radius,row/2+radius,col/2-radius,col/2+radius);
    	sceneMat.copyTo(mIntermediateMat);
    	
		// 1) Apply gaussian blur to remove noise
		//Imgproc.GaussianBlur(sceneMat, mIntermediateMat, new Size(11,11), 0);
    	Imgproc.GaussianBlur(subArea, mIntermediateMat, new Size(11,11), 0);
    	
		// 2) AdaptiveThreshold -> classify as either black or white
		//Imgproc.adaptiveThreshold(mIntermediateMat, mIntermediateMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, 2);

		// 3) Invert the image -> so most of the image is black
		//Core.bitwise_not(mIntermediateMat, mIntermediateMat);

		// 4) Dilate -> fill the image using the MORPH_DILATE
		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, new Size(3,3), new Point(1,1));
		Imgproc.dilate(mIntermediateMat, mIntermediateMat, kernel);
		
		return mIntermediateMat;
	}
	
	private static int nPrevIndex = -1;
	private static int nConsequenceHit = 0;
	public static Bitmap mSceneImg = null;
	
	public int findObject(Mat secenMat){

		int [] res = mResources;
		
		Mat src = new Mat();
    	Mat target = new Mat();
    	
    	Mat dst = preProcess(secenMat);
    	Bitmap sceneImg = Bitmap.createBitmap( dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dst,sceneImg);
    	
        mSceneImg = scaleAndTrun(sceneImg);
        
    	BitmapFactory.Options options = new BitmapFactory.Options();
    	options.inPreferredConfig = Config.ARGB_8888;
    	Utils.bitmapToMat(mSceneImg, src);
    	
    	mSceneDescriptors = null;
    	
    	if(D) Log.v(TAG,"-------------START----------------");
    	
    	long startTime, endTime;
    	
    	if(D) startTime = (new Date()).getTime();
    	
    	double maxRate = 0.0;
    	int maxNdx = 0;
    	
    	Mat matScene = getSecenDescriptor(src);
    	Mat matTrain = null;
    	for(int i = 0 ;i < res.length ; i++){
  	       	
    		//Log.v(TAG,"check cached = " + isCached(i,isPortrait));
    		
    		if(!isCached(i)){
    			Bitmap input2 = scaleAndTrun(BitmapFactory.decodeResource(mctx.getResources(), res[i]));
  	       		Utils.bitmapToMat(input2, target);
  	       		matTrain = getTrainDescriptor(target,i);
    		}else{
    			try{
    				matTrain = getCachedTrainDescriptor(i);
    			}catch(Exception e){
    				return -1;
    			}
    		}
    		
  	       	double nMatchRate = match(matScene,matTrain);
  	       	if(maxRate <= nMatchRate){
  	       		maxRate = nMatchRate;
  	       		maxNdx = i;
  	       	}
  	       	
  	       	if(maxRate > THRESHOLD){
  	       		break;
  	       	}
  	       	
    	}
    	
    	if(D){
    		Log.v(TAG, "i="+maxNdx+",rate="+maxRate);
    		Log.v(TAG, "nPrevIndex="+nPrevIndex);
    		endTime = (new Date()).getTime();
    		Log.v(TAG,"execute time = "+(endTime-startTime));
    	}
    	
    	if(maxNdx == nPrevIndex){
    		nConsequenceHit++;	
    	}else{
    		nConsequenceHit = 0;
    	}
    	nConsequenceHit = Math.min(nConsequenceHit, DEDUCT.length - 1);
    	nPrevIndex = maxNdx;
    	
    	if(maxRate > THRESHOLD - DEDUCT[nConsequenceHit]){
	    	double reverseRate = 0.0;
	    	try{
	    		reverseRate = match(getCachedTrainDescriptor(maxNdx),matScene);
	    		if(D) Log.v(TAG,"reverseRate="+reverseRate);
	    	}catch(Exception e){}
	    	
	    	//if(reverseRate <= REVERSE_THRESHOLD){
	    	if(reverseRate < maxRate * 0.5){
	    		nConsequenceHit = 0;
	    		return -1;
	    	}
    	}
    	
    	if(D) Log.v(TAG, "nConsequenceHit="+nConsequenceHit);    	
    	
    	return ( maxRate > THRESHOLD - DEDUCT[nConsequenceHit] ? maxNdx : -1 ) ;
	}
	
	public static void clearHit(){
		nPrevIndex = -1;
		nConsequenceHit = 0;
	}
	
	private Mat getSecenDescriptor(Mat srcMat){
		if(mSceneDescriptors == null){
			
			FeatureDetector detector = FeatureDetector.create(FeatureDetector.FAST);
	        DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.BRIEF);
			
	        Mat srcImage = new Mat();
	        Mat quSrcImage = new Mat();
	        srcMat.copyTo(quSrcImage);
	        
	        Imgproc.cvtColor(quSrcImage, srcImage, Imgproc.COLOR_RGBA2RGB,3);
	        MatOfKeyPoint vectorSrc = new MatOfKeyPoint();
	        detector.detect(srcImage, vectorSrc );
	        
	        Mat sceneDescriptors = new Mat();
	        extractor.compute( srcImage, vectorSrc, sceneDescriptors );
	        
	        mSceneDescriptors = sceneDescriptors;
        }
		
		return mSceneDescriptors;
	}
	
	private boolean isCached(int i){
		if(CACHED){
			String key = "mat_"+i+"_land";
			return mCachedMap.containsKey(key);
		}else{
			return false;
		}
	}
	
	public void loadCachedFiles(){
		
		for(int i = 0 ;i < mResources.length ; i++){
			try{
				Mat target = new Mat();
				if(!isCached(i)){
					Bitmap input2 = scaleAndTrun(BitmapFactory.decodeResource(mctx.getResources(), mResources[i]));
			       		Utils.bitmapToMat(input2, target);
			       		getTrainDescriptor(target,i);
				}else{
					try{
						 getCachedTrainDescriptor(i);
					}catch(Exception e){}
				}
			}catch(Exception e){}
		}
		
	}
	
	private Mat getCachedTrainDescriptor(int i) throws IOException{
		if(CACHED){
			try{
				String key = "mat_"+i+"_land";
				
				String data = "";
				if(mCachedMap.containsKey(key)){
					//if(D) Log.v(TAG,"cached hash are used now.");
					data = mCachedMap.get(key);
				}else{
					//if(D) Log.v(TAG,"cached files are used now.");
					
					String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
					
					File dir = new File(extStorageDirectory, "tcs");
					if(!dir.exists()){
						dir.mkdir();
					}
					
					File file = new File(extStorageDirectory, "tcs/disney_mat_"+i+"_landscape.txt");

					if(file.exists()){
					
						FileInputStream fIn = new FileInputStream(file);
				        InputStreamReader isr = new InputStreamReader(fIn);
				        
				        char[] inputBuffer = new char[(int) file.length()];
				        isr.read(inputBuffer);
				        isr.close();
				        
				        data = new String(inputBuffer);
				        mCachedMap.put(key, data);
				        
					}
				}
				
				if(!data.equals("")){
			        String base64="";
			        int type,cols,rows;
			        
			        String [] raw = data.split("\t");
			        
			        if(raw.length == 4){
			        	rows = Integer.parseInt(raw[0]);
			        	cols = Integer.parseInt(raw[1]);
			        	type = Integer.parseInt(raw[2]);
			        	base64 = raw[3];
			        	
				        byte [] buff  = Base64.decode(base64, Base64.DEFAULT);
				        Mat trainDescriptors = new Mat(rows,cols,type);
				        trainDescriptors.put(0, 0, buff);
				        
				        return trainDescriptors;
			        }
				}
		    }catch(IOException e){
		    	
		    }
		}
		return null;
	}
	
	private Mat getTrainDescriptor(Mat targetMat,int i){
		//Load From File.
		if(CACHED){
			try{
				Mat retMat = getCachedTrainDescriptor(i);
				if(retMat != null){
					return retMat;
				}
		    }catch(IOException e){
		    	
		    }
		}
		
		//Making the cache files for Mat.
		String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
		
		File dir = new File(extStorageDirectory, "tcs");
		if(!dir.exists()){
			dir.mkdir();
		}
		
		File file = null;
		file = new File(extStorageDirectory, "tcs/disney_mat_"+i+"_landscape.txt");
		String key = "mat_"+i+"_land";
		
		FeatureDetector detector = FeatureDetector.create(FeatureDetector.FAST);
        DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.BRIEF);
        
    	//Target - Train-------------------------------------------------------
        Mat targetImage = new Mat();
        Mat quTargetImage = new Mat();
        targetMat.copyTo(quTargetImage);
        
        Imgproc.cvtColor(quTargetImage, targetImage, Imgproc.COLOR_RGBA2RGB,3);
        MatOfKeyPoint vectorTarget = new MatOfKeyPoint();
        detector.detect(targetImage, vectorTarget );
        
        Mat trainDescriptors = new Mat();
        extractor.compute( targetImage, vectorTarget, trainDescriptors );
        
        if(CACHED){
	        int count = (int) (trainDescriptors.total() * trainDescriptors.channels());
		    byte[] buff = new byte[count];
		    trainDescriptors.get(0, 0, buff);
		    String base64 = Base64.encodeToString(buff, Base64.DEFAULT);
		    
		    try{
				OutputStream outStream = null;
			    outStream = new FileOutputStream(file);
			    OutputStreamWriter osw = new OutputStreamWriter(outStream); 
			    
			    int type = trainDescriptors.type();
			    int cols = trainDescriptors.cols();
			    int rows = trainDescriptors.rows();
			    
			    String data = ""+rows+"\t"+cols+"\t"+type+"\t"+base64;
			    osw.write(data);
			    
			    mCachedMap.put(key, data);
			    
			    osw.flush();
			    osw.close();		    
		    }catch(IOException e){
		    	
		    }
        }
        
        return trainDescriptors;
	}
	
	private double match(Mat matScene, Mat matTrain){
		MatOfDMatch matches = new MatOfDMatch();
        DescriptorMatcher matcherHamming = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT);
        List<Mat> listMat = new ArrayList<Mat>();
        listMat.add(matTrain);
        matcherHamming.add(listMat);
        matcherHamming.train();
                
        matcherHamming.match(matScene, matches); 
        
		List<DMatch> good_matches = new ArrayList<DMatch>();
		
    	List<DMatch> in_matches = matches.toList();
		int rowCount = in_matches.size();
		
		for (int i = 0; i < rowCount; i++) {
			if (in_matches.get(i).distance <= BOUNDARY) {
				good_matches.add(in_matches.get(i));
			}
		}        
        		
		return (1.0  * good_matches.size() / rowCount) * 100.0;
	}
	
    public static Bitmap scaleAndTrun(Bitmap bm) {
		int MAX_DIM = 200;
		int w, h;
		if (bm.getWidth() >= bm.getHeight()) {
			w = MAX_DIM;
			h = bm.getHeight() * MAX_DIM / bm.getWidth();
		} else {
			h = MAX_DIM;
			w = bm.getWidth() * MAX_DIM / bm.getHeight();
		}
		bm = Bitmap.createScaledBitmap(bm, w, h, false);
		Bitmap img_bit = bm.copy(Bitmap.Config.ARGB_8888, false);
		return img_bit;
	}
}
