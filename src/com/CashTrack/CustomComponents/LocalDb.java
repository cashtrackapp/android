package com.CashTrack.CustomComponents;

import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class LocalDb {
	
	private static final String TAG = "DebtsProvider";

    private static final String DATABASE_NAME = "cash_track.db";
    private static final int DATABASE_VERSION = 3;
    private static final String DEBTS_TABLE_NAME = "debts";
    private static final String MESSAGES_TABLE_NAME = "messages";

    private DatabaseHelper mOpenHelper;
    private BillServer server;
    
	public LocalDb(Context c) {
		mOpenHelper = new DatabaseHelper(c);
		
		this.server = ((BillServer) c.getApplicationContext());
		
        return ;
    }
	
	public boolean saveDebt(HashMap<String, String> send_data) throws JSONException {
		HashMap<String, String> debt = new HashMap<String, String>(send_data);
		
		JSONObject rtn = server.send(send_data);
		
		//Log.d(this.getClass().getSimpleName(), debt.toString());
		
		if (rtn.getBoolean("error")) {
			return false;
		}
		
		if (debt.get("action").equals("debt_add")) {
			debt.remove("action");
			debt.put("debt_id", rtn.getString("return"));
			
			if (debt.get("borrower_id").equals(String.valueOf(server.getUser()))) {
				int id = Integer.valueOf(debt.get("lender_id")).intValue();
								
				Friend friend = new Friend(id);
				Friend friend2 = BillServer.friendList.get(BillServer.friendList.indexOf(friend));
				
				debt.put("lender_name", friend2.fname + " " + friend2.lname);
			}
			else if (debt.get("lender_id").equals(String.valueOf(server.getUser()))) {
				int id = Integer.valueOf(debt.get("borrower_id")).intValue();
				
				Friend friend = new Friend(id);
				Friend friend2 = BillServer.friendList.get(BillServer.friendList.indexOf(friend));
				
				debt.put("borrower_name", friend2.fname + " " + friend2.lname);
			}
			
			insertDebt(debt);
			//getAllDebts(Constants.SERVER);
		}
		else {
			debt.remove("action");
			updateDebt(debt);
		}
		
		return true;
	}
	
	private void insertDebt(HashMap<String, String> debt_) throws JSONException {
		ContentValues values = new ContentValues();
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		JSONObject debt = new JSONObject(debt_);
		
		Iterator<?> iter = debt.keys();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			String value = debt.getString(key);
			values.put(key, value);
		}
		
		long row = db.insert(DEBTS_TABLE_NAME, "debt", values);
		Log.d(this.getClass().getSimpleName(), "LocalDb: Row " + row + " added");
		
		db.close();
	}
	
	private void updateDebt(HashMap<String, String> debt_) throws JSONException {
		ContentValues values = new ContentValues();
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		JSONObject debt = new JSONObject(debt_);
		
		Log.d(this.getClass().getSimpleName(), debt_.toString());
		Log.d(this.getClass().getSimpleName(), debt.toString(1));
		
		Iterator<?> iter = debt.keys();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			String value = debt.getString(key);
			values.put(key, value);
		}
		
		String where_clause = "debt_id=" + debt.getString("debt_id");
		int row = db.update(DEBTS_TABLE_NAME, values, where_clause, null);
		Log.d(this.getClass().getSimpleName(), "LocalDb: Row " + row + " updated");
		
		db.close();
	}
	
	public JSONObject getDebtDetails(int debt_id) throws JSONException {
		JSONObject debt = new JSONObject();
		
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();

		String selection = "debt_id=" + debt_id;
		Cursor c = db.query(DEBTS_TABLE_NAME, null, selection, null, null, null, null);
		
		if (c.getCount() > 0) {
			c.moveToFirst();
			for (int i=0; i<c.getColumnCount(); ++i) {      				
				debt.put(c.getColumnName(i), c.getString(i));
			}
		}
		else {
			HashMap<String, String> data = new HashMap<String, String>();
			data.put("action", "debt_details");
			data.put("debt_id", Integer.toString(debt_id));
		
			debt = server.send(data).getJSONObject("return");
		}
		
		c.close();
				
		db.close();
		
		return debt;
	}
	
	public void submitNewMessage(String friend_id, String subject, String body) throws JSONException, Exception {
		HashMap<String, String> data = new HashMap<String, String>();
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		
		data.put("action", "message_create");
		data.put("user_id", friend_id);
		data.put("subject", subject);
		data.put("body", body);
		
		this.server.send(data);
		
		db.close();
	}
	
	public void markMessageAsRead(int message_id) throws SQLException, JSONException {
		HashMap<String, String> data = new HashMap<String, String>();
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		
		data.put("action", "message_viewed");
		data.put("message_id", "" + message_id);
		
		if (!server.send(data).getBoolean("error")) {
			String cmd = "UPDATE " + MESSAGES_TABLE_NAME + " SET viewed=1 WHERE message_id=" + message_id;
			Log.d(this.getClass().getSimpleName(), "LocalDb: " + cmd);
			db.execSQL(cmd);
		}
		
		db.close();
	}
	
	public JSONArray getAllMessages(int type) throws JSONException, Exception {
		
		if (type == Constants.SERVER) {
			HashMap<String, String> data = new HashMap<String, String>();
			
			data.put("action", "message_all");
			data.put("user_id", String.valueOf(this.server.getUser()));
			
			JSONObject result = this.server.send(data);
			
			if (result.getBoolean("error")) {
				throw new Exception(result.getString("error_detail"));
			}
			
			JSONArray messages = result.getJSONArray(Constants.VAR_RETURN);
			insertMessages(messages);
			
			return messages;
		}
		
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        
    	String selection = "user_id=" + server.getUser() + " OR from_id=" + server.getUser();
    	
    	Cursor c = db.query(MESSAGES_TABLE_NAME, null, selection, null, null, null, "date_created DESC");
    
    	JSONArray messages = new JSONArray();
        
    	for (int j=0; j<c.getCount(); ++j) {
    		c.moveToNext();
        	
    		JSONObject message = new JSONObject();
        		
    		for (int i=0; i<c.getColumnCount(); ++i) {      				
    			message.put(c.getColumnName(i), c.getString(i));
			}
        		
        	messages.put(message);
        }
    	
    	c.close();
        
    	db.close();
        
    	return messages;
	}
	
	public void insertMessages(JSONArray messages) {
		ContentValues values = new ContentValues();
    	SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    	
    	String cmd = "DELETE FROM " + MESSAGES_TABLE_NAME + " WHERE user_id=" + server.getUser() +
    	             " OR from_id=" + server.getUser();
    	Log.d(this.getClass().getSimpleName(), "LocalDb: " + cmd);
    	db.execSQL(cmd);
    	
    	try {

    	for (int i=0; i<messages.length(); ++i) {
    		JSONObject message = messages.getJSONObject(i);
    		Iterator<?> iter = message.keys();
    		while (iter.hasNext()) {
    			String key = (String) iter.next();
    			String value = message.getString(key);
    			if (value.equals("null")) {
    				values.put(key, "0");
    			}
    			else {
    				values.put(key, value);
    			}
    		}
    		long row = db.insert(MESSAGES_TABLE_NAME, "message", values);
    		Log.d(this.getClass().getSimpleName(), "LocalDb: Row " + row + " added");
    	}
    	
    	} catch (JSONException e) {
    		Log.e(this.getClass().getSimpleName(), e.getMessage());
    		e.printStackTrace();
		}
    	
    	db.close();
	}
    
    public JSONArray getAllDebts(int type) throws JSONException {
    	
    	if (type == Constants.SERVER) {
    		HashMap<String, String> data = new HashMap<String, String>();
    		
    		data.put("action", Constants.FN_DEBT_ALL);
    		data.put(Constants.VAR_USER_ID, Integer.toString(this.server.getUser()));
    		
    		JSONObject rtn = this.server.send(data);
    		
    		if (!rtn.getBoolean("error")) {
    			JSONArray debts = rtn.getJSONArray(Constants.VAR_RETURN);
    			insertDebts(debts);
    		
    			return debts;
    		}
    	}
    	
       	SQLiteDatabase db = mOpenHelper.getReadableDatabase();
                
    	String selection = "borrower_id=" + server.getUser() + " OR lender_id=" + server.getUser();
    	
    	Cursor c = db.query(DEBTS_TABLE_NAME, null, selection, null, null, null, "paid ASC, date DESC");
    
    	JSONArray all_debts = new JSONArray();
        
    	for (int j=0; j<c.getCount(); ++j) {
    		c.moveToNext();
        	
    		JSONObject debt = new JSONObject();
        		
    		for (int i=0; i<c.getColumnCount(); ++i) {      				
    			debt.put(c.getColumnName(i), c.getString(i));
			}
        		
        	all_debts.put(debt);
        }
    	
    	c.close();
        
    	db.close();
        
    	return all_debts;
    }
    
    private void insertDebts(JSONArray debts) {
    	
    	ContentValues values = new ContentValues();
    	SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    	
    	String cmd = "DELETE FROM " + DEBTS_TABLE_NAME + " WHERE borrower_id=" + 
    				 server.getUser() + " OR lender_id=" + server.getUser();
    	Log.d(this.getClass().getSimpleName(), "LocalDb: " + cmd);
    	db.execSQL(cmd);
    	
    	try {

    	for (int i=0; i<debts.length(); ++i) {
    		JSONObject debt = debts.getJSONObject(i);
    		Iterator<?> iter = debt.keys();
    		while (iter.hasNext()) {
    			String key = (String) iter.next();
    			String value = debt.getString(key);
    			if (value.equals("null")) {
    				values.put(key, "0");
    			}
    			else {
    				values.put(key, value);
    			}
    		}
    		long row = db.insert(DEBTS_TABLE_NAME, "debt", values);
    		Log.d(this.getClass().getSimpleName(), "LocalDb: Row " + row + " added");
    	}
    	
    	} catch (JSONException e) {
    		Log.e(this.getClass().getSimpleName(), e.getMessage());
    		e.printStackTrace();
		}
    	
    	db.close();
    	
        return;
    }
    
    public void deleteDebt(String debt_id) throws SQLException, JSONException {
    	HashMap<String, String> data = new HashMap<String, String>();
    	SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    	
    	data.put("action", Constants.FN_DEBT_REMOVE);
		data.put(Constants.VAR_DEBT_ID, debt_id);
	
		if (!server.send(data).getBoolean("error")) {
    	
			String cmd = "DELETE FROM " + DEBTS_TABLE_NAME + " WHERE debt_id=" + debt_id;
    		Log.d(this.getClass().getSimpleName(), "LocalDb: " + cmd);
    		db.execSQL(cmd);
		}
		
		db.close();
		
    	return;
    }
    
    public void markDebtAsPaid(String debt_id) throws SQLException, JSONException {
    	HashMap<String, String> data = new HashMap<String, String>();
    	SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    	
    	data.put("action", Constants.FN_DEBT_RESOLVE);
		data.put(Constants.VAR_DEBT_ID, debt_id);
	
		if (!server.send(data).getBoolean("error")) {
    	
			String cmd = "UPDATE " + DEBTS_TABLE_NAME + " SET paid=1 WHERE debt_id=" + debt_id;
			Log.d(this.getClass().getSimpleName(), "LocalDb: " + cmd);
			db.execSQL(cmd);
		}
		
		db.close();
    	
    	return;
    }
    
    public void close() {
    	mOpenHelper.close();
    }
    
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + DEBTS_TABLE_NAME + "( " +
        			   "debt_id TEXT PRIMARY KEY," +
        			   "borrower_id TEXT," +
        			   "lender_id TEXT," +
        			   "amount TEXT," +
        			   "category TEXT," +
        			   "details TEXT," +
        			   "date TEXT," +
        			   "date_due TEXT, " +
        			   "paid TEXT, " +
        			   "borrower_name TEXT, " +
        			   "lender_name TEXT" +
        			   ");");
            
            db.execSQL("CREATE TABLE IF NOT EXISTS " + MESSAGES_TABLE_NAME + "( " +
            		   "message_id TEXT PRIMARY KEY," +
            		   "topic TEXT," +
            		   "user_id TEXT," +
            		   "from_id TEXT," +
            		   "viewed TEXT," +
            		   "date_created TEXT," +
            		   "date_expire TEXT," +
            		   "subject TEXT," +
            		   "body TEXT" +
            		   ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DEBTS_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + MESSAGES_TABLE_NAME);
            onCreate(db);
        }
    }

}
