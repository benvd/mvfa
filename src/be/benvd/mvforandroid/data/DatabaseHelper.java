/*
	Copyright (C) 2010 Ben Van Daele (vandaeleben@gmail.com)

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package be.benvd.mvforandroid.data;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "mvforandroid.db";
	private static final int SCHEMA_VERSION = 1;

	private static final String HISTORY_TABLE = "history";
	private static final String TOPUPS = "topups";
	private static final String CREDIT_TABLE = "credit";

	public final History history;
	public final Credit credit;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, SCHEMA_VERSION);

		this.history = new History();
		this.credit = new Credit();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS " + HISTORY_TABLE + " (" + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "timestamp INTEGER NOT NULL, " + "duration INTEGER NOT NULL, " + "type INTEGER NOT NULL, "
				+ "incoming INTEGER NOT NULL, " + "contact TEXT NOT NULL, " + "cost REAL NOT NULL);");
		db.execSQL("CREATE TABLE IF NOT EXISTS " + TOPUPS + " (" + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "amount REAL NOT NULL, " + "method TEXT NOT NULL, " + "executed_on INTEGER NOT NULL, "
				+ "received_on INTEGER NOT NULL, " + "status TEXT NOT NULL);");
		db.execSQL("CREATE TABLE IF NOT EXISTS " + CREDIT_TABLE + " (" + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "valid_until INTEGER NULL, " + "expired INTEGER NOT NULL, " + "sms INTEGER NOT NULL, "
				+ "data INTEGER NOT NULL, " + "credits REAL NOT NULL);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// We're still at the first version of our schema (see SCHEMA_VERSION), so this method will not be called.
	}

	public class Credit {

		public void update(JSONObject json) throws JSONException {
			Cursor query = getWritableDatabase().query(CREDIT_TABLE, new String[] { "_id" }, null, null, null, null,
					null);

			ContentValues values = new ContentValues();

			values.put("valid_until", ParseUtil.getDateFromAPI(json.getString("valid_until")).getTime());
			values.put("expired", (Boolean.parseBoolean(json.getString("is_expired")) ? 1 : 0));
			values.put("sms", Integer.parseInt(json.getString("sms")));
			values.put("data", Long.parseLong(json.getString("data")));
			values.put("credits", Double.parseDouble(json.getString("credits")));

			if (query.getCount() == 0) {
				// No credit info stored yet, insert a row

				Log.d("MVFA", "Inserted credit: " + getWritableDatabase().insert(CREDIT_TABLE, "valid_until", values));
			} else {
				// Credit info present already, so update it
				Log.d("MVFA", "Updated credit: " + getWritableDatabase().update(CREDIT_TABLE, values, null, null));
			}

			query.close();
			getWritableDatabase().close();
		}

		public long getValidUntil() {
			Cursor c = getWritableDatabase().query(CREDIT_TABLE, new String[] { "_id" }, null, null, null, null, null);
			long result;

			if (c.moveToFirst())
				result = c.getLong(1);
			else
				result = 0;

			c.close();
			return result;
		}

		public boolean isExpired() {
			Cursor c = getWritableDatabase().query(CREDIT_TABLE, new String[] { "_id" }, null, null, null, null, null);
			boolean result;

			if (c.moveToFirst())
				result = c.getLong(2) == 1;
			else
				result = true;

			c.close();
			return result;
		}

		public int getRemainingSms() {
			Cursor c = getWritableDatabase().query(CREDIT_TABLE, new String[] { "_id" }, null, null, null, null, null);
			int result;

			if (c.moveToFirst())
				result = c.getInt(3);
			else
				result = 0;

			c.close();
			return result;
		}

		public int getRemainingData() {
			Cursor c = getWritableDatabase().query(CREDIT_TABLE, new String[] { "_id" }, null, null, null, null, null);
			int result;

			if (c.moveToFirst())
				result = c.getInt(4);
			else
				result = 0;

			c.close();
			return result;
		}

		public double getRemainingCredit() {
			Cursor c = getWritableDatabase().query(CREDIT_TABLE, new String[] { "_id" }, null, null, null, null, null);
			double result;

			if (c.moveToFirst())
				result = c.getDouble(5);
			else
				result = 0;

			c.close();
			return result;
		}
	}

	public class History {

		public static final int TYPE_DATA = 0;
		public static final int TYPE_SMS = 1;
		public static final int TYPE_VOICE = 2;
		public static final int TYPE_MMS = 3;

		public void insert(JSONObject json) throws JSONException {
			// TODO Check unicity (based on timestamp + contact, for instance.
			// TODO Respect max history entries preference (clean up oldest if necessary).
			ContentValues values = new ContentValues();
			values.put("timestamp", ParseUtil.getDateFromAPI(json.getString("start_timestamp")).getTime());
			values.put("duration", json.getLong("start_timestamp"));

			if (Boolean.parseBoolean(json.getString("is_data")))
				values.put("type", TYPE_DATA);
			if (Boolean.parseBoolean(json.getString("is_sms")))
				values.put("type", TYPE_SMS);
			if (Boolean.parseBoolean(json.getString("is_voice")))
				values.put("type", TYPE_VOICE);
			if (Boolean.parseBoolean(json.getString("is_mms")))
				values.put("type", TYPE_MMS);

			values.put("incoming", (Boolean.parseBoolean(json.getString("is_incoming")) ? 1 : 0));
			values.put("contact", json.getString("to"));
			values.put("cost", Double.parseDouble(json.getString("price")));

			getWritableDatabase().insert(HISTORY_TABLE, "timestamp", values);
			getWritableDatabase().close();
		}

		public long getTimestamp(Cursor c) {
			return c.getLong(1);
		}

		public long getduration(Cursor c) {
			return c.getLong(2);
		}

		public int getType(Cursor c) {
			return c.getInt(3);
		}

		public boolean isIncoming(Cursor c) {
			return c.getInt(4) == 1;
		}

		public String getContact(Cursor c) {
			return c.getString(5);
		}

		public double getCost(Cursor c) {
			return c.getDouble(6);
		}

	}

}
