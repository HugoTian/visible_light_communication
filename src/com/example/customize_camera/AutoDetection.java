package com.example.customize_camera;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class AutoDetection extends Activity implements PictureCallback, SurfaceHolder.Callback {
	  
	  public static final String EXTRA_CAMERA_DATA = "camera_data";

	  private static final String KEY_IS_CAPTURING = "is_capturing";

	  private static Camera mCamera = Camera.open();     
	  private ImageView mCameraImage;
	  private SurfaceView mCameraPreview;
	  private float mDist;
	  private static String result;
	  private static String finalResult;
	  private Button mCaptureImageButton;
	  private Button mexitButton;
	  
	  private byte[] mCameraData;
	  private static Bitmap mBitmap;
	  private static Bitmap bmpGrayscale;
	  private boolean mIsCapturing;
	  private static Camera.Parameters camParams  ;
	  private static boolean find = false;
	  
	  private int speed = 1000;
	  
	  private OnClickListener mButtonClickListener = new OnClickListener() {
	    @Override
	    public void onClick(View v) {
	      try {
			detect();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    }
	  };
	  
	 private void detect() throws InterruptedException{
		
	         try {
		     	captureImage();
			    
		     } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		     } 	      
		
	      
	     
	    }
	
	  
	  @Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    setContentView(R.layout.auto_detect);
	    
	    find = false;
	    mCameraImage = (ImageView) findViewById(R.id.camera_image_view);
	    mCameraImage.setVisibility(View.INVISIBLE);
	    
	    mCameraPreview = (SurfaceView) findViewById(R.id.preview_view);
	    final SurfaceHolder surfaceHolder = mCameraPreview.getHolder();
	    surfaceHolder.addCallback(this);
	    surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	    mCaptureImageButton = (Button) findViewById(R.id.image_button);
	    mCaptureImageButton.setOnClickListener(mButtonClickListener);
	     
	    mexitButton = (Button) findViewById(R.id.exit);
	    mexitButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				exit();
			}
		});
	    
	    mIsCapturing = true;
	    Log.d("check",Boolean.toString(mCamera!=null));
	    if (mCamera==null)
	    {
	    	try {
	    		mCamera = Camera.open();
	    		camParams =  mCamera.getParameters() ;
			} catch (Exception e) {
				// TODO: handle exception
			}
	    	
	    	
	    }
	    
	    camParams =  mCamera.getParameters() ;
	    camParams.set("iso", "800");
        camParams.setAutoExposureLock(false);
        camParams.set("shutter-spped", speed);
        int minTime = camParams.getMinExposureCompensation();
        
        camParams.setExposureCompensation(minTime);
        camParams.setPictureSize(640, 480);
        
        mCamera.setParameters(camParams);
	    try {
			mCamera.setPreviewDisplay(mCameraPreview.getHolder());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    mCamera.setDisplayOrientation(90);
	  }
	  
	  private void exit(){
		  this.finish();
	  }
	  @Override
	  protected void onSaveInstanceState(Bundle savedInstanceState) {
	    super.onSaveInstanceState(savedInstanceState);
	    
	    savedInstanceState.putBoolean(KEY_IS_CAPTURING, mIsCapturing);
	  }
	  
	  @Override
	  protected void onRestoreInstanceState(Bundle savedInstanceState) {
	    super.onRestoreInstanceState(savedInstanceState);
	    
	    mIsCapturing = savedInstanceState.getBoolean(KEY_IS_CAPTURING, mCameraData == null);
	    
	  }
	  @Override
	  protected void onResume() {
	    super.onResume();
	    
	    if (mCamera == null) {
	      try {
	        mCamera = Camera.open();
	    	camParams.set("iso", "800");
          	camParams.setAutoExposureLock(false);
          	camParams.set("shutter-spped", speed);
          	int minTime = camParams.getMinExposureCompensation();
          	
          	camParams.setExposureCompensation(minTime);
          	camParams.setPictureSize(640, 480);
          	mCamera.setParameters(camParams);
	        mCamera.setPreviewDisplay(mCameraPreview.getHolder());
	        mCamera.setDisplayOrientation(90);
	        if (mIsCapturing) {
	          mCamera.startPreview();
	        }
	      } catch (Exception e) {
	        Toast.makeText(AutoDetection.this, "Unable to open camera.", Toast.LENGTH_LONG)
	        .show();
	      }
	    }
	  }
	  
	  @Override
	  protected void onPause() {
	    super.onPause();
	    
	    if (mCamera != null) {
	      mCamera.release();
	      mCamera = null;
	    }
	  }
	  
	  @Override
	  public void onDestroy() {
		    super.onDestroy();
		    if (mCamera != null) {
			      mCamera.release();
			      mCamera = null;
			 }
	  }
	  @Override
	  public void onPictureTaken(byte[] data, Camera camera) {
	    mCameraData = data;
	    
	    
	      if (mCameraData != null) {
	    	   mBitmap = BitmapFactory.decodeByteArray(mCameraData, 0, mCameraData.length);
	    	   Matrix matrix = new Matrix();
	              matrix.postRotate(90);

				  mBitmap = Bitmap.createBitmap(mBitmap , 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
	    	   decodeImage();
		       
		      
	      }
	      
	      Toast.makeText(AutoDetection.this, result, Toast.LENGTH_LONG);
	      finalResult = MainActivity.correlation_detection(result);
	      for(int i = 0 ; i <4 ;i++){
	    	  if(finalResult.equals(MainActivity.codewordString[i]))
	    		  find = true;
	      }
	      if(find){
	      mCamera.release();
	      mCamera =null;
	      
	      Intent intent = new Intent(getBaseContext(), result.class);
	      intent.putExtra("result", finalResult);
	      startActivity(intent);
	      
	      finish();
	      overridePendingTransition(R.anim.fadein, R.anim.fadeout);
	      }
	      else{
	    	  try {
				captureImage();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	      }
	  }
	  
	  @Override
	  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	    if (mCamera != null) {
	      try {
	        mCamera.setPreviewDisplay(holder);
	        if (mIsCapturing) {
	          mCamera.startPreview();
	        }
	      } catch (IOException e) {
	        Toast.makeText(AutoDetection.this, "Unable to start camera preview.", Toast.LENGTH_LONG).show();
	      }
	    }
	  }
	  @Override
	  public void surfaceCreated(SurfaceHolder holder) {
	  }
	  
	  @Override
	  public void surfaceDestroyed(SurfaceHolder holder) {
	  }
	  
	  private  void captureImage() throws IOException {
		  try {
			  mCamera.takePicture(null, null, this);
		} catch (Exception e) {
			// TODO: handle exception
		}
	         
	        
		
	  }
	  
	 
	  
	  /*
	   * Zoom effect by two finger
	  */
	  @Override
	  public boolean onTouchEvent(MotionEvent event) {
	      // Get the pointer ID
	     
	      int action = event.getAction();


	      if (event.getPointerCount() > 1) {
	          // handle multi-touch events
	          if (action == MotionEvent.ACTION_POINTER_DOWN) {
	              mDist = getFingerSpacing(event);
	          } else if (action == MotionEvent.ACTION_MOVE && camParams.isZoomSupported()) {
	              mCamera.cancelAutoFocus();
	              handleZoom(event, camParams);
	          }
	      } else {
	          // handle single touch events
	          if (action == MotionEvent.ACTION_UP) {
	              handleFocus(event, camParams);
	          }
	      }
	      return true;
	  }

	  private void handleZoom(MotionEvent event, Camera.Parameters params) {
	      int maxZoom = params.getMaxZoom();
	      int zoom = params.getZoom();
	      float newDist = getFingerSpacing(event);
	      if (newDist > mDist) {
	          //zoom in
	          if (zoom < maxZoom)
	              zoom++;
	      } else if (newDist < mDist) {
	          //zoom out
	          if (zoom > 0)
	              zoom--;
	      }
	      mDist = newDist;
	      params.setZoom(zoom);
	      mCamera.setParameters(params);
	  }

	  public void handleFocus(MotionEvent event, Camera.Parameters params) {
	      int pointerId = event.getPointerId(0);
	      int pointerIndex = event.findPointerIndex(pointerId);
	      event.getX(pointerIndex);
	      event.getY(pointerIndex);

	      List<String> supportedFocusModes = params.getSupportedFocusModes();
	      if (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
	          mCamera.autoFocus(new Camera.AutoFocusCallback() {
	              @Override
	              public void onAutoFocus(boolean b, Camera camera) {
	                  // currently set to auto-focus on single touch
	              }
	          });
	      }
	  }

	  /** Determine the space between the first two fingers */
	  private float getFingerSpacing(MotionEvent event) {
	      // ...
	      float x = event.getX(0) - event.getX(1);
	      float y = event.getY(0) - event.getY(1);
	      return FloatMath.sqrt(x * x + y * y);
	  }
	  
	  private void decodeImage(){
		   //first change image from RGB to gray scale
		   
		   
		 //  bmpRotate = Bitmap.createBitmap(bmpGrayscale, 0, 0, bmpGrayscale.getWidth(), bmpGrayscale.getHeight(), matrix, true);
		   
		   //mCameraBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test_500);
		  int[] location = new int[2]; 
		  mCameraImage.getLocationOnScreen(location);
		   int imageX = location[0];
		   int imageY = location[1];
		   int imageW = mCameraPreview.getWidth();
		   int imageH = mCameraPreview.getHeight();
		   int imgWidth = mBitmap.getWidth();
		   int imgHeight = mBitmap.getHeight();
		   
		   bmpGrayscale = MainActivity.toGrayscale(mBitmap);
		   
		   
		   int p = bmpGrayscale.getPixel(0, 0);
		   int alpha = (p & 0xff000000) >> 24;
		   int R = (p & 0xff0000) >> 16;
		   int G = (p & 0xff00) >> 8;
		   int B = p & 0xff;
		   Log.d("red",Integer.toString(R));
		   Log.d("green",Integer.toString(G));
		   Log.d("blue",Integer.toString(B));
		   Log.d("gray",Boolean.toString(R==G && G==B));
		   Log.d("height",Integer.toString(imgHeight));
		  Log.d("width",Integer.toString(imgWidth)); 
		  Log.d("alpha",Integer.toString(alpha)); 
		 
		  
		  int decodeXstart;
		  int decodeYstart;
		  
		  
		 
		  decodeXstart = (int) ((mask.x-mask.wid-imageX)/imageW*imgWidth);
			  
		
		 
		  decodeYstart = (int) ((mask.y-mask.hei-imageY)/imageH *imgHeight);
			
		  
		  int decodeXend ;
		  int decodeYend ;
		 
		  decodeXend =  (int) ((mask.x+mask.wid-imageX)/imageW*imgWidth);
			
		  
		  
		  decodeYend =(int) ((mask.y+mask.hei-imageY)/imageH *imgHeight);
		
		  
		  
		  // sum the pixels
		  imgWidth = decodeXend-decodeXstart;
		  int[] data = new int[imgWidth];
		  
		  
		  for (int i = decodeXstart ; i < decodeXend;i++){
			  int sum = 0;
			  for(int j = decodeYstart; j<decodeYend ; j++){
				  sum += (bmpGrayscale.getPixel(i,j) & 0xff);
			  }
			  data[i-decodeXstart] = sum;
		  }
		  
		  //find local maximum
		  
		  List<Integer> loc = new ArrayList<Integer>();
		  List<Integer> pks = new ArrayList<Integer>();
		  int mean = 0;
		  for (int j = imgWidth/4; j < imgWidth*3/4;j++){
			  mean +=data[j];
		  }
		  mean = mean / (imgWidth*3/4 - imgWidth/4);
		  for (int i = 1; i < imgWidth-1; i++){
				   if (data[i-1]<data[i] && data[i] > data[i+1] && data[i] > mean){
				       loc.add(i);
				       pks.add(data[i]);
		  			}
		  }
		  
		  
		  
		  List<Integer> minIdx = new ArrayList<Integer>();
		  List<Integer> minValue = new ArrayList<Integer>();
		  
		  for (int i = 1; i < imgWidth-1; i++){
				   if (data[i-1]>data[i] && data[i] <data[i+1] && data[i] <mean){
				       minIdx.add(i);
				       minValue.add(data[i]);
		  			}
		  }
		  
		  //get the threshold
		  List<Integer> middle = new ArrayList<Integer>();
		  
		  for (int i = 0 ; i < loc.get(0);i++){
		      middle.add(((pks.get(0)+minValue.get(0))/2));
		  }
		  for (int i = 0; i < loc.size()-1;i++){
		      for (int j = loc.get(i); j < loc.get(i+1);j++){
		          middle.add(((pks.get(i)+pks.get(i+1)+2*minValue.get(0))/4));
		  	  }
		  }
		  for (int i = loc.get(loc.size()-1); i < imgWidth ; i++ ){
		      middle.add(((pks.get(pks.size()-1)+minValue.get(0))/2));
		  }
		  
		  //  get the Binary data
		  int[] decode = new int[imgWidth];
		  for (int i = 0 ; i <imgWidth; i++){
			  if (data[i]>middle.get(i)) {
				  decode[i]=1;
			  }else {
				  decode[i]=0;
			  }
		  }
		  
		  
		  int gap = 1;
		  int mini = 100;
		  for (int i = 0;  i <imgWidth-1; i++){
		      if (decode[i]==1 && decode[i+1]==1)
		          gap = gap +1;
		      if (decode[i]==0 && decode[i+1]==0)
		          gap = gap +1 ;
		      if (decode[i] != decode[i+1]){
		          
		          if (gap < mini && gap >3){
		              mini = gap;
		      	   }
		          gap = 1 ;
		  		}
		  }
		 // String result = new String(" ");
		  result = " ";
		  int one = 0;
		  int zero = 0;
		  gap = mini;
		  
		  for (int i = 0 ; i <imgWidth ;i++){
				    if(decode[i]==1)
				        one = one +1;
				    else
				        zero = zero +1 ;
				    
				    
				    while(one >= gap){
				        zero = 0;
				        result+="1";
				        one = one -gap;
				        if (one < gap)
				            one = 0;
				        
		  			}
				    
				    
				    while(zero >= gap){
				        one = 0;
				        result+="0";
				        zero = zero -gap;
				        if (zero < gap )
				            zero = 0;
				        
				    }
				    
		  }
		  
		  for (int j = 0 ; j < decode.length;j++){
			  decode[j]=0;
		  }
		  
		  // for debug purpose
		  /*
		  String test = result;
		  String patternString = correlation_match(test);
		  String correctString = Error_correction(patternString);
		 */ 
		  //String correctString;
		  
		   
		    
		  
	  }
	  

	}
