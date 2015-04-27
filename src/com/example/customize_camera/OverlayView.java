package com.example.customize_camera;

import android.R.integer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;

public class OverlayView extends View {
   

	public static float x,y;
	Bitmap bmp;
	Paint mPaint;
	public static int size = 240;
	private float dist;
	
	public static boolean touched = false;
	public static boolean started = false;

	public OverlayView(Context context,AttributeSet attrs) {
		super(context,attrs);
	    x = y = 0;
	    mPaint = new Paint();
	    
	    
	}


	@Override
    protected void onDraw(Canvas canvas) {
		mPaint.setColor(Color.TRANSPARENT);
	    mPaint.setStyle(Style.FILL);
	    
		canvas.drawColor(Color.TRANSPARENT);
		canvas.drawRect(0, 0, MainActivity.width, MainActivity.height-100, mPaint);
	    if(touched && started)
	    {
	    	mPaint.setColor(Color.GREEN);
		    mPaint.setStyle(Style.STROKE);
		    mPaint.setStrokeWidth(3);
		    int startX  = (int) ((x-size/2<0) ? 0 : (x-size/2));
		    int startY  = (int) ((y-size/2<0) ? 0 : (y-size/2));
		    int endX  = (int) ((x+size/2> MainActivity.width) ? MainActivity.width : (x+size/2));
		    int endY  = (int) ((y+size/2> MainActivity.height-100) ? MainActivity.height-100 : (y+size/2));
			
	        canvas.drawRect(startX, startY,endX, endY, mPaint);

	    }  
    }

	@Override
	public boolean onTouchEvent (MotionEvent event)
	{
		touched =true;
	    int action = event.getAction();
	    if(event.getPointerCount() > 1){
	    	
	        
	         
	         if(action ==  MotionEvent.ACTION_MOVE ){
	         x = ( event.getX(0) + event.getX(1))/2;
	         y = ( event.getY(0) + event.getY(1))/2;
	         size = (int ) (1.414*getFingerSpacing(event)/2);
	         }
	        
	    }else{
	    	
	    	if(action ==  MotionEvent.ACTION_DOWN || action ==MotionEvent.ACTION_MOVE){
	    		//getting the touched x and y position
	 	    x = event.getX();
	 	    y = event.getY();
	    	}
	    	
	    }
	    invalidate();
	    return true;
	}
	
	/** Determine the space between the first two fingers */
	  private float getFingerSpacing(MotionEvent event) {
	      // ...
	      float x = event.getX(0) - event.getX(1);
	      float y = event.getY(0) - event.getY(1);
	      return FloatMath.sqrt(x * x + y * y);
	  }
}