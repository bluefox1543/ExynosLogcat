package com.sec.apps.exynos_logcat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class LogcatProcess {
	private static final long CAT_DELAY = 1;

	private Context mContext;
	private Handler mHandler;
	private Process mlogcatProc;
	private ScheduledExecutorService mExecutor;
	private String mFilter = null;
	private BufferedReader mReader = null;
	private ArrayList<String> mLogCache = new ArrayList<String>();
	private boolean mRunning = false;
	private boolean mIsFilterPattern;
	private boolean mPlay = true;
	private long mlastCat = -1;

	private Preferences mPrefs = null;
	private Pattern mFilterPattern = null;
	private LogLevel mLevel = null;
	private LogType mType = null;

	PatternFormat mFormat = null;

	//로그 갱신
	private Runnable catRunner = new Runnable() {

		@Override
		public void run() {
			if (!mPlay) {
				return;
			}
			long now = System.currentTimeMillis();
			if (now < mlastCat + CAT_DELAY) {
				return;
			}
			mlastCat = now;
			cat();
		}
	};

	public LogcatProcess(Context context, Handler handler) {
		mHandler = handler;
		
		mContext = context;
		mPrefs = new Preferences(mContext);

		mLevel = mPrefs.getLevel();
		mIsFilterPattern = mPrefs.isFilterPattern();
		mFilter = mPrefs.getFilter();
		mFilterPattern = mPrefs.getFilterPattern();
		mFormat = mPrefs.getFormat();
		mType = mPrefs.getType();
	}

	//로그캣 시작
	public void start() {
		stop();

		mRunning = true;

		mExecutor = Executors.newScheduledThreadPool(1);
		mExecutor.scheduleAtFixedRate(catRunner, CAT_DELAY, CAT_DELAY, TimeUnit.SECONDS);

		try {
			Message m = Message.obtain(mHandler, LogcatMain.CLEAR_EVT);
			mHandler.sendMessage(m);

			List<String> progs = new ArrayList<String>();

			progs.add("logcat");
			progs.add("-v");
			progs.add(mFormat.getValue());
			//logcat -v brief = 레벨/태그/PID 포커싱(기본 포맷)
			//logcat -v process = PID만 출력
			//logcat -v tag = 레벨/태그만 출력
			//logcat -v raw = raw 로그 메시지 출력. 다른 메타데이터 필드는 포함하지 않는다.
			//logcat -v time = 프로세스에 대한 날짜, 시행날짜, 레벨/태그/PID 포함.
			//logcat -v thread = 레벨/태그/PID/TID 포함
			//logcat -v threadtime = 날짜, 시행날짜, 레벨/태그/PID/TID 포함
			//logcat -v long = 모든 메타데이터 포함.
			if (mType != LogType.MAIN) {
				progs.add("-b");
				progs.add(mType.getValue());
			}
			progs.add("*:" + mLevel);

			//mlogcatProc = new ProcessBuilder(progs).command("su").redirectErrorStream(true).start();
			mlogcatProc = Runtime.getRuntime().exec(progs.toArray(new String[0]));
			mReader = new BufferedReader(new InputStreamReader(mlogcatProc.getInputStream()), 1024);

			String line;
			while (mRunning && (line = mReader.readLine()) != null) {
				if (!mRunning) {
					break;
				}
				if (line.length() == 0) {
					continue;
				}
				if (mIsFilterPattern) {
					if (mFilterPattern != null && !mFilterPattern.matcher(line).find()) {
						continue;
					}
				} else {
					if (mFilter != null && !line.toLowerCase().contains(mFilter.toLowerCase())) {
						continue;
					}
				}
				synchronized (mLogCache) {
					mLogCache.add(line);
				}
			}
		} catch (IOException e) {
			//Log.e("start()", "로그 읽기 실패", e);
			return;
		} finally {

			if (mlogcatProc != null) {
				mlogcatProc.destroy();
				mlogcatProc = null;
			}
			if (mReader != null) {
				try {
					mReader.close();
					mReader = null;
				} catch (IOException e) {
					//Log.d("start()", "스트림 닫기 실패", e);
				}
			}
		}
	}

	//로그 메시지 핸들러로 전송.
	private void cat() {
		Message msg;

		if (mLogCache.size() > 0) {
			synchronized (mLogCache) {
				if (mLogCache.size() > 0) {
					msg = Message.obtain(mHandler, LogcatMain.CAT_EVT);
					msg.obj = mLogCache.clone();
					mLogCache.clear();
					mHandler.sendMessage(msg);					
				}
			}
		}
	}

	public void stop() {
		mRunning = false;

		if (mExecutor != null && !mExecutor.isShutdown()) {
			mExecutor.shutdown();
			mExecutor = null;
		}
	}

	public boolean isRunning() {
		return mRunning;
	}

	public boolean isPlay() {
		return mPlay;
	}

	public void setPlay(boolean play) {
		mPlay = play;
	}
}