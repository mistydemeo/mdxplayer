package com.bkc.android.mdxplayer;

import java.io.Serializable;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SongDBHelper extends SQLiteOpenHelper implements Serializable 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static String DB_TABLE = "songdb";
	private final static String DB_NAME = "songdb";
	
	private final String DB_CREATE = "CREATE TABLE "
		+ DB_TABLE + " ( "
		+ "dir TEXT ,"
		+ "name TEXT ,"
		+ "title TEXT ,"
		+ "len INTEGER ,"
		+ "primary key ( dir , name ) "
		+ ")";
	
	private final String DB_DROP = "DROP TABLE IF EXISTS " + DB_TABLE;
	
	public SongDBHelper(Context context) {
		super(context, DB_NAME, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL( DB_CREATE );
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
		db.execSQL( DB_DROP );
		onCreate(db);
	}
	
	public void startTransaction(SQLiteDatabase db)
	{
		db.beginTransaction();
	}
	
	public void endTransaction(SQLiteDatabase db)
	{
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	
	public void failedTransaction(SQLiteDatabase db)
	{
		db.endTransaction();
	}
	
	public void deleteFilesInDir(SQLiteDatabase db,String dir)
	{
		db.delete(DB_TABLE,"dir = ?",new String[] {dir});
	}
	

	public void insert(SQLiteDatabase db, String dir, String name, String title, int len)
	{
		ContentValues cv = new ContentValues();
		cv.put("dir", dir);
		cv.put("name", name);
		cv.put("title", title);
		cv.put("len", len);
		
		db.insert(DB_TABLE, "", cv);
		
	}
	
	public void replace(SQLiteDatabase db, String dir, String name, String title, int len)
	{
		ContentValues cv = new ContentValues();
		cv.put("dir", dir);
		cv.put("name", name);
		cv.put("title", title);
		cv.put("len", len);
		
		db.replace(DB_TABLE, "", cv);
		
	}
	public String getTableName()
	{
		return DB_TABLE;
	}
	
	public String[] getSelection()
	{
		return new String[] { 
				"dir",
				"name",
				"title",
				"len"
		};
	}

}
