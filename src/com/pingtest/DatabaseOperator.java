package com.pingtest;

import java.util.Calendar;

import org.json.JSONException;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class DatabaseOperator {
	private static final String TAG = "Database.Operator";

	private Context context;
	private ContentResolver resolver;
	private ContentValues values;

	public DatabaseOperator(Context context) {
		this.context = context;
		resolver = this.context.getContentResolver();
	}

	public int queryCount() {
		Cursor cur;
		String whereClause = null;
		String[] whereArgs = null;

		cur = resolver.query(
				Uri.parse(DatabaseProvider.CONTENT_URI
						+ PingtestColumns.TABLE_NAME), null, whereClause,
				whereArgs, null);
		cur.moveToLast();

		int result = cur.getPosition() + 1;
		cur.close();

		return result;
	}

	public boolean insertCellInfo(CellInfo cellInfo) {

		values = new ContentValues();

		values.put(PingtestColumns.STATUS, CellInfo.FAILED);
		values.put(PingtestColumns.DATA, cellInfo.toString());

		try {
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(cellInfo.getLong("agpsTimestamp"));

			values.put(PingtestColumns.AGPS_TIME,
					(calendar.getTime().getYear() + 1900) + "-"
							+ (calendar.getTime().getMonth() + 1) + "-"
							+ calendar.getTime().getDate() + " "
							+ calendar.getTime().getHours() + ":"
							+ calendar.getTime().getMinutes() + ":"
							+ calendar.getTime().getSeconds());

			calendar.setTimeInMillis(cellInfo.getLong("gpsTimestamp"));
			values.put(PingtestColumns.GPS_TIME,
					(calendar.getTime().getYear() + 1900) + "-"
							+ (calendar.getTime().getMonth() + 1) + "-"
							+ calendar.getTime().getDate() + " "
							+ calendar.getTime().getHours() + ":"
							+ calendar.getTime().getMinutes() + ":"
							+ calendar.getTime().getSeconds());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Uri uri = resolver.insert(
				Uri.parse(DatabaseProvider.CONTENT_URI
						+ PingtestColumns.TABLE_NAME), values);

		if (!uri.equals(Uri.EMPTY)) {

			Log.v(TAG, "insert pingtest into database succeed");
			cellInfo.id = Integer.parseInt(uri.getLastPathSegment());
			return true;

		} else {

			Log.e(TAG, "insert pingtest into database failed");
			return false;

		}
	}
	
	public int queryUnuploadCount() {
		Cursor cur;
		String whereClause = PingtestColumns.STATUS + " = ?";
		String[] whereArgs = new String[]{ CellInfo.FAILED };

		cur = resolver.query(
				Uri.parse(DatabaseProvider.CONTENT_URI
						+ PingtestColumns.TABLE_NAME), null, whereClause,
				whereArgs, null);
		
		cur.moveToLast();

		int result = cur.getPosition() + 1;
		cur.close();

		return result;
	}
	
	public CellInfo queryUnupload() {
		Cursor cur;
		String whereClause = PingtestColumns.STATUS + " = ?";
		String[] whereArgs = new String[]{ CellInfo.FAILED };

		cur = resolver.query(
				Uri.parse(DatabaseProvider.CONTENT_URI
						+ PingtestColumns.TABLE_NAME), null, whereClause,
				whereArgs, null);
		cur.moveToFirst();

		CellInfo result;
		try {
			result = new CellInfo(cur.getString(cur.getColumnIndex(PingtestColumns.DATA)));
			result.id = cur.getLong(cur.getColumnIndex(PingtestColumns._ID));
		} catch (JSONException e) {
			e.printStackTrace();
			result = new CellInfo();
		}
		cur.close();

		return result;
	}
	
	public int updateUnuloadToOk(long id) {
		
		values = new ContentValues();

		values.put(PingtestColumns.STATUS, CellInfo.OK);

		String whereClause = PingtestColumns._ID + " = ?";
		String[] whereArgs = new String[]{ Long.toString(id) };

		int affected = resolver.update(
				Uri.parse(DatabaseProvider.CONTENT_URI + PingtestColumns.TABLE_NAME), 
				values, 
				whereClause,
				whereArgs
				);

		return affected;
	} 
}
