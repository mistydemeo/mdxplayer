package com.bkc.android.mdxplayer;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class FileListObject 
{
    // songdb  ( dir , name , title , len )
	
	// ファイル情報
	private ArrayList<File> afiles;
	private ArrayList<String> afiles_name;
	private HashMap<String,String> files_title;
	private HashMap<String,Integer> files_len;
	
	// 現在の位置
	public int position = 0;
	public String current_dir = null;
	public String current_filepath = null;
	
	SongDBHelper songHelper;
	// private static final String TAG = "FileListObject";
	
	private static FileListObject inst = new FileListObject();

	// コンストラクタ
	private FileListObject()
	{
		
		afiles = new ArrayList<File>();
    	afiles_name = new ArrayList<String>();
    	files_title = new HashMap<String,String>();
    	files_len = new HashMap<String,Integer>();
    	
    	current_dir = "";
    	current_filepath = "";
	}
	
	public static FileListObject getInst()
	{
		return inst;
	}
	
	public static FileListObject getTemporalInst()
	{
		return new FileListObject();
	}

	
	public void setContext ( Context context )
	{
		songHelper = new SongDBHelper( context );
	}
	

	public ArrayList<File> getFileList()
	{
		return afiles;
	}
	
	public ArrayList<String> getNameList()
	{
		return afiles_name;
	}
	
	   
    // ファイルフィルター
    public FileFilter getMDXfilter()
    {
    	return new FileFilter()
    	{
    		public boolean accept(File file)
    		{
    			String fname = (file.getName()).toLowerCase(Locale.ENGLISH);
    			
    			if (fname.startsWith("."))
    				return false;

    			if (file.isDirectory())
    				return true;
    			else   			
    			if (fname.endsWith(".mdx"))
    				return true;
       			else   			
       			if (fname.endsWith(".m"))
        				return true;
       			else   			
           		if (fname.endsWith(".m2"))
            			return true;
           		else   			
               	if (fname.endsWith(".mz"))
                		return true;
           		else   			
                if (fname.endsWith(".mp"))
                    		return true;
           		else   			
               	if (fname.endsWith(".ms"))
                    		return true;

    			return false;
    		}
    	};
    }
    
    // ファイルリスト
    public int readDirectory(String path)
    {
    	File file = new File(path);
    	if ( path.contentEquals("") )
    		return -1;    		
    	
    	if ( ! file.exists() || ! file.isDirectory() )
    		return -1;
  

    	File[] lf = file.listFiles(getMDXfilter());
    	File pf = file.getParentFile();
    	
    	// ファイル取得失敗
    	if (lf == null)
    		return -1;
    	
    	List<File> files = null;
    	
    	// ファイルのソート
    	if (lf != null)
    	{
    		Arrays.sort(lf,new FileComparator());
    	}

    	    	
    	afiles.clear();
    	afiles_name.clear();
    	files_len.clear();
    	files_title.clear();
    	
    	// rootでなければディレクトリを追加
    	if (!pf.getAbsolutePath().equals("/"))
    	{
    		afiles.add(file.getParentFile());
    		afiles_name.add("..");
       		
       		if (lf != null)
       		   		afiles.addAll(Arrays.asList(lf));
       		files = afiles.subList(1, afiles.size());
    	}
    	else
    	{
        	if (lf != null)
        		afiles.addAll(Arrays.asList(lf));
        	files = afiles;
    	}
    	   	
        for (File f : files)
        {
        		afiles_name.add(f.getName());    	
        }
        return 0;
     }
    
    // アップデート
    private void updateDatabase( String path )
    {
    	if (path.contentEquals(""))
    		return;
    	
    	if (songHelper == null)
    		return;
    	
    	SQLiteDatabase db = songHelper.getWritableDatabase();
    	
    	songHelper.startTransaction(db);
    	songHelper.deleteFilesInDir(db, path);
    	
    	for (int i = 0; i < afiles.size(); i++)
    	{
    		File file = afiles.get(i);

    		if (file.isDirectory())
    			continue;
    		
    		String dir = file.getParent();
    		String name = file.getName();
    		String title = files_title.get ( name );
    		Integer len = files_len.get( name );
    		
    		if (title == null)
    			title = new String("");
    		
    		if (len == null)
    			len = Integer.valueOf(0);
    		    		
    		songHelper.insert(db,
    				dir,
    				name,
    				title,
    				len);
    		
    	}
    	
    	songHelper.endTransaction(db);
    	db.close();
    }
    // 読み出し
    
    private void readDatabase( String path )
    {
    	String where = "dir = ?";
    	String[] select = new String[] { path };
    	
    	if (songHelper == null)
    		return;
    		
     	SQLiteDatabase db = songHelper.getReadableDatabase();
     	Cursor c = db.query( 
     			songHelper.getTableName(),
     			songHelper.getSelection(), 
     			where, select, null, null, null);
     	
        c.moveToFirst();

        for (int i = 0; i < c.getCount(); i++) 
        {
            String name = c.getString(1);
            String title = c.getString(2);
            int len = c.getInt(3);
            
            if (title.length() > 0)
            	files_title.put(name, title);
            
            if (len > 0)
            	files_len.put(name, len);
            
            c.moveToNext();
        }
        c.close();
        db.close();
           	
    }
   
    // ディレクトリを開く
    public int openDirectory( String path )
    {
    	// データベースの更新
    	updateDatabase( current_dir );
    	
    	if ( path.contentEquals( current_dir ) )
    		return 0;
    	
       	if ( readDirectory( path ) < 0 )
       	{
       		current_dir = "";
       		return -1;
       	}
       	
       	// データベースの取得
       	readDatabase( path );
       	
        current_dir = path;
        
        return 0;
    }
    
    // ファイルパスから配列の位置を得る
	public int getSongPosFromPath( String filepath )
	{
		if (filepath.equals(""))
			return 0;
		
		int ret = afiles_name.indexOf( new File(filepath).getName() );
		
		if (ret < 0)
			return 0;
		return ret;
	}
	
	// 現在のファイルをセットする
	public void setCurrentFilePath(String filepath)
	{
		position = getSongPosFromPath ( filepath );
		current_filepath = filepath;
	}
	
	// 現在の曲のタイトルをセットする
	public void setCurrentSongTitle ( String title )
	{
		String filename = afiles_name.get(position);
		
		files_title.put( filename , title );
	}

	// 現在の曲の長さをセットする
	public void setCurrentSongLen ( int len )
	{
		String filename = afiles_name.get(position);
		
		files_len.put( filename , len );
	}
	
	// タイトルを得る
	public String getTitleFromFile ( String filename )
	{
		return files_title.get( filename );
	}
	

	// 長さを得る
	public Integer getLengthFromFile ( String filename )
	{
		return files_len.get( filename );
	}


	//  次の曲を探す
    public void getNextSong(int step)
    {        
    	// Log.d(TAG, "next pos=" + position + " hash:" + this.hashCode());
    	int pos = position;
    	
    	if (pos < 0 || pos >= afiles.size())
    		pos = getSongPosFromPath(current_filepath);

    	for (int i = 0; i < afiles.size(); i++)
    	{
    		if (step < 0)
    		{
    			pos--;
    			if (pos < 0)
    				pos = afiles.size()-1;        			
    		}
    		else
    		{
    			pos++;
    			if (pos >= afiles.size()) 
    				pos = 0;
    		}
    		if (!afiles.get(pos).isDirectory())
    		{
    			current_filepath = afiles.get(pos).getAbsolutePath();
    			break;
    		}
    	}
    	position = pos;
    }
}
