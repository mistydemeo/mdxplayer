package com.bkc.android.mdxplayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Calendar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
			checkLog ( ctx );
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
		StringBuilder sb = new StringBuilder();
		
		Calendar cal = Calendar.getInstance();
		
		
		sb.setLength(0);
		sb.append("Version:").append( pkgInfo.versionName );
		sb.append(" ");
		sb.append("Date:");
		sb.append( cal.get(Calendar.YEAR) ).append("-")
		.append( cal.get(Calendar.MONTH) ).append("-")
		.append( cal.get(Calendar.DAY_OF_MONTH) ).append(" ")
		.append( cal.get(Calendar.HOUR_OF_DAY) ).append(":")
		.append( cal.get(Calendar.MINUTE) ).append(":")
		.append( cal.get(Calendar.SECOND) );
		
		pw.println(sb);
		e.printStackTrace(pw);
		
		pw.close();
		
	}
	
	public static final void checkLog(Context ctx)
	{
		File file = LOGFILE;
		if (file != null & file.exists())
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
			builder.setMessage(ctx.getString(R.string.log_found))
			.setCancelable(false)
			.setPositiveButton("Yes", new DialogInterface.OnClickListener()
			{
					public void onClick(DialogInterface dialog,int id)
					{
						LOGFILE.delete();
					}
			})
			.setNegativeButton("No", new DialogInterface.OnClickListener()
			{
					public void onClick(DialogInterface dialog,int id)
					{
						dialog.cancel();
					}
			});
			
			AlertDialog alert = builder.create();
			alert.show();
		}
	}
}
