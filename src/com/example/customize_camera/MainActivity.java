package com.example.customize_camera;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

public class MainActivity extends Activity {

	  private static final int TAKE_PICTURE_REQUEST_B = 100;
	  private static final int SELECT_PICTURE = 1;
	  
	  private String selectedImagePath;
	  
	  private ImageView mCameraImageView;
	  private static Bitmap mCameraBitmap;
	  private static Bitmap bmpGrayscale;
	  private Button mSaveImageButton;
	  private Button openImageButton;
	  private Button mFFTImageButton;
	  private Button mAutoDetect;
	  private Button mDrawButton;
	  
	  public static int width,height;
	  int rotation = 90;
	  private int numberOfdetect = 20;
	  
	  private int decode_width = 360;
	  private int decode_height = 180;
	  
	  private static String result = " ";
	  public static String[] codewordString ={"01010101","10010110","10110101","10101101"};
	//  public String[] H = {"10101010","0110011","0001111"};
	//  public String[] h_T={"100","010","110","001","101","011","111"};
	//  public String[] word = {"0000000", "1010001","1110010","0100011","0110100","1100101","1000110","0010111",
	//		                  "1101000", "0111001","0011010","1001011", "1011100","0001101","0101110","1111111"};
	  private OnClickListener mCaptureImageButtonClickListener = new OnClickListener() {
	    @Override
	    public void onClick(View v) {
	      OverlayView.started = false;
	      startImageCapture();
	    }
	  };
	  
