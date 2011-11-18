package com.bkc.android.mdxplayer;

import java.util.Timer;
import java.util.TimerTask;

import com.bkc.android.mdxplayer.R;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.SeekBar;

public class Player extends Activity implements OnClickListener , NotifyBind
{
	// UI関連
	TextView time_view;
	TextView title_view;
	TextView file_view;
	TextView vol_view;
	SeekBar  seek_view;
	
	private boolean isStartService = false;
	private boolean isStoped = false;

	String TAG = "MDXPlayer";
	String MDXPlayerTitle;

	static String KEY_PCMPATH  = "PCMPath";   
	static String KEY_LASTFILE = "lastFile";
	static String KEY_LASTPATH = "lastPath";
	static String KEY_VOLUME = "volume";

	final Handler ui_handler = new Handler();
	
	private FileListObject fobj = null;
	private PCMService pcmService = new PCMService();
	private StringBuilder infoSB = new StringBuilder();
	
	// 初期化
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        Resources res = getResources();
        
        // タイトル作成
        MDXPlayerTitle = String.format("%s %s",res.getString(R.string.app_name),res.getString(R.string.app_version));
        
        
        // 例外ハンドラの設定        
		 Thread.setDefaultUncaughtExceptionHandler(
				new EXUncaughtExceptionHandler(this));

        setContentView(R.layout.main);
        
		Log.d(TAG,"onCreate");

        // サービスへの接続
        doStartService();
        pcmService.doBindService( this , new Intent(Player.this , PCMRender.class ) , this );
        
        fobj = FileListObject.getInst();
        fobj.setContext( getApplicationContext() );
        
        time_view  = (TextView)findViewById(R.id.time_value);
        title_view = (TextView)findViewById(R.id.title_value);
        file_view  = (TextView)findViewById(R.id.filename_value);
        vol_view   = (TextView)findViewById(R.id.volume_value);
        seek_view  = (SeekBar)findViewById(R.id.seektime);
        
        ((TextView)findViewById(R.id.title_value)).setOnClickListener(this);
        
        
        time_view.setOnClickListener(this);    
  
