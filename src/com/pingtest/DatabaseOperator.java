package com.pingtest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

			values.put(PingtestColumns.AGPS_TIME,
					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(cellInfo.getLong("agpsTimestamp"))));
			values.put(PingtestColumns.GPS_TIME,
					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(cellInfo.getLong("gpsTimestamp"))));
			values.put(PingtestColumns.F_TIME,
					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(cellInfo.getLong("timestamp"))));
			
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
		String[] whereArgs = new String[] { CellInfo.FAILED };

		cur = resolver.query(
				Uri.parse(DatabaseProvider.CONTENT_URI
						+ PingtestColumns.TABLE_NAME), null, whereClause,
				whereArgs, null);

		cur.moveToLast();

		int result = cur.getPosition() + 1;
		cur.close();

		return result;
	}

	public List<CellInfo> queryUnupload() {
		Cursor cur;
		String whereClause = PingtestColumns.STATUS + " = ?";
		String[] whereArgs = new String[] { CellInfo.FAILED };

		cur = resolver.query(
				Uri.parse(DatabaseProvider.CONTENT_URI
						+ PingtestColumns.TABLE_NAME), null, whereClause,
				whereArgs, null);

		List<CellInfo> result = new ArrayList<CellInfo>();

		while (cur.moveToNext()) {
			try {
				CellInfo value = new CellInfo(cur.getString(cur
						.getColumnIndex(PingtestColumns.DATA)));
				value.id = cur.getLong(cur.getColumnIndex(PingtestColumns._ID));
				value.status = cur.getString(cur.getColumnIndex(PingtestColumns.STATUS));
				value.agpsTimestamp = cur.getString(cur.getColumnIndex(PingtestColumns.AGPS_TIME));
				value.gpsTimestamp = cur.getString(cur.getColumnIndex(PingtestColumns.GPS_TIME));
				value.fTimestamp = cur.getString(cur.getColumnIndex(PingtestColumns.F_TIME));
				result.add(value);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		cur.close();

		return result;
	}
	
	public List<CellInfo> queryAll() {
		Cursor cur;
		String whereClause = null;
		String[] whereArgs = null;

		cur = resolver.query(
				Uri.parse(DatabaseProvider.CONTENT_URI
						+ PingtestColumns.TABLE_NAME), null, whereClause,
				whereArgs, null);

		List<CellInfo> result = new ArrayList<CellInfo>();

		while (cur.moveToNext()) {
			try {
				CellInfo value = new CellInfo(cur.getString(cur
						.getColumnIndex(PingtestColumns.DATA)));
				value.id = cur.getLong(cur.getColumnIndex(PingtestColumns._ID));
				value.status = cur.getString(cur.getColumnIndex(PingtestColumns.STATUS));
				value.agpsTimestamp = cur.getString(cur.getColumnIndex(PingtestColumns.AGPS_TIME));
				value.gpsTimestamp = cur.getString(cur.getColumnIndex(PingtestColumns.GPS_TIME));
				value.fTimestamp = cur.getString(cur.getColumnIndex(PingtestColumns.F_TIME));
				result.add(value);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		cur.close();

		return result;
	}

	public int updateUnuloadToOk(List<Long> ids) {

		values = new ContentValues();

		values.put(PingtestColumns.STATUS, CellInfo.OK);

		String whereClause = null;
		String[] whereArgs = null;

		for (int i = 0; i < ids.size(); i++) {
			whereClause = PingtestColumns._ID + " = ?";
			whereArgs = new String[] { Long.toString(ids.get(i)) };
			resolver.update(
					Uri.parse(DatabaseProvider.CONTENT_URI
							+ PingtestColumns.TABLE_NAME), values, whereClause,
					whereArgs);
		}

		return ids.size();
	}
	
	public int deleteOK() {
		String whereClause = PingtestColumns.STATUS + " = ?";
		String[] whereArgs = new String[]{ CellInfo.OK };
		
		int affected = resolver.delete(
				Uri.parse(DatabaseProvider.CONTENT_URI + PingtestColumns.TABLE_NAME), 
				whereClause, 
				whereArgs
				);
		
		return affected;
	}
}
