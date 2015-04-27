package com.example.customize_camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

public class mask extends View {
   

	public static float x,y;
	Bitmap bmp;
	Paint mPaint;
	public static int wid = 360;
	public static int hei = 180;
	
	
	public static boolean touched = false;
	public static boolean started = false;
	
	 

	public mask(Context context,AttributeSet attrs) {
		super(context,attrs);
	    x = y = 0;
	    mPaint = new Paint();
	    
	    
	}


	@Override
    protected void onDraw(Canvas canvas) {
		
	    mPaint.setStyle(Style.STROKE);
	    mPaint.setStrokeWidth(5);
	    x = MainActivity.width/2;
	    y = MainActivity.height/2;
		mPaint.setColor(Color.GREEN);
		canvas.drawRect(x-wid, y-hei, x+wid, y+hei, mPaint);
		
		mPaint.setStyle(Style.FILL);
		mPaint.setColor(Color.parseColor("#80000000"));
		//canvas.drawColor(Color.TRANSPARENT);
	    canvas.drawRect(0, 0, MainActivity.width, y-hei, mPaint);
	    canvas.drawRect(0, y-hei, x-wid , y+hei, mPaint);
	    canvas.drawRect(0, y+hei, MainActivity.width , MainActivity.height, mPaint);
	    canvas.drawRect(x+wid, y-hei, MainActivity.width , y+hei, mPaint);

	     
    }

	
}