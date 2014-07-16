package com.example.touchpanel;

import java.io.FileOutputStream;
import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * This class is pulled from the Square Up Blog: http://corner.squareup.com/2010/07/smooth-signatures.html
 * This code is Apache 2.0-licensed
 * @author Eric Burke - Square Android Client Programmer
 *
 */
public class SignatureView extends View {

	  private static final float STROKE_WIDTH = 10f;

	  /** Need to track this so the dirty region can accommodate the stroke. **/
	  private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
	  private ArrayList<Bitmap> bitmap_list=new ArrayList<Bitmap>();
	  private Paint paint = new Paint();
	  private Path path = new Path();
	  private ImageView imgview;
	  private ContentResolver cr;
	  private int width;
	  private int height;
	  private int space=1;
	  /**
	   * Optimizes painting by invalidating the smallest possible area.
	   */
	  private float lastTouchX;
	  private float lastTouchY;
	  private final RectF dirtyRect = new RectF();

	  public SignatureView(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    mHandler = new Handler();
	    paint.setAntiAlias(true);
	    paint.setColor(Color.BLACK);
	    paint.setStyle(Paint.Style.STROKE);
	    paint.setStrokeJoin(Paint.Join.ROUND);
	    paint.setStrokeWidth(STROKE_WIDTH);
	  }
	  
	  public void setContentResolver(ContentResolver cr){
		  this.cr=cr;
	  }
	  
	  public void setImageView(ImageView img){
		  imgview=img;
	  }
	  
	  public void setSize(int width,int height){
		  this.width=width;
		  this.height=height;
	  }

	  /**
	   * Erases the signature.
	   */
	  public void clear() {
		Bitmap bmp=Bitmap.createBitmap(width+space,height,Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(bmp);
		c.drawPath(path, paint);
		bitmap_list.add(bmp);
		imgview.setImageBitmap(drawBitmap());
	    path.reset();

	    // Repaints the entire view.
	    invalidate();
	  }
	  
	  public void goback(){
		  if(bitmap_list.size()==0)
			  return;
		  bitmap_list.remove(bitmap_list.size()-1);
      	  imgview.setImageBitmap(drawBitmap());
	  }
	  
	  /*
	  public Path CombinePath(){
		  if(path_list.size()==0)
			  return null;
		  Path p=path_list.get(0);
		  for(int i=0;i<path_list.size();i++){
			  p.addPath(path_list.get(i),(width+space)*i,0);
		  }
		  return p;
	  }
	  */
	  
	  
	  public Bitmap drawBitmap(){
		  
		  if(bitmap_list.size()==0)
			  return null;
		  Bitmap bmp=Bitmap.createBitmap(bitmap_list.size()*(width+space),height,Bitmap.Config.ARGB_8888);
		  Canvas c = new Canvas(bmp);
		  //Path p=CombinePath();
		  //c.drawPath(p, paint);
		  for(int i=0;i<bitmap_list.size();i++)
			  c.drawBitmap(bitmap_list.get(i),(width+space)*i,0, paint);
		  return bmp;
	  }

	  @Override
	  protected void onDraw(Canvas canvas) {
	    canvas.drawPath(path, paint);
	  }

	  @Override
	  public boolean onTouchEvent(MotionEvent event) {
	    float eventX = event.getX();
	    float eventY = event.getY();

	    switch (event.getAction()) {
	      case MotionEvent.ACTION_DOWN:
	    	
	    	mHandler.removeCallbacks(myTask);
	        path.moveTo(eventX, eventY);
	        lastTouchX = eventX;
	        lastTouchY = eventY;
	        // There is no end point yet, so don't waste cycles invalidating.
	        return true;

	      case MotionEvent.ACTION_MOVE:
	      
	        // Start tracking the dirty region.
	        resetDirtyRect(eventX, eventY);

	        // When the hardware tracks events faster than they are delivered, the
	        // event will contain a history of those skipped points.
	        int historySize = event.getHistorySize();
	        for (int i = 0; i < historySize; i++) {
	          float historicalX = event.getHistoricalX(i);
	          float historicalY = event.getHistoricalY(i);
	          expandDirtyRect(historicalX, historicalY);
	          path.lineTo(historicalX, historicalY);
	        }

	        // After replaying history, connect the line to the touch point.
	        path.lineTo(eventX, eventY);
	        break;
	      case MotionEvent.ACTION_UP:
	    	  count=0;
	    	 
	    	  mHandler.postDelayed(myTask, 100);
	    	  break;
	      default:
	        return false;
	    }

	    // Include half the stroke width to avoid clipping.
	    invalidate(
	        (int) (dirtyRect.left - HALF_STROKE_WIDTH),
	        (int) (dirtyRect.top - HALF_STROKE_WIDTH),
	        (int) (dirtyRect.right + HALF_STROKE_WIDTH),
	        (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

	    lastTouchX = eventX;
	    lastTouchY = eventY;

	    return true;
	  }
	  
	  public void save(String filename){
		  if(bitmap_list.size()==0)
			  return;
		  FileOutputStream out = null;
		  try {
		         out = new FileOutputStream(filename);
		         drawBitmap().compress(Bitmap.CompressFormat.PNG, 90, out);
		  } catch (Exception e) {
		      e.printStackTrace();
		  } finally {
		         try{
		             out.close();
		         } catch(Throwable ignore) {}
		  }

		  MediaStore.Images.Media.insertImage(cr, drawBitmap(), "myPhoto", "this is a Photo");
	  }

	  /**
	   * Called when replaying history to ensure the dirty region includes all
	   * points.
	   */
	  private void expandDirtyRect(float historicalX, float historicalY) {
	    if (historicalX < dirtyRect.left) {
	      dirtyRect.left = historicalX;
	    } else if (historicalX > dirtyRect.right) {
	      dirtyRect.right = historicalX;
	    }
	    if (historicalY < dirtyRect.top) {
	      dirtyRect.top = historicalY;
	    } else if (historicalY > dirtyRect.bottom) {
	      dirtyRect.bottom = historicalY;
	    }
	  }

	  /**
	   * Resets the dirty region when the motion event occurs.
	   */
	  private void resetDirtyRect(float eventX, float eventY) {

	    // The lastTouchX and lastTouchY were set when the ACTION_DOWN
	    // motion event occurred.
	    dirtyRect.left = Math.min(lastTouchX, eventX);
	    dirtyRect.right = Math.max(lastTouchX, eventX);
	    dirtyRect.top = Math.min(lastTouchY, eventY);
	    dirtyRect.bottom = Math.max(lastTouchY, eventY);
	  }
	  static int count=0;
	  private Handler mHandler;
	  Runnable myTask = new Runnable() {
	    @Override
	    public void run() {
	       //do work
	       count++;
	       if(count==10)
	       {
	    	   mHandler.removeCallbacks(myTask);
	    	   clear();
	    	   return;
	       }
	       Log.d("test",String.valueOf(count));
	       mHandler.postDelayed(this, 100);
	    }
	  };
	}