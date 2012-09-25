package com.pingtest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "databse.db";
	
	private static final String[] TABLE_CREATE = {
		"create table if not exists "
		+ PingtestColumns.TABLE_NAME + "("
		+ PingtestColumns._ID + " integer primary key autoincrement, "
		+ PingtestColumns.STATUS + " text, "
		+ PingtestColumns.AGPS_TIME + " text, "
		+ PingtestColumns.GPS_TIME + " text, "
		+ PingtestColumns.F_TIME + " text, "
		+ PingtestColumns.DATA + " text "
		+ ");"
	};
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		for (int i = 0; i < TABLE_CREATE.length; i++) {
			db.execSQL(TABLE_CREATE[i]);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
	}

}
