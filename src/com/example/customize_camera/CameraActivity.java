package com.example.customize_camera;

import java.io.IOException;
import java.util.List;

import android.R.integer;
import android.app.Activity;
import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class CameraActivity extends Activity implements PictureCallback, SurfaceHolder.Callback {
	  
	  public static final String EXTRA_CAMERA_DATA = "camera_data";

	  private static final String KEY_IS_CAPTURING = "is_capturing";

	  private static Camera mCamera = Camera.open();     
	  private ImageView mCameraImage;
	  private SurfaceView mCameraPreview;
	  private float mDist;
	  
	  private Button mCaptureImageButton;
	  private byte[] mCameraData;
	  private boolean mIsCapturing;
	  private static Camera.Parameters camParams  ;
	  private  int minTime ;
      private  int maxTime ;
	  
	  private int speed = 1000;
	  private int Iso = 800 ;
	  private int exp ;
	  private OnClickListener mCaptureImageButtonClickListener = new OnClickListener() {
	    @Override
	    public void onClick(View v) {
	      try {
			captureImage();
		  } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		  }
	      
	    
	    }
	  };
	  
	  private OnClickListener mRecaptureImageButtonClickListener = new OnClickListener() {
	    @Override
	    public void onClick(View v) {
	      setupImageCapture();
	      if (mCameraData != null) {
		        Intent intent = new Intent();
		        intent.putExtra(EXTRA_CAMERA_DATA, mCameraData);
		        setResult(RESULT_OK, intent);
		      } else {
		        setResult(RESULT_CANCELED);
		      }
		      finish();
	    }
	  };
	  
	  private OnClickListener mDoneButtonClickListener = new OnClickListener() {
	    @Override
	    public void onClick(View v) {
	      if (mCameraData != null) {
	        Intent intent = new Intent();
	        intent.putExtra(EXTRA_CAMERA_DATA, mCameraData);
	        setResult(RESULT_OK, intent);
	      } else {
	        setResult(RESULT_CANCELED);
	      }
	      mCamera.release();
	      mCamera =null;
	      finish();
	    }
	  };
	  
	  @Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    setContentView(R.layout.activity_camera);
	    
	    mCameraImage = (ImageView) findViewById(R.id.camera_image_view);
	    mCameraImage.setVisibility(View.INVISIBLE);
	    
	    mCameraPreview = (SurfaceView) findViewById(R.id.preview_view);
	    final SurfaceHolder surfaceHolder = mCameraPreview.getHolder();
	    surfaceHolder.addCallback(this);
	    surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	    
	    mCaptureImageButton = (Button) findViewById(R.id.capture_image_button);
	    mCaptureImageButton.setOnClickListener(mCaptureImageButtonClickListener);
	     
        
	    final Button doneButton = (Button) findViewById(R.id.done_button);
	    doneButton.setOnClickListener(mDoneButtonClickListener);
	    
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
	    Log.d("check2",Boolean.toString(mCamera!=null));
	    camParams =  mCamera.getParameters() ;
	    minTime = camParams.getMinExposureCompensation();
	    maxTime = camParams.getMinExposureCompensation();
	    exp = minTime;
	    camParams.set("iso", Integer.toString(Iso));
        camParams.setAutoExposureLock(false);
        camParams.set("shutter-spped", speed);
        
        camParams.setExposureCompensation(exp);
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
	  
	  @Override
	  protected void onSaveInstanceState(Bundle savedInstanceState) {
	    super.onSaveInstanceState(savedInstanceState);
	    
	    savedInstanceState.putBoolean(KEY_IS_CAPTURING, mIsCapturing);
	  }
	  
	  @Override
	  protected void onRestoreInstanceState(Bundle savedInstanceState) {
	    super.onRestoreInstanceState(savedInstanceState);
	    
	    mIsCapturing = savedInstanceState.getBoolean(KEY_IS_CAPTURING, mCameraData == null);
	    if (mCameraData != null) {
	      setupImageDisplay();
	    } else {
	      setupImageCapture();
	    }
	  }
	  @Override
	  protected void onResume() {
	    super.onResume();
	    
	    if (mCamera == null) {
	      try {
	        mCamera = Camera.open();
	    	camParams.set("iso", Integer.toString(Iso));
          	camParams.setAutoExposureLock(false);
          	camParams.set("shutter-spped", speed);
          	
          	camParams.setExposureCompensation(exp);
          	camParams.setPictureSize(640, 480);
          	mCamera.setParameters(camParams);
	        mCamera.setPreviewDisplay(mCameraPreview.getHolder());
	        mCamera.setDisplayOrientation(90);
	        if (mIsCapturing) {
	          mCamera.startPreview();
	        }
	      } catch (Exception e) {
	        Toast.makeText(CameraActivity.this, "Unable to open camera.", Toast.LENGTH_LONG)
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
	    data.clone();
	    
	      if (mCameraData != null) {
	    	   
		        Intent intent = new Intent();
		        intent.putExtra(EXTRA_CAMERA_DATA, mCameraData);
		        setResult(RESULT_OK, intent);
		      } else {
		        setResult(RESULT_CANCELED);
		      }
		      mCamera.release();
		      mCamera =null;
		      finish();
		      overridePendingTransition(R.anim.fadein, R.anim.fadeout);
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
	        Toast.makeText(CameraActivity.this, "Unable to start camera preview.", Toast.LENGTH_LONG).show();
	      }
	    }
	  }
	  @Override
	  public void surfaceCreated(SurfaceHolder holder) {
	  }
	  
	  @Override
	  public void surfaceDestroyed(SurfaceHolder holder) {
	  }
	  
	  private void captureImage() throws IOException {
	    mCamera.takePicture(null, null, this);
	  }
	  
	  private void setupImageCapture() {
	    mCameraImage.setVisibility(View.INVISIBLE);
	    mCameraPreview.setVisibility(View.VISIBLE);
	    mCamera.startPreview();
	    mCaptureImageButton.setText(R.string.capture_image);
	    mCaptureImageButton.setOnClickListener(mCaptureImageButtonClickListener);
	  }
	  
	  private void setupImageDisplay() {
		
		Bitmap bitmap = BitmapFactory.decodeByteArray(mCameraData, 0, mCameraData.length);
		Matrix matrix = new Matrix();

		matrix.postRotate(90);

		bitmap = Bitmap.createBitmap(bitmap , 0, 0, bitmap.getWidth(),bitmap.getHeight(), matrix, true);
	    mCameraImage.setImageBitmap(bitmap);
	    mCamera.stopPreview();
	    mCameraPreview.setVisibility(View.INVISIBLE);
	    mCameraImage.setVisibility(View.VISIBLE);
	    mCaptureImageButton.setText(R.string.recapture_image);
	    mCaptureImageButton.setOnClickListener(mRecaptureImageButtonClickListener);
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
	  
	  @Override
		public boolean onCreateOptionsMenu(Menu menu) {
			
			// Inflate the menu; this adds items to the action bar if it is present.
			getMenuInflater().inflate(R.menu.camera_option, menu);
			return true;
		}
	  
	  public boolean onOptionsItemSelected(MenuItem item) {
		  switch (item.getItemId()) {
		    case R.id.large:
		        Iso=800;
		      return true;
		    case R.id.medium:
		    	Iso = 400;
		    	return true;
		    case R.id.small:
		    	Iso = 200;
		    	return true;
		    case R.id.max:
		    	exp = maxTime;
		    	return true;
		    case R.id.middle:
		    	exp = (maxTime + minTime)/2;
		    	return true;
		    case R.id.min:
		    	exp = minTime;
		    	return true;
		      
		    default:
		      return super.onOptionsItemSelected(item);
		  }
		}
	 

	}
