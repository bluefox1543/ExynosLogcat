package com.sec.apps.exynos_logcat;

import android.content.Context;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListView;

public class OverlayListview extends ListView {

	private WindowManager wm;
	private LinearLayout mLinearLayout;
	private static final int NONE = 0;
	private static final int DRAG_RD = 3;
	int mode = NONE , zoomX1 = 0,zoomX2 = 0,zoomY1 = 0,zoomY2 = 0;

	public OverlayListview(Context context){
		super(context);
		mLinearLayout = null;
		wm = null;
	}
	
	public OverlayListview(Context context , LinearLayout base , WindowManager manager) {
		super(context);
		mLinearLayout = base;
		wm = manager;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			zoomX1 = (int)event.getX();
			zoomY1 = (int)event.getY();

			if((zoomX1 > mLinearLayout.getWidth() - 30) || (zoomY1 > mLinearLayout.getHeight() - 70)){
				mode = DRAG_RD;
				return false;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if(mode == DRAG_RD){
				zoomX2 = (int)event.getX();
				zoomY2 = (int)event.getY();
				if((zoomX2 - zoomX1) > 10){
					zoomX1 = zoomX2;
					WindowManager.LayoutParams params = (WindowManager.LayoutParams)mLinearLayout.getLayoutParams();
					params.width+=10;
					wm.updateViewLayout(mLinearLayout, params);
					return false;
				} else if((zoomX2 - zoomX1) < -10){
					WindowManager.LayoutParams params = (WindowManager.LayoutParams)mLinearLayout.getLayoutParams();
					params.width-=10;
					wm.updateViewLayout(mLinearLayout, params);
					return false;
				} else if((zoomY2 - zoomY1) > 10){
					zoomY1 = zoomY2;
					WindowManager.LayoutParams params = (WindowManager.LayoutParams)mLinearLayout.getLayoutParams();
					params.height+=10;
					wm.updateViewLayout(mLinearLayout, params);
					return false;
				} else if((zoomY2 - zoomY1) < -10){
					WindowManager.LayoutParams params = (WindowManager.LayoutParams)mLinearLayout.getLayoutParams();
					params.height-=10;
					wm.updateViewLayout(mLinearLayout, params);
					return false;
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			mode = NONE;
			return false;
		}
		return super.onInterceptTouchEvent(event);
	}
}