	  private OnClickListener mDrawButtonClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			OverlayView.started = true;
		}
	  };
	  private OnClickListener mSaveImageButtonClickListener = new OnClickListener() {
	    @Override
	    public void onClick(View v) {
	      File saveFile = openFileForImage();
	      if (saveFile != null) {
	        saveImageToFile(saveFile);
	      } else {
	        Toast.makeText(MainActivity.this, "Unable to open file for saving image.",
	        Toast.LENGTH_LONG).show();
	      }
	    }
	  };
	  
	  @Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_main);
	    
	    mCameraImageView = (ImageView) findViewById(R.id.camera_image_view);
	    
	    findViewById(R.id.capture_image_button).setOnClickListener(mCaptureImageButtonClickListener);
	    
	    mSaveImageButton = (Button) findViewById(R.id.save_image_button);
	    mSaveImageButton.setOnClickListener(mSaveImageButtonClickListener);
	    mSaveImageButton.setEnabled(false);
	    
	    mDrawButton = (Button) findViewById(R.id.drawFocus);
	    mDrawButton.setOnClickListener(mDrawButtonClickListener);
	    
	    
	    //findViewById(R.id.loadingPanel).setVisibility(View.INVISIBLE);
	    Display display = getWindowManager().getDefaultDisplay();
	    Point size = new Point();
	    display.getSize(size);
	    width = size.x;
	    height = size.y;
	    
	    
	   
	    
	    mFFTImageButton = (Button) findViewById(R.id.fft);
	    mFFTImageButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//decodeImage();
				String tmp = random_detection();

				Intent intent = new Intent(getBaseContext(), result.class);
			    intent.putExtra("result", tmp);
			    startActivity(intent);
				
			}
		});
	    
	    mAutoDetect = (Button) findViewById(R.id.autoD);
	    mAutoDetect.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this, AutoDetection.class);
				startActivity(intent);
			}
		});
	    
	    openImageButton = (Button) findViewById(R.id.openFile);
	    openImageButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

                // in onCreate or any event where your want the user to
                // select a file
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        "Select Picture"), SELECT_PICTURE);
				/*
				String storageState = Environment.getExternalStorageDirectory().getPath();
				
				String imageURI = storageState+"/Pictures/com.oreillyschool.android2.camera/10110100_1000.png";
				//Uri uri = Uri.parse(Environment.getExternalStorageDirectory().toString() + imageURI); 
				File imageFile = new File(imageURI);
				if(imageFile.exists()){
					Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
				
					
					Matrix matrix = new Matrix();

					matrix.postRotate(90);

					mCameraBitmap = Bitmap.createBitmap(bitmap , 0, 0, bitmap.getWidth(),bitmap.getHeight(), matrix, true);
					mCameraImageView.setImageBitmap(mCameraBitmap);
				} 
				*/
				
			}
		});
	  }
	  
	  @Override
	  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == TAKE_PICTURE_REQUEST_B) {
	      if (resultCode == RESULT_OK) {
	        // Recycle the previous bitmap.
	        if (mCameraBitmap != null) {
	          mCameraBitmap.recycle();
	          mCameraBitmap = null;
	        }
	        Bundle extras = data.getExtras();
	       // mCameraBitmap = (Bitmap) extras.get("data");
	        byte[] cameraData = extras.getByteArray(CameraActivity.EXTRA_CAMERA_DATA);
	        if (cameraData != null) {
	          mCameraBitmap = BitmapFactory.decodeByteArray(cameraData, 0, cameraData.length);
	          Matrix matrix = new Matrix();
              matrix.postRotate(90);

			  mCameraBitmap = Bitmap.createBitmap(mCameraBitmap , 0, 0, mCameraBitmap.getWidth(), mCameraBitmap.getHeight(), matrix, true);
	          mCameraImageView.setImageBitmap(mCameraBitmap);
	          mSaveImageButton.setEnabled(true);
	        }
	      } 
	      
	      else {
		        mCameraBitmap = null;
		        mSaveImageButton.setEnabled(false);
		   }
	      
	    }
	    else if (requestCode == SELECT_PICTURE){
            if(resultCode ==RESULT_OK){
            	 Uri selectedImageUri = data.getData();
                 selectedImagePath = getPath(selectedImageUri);
                 File imageFile = new File(selectedImagePath);
 				 if(imageFile.exists()){
 					Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
 				
 					
 					Matrix matrix = new Matrix();

 					matrix.postRotate(0);

 					mCameraBitmap = Bitmap.createBitmap(bitmap , 0, 0, bitmap.getWidth(),bitmap.getHeight(), matrix, true);
 					mCameraImageView.setImageBitmap(mCameraBitmap);
 				} 
            }
	    }
	   
	  }
	  private void startImageCapture() {
	//	    startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), TAKE_PICTURE_REQUEST_B);
		    startActivityForResult(new Intent(MainActivity.this, CameraActivity.class), TAKE_PICTURE_REQUEST_B);
	}
	  private File openFileForImage() {
		    File imageDirectory = null;
		    String storageState = Environment.getExternalStorageState();
		    if (storageState.equals(Environment.MEDIA_MOUNTED)) {
		      imageDirectory = new File(
		        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
		        "com.oreillyschool.android2.camera");
		      if (!imageDirectory.exists() && !imageDirectory.mkdirs()) {
		        imageDirectory = null;
		      } else {
		        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_mm_dd_hh_mm",
		          Locale.getDefault());
		    
		        return new File(imageDirectory.getPath() +
		          File.separator + "image_" +
		          dateFormat.format(new Date()) + ".png");
		      }
		    }
		    return null;
		  }
	  	
		  private void saveImageToFile(File file) {
		    if (mCameraBitmap != null) {
		      FileOutputStream outStream = null;
		      try {
		        outStream = new FileOutputStream(file);
		        if (!mCameraBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)) {
		          Toast.makeText(MainActivity.this, "Unable to save image to file.",
		          Toast.LENGTH_LONG).show();
		        } else {
		          Toast.makeText(MainActivity.this, "Saved image to: " + file.getPath(),
		          Toast.LENGTH_LONG).show();
		        }
		        outStream.close();
		      } catch (Exception e) {
		        Toast.makeText(MainActivity.this, "Unable to save image to file.",
		        Toast.LENGTH_LONG).show();
		      }
		    }
		  }
		  
		  @SuppressLint("ShowToast")
		private void decodeImage(){
			   //first change image from RGB to gray scale
			   
			   
			 //  bmpRotate = Bitmap.createBitmap(bmpGrayscale, 0, 0, bmpGrayscale.getWidth(), bmpGrayscale.getHeight(), matrix, true);
			   
			   //mCameraBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test_500);
			  int[] location = new int[2]; 
			  mCameraImageView.getLocationOnScreen(location);
			   int imageX = location[0];
			   int imageY = location[1];
			   int imageW = mCameraImageView.getWidth();
			   int imageH = mCameraImageView.getHeight();
			   int imgWidth = mCameraBitmap.getWidth();
			   int imgHeight = mCameraBitmap.getHeight();
			   
			   bmpGrayscale = toGrayscale(mCameraBitmap);
			   mCameraImageView.setImageBitmap(bmpGrayscale);
			   
			   int p = bmpGrayscale.getPixel(479, 639);
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
			  
			  
			  if (OverlayView.started){
				  if (OverlayView.x-OverlayView.size/2-imageX > 0){
					  decodeXstart = (int) ((OverlayView.x-OverlayView.size/2-imageX)/imageW*imgWidth);
				  }else{
					  decodeXstart = 0;
				  }
			  }
			  else{
				  decodeXstart = 0;
			  }
			  
			  if (OverlayView.started){
				  if (OverlayView.y-OverlayView.size/2-imageY > 0){
					  decodeYstart = (int) ((OverlayView.y-OverlayView.size/2-imageY)/imageH *imgHeight);
				  }else{
					  decodeYstart = 0;
				  }
			  }
			  else{
				  decodeYstart = 0;
			  }
			  
			  int decodeXend ;
			  int decodeYend ;
			  
			  if (OverlayView.started){
				  if (OverlayView.x+OverlayView.size/2-imageX < imageW){
					  decodeXend =  (int) ((OverlayView.x+OverlayView.size/2-imageX)/imageW*imgWidth);
				  }else{
					  decodeXend = imgWidth;
				  }
			  }
			  else{
				  decodeXend = imgWidth;
			  }
			  
			  if (OverlayView.started){
				  if (OverlayView.y+OverlayView.size/2-imageY < imageH){
					  decodeYend =(int) ((OverlayView.y+OverlayView.size/2-imageY)/imageH *imgHeight);
				  }else{
					  decodeYend = imgHeight;
				  }
			  }
			  else{
				  decodeYend = imgHeight;
			  }
			  Log.d("imgW",Float.toString(imageW));
			  Log.d("imgH",Float.toString(imageH));
			  
			  Log.d("x",Float.toString(OverlayView.x));
			  Log.d("y",Float.toString(OverlayView.y));
			  
			  Log.d("x_s",Integer.toString(decodeXstart));
			  Log.d("y_s",Integer.toString(decodeYstart));
			  
			  Log.d("x_d",Integer.toString(decodeXend));
			  Log.d("y_d",Integer.toString(decodeYend));
			  
			  Log.d("x_i",Integer.toString(imageX));
			  Log.d("y_i",Integer.toString(imageY));
			  
			  // sum the pixels
			  if(OverlayView.started) imgWidth = decodeXend-decodeXstart;
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
			  result = "";
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
			  Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG);
			   result = correlation_detection(result);
			  
			   Intent intent = new Intent(getBaseContext(), result.class);
		       intent.putExtra("result", result);
		       startActivity(intent);
			    
			  
		  }
		  public static Bitmap toGrayscale(Bitmap bmpOriginal)
		  {        
		      int width, height;
		      height = bmpOriginal.getHeight();
		      width = bmpOriginal.getWidth();    

		      Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		      Canvas c = new Canvas(bmpGrayscale);
		      Paint paint = new Paint();
		      ColorMatrix cm = new ColorMatrix();
		      cm.setSaturation(0);
		      ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
		      paint.setColorFilter(f);
		      c.drawBitmap(bmpOriginal, 0, 0, paint);
		      return bmpGrayscale;
		  }
		  
		  
		  /**
		     * helper to retrieve the path of an image URI
		     */
		    public String getPath(Uri uri) {
		            // just some safety built in 
		            if( uri == null ) {
		                // TODO perform some logging or show user feedback
		                return null;
		            }
		            // try to retrieve the image from the media store first
		            // this will only work for images selected from gallery
		            String[] projection = { MediaStore.Images.Media.DATA };
		            Cursor cursor = managedQuery(uri, projection, null, null, null);
		            if( cursor != null ){
		                int column_index = cursor
		                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		                cursor.moveToFirst();
		                return cursor.getString(column_index);
		            }
		            // this is our fallback here
		            return uri.getPath();
		    }
		    
		    
		    
		   /*Error correction*/
		    /*
		    public String Error_correction(String result){
		    	int first = matrix_muti(result, H[0]);
		    	int second = matrix_muti(result, H[1]);
		    	int Third = matrix_muti(result, H[2]);
		    	int i = 0;
		    	while(i<8){
		    		if(first==(int) h_T[i].charAt(0)
		    		 && second==(int) h_T[i].charAt(1)
		    		 && Third==(int) h_T[i].charAt(2))
		    		 break;
		    			
		    	}
				
		    	String corrString = new String("");
		    	for(int j = 0 ; j < result.length();j++){
		    		if(j != i) corrString += result.charAt(j);
		    		else{
		    			if(result.charAt(i)=='1') corrString+='0';
		    			else corrString+='1';
		    		}
		    	}
		    	
		    	return corrString;
		    	
		    }
		    */
		    public static String correlation_detection(String result){
		    	
		    	if(result.length()<8) return "noting detected";
		    	
		    	Log.d("raw result",result);
		    	
		    	for(int i = 0;i< result.length()-7;i++){
					for(int j = 0 ; j < 4; j++){
						String detect = result.substring(i, i+8);
						int corr = matrix_muti(detect, codewordString[j]);
						Log.d("detect",Integer.toString(corr));
						if(corr >= 4){
							for(int m = 0; m<4;m++){
								if(detect.equals(codewordString[m])) return detect;
							}
							
							
						}
					}
				}
		    	
		    	
		    	
		    	return "decode failure";
		    	
		    }
		    public static int matrix_muti(String a, String b){
		    	int result = 0;
				for(int i = 0 ; i < a.length();i++){
					int x,y;
					if (a.charAt(i) =='1') x =1;
					else x=0;
					
					if(b.charAt(i) =='1') y =1;
					else y=0;
					result+= x*y;
				}
		    	//result = result % 2;
		    	return result;
		    	
		    }
		    
		    public static int vector_multi(String a, int b){
		    	int result = 0;
		    	String tmp = Integer.toBinaryString(b);
		    	while(tmp.length()<7){
		    		tmp = '0'+tmp;
		    	}
		    	
		    	for(int i = 0 ; i < a.length();i++){
					result+= (int) a.charAt(i) * (int) tmp.charAt(i);
				}
		    	
		    	return result;
		    	
		    }
		    public static int randInt(int min, int max) {

		        // NOTE: Usually this should be a field rather than a method
		        // variable so that it is not re-seeded every call.
		        Random rand = new Random();

		        // nextInt is normally exclusive of the top value,
		        // so add 1 to make it inclusive
		        int randomNum = rand.nextInt((max - min) + 1) + min;

		        return randomNum;
		    }
		    private String random_detection(){
		    	int imageW = mCameraImageView.getWidth();
				int imageH = mCameraImageView.getHeight();
				int imgWidth = mCameraBitmap.getWidth();
				int imgHeight = mCameraBitmap.getHeight();
		    	String final_String = new String();
		    	int[] record = {0,0,0,0};
		    	for(int i = 0 ; i<numberOfdetect;i++){
		    		int x = randInt(0, imageW-(int)(((float) decode_width)/imgWidth*imageW));
		    		int y = randInt(0, imageH-(int)(((float) decode_height)/imgHeight*imageH));
		    		String tmpString =decodeImage_auto(x, y);
		    		for(int j = 0 ; j < 4 ;j++){
		    			if(tmpString.equals(codewordString[j]))
		    				record[j]++;
		    		}
		    	}
		    	int maxIndex = 0;
		    	for (int i = 1; i < record.length; i++){
		    	   int newnumber = record[i];
		    	   if ((newnumber > record[maxIndex])){
		    	   maxIndex = i;
		    	  }
		    	}
		    	return codewordString[maxIndex];
		    }
		    private String decodeImage_auto(int x, int y){
				 
				 
				  int[] location = new int[2]; 
				  mCameraImageView.getLocationOnScreen(location);
				   int imageX = location[0];
				   int imageY = location[1];
				   int imageW = mCameraImageView.getWidth();
				   int imageH = mCameraImageView.getHeight();
				   int imgWidth = mCameraBitmap.getWidth();
				   int imgHeight = mCameraBitmap.getHeight();
				   
				   bmpGrayscale = toGrayscale(mCameraBitmap);
				   mCameraImageView.setImageBitmap(bmpGrayscale);
				   
				  int decodeXstart;
				  int decodeYstart;
				  
				  
				  decodeXstart = (int )( ((float)x) /imageW * imgWidth);
				  if (decodeXstart>imgWidth-decode_width)
					  decodeXstart = imgWidth-decode_width;
				
				
				  decodeYstart = (int )( ((float)y) /imageH * imgHeight);
				  if (decodeYstart>imgHeight-decode_height)
					  decodeXstart = imgHeight-decode_height;
				
				  
				  int decodeXend  = decodeXstart + decode_width;
				  int decodeYend  = decodeYstart+ decode_height;
				  
				 
				  
				  
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
				  String tmp = "";
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
						        tmp+="1";
						        one = one -gap;
						        if (one < gap)
						            one = 0;
						        
				  			}
						    
						    
						    while(zero >= gap){
						        one = 0;
						        tmp+="0";
						        zero = zero -gap;
						        if (zero < gap )
						            zero = 0;
						        
						    }
						    
				  }
				  
				  for (int j = 0 ; j < decode.length;j++){
					  decode[j]=0;
				  }
				  
				  
				   String tmp_result = correlation_detection(tmp);
				  
				   return tmp_result;
				    
				  
			  }
		    
		    private void start_browse(String input){
		    	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(input));
		    	startActivity(browserIntent);
		    }
}