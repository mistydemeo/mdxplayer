package com.bkc.android.mdxplayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

// メモ:
// AndroidManifest.xmlへのActivity追加を忘れないように。

public class HelpActivity extends Activity implements OnClickListener
{
	File ErrFile = null;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);        
        
        // ヘルプ画面の表示
        setContentView(R.layout.help);
        ((TextView)this.findViewById(R.id.help_text)).setOnClickListener( this );

        ((Button)this.findViewById(R.id.help_ok)).setOnClickListener( this );
        

        String sdcard = Environment.getExternalStorageDirectory().getPath();
    	String error_name = sdcard + File.separator + "mdxplayer_errlog.txt";
    	ErrFile = new File( error_name );

    	// エラーファイルが存在しない場合は非表示
    	if (! ErrFile.exists())
            ((TextView)this.findViewById(R.id.error_text)).setVisibility(TextView.GONE);    		
    	else
            ((TextView)this.findViewById(R.id.error_text)).setOnClickListener( this );

        loadHelpFile();
	
	}
	

	
	private void loadHelpFile()
	{
	       try {
	        	
	           Resources res = getResources();
	           InputStream text = res.openRawResource( R.raw.help );
	           
	           byte[] data = new byte[text.available()];
	           text.read(data);
	           ((TextView)this.findViewById(R.id.help_body)).setText(new String(data));
	           text.close();

	           } catch (IOException e) 
	           {
	        	   e.printStackTrace();
	           }
	}
	
	private void loadErrorFile()
	{
	       try {	        	
	           FileInputStream text = new FileInputStream(ErrFile);
	           
	           byte[] data = new byte[text.available()];
	           text.read(data);
	           ((TextView)this.findViewById(R.id.help_body)).setText(new String(data));
	           text.close();

	           } catch (IOException e) 
	           {
	        	   e.printStackTrace();
	           }
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {

		switch(v.getId())
		{
			case R.id.help_text:
				loadHelpFile();
			break;
			case R.id.error_text:
				loadErrorFile();
			break;
			case R.id.help_ok:
			finish();
			break;
		}
	}
}
