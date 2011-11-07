package com.bkc.android.mdxplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class PCMService
{
	private String TAG = "PCMService";
	private boolean isBindService = false;
	private NotifyBind notify;
	
    // バインド
    public void doBindService( Context ctx , Intent intent , NotifyBind destNotify )
    {
    	if ( ! isBindService )
    	{
    		Log.d(TAG, "BindService");
    		ctx.bindService( intent , PCMConnect , Context.BIND_AUTO_CREATE  );
    		isBindService = true;
    		notify = destNotify;
    	}
    }
    

    // アンバインド
    public void doUnbindService( Context ctx )
    {
    	if ( isBindService )
    	{
    		Log.d(TAG, "UnbindService");
        	ctx.unbindService(PCMConnect);
        	isBindService = false;  	
    	}
    }
    
    // バインド用クラス
    private ServiceConnection PCMConnect = new ServiceConnection()
    {
    	// 接続
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			notify.notifyConnected( ((PCMRender.LocalBinder)service).getService() );
		}

		// 切断
		@Override
		public void onServiceDisconnected(ComponentName className) {
    		Log.d(TAG, "onServiceDisconnected");
    		notify.notifyDisconnected();
			isBindService = false;
		}
    };
}
