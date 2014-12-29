package com.sec.apps.exynos_logcat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class LogOverlayService extends Service implements OnTouchListener {

	private LinearLayout mLinearLayout;
	private RelativeLayout TopMenuLayout;
	private Button moveBtn,closeBtn,PlayBtn,ReturnBtn,ClearBtn,SaveBtn,SetupBtn;
	private ListView mLogView;
	private LogcatProcess mLogcat;
	private Preferences mPrefs;
	private ServiceListAdapter mListAdapter;
	private WindowManager wm = null;
	private LogLevel mlastlevel = LogLevel.V;
	private static final Executor executor = Executors.newCachedThreadPool();

	private float offsetX,offsetY;
	private float originX,originY;
	private float TransX,TransY;
	private boolean moving = true , absolutePos = true , mPlay = true;
	private int MOVE_ID = 11,PLAY_ID=12,RETURN_ID=13,CLEAR_ID=14,SAVE_ID=15;
	private static final int MAX_COUNT = 250;

	private Handler mHandler = new Handler() {
		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case LogcatMain.CAT_EVT:
				final List<String> lines = (List<String>) msg.obj;
				cat(lines);
				break;
			case LogcatMain.CLEAR_EVT:
				mListAdapter.clear();
				break;
			}
		}
	};

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		wm = (WindowManager)getSystemService(WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		int width=display.getWidth();
		int height=display.getHeight();

		if(Build.VERSION.SDK_INT >= 14){
			try {
				Point realSize = new Point();
				Display.class.getMethod("getRealSize", Point.class).invoke(display, realSize);
				width = realSize.x;
				height = realSize.y;
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
		
		int btnWidth = width / 15;
		int btnHeight = height / 15;
		int btnSize = (btnWidth<btnHeight)?btnWidth:btnHeight;

		mLinearLayout = new LinearLayout(this);
		mLinearLayout.setBackgroundColor(0x99ffffff);
		mLinearLayout.setOrientation(LinearLayout.VERTICAL);

		TopMenuLayout = new RelativeLayout(this);
		TopMenuLayout.setBackgroundColor(0x99ffffff);
		TopMenuLayout.setLayoutParams(new android.view.ViewGroup.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,btnSize));
		TopMenuLayout.setGravity(Gravity.CENTER | Gravity.TOP);

		moveBtn = new Button(this);
		RelativeLayout.LayoutParams moveParam = new RelativeLayout.LayoutParams(btnSize,btnSize);
		moveParam.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		moveParam.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		moveBtn.setLayoutParams(moveParam);
		moveBtn.setBackgroundResource(R.drawable.move);
		moveBtn.setId(MOVE_ID);
		moveBtn.setOnTouchListener(this);

		PlayBtn = new Button(this);
		RelativeLayout.LayoutParams PlayParam = new RelativeLayout.LayoutParams(btnSize,btnSize);
		PlayParam.addRule(RelativeLayout.RIGHT_OF, MOVE_ID);
		PlayParam.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		PlayBtn.setLayoutParams(PlayParam);
		PlayBtn.setBackgroundResource(android.R.drawable.ic_media_pause);
		PlayBtn.setId(PLAY_ID);
		PlayBtn.setOnTouchListener(this);

		ReturnBtn = new Button(this);
		RelativeLayout.LayoutParams ReturnParam = new RelativeLayout.LayoutParams(btnSize,btnSize);
		ReturnParam.addRule(RelativeLayout.RIGHT_OF, PLAY_ID);
		ReturnParam.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		ReturnBtn.setLayoutParams(ReturnParam);
		ReturnBtn.setBackgroundResource(android.R.drawable.ic_menu_revert);
		ReturnBtn.setId(RETURN_ID);
		ReturnBtn.setOnTouchListener(this);

		ClearBtn = new Button(this);
		RelativeLayout.LayoutParams ClearParam = new RelativeLayout.LayoutParams(btnSize,btnSize);
		ClearParam.addRule(RelativeLayout.RIGHT_OF, RETURN_ID);
		ClearParam.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		ClearBtn.setLayoutParams(ClearParam);
		ClearBtn.setBackgroundResource(android.R.drawable.ic_menu_close_clear_cancel);
		ClearBtn.setId(CLEAR_ID);
		ClearBtn.setOnTouchListener(this);

		SaveBtn = new Button(this);
		RelativeLayout.LayoutParams SaveParam = new RelativeLayout.LayoutParams(btnSize,btnSize);
		SaveParam.addRule(RelativeLayout.RIGHT_OF,CLEAR_ID);
		SaveParam.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		SaveBtn.setLayoutParams(SaveParam);
		SaveBtn.setBackgroundResource(android.R.drawable.ic_menu_save);
		SaveBtn.setId(SAVE_ID);
		SaveBtn.setOnTouchListener(this);

		SetupBtn = new Button(this);
		RelativeLayout.LayoutParams SetupParam = new RelativeLayout.LayoutParams(btnSize,btnSize);
		SetupParam.addRule(RelativeLayout.RIGHT_OF,SAVE_ID);
		SetupParam.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		SetupBtn.setLayoutParams(SetupParam);
		SetupBtn.setBackgroundResource(android.R.drawable.ic_menu_preferences);
		SetupBtn.setOnTouchListener(this);

		closeBtn = new Button(this);
		RelativeLayout.LayoutParams closeParam = new RelativeLayout.LayoutParams(btnSize,btnSize);
		closeParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		closeParam.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		closeBtn.setLayoutParams(closeParam);
		closeBtn.setBackgroundResource(android.R.drawable.ic_delete);
		closeBtn.setOnTouchListener(this);

		TopMenuLayout.addView(moveBtn);
		TopMenuLayout.addView(PlayBtn);
		TopMenuLayout.addView(ReturnBtn);
		TopMenuLayout.addView(ClearBtn);
		TopMenuLayout.addView(SaveBtn);
		TopMenuLayout.addView(SetupBtn);
		TopMenuLayout.addView(closeBtn);

		mListAdapter = new ServiceListAdapter(this, R.layout.listelement, new ArrayList<LogElement>(MAX_COUNT));
		mLogView = new OverlayListview(this,mLinearLayout,wm);
		mLogView.setAdapter(mListAdapter);
		mLogView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				pauseLog();
			}
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
			}
		});
		android.view.ViewGroup.LayoutParams logViewParam = new android.view.ViewGroup.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,android.view.ViewGroup.LayoutParams.MATCH_PARENT);
		mLogView.setLayoutParams(logViewParam);

		mLinearLayout.addView(TopMenuLayout);
		mLinearLayout.addView(mLogView);

		mPrefs = new Preferences(this);
		OverlaySize size = mPrefs.getOverlaySize();
		if(size.isDefaultSize()){
			size.setWidth(width / 2);
			size.setHeight(height / 2);
		}
		WindowManager.LayoutParams params = new WindowManager.LayoutParams(size.getWidth(),size.getHeight(),WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,0|WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,PixelFormat.TRANSLUCENT);
		params.gravity = Gravity.CENTER | Gravity.TOP;
		params.x = 0;
		params.y = 0;
		wm.addView(mLinearLayout, params);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		reset();
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(wm == null)
			wm = (WindowManager)getSystemService(WINDOW_SERVICE);
		mPrefs.setOverlaySize(new OverlaySize(mLinearLayout.getWidth(), mLinearLayout.getHeight(),false));
		wm.removeView(mLinearLayout);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(v.equals(moveBtn)){
			if (event.getAction() == MotionEvent.ACTION_DOWN){
				float x = event.getRawX();
				float y = event.getRawY();	
				//터치지점 x,y

				moving = false;

				int[] location = new int[2];
				mLinearLayout.getLocationOnScreen(location);

				originX = location[0];
				originY = location[1];
				//레이아웃의 위치(레이아웃 기준 0,0)

				offsetX = x-originX;
				offsetY = y-originY;
				//터치지점과 레이아웃0,0지점의 차이

				if(absolutePos){
					TransX = originX;
					TransY = originY;
					absolutePos = !absolutePos;
					return false;
				}

			} else if(event.getAction() == MotionEvent.ACTION_MOVE){
				float x = event.getRawX();
				float y = event.getRawY();	

				WindowManager.LayoutParams params = (WindowManager.LayoutParams)mLinearLayout.getLayoutParams();

				int newX = (int)(x - offsetX);
				int newY = (int)(y - offsetY);

				if(Math.abs(newX - originX) < 1 && Math.abs(newY - originY) < 1 && !moving){
					return false;
				}

				params.x = newX - (int)TransX;
				params.y = newY - (int)TransY;

				wm.updateViewLayout(mLinearLayout, params);
				moving = true;
			} else if(event.getAction() == MotionEvent.ACTION_UP){
				if(moving)
					return true;
			}
		}
		else if(v.equals(closeBtn)){
			if (event.getAction() == MotionEvent.ACTION_DOWN){
				this.stopSelf();
			}
		}
		else if(v.equals(PlayBtn)){
			if (event.getAction() == MotionEvent.ACTION_DOWN)
				if (mPlay) {
					pauseLog();
				} else {
					jumpBottom();
				}
		}
		else if(v.equals(ReturnBtn)){
			if (event.getAction() == MotionEvent.ACTION_DOWN){
				Intent intent = new Intent(this, LogcatMain.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				this.stopSelf();
			}
		}
		else if(v.equals(SaveBtn)){
			if (event.getAction() == MotionEvent.ACTION_DOWN){
				File f = save();
				String msg = getResources().getString(R.string.saving_log,f.toString());
				Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
			}
		}
		else if(v.equals(ClearBtn)){
			if (event.getAction() == MotionEvent.ACTION_DOWN){
				clear();
				reset();
			}
		}
		else if(v.equals(SetupBtn)){
			if (event.getAction() == MotionEvent.ACTION_DOWN){
				Intent intent = new Intent(this, PreferencesActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		}
		else if(v.equals(closeBtn)){
			if (event.getAction() == MotionEvent.ACTION_DOWN){
				this.stopSelf();
			}
		}
		/*else if(v.equals(mLogView)){
			switch(event.getAction()){
			case MotionEvent.ACTION_DOWN:
				zoomX1 = (int)event.getX();
				zoomY1 = (int)event.getY();

				if((zoomX1 > mLinearLayout.getWidth() - 10) && (zoomY1 > mLinearLayout.getHeight() - 60))
					mode = DRAG_RD;
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
					} else if((zoomX2 - zoomX1) < -10){
						WindowManager.LayoutParams params = (WindowManager.LayoutParams)mLinearLayout.getLayoutParams();
						params.width-=10;
						wm.updateViewLayout(mLinearLayout, params);
					} else if((zoomY2 - zoomY1) > 10){
						zoomY1 = zoomY2;
						WindowManager.LayoutParams params = (WindowManager.LayoutParams)mLinearLayout.getLayoutParams();
						params.height+=10;
						wm.updateViewLayout(mLinearLayout, params);
					} else if((zoomY2 - zoomY1) < -10){
						WindowManager.LayoutParams params = (WindowManager.LayoutParams)mLinearLayout.getLayoutParams();
						params.height-=10;
						wm.updateViewLayout(mLinearLayout, params);
					}
				}
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				mode = NONE;
				break;
			}
		}*/
		return false;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		WindowManager.LayoutParams params = (WindowManager.LayoutParams)mLinearLayout.getLayoutParams();
		params.x = 0;
		params.y = 0;

		wm.updateViewLayout(mLinearLayout, params);

		absolutePos = !absolutePos;
	}

	private void playLog() {
		if (mPlay) {
			return;
		}
		if (mLogcat != null) {
			mLogcat.setPlay(true);
			mPlay = true;
		} else {
			reset();
		}
		setPlayMenu();
	}

	private void pauseLog() {
		if (!mPlay) {
			return;
		}
		if (mLogcat != null) {
			mLogcat.setPlay(false);
			mPlay = false;
		}
		setPlayMenu();
	}

	public void reset() {
		mlastlevel = LogLevel.V;

		if (mLogcat != null) {
			mLogcat.stop();
		}

		mPlay = true;

		executor.execute(new Runnable() {
			@Override
			public void run() {
				mLogcat = new LogcatProcess(LogOverlayService.this, mHandler);
				mLogcat.start();
			}
		});
	}

	private void jumpBottom() {
		playLog();
		mLogView.setSelection(mListAdapter.getCount() - 1);
	}

	private void cat(final String s) {
		if (mListAdapter.getCount() > LogcatMain.MAX_COUNT) {
			mListAdapter.remove(0);
		}

		PatternFormat format = mLogcat.mFormat;
		LogLevel level = format.getLevel(s);
		if (level == null) {
			level = mlastlevel;
		} else {
			mlastlevel = level;
		}

		final LogElement elem = new LogElement(s, level);
		mListAdapter.add(elem);
	}

	private void cat(List<String> lines) {
		for (String line : lines) {
			cat(line);
		}
		jumpBottom();
	}

	private void clear() {
		try {
			Runtime.getRuntime().exec(new String[] { "logcat", "-c" });
		} catch (IOException e) {
			Log.e("ExynosLogcat", "로그캣 Clear 실패.", e);
		}
	}

	private File save() {
		final File path = new File(Environment.getExternalStorageDirectory(),"logcat_dump");
		final File file = new File(path.getPath() + File.separator + "Exynos_Logcat_" + LogcatMain.LogDateFormat.format(new Date()) + ".txt");
		Log.d("",path.getPath() + File.separator + "Exynos_Logcat_" + LogcatMain.LogDateFormat.format(new Date()) + ".txt");

		executor.execute(new Runnable() {
			@Override
			public void run() {
				String content = dump();

				if (!path.exists()) {
					path.mkdir();
				}

				BufferedWriter bw = null;
				try {
					file.createNewFile();
					bw = new BufferedWriter(new FileWriter(file), 1024);
					bw.write(content);
				} catch (IOException e) {
					Log.e("ExynosLogcat", "로그 저장 실패.", e);
				} finally {
					if (bw != null) {
						try {
							bw.close();
						} catch (IOException e) {
							Log.e("ExynosLogcat", "Writer 닫기 실패.", e);
						}
					}
				}
			}
		});

		return file;
	}

	private String dump() {
		StringBuilder sb = new StringBuilder();

		List<LogElement> elements = new ArrayList<LogElement>(mListAdapter.getElements());

		for (LogElement le : elements) {
			sb.append(le.getText());
			sb.append('\n');
		}

		return sb.toString();
	}

	public void setPlayMenu() {
		if (mPlay) {
			PlayBtn.setBackgroundResource(android.R.drawable.ic_media_pause);
		} else {
			PlayBtn.setBackgroundResource(android.R.drawable.ic_media_play);
		}
	}
}
