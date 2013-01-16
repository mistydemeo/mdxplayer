package com.bkc.android.mdxplayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.os.Process;

public class EXUncaughtExceptionHandler implements UncaughtExceptionHandler
{

	private static File LOGFILE = null;
	private static PackageInfo pkgInfo;
	static {
		String sdcard = Environment.getExternalStorageDirectory().getPath();
		String path = sdcard + File.separator + "mdxplayer_errlog.txt";
		LOGFILE = new File(path);
	}
	
	
	public EXUncaughtExceptionHandler(Context ctx)
	{
		try {
			pkgInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
		} 
		catch ( NameNotFoundException e )
		{
			e.printStackTrace();
		}
	}

	@Override
	public void uncaughtException(Thread th, Throwable et) 
	{
		try 
		{
			saveLog ( et );
			Process.killProcess( Process.myPid() ); 
			
		} catch ( FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	
	private void saveLog(Throwable e) throws FileNotFoundException 
	{
		File file = LOGFILE; // save
		PrintWriter pw = null;
		pw = new PrintWriter( new FileOutputStream( file , true ) );
		
		Date date = new Date();
		
		StringBuilder sb = new StringBuilder();		
		sb.setLength(0);
		sb.append("\n");
		sb.append("Package:").append( pkgInfo.packageName ).append("\n");
		sb.append("Version:").append( pkgInfo.versionName ).append("\n");
		sb.append("Date:");
		sb.append( date.toString() );
		
		pw.println(sb);
		e.printStackTrace(pw);
		
		pw.close();
		
	}
	
	public File getLogFile()
	{
		return LOGFILE;
	}
	
	public long getLastModLog()
	{		
		return LOGFILE.lastModified();
	}		
	
	public void removeLog()
	{
		LOGFILE.delete();
	}

}
