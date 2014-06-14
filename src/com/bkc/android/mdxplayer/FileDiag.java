package com.bkc.android.mdxplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import com.bkc.android.mdxplayer.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class FileDiag extends Activity implements OnClickListener, OnItemClickListener
{
    
	// 表示用アダプタ
	SimpleAdapter adpt;
	ArrayList<HashMap<String, Object>> items;
	
	FileListObject fobj = null;
    final static String DIRMODE = "dirMode";
    
    private boolean dirMode = false;
    private String lastPath = "";
    
    
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        
        // ファイルダイアログの表示
        setContentView(R.layout.filediag);
        
        // Intentの取得
		Intent intent = getIntent();
		dirMode = intent.getBooleanExtra( DIRMODE, false );
        
		if (!dirMode)
		{
			// ファイルリストオブジェクトの取得
	        fobj = FileListObject.getInst();
		}
		else
		{
			// 仮ファイルリスト
			fobj = FileListObject.getTemporalInst();
		}
        
        loadPref(dirMode);
        
        // ディレクトリの読み出し
        if (getDirectory(lastPath) < 0 &&
        	getDirectory(Environment.getExternalStorageDirectory().getAbsolutePath()) < 0)
        {
        	// 読めなかったので表示
        	Toast.makeText(FileDiag.this,
                           getString(R.string.error_sdcard), Toast.LENGTH_LONG).show();
        	fobj.current_dir = "";
        }
        
        Button fileOk = (Button)this.findViewById(R.id.file_ok);
        if ( dirMode == false )
            fileOk.setVisibility(Button.GONE);
        
       	fileOk.setOnClickListener( this );
    }
	
	
	@Override
	protected void onDestroy()
	{
		savePref(dirMode);
		super.onDestroy();
	}
	
	private void savePref(boolean dirmode)
	{
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		
		if (pref == null)
			return;
		
		SharedPreferences.Editor edit = pref.edit();
		
		if (edit == null)
			return;
		
		if (dirmode)
			edit.putString(Setting.PCMPATH, fobj.current_dir);
		else	
			edit.putString(Setting.LASTPATH, fobj.current_dir);
		
		edit.commit();
	}
	
	
	private void loadPref(boolean dirmode)
	{
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        
		lastPath = "";
		
		if (pref == null)
			return;
		
		if (dirmode)
			lastPath = pref.getString(Setting.PCMPATH,  "");
		else
			lastPath = pref.getString(Setting.LASTPATH , "");
		
	}
	
	private void setCurrentPathInfo()
	{
        TextView pathInfo = (TextView)this.findViewById(R.id.fileDiagText2);
        pathInfo.setText( String.format("フォルダ:%s",fobj.current_dir ));
	}
	
    
	// ディレクトリを開く
	private int getDirectory( String path )
	{
        if ( path.equals("") || fobj.openDirectory( path ) < 0 )
        {
        	fobj.current_dir = "";
            makeFileList();
        	return -1;
        }
        
        makeFileList();
        setCurrentPathInfo();
        return 0;
	}
	
	// 表示用ファイルリストの作成
	private void makeFileList()
	{
        // リストビューの初期化
        ListView lv = (ListView) this.findViewById(R.id.filelist);
        lv.setOnItemClickListener(this);
        
        // 配列の初期化
		items = new ArrayList<HashMap<String, Object>>();
		
		ArrayList<File> afiles = fobj.getFileList();
		ArrayList<String> afiles_name = fobj.getNameList();
    	
    	for (int i = 0; i < afiles.size(); i++)
    	{
    		File file = afiles.get(i);
    		String name = afiles_name.get(i);
    		
            HashMap<String, Object> map = new HashMap<String, Object>();
    		String title = fobj.getTitleFromFile( name );
    		Integer len = fobj.getLengthFromFile( name );
            
    		// int count = fobj.getIntFromFile(name, FileListObject.COUNT );
    		// long last = fobj.getLongFromFile(name, FileListObject.LASTPLAY );
            
    		String data = "";
    		
    		if (title == null || title.length() == 0)
    			map.put("title", name);
    		else
    		{
    			map.put("title", title);
                
    			data = String.format("%s", name);
    			data += " ";
    			if (len != null)
        			data += String.format("%02d:%02d ", len / 60, len % 60);
    			
    			// data += String.format("Play:%d ",count);
    			
    			// SimpleDateFormat sdf =
                // new SimpleDateFormat(getString(R.string.dateformat_string),Locale.getDefault());
    			
    			// data += String.format("%s ",sdf.format(new Date(last)));
    		}
    		
    		
    		map.put("data", data);
    		
    		int icon = R.drawable.icon;
    		
    		if (file.isDirectory())
    			icon = R.drawable.folder;
    		
    		map.put("image",icon);
    		
    		items.add(map);
        }
    	
    	adpt = new SimpleAdapter(this,
                                 items,
                                 R.layout.filelist,
                                 new String[] {"image", "title", "data"},
                                 new int[] {R.id.imageView1, android.R.id.text1, android.R.id.text2});
    	
        lv.setAdapter(adpt);
        
	}
	
	private void finishForDirMode()
	{
		savePref(dirMode);
		Intent intent = getIntent();
		setResult(RESULT_OK,intent);
		finish();
	}
    
	public void onClick(View v)
	{
		switch(v.getId())
		{
			case R.id.file_ok:
				finishForDirMode();
                break;
		}
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
	{
		File file = fobj.getFileList().get(pos);
		
		if (file.isDirectory())
		{
			// ディレクトリを開く
			getDirectory(file.getAbsolutePath());
		}
		else
		{
			if ( dirMode == true )
			{
				finishForDirMode();
				return;
			}
			// ファイルを開く
			fobj.position = pos;
			fobj.current_filepath = file.getAbsolutePath();
			
			Intent intent = getIntent();
			setResult(RESULT_OK,intent);
			finish();
		}
	}
}
