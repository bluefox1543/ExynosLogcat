package com.sec.apps.exynos_logcat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

public class LogSavingService extends Service {

	private int timersec = 0;
	private LogcatProcess mLogcat;
	private static final Executor executor = Executors.newCachedThreadPool();
	private final File path = new File(Environment.getExternalStorageDirectory(),"logcat_dump");
	private File file = null;
	private BufferedWriter bw = null;
	
	private Handler mHandler = new Handler() {
		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case LogcatMain.CAT_EVT:
				final List<String> lines = (List<String>) msg.obj;
				if(bw != null){
					for(String tmp : lines){
						save(tmp + "\n");
					}
				}
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
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent != null){
			timersec = intent.getIntExtra("timersec", 0);
			CalTime scheduledTask = new CalTime();
			Timer serviceTimer = new Timer();
			serviceTimer.schedule(scheduledTask, 0, 1000);
			
			if (!path.exists()) {
				path.mkdir();
			}
			
			file=new File(path.getPath() + File.separator + "Timer_" + LogcatMain.LogDateFormat.format(new Date()) + "_" + timersec + ".txt");
			
			try {
				bw = new BufferedWriter(new FileWriter(file), 1024);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			executor.execute(new Runnable() {
				@Override
				public void run() {
					mLogcat = new LogcatProcess(LogSavingService.this, mHandler);
					mLogcat.stop();
					mLogcat.start();
				}
			});
		}

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		if(mLogcat != null){
			mLogcat.stop();
			mLogcat = null;
		}
		if(bw != null){
			try {
				bw.close();
				bw = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		Toast.makeText(this, file.toString() + " 저장이 완료되었습니다.", Toast.LENGTH_SHORT).show();
		super.onDestroy();
	}
	
	private void save(String content) {
		try {
			bw.write(content);
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class CalTime extends TimerTask{
		@Override
		public void run() {
			if(timersec > 0) timersec -=1;
			else{
				this.cancel();
				stopSelf();
			}
		}
	}
}
