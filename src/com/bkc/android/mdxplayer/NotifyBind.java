package com.bkc.android.mdxplayer;

//通知用インターフェース

public interface NotifyBind {

    public void notifyConnected( PCMRender pcm );
    public void notifyDisconnected( );
}