        ((ImageButton)findViewById(R.id.play_btn)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.rev_btn)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.ff_btn)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.voldown_btn)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.volup_btn)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.stop_btn)).setOnClickListener(this);

        loadPref();
    }
    
    // メニュー作成
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate( R.menu.extmenu, menu );
    	return true;
	}

    // メニュー選択
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch ( item.getItemId() )
		{
			case R.id.menuItem01:
				startHelpActivity();
			return true;
			case R.id.menuItem02:
				startPrefActivity();
			return true;
			case R.id.menuItem03:				
				startKeyViewActivity();
			return true;
		}
		return false;
	}

	// 接続通知
	public void notifyConnected( PCMRender pcm )
    {
    	PCMBound = pcm;
    	
		Log.d(TAG,"notifyConnected");
        if (PCMBound.isPlayed())
        {
        	fobj = PCMBound.getFobj();
        	if ( PCMBound.isPlay() )
        		setPauseButton( true );
        }
        else
        {
        	PCMBound.setFobj( fobj );
        	loadPref();
        	PCMBound.setTitle( MDXPlayerTitle );
        }
    	PCMBound.doUpdate();
        doScheduleUpdateTimer();
    }
    
	// 切断通知
    public void notifyDisconnected()
    {
    	PCMBound = null;
		Log.d(TAG,"notifyDisconnected");
		doCancelTimer();	
    }
   
    // 終了処理
    @Override
    public void onDestroy()
    {
		Log.d(TAG,"onDestroy");	
		
		pcmService.doUnbindService( this );
		
		if ( isStoped )
		{
			Log.d(TAG,"StopService");
			doStopService();
		}
    	super.onDestroy();
    }
	
    
    
    ////////////////////////////////////////
    // サービス関連
	private PCMRender PCMBound = null;	
    
    // サービスの開始
    private void doStartService()
    {
        if ( ! isStartService )
        {
    		Log.d(TAG, "StartService");

        	startService(new Intent(Player.this , PCMRender.class ) );
        	isStartService = true;
        }
    }
    // サービスの終了
    private void doStopService()
    {
    	if ( isStartService )
        {
    		Log.d(TAG, "stopService");

        	stopService(new Intent(Player.this , PCMRender.class ) );
        	isStartService = false;
        }
    }


    //////////////////////////////////////
    // 画面関連
    private Timer DispTimer = null;
       
    private void doScheduleUpdateTimer()
    {
    	TimerTask uiTask = new TimerTask() 
    	{
    		@Override
    		public void run()
    		{
        		ui_handler.post(new Runnable()
        		{
        			@Override
        			public void run() 
        			{
        				updateInfo();
        			}
        		});
    		}
    	};
    	
		Log.d(TAG,"Scheduling timer..");
    	DispTimer = new Timer(true);
    	DispTimer.scheduleAtFixedRate(uiTask,0,200);    	
    }
    
    // タイマーのキャンセル
    private void doCancelTimer()
    {
		Log.d(TAG,"Cancelled timer..");
    	if ( DispTimer == null )
    		return;

    	DispTimer.cancel();
    	DispTimer = null;
    	
    	Log.d(TAG,"Cancelled");
    }
    
    // 画面表示
    private void updateTime()
    {
    	int len = PCMBound.getLen();
    	int pos = PCMBound.getPos();
    	boolean loop = PCMBound.getLoop();
    	        
        if ( loop )
        {
        	infoSB.setLength(0);
        	infoSB.append(String.format("%02d:%02d", pos/60,pos%60));
        	infoSB.append(" / ");
        	infoSB.append("--:--");
        }
        else
        {
        	infoSB.setLength(0);
        	infoSB.append(String.format("%02d:%02d", pos/60,pos%60));
        	infoSB.append(" / ");
        	infoSB.append(String.format("%02d:%02d", len/60,len%60));
        }
        
	   	time_view.setText( infoSB );
	   	
        seek_view.setMax( len );
        seek_view.setProgress( pos );
    }
    
    // 情報更新
	private void updateInfo()
	{
		
		if ( !PCMBound.isUpdate() )
			return;
		
		updateTime();
	   	
        title_view.setText( PCMBound.getTitle() );
        file_view.setText( fobj.path );
        
        StringBuilder sb = new StringBuilder();
        sb.setLength(0);
        sb.append("Volume:").append(PCMBound.getVolume()).append(" ");
        sb.append("PCM:").append(PCMBound.getPCMRate()).append("Hz ");

    	vol_view.setText(sb); 
	}
	
	// 現在の設定を読み出す
	public void loadPref()
	{
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        if (pref == null)
        	return;
        
        if ( fobj != null )
        {
            fobj.openDirectory( pref.getString(KEY_LASTPATH, "") );
            fobj.setCurrentFilePath( pref.getString(KEY_LASTFILE, "") );
        }
        if ( PCMBound != null )
        {
        	PCMBound.setVolume( pref.getInt( KEY_VOLUME , 100) );
        }
	}


	// 現在の設定を保存する
	public void savePref()
	{
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        if (pref == null)
        	return;
        
        SharedPreferences.Editor edit = pref.edit();
        
        if (edit == null)
        	return;
        
        if ( fobj != null )
        {
        	edit.putString( KEY_LASTPATH, fobj.current_path );
        	edit.putString( KEY_LASTFILE, fobj.path );
        }
        if ( PCMBound != null )
        {
        	edit.putInt( KEY_VOLUME , PCMBound.getVolume() );
        }
    	edit.commit();
	}
    
	/////////////////////////////
    // Activity遷移
    @Override
    public void onPause()
    {
    	Log.d(TAG,"onPause");
    	// 現在の設定を保存する
    	savePref();
        pcmService.doUnbindService( this );
		doCancelTimer();	    
		
		super.onPause();
    }
    
    @Override
    public void onResume()
    {
    	Log.d(TAG,"onResume");
 
        // サービスへの接続
        pcmService.doBindService( this , new Intent(Player.this , PCMRender.class ) , this );

    	super.onResume();
    }
    @Override
    public void onStart()
    {
    	Log.d(TAG,"onStart");
    	super.onStart();
    }
    
    @Override
    public void onStop()
    {
    	Log.d(TAG,"onStop");
    	super.onStop();
    }
    
    // 曲選択を開く
    private void openSongSelector()
    {
    	PCMBound.doSetPause(true);
		Intent intent = new Intent(Player.this , FileDiag.class );
		startActivityForResult(intent,0);
    }
    
    // 再生ボタンの変更
    private void setPauseButton( boolean pause_flag )
    {
		if ( pause_flag )
			((ImageButton)findViewById(R.id.play_btn)).setImageResource( R.drawable.pause );
		else
			((ImageButton)findViewById(R.id.play_btn)).setImageResource( R.drawable.play );	
    }
    
    // ヘルプ画面の表示
    public void startHelpActivity()
    {
		Intent intent = new Intent(Player.this , HelpActivity.class );
    	startActivityForResult(intent,0);
    }

    // 設定画面の表示
    public void startPrefActivity()
    {
		Intent intent = new Intent(Player.this , Setting.class );
    	startActivityForResult(intent,0);
    }
    
    // キー画面の表示
    public void startKeyViewActivity()
    {
		Intent intent = new Intent(Player.this , KeyView.class );
    	startActivityForResult(intent,0);
    }

    // ボタンクリック検出
	@Override
	public void onClick(View v)
	{
		switch(v.getId())
		{
		case R.id.play_btn:
			isStoped = false;
			if (PCMBound.isLoaded())
			{
				PCMBound.doPause();				
				setPauseButton( ! PCMBound.getPause() );
			}
			else
			{
				PCMBound.doPlaySong();
				setPauseButton( true );			
			}
			PCMBound.doUpdate();
		break;
		case R.id.stop_btn:
			isStoped = true;
			setPauseButton( false );
			PCMBound.doStop();
		break;			
		case R.id.voldown_btn:
			PCMBound.doVolumeDown();
			PCMBound.doUpdate();
		break;
		case R.id.volup_btn:
			PCMBound.doVolumeUp();
			PCMBound.doUpdate();
		break;			
		case R.id.rev_btn:
			PCMBound.doPlayPrevSong();
			setPauseButton( true );
			updateInfo();
			break;
		case R.id.ff_btn:
			PCMBound.doPlayNextSong();
			setPauseButton( true );
			PCMBound.doUpdate();
			break;
		case R.id.time_value:
			PCMBound.doSetLoop();
			updateInfo();
			break;
		case R.id.title_value:
			openSongSelector();
			break;
		}
	}

	// Activityからの帰還と処理
	public void onActivityResult(int reqCode,int result,Intent intent)
	{
		// ファイルダイアログ処理
		if (reqCode == 0)
		{
			if ( result == RESULT_OK )	
			{					
				savePref();
				
				PCMBound.setFobj( fobj );
				PCMBound.doPlaySong();
				setPauseButton( true );
			}
			else
			{
				PCMBound.doSetPause(false);
			}
		}
	}
}