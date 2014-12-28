package com.sec.apps.exynos_logcat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.Manifest;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

public class LogcatMain extends ListActivity {

	private ListView mListView = null;

	private LogListAdapter mLogListAdapter;
	private LogLevel mlastlevel = LogLevel.V;
	private LogcatProcess mLogcat;
	private Preferences mPrefs;

	static final int FILTER_DIALOG = 1;

	private static final int MENU_FILTER = 2;
	private static final int MENU_PLAY = 3;
	private static final int MENU_CLEAR = 4;
	private static final int MENU_SAVE = 5;
	private static final int MENU_PREFS = 6;
	private static final int MENU_SERVICE = 7;

	private boolean mPlay = true;

	MenuItem mPlayItem, mFilterItem;

	private static final Executor executor = Executors.newCachedThreadPool();
	static final SimpleDateFormat LogDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

	static final int CAT_EVT = 0;
	static final int CLEAR_EVT = 2;
	static final int MAX_COUNT = 1000;

	private Handler mHandler = new Handler() {
		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CAT_EVT:
				final List<String> lines = (List<String>) msg.obj;
				cat(lines);
				break;
			case CLEAR_EVT:
				mLogListAdapter.clear();
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.logcatmain);

		boolean hasPermission = getPackageManager().checkPermission(Manifest.permission.READ_LOGS, getPackageName()) == PackageManager.PERMISSION_GRANTED;
		//권한이 있는지 검사한다.
		if(!hasPermission){
			try {
				Process process = Runtime.getRuntime().exec("sh",null,null);
				OutputStream os = process.getOutputStream();
				os.write(String.format("pm grant %s android.permission.READ_LOGS", getPackageName()).getBytes());
				os.flush();
				os.close();
				if(process.waitFor() != 0){
					Toast.makeText(this, "Android SDK16 (JellyBean) 이상은\n보안 정책 변경으로 루팅이 필요합니다.", Toast.LENGTH_SHORT).show();
					//Log.e("error", "grant 실패 " + process.exitValue());
					//android.os.Process.killProcess(android.os.Process.myPid());
				}
			} catch (Exception e) {
				Toast.makeText(this, "Android SDK16 (JellyBean) 이상은\n보안 정책 변경으로 루팅이 필요합니다.", Toast.LENGTH_SHORT).show();
				//Log.e("error", "권한 획득 실패");
				//android.os.Process.killProcess(android.os.Process.myPid());
			}
		}

		mPrefs = new Preferences(this);

		mListView = (ListView)findViewById(android.R.id.list);

		mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				pauseLog();
			}
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		onNewIntent(getIntent());
		init();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if(mLogcat != null)
			mLogcat.stop();
	}

	private void jumpBottom() {
		playLog();
		mListView.setSelection(mLogListAdapter.getCount() - 1);
	}

	private void cat(final String s) {
		if (mLogListAdapter.getCount() > MAX_COUNT) {
			mLogListAdapter.remove(0);
		}

		PatternFormat format = mLogcat.mFormat;
		LogLevel level = format.getLevel(s);
		if (level == null) {
			level = mlastlevel;
		} else {
			mlastlevel = level;
		}

		final LogElement elem = new LogElement(s, level);
		mLogListAdapter.add(elem);
	}

	private void cat(List<String> lines) {
		for (String line : lines) {
			cat(line);
		}
		jumpBottom();
	}

	private void init() {
		int color = Color.WHITE;
		mListView.setBackgroundColor(color);
		mListView.setCacheColorHint(color);

		mLogListAdapter = new LogListAdapter(this, R.layout.listelement,new ArrayList<LogElement>(MAX_COUNT));
		setListAdapter(mLogListAdapter);
		reset();
	}

	public void reset() {
		Toast.makeText(this, "로그를 읽는 중입니다.\n잠시만 기다려 주세요", Toast.LENGTH_SHORT).show();
		mlastlevel = LogLevel.V;

		if (mLogcat != null) {
			mLogcat.stop();
		}

		mPlay = true;

		executor.execute(new Runnable() {
			@Override
			public void run() {
				mLogcat = new LogcatProcess(LogcatMain.this, mHandler);
				mLogcat.start();
			}
		});
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		mPlayItem = menu.add(0, MENU_PLAY, 0, "일시정지");
		mPlayItem.setIcon(android.R.drawable.ic_media_pause);
		MenuItemCompat.setShowAsAction(mPlayItem,MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
		setPlayMenu();

		mFilterItem = menu.add(0,MENU_FILTER,0,String.format("필터 (%s)",mPrefs.getFilter()));
		mFilterItem.setIcon(android.R.drawable.ic_menu_search);
		MenuItemCompat.setShowAsAction(mFilterItem,MenuItemCompat.SHOW_AS_ACTION_IF_ROOM | MenuItemCompat.SHOW_AS_ACTION_WITH_TEXT);
		setFilterMenu();

		MenuItem clearItem = menu.add(0, MENU_CLEAR, 0, "초기화");
		clearItem.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		MenuItemCompat.setShowAsAction(clearItem,MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);

		MenuItem saveItem = menu.add(0, MENU_SAVE, 0, "저장");
		saveItem.setIcon(android.R.drawable.ic_menu_save);
		MenuItemCompat.setShowAsAction(saveItem,MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);

		MenuItem prefsItem = menu.add(0, MENU_PREFS, 0, "설정");
		prefsItem.setIcon(android.R.drawable.ic_menu_preferences);
		MenuItemCompat.setShowAsAction(prefsItem,MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
		
		MenuItem serviceItem = menu.add(0, MENU_SERVICE, 0, "오버레이 화면");
		serviceItem.setIcon(android.R.drawable.ic_menu_view);
		MenuItemCompat.setShowAsAction(serviceItem,MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return true;
	}

	void setFilterMenu() {
		if (mFilterItem == null) {
			return;
		}
		int filterMenuId;
		String filter = mPrefs.getFilter();
		if (filter == null || filter.length() == 0) {
			filterMenuId = R.string.filter_menu_empty;
		} else {
			filterMenuId = R.string.filter_menu;
		}
		mFilterItem.setTitle(getResources().getString(filterMenuId, filter));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_FILTER:
			showDialog(FILTER_DIALOG);
			return true;
		case MENU_SAVE:
			File f = save();
			String msg = getResources().getString(R.string.saving_log,f.toString());
			Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
			return true;
		case MENU_PLAY:
			if (mPlay) {
				pauseLog();
			} else {
				jumpBottom();
			}
			return true;
		case MENU_CLEAR:
			clear();
			reset();
			return true;
		case MENU_PREFS:
			Intent intent = new Intent(this, PreferencesActivity.class);
			startActivity(intent);
			return true;
		case MENU_SERVICE:
			Intent sintent = new Intent(this,LogOverlayService.class);
			if(!stopService(sintent))
				startService(sintent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
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
		final File file = new File(path.getPath() + File.separator + "Exynos_Logcat_" + LogDateFormat.format(new Date()) + ".txt");
		Log.d("",path.getPath() + File.separator + "Exynos_Logcat_" + LogDateFormat.format(new Date()) + ".txt");

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

		List<LogElement> elements = new ArrayList<LogElement>(mLogListAdapter.getElements());

		for (LogElement le : elements) {
			sb.append(le.getText());
			sb.append('\n');
		}

		return sb.toString();
	}

	public void setPlayMenu() {
		if (mPlayItem == null) {
			return;
		}
		if (mPlay) {
			mPlayItem.setTitle("일시정지");
			mPlayItem.setIcon(android.R.drawable.ic_media_pause);
		} else {
			mPlayItem.setTitle("시작");
			mPlayItem.setIcon(android.R.drawable.ic_media_play);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case FILTER_DIALOG:
			return new FilterDialog(this);
		}
		return null;
	}
}