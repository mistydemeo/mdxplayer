package com.bkc.android.mdxplayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
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

    	// エラーファイルの存在確認
    	if (! ErrFile.exists())
    	{
            ((TextView)this.findViewById(R.id.error_text)).setVisibility(TextView.GONE);    		
            ((TextView)this.findViewById(R.id.send_log)).setVisibility(TextView.GONE);    		
            ((TextView)this.findViewById(R.id.delete_log)).setVisibility(TextView.GONE);    		
            loadHelpFile();
    	}
    	else
    	{
            ((TextView)this.findViewById(R.id.error_text)).setOnClickListener( this );
            ((TextView)this.findViewById(R.id.send_log)).setOnClickListener( this );  		
            ((TextView)this.findViewById(R.id.delete_log)).setOnClickListener( this );   		
            loadLogFile();
    	}
	
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
	
	private void loadLogFile()
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
				loadLogFile();
			break;
			case R.id.send_log:
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				
				intent.putExtra(Intent.EXTRA_EMAIL, new String[] { getString(R.string.mailaddr) });
				intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.errorlog_string));
				intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(ErrFile));
				startActivity(intent);
			break;
			case R.id.delete_log:
		   		AlertDialog.Builder builder = new AlertDialog.Builder(this);
	     		
	    		builder.setMessage(R.string.log_delete_message)
	    		.setCancelable(false)
	    		.setPositiveButton("OK", new DialogInterface.OnClickListener()
	    		{
	    				public void onClick(DialogInterface dialog,int id)
	    				{
	    					ErrFile.delete();
	    					finish();
	    				}
	    		})
	    		.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
	    		{
	    				public void onClick(DialogInterface dialog,int id)
	    				{
	    					dialog.cancel();
	    				}
	    		});
	    		
	    		AlertDialog alert = builder.create();
	    		alert.show();
	    		
			break;
			case R.id.help_ok:
			finish();
			break;
		}
	}
}
