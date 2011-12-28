package com.CashTrack.CustomComponents;

import java.net.*;
import java.util.*;
import java.io.*;
import org.json.*;
import java.lang.String;
import android.util.*;
import android.app.Application;
import android.content.SharedPreferences;
import android.app.*;
import android.content.Context;
import android.content.DialogInterface;

/* ===============================================================================================
 * This class provides an abstraction layer for server <--> client communication for the 
 * BillShare mobile application. It handles connecting to, sending data, receiving responses,
 * and packaging output for use in Java. 
 *
 * Example use:
 * 		HashMap<String,String> data = new HashMap<String,String>();
 * 		data.put("action","login");
 * 		data.put("username","abe");
 * 		data.put("password","123456");
 *		BillServer server = new BillServer();
 *		JSONObject json = server.send(data)
 * 
 * ===============================================================================================
 */

public class BillServer extends Application {

	// public storage space
	public String email;
	
	// public preferences
	public static final String prefs = "BillSharePrefs";
	public SharedPreferences settings;
	public SharedPreferences.Editor settings_edit;
	public static ArrayList<Friend> friendList = new ArrayList<Friend>();
	
	// private preferences
	private String target = "http://api.cashtrackapp.com/index.php";
	private String dkey = "V0upnOgR1mg3POJDdrHMcTFy2QFEOb9qIahdlgrAHKX1Dr5T6pP7rQoah5ahxZP"; 
		// CashTrack 1.2 dkey
		// One key per valid application version
		// Use https://www.grc.com/passwords.htm to generate more keys
	private String ukey = "";
	private Context parent;
	private int uid = 0;
		
	// RETRIEVE WELCOME INFORMATION FROM SERVER
	public void welcome() {
		
		// setup query
		HashMap<String,String> data = new HashMap<String,String>();
		data.put("action","welcome");
		JSONObject json = this.sendSilent(data);
		
		try {
			if ( !json.getBoolean("error") ) {
				JSONObject response = json.getJSONObject("return");
				if ( response.getBoolean("message_show") ) {
					this.alert(response.getString("message_text"), "Welcome!");
				}
				if ( !response.getBoolean("server_valid") ) {
					this.target = response.getString("server_new");
					this.alert("You are using an older version of the application. Please update as soon as possible. For assitance, please contact help@cashtrackapp.com.","Update Required");
				}
			}
		} catch (Exception ex) {
			Log.e(this.getClass().getSimpleName(), ex.toString());
			ex.printStackTrace();
		}
	}
	
	// SEND LOGIN REQUEST TO SERVER
	public boolean login(String username, String password) {
		
		// setup query
		HashMap<String,String> data = new HashMap<String,String>();
		data.put("action", "login");
		data.put("username",username);
		data.put("password",password);
		
		// send query to server
		JSONObject json = this.sendSilent(data);
		if(json.length() == 0)return false;
		
		// retrieve status
		try {
			// process return
			if ( !json.getBoolean("error") ) {
				String key = json.getString("key");
				this.ukey = key;
				this.uid = json.getInt("user_id");
				refreshFriendList();
				return true;
			} else return false;
		} catch (Exception ex) {
			Log.e(this.getClass().getSimpleName(), ex.toString());
			ex.printStackTrace();
		}
		return false;
		
	}
	
	//REFRESH FRIENDS LIST
	public void refreshFriendList(){
		HashMap<String,String> request = new HashMap<String,String>();
		request.put("action", "friend_list");
		try{
			JSONArray json = send(request).getJSONArray("return");
			friendList = new ArrayList<Friend>(json.length());
			for(int i = 0; i< json.length(); i++){
				JSONObject friend = json.getJSONObject(i);
				friendList.add(new Friend(friend.getInt("user_id"), friend.getString("fname"), 
						friend.getString("lname"), friend.getInt("confirmed")));
			}
			Collections.sort(friendList);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	// SEND DATA TO SERVER (public)
	public JSONObject send(HashMap<String,String> data) {
		return this.send(data,false);
	}
	
	// SEND DATA TO SERVER (public: silent on server error)
	public JSONObject sendSilent(HashMap<String,String> data) {
		return this.send(data,true);
	}
	
	// SEND DATA TO SERVER
	private JSONObject send(HashMap<String,String> data, Boolean silent) {
		
		// add developer key to request
		if ( this.dkey == "" ) {
			Log.e(this.getClass().getSimpleName(), "Developer key is not set." );
			System.exit(1);
		}
		data.put("dkey", this.dkey);
		
		// add user key to request
		if ( data.get("action") != "login" && 
			 data.get("action") != "user_create" &&
			 data.get("action") != "welcome" ) {
			if ( this.ukey == "" ) {
				Log.e(this.getClass().getSimpleName(), "User key is not set." );
				System.exit(1);
			}
			data.put("ukey", this.ukey);
		}
		
		// create return object
		JSONObject result = new JSONObject();
		
		try {
			// setup default return object
			result.put("error", true);
			result.put("error_detail", "Unknown error.");
			
			URL url = new URL(target);
			URLConnection conn = url.openConnection();
			
			// set connection options
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setConnectTimeout(30000);
			
			// prepare output stream
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			DataOutputStream out = new DataOutputStream(conn.getOutputStream());
			
			// parameterize data
			Set<String> keys = data.keySet();
			Iterator<String> keyIter = keys.iterator();
			String content = "";
			for ( int i=0; keyIter.hasNext(); i++ ) {
				if ( i != 0 ) content += "&";
				Object key = keyIter.next();
				content += key + "=" + URLEncoder.encode(data.get(key),"UTF-8");
			}
			
			// show url in log
			Log.w(this.getClass().getSimpleName(), "Request: "+target+"?"+content );
			
			// send data
			out.writeBytes(content);
			out.flush();
			out.close();
			
			// read response
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line = ""; String response = "";
			while ((line = in.readLine()) != null) {
				response += line + "\n";
			}
			in.close();
			
			Log.w(this.getClass().getSimpleName(), "Raw Response: "+response );
			
			// convert response to JSONObject
			try {
				result = new JSONObject(response);
				// check for server error
				if ( result.getBoolean("error") ) {
					if (!silent) warning(result.getString("error_detail"),"Server Error");
					return result;
				}
			// catch unknown error	
			} catch (JSONException e) {
				Log.e(this.getClass().getSimpleName(), e.toString());
				e.printStackTrace();
				warning("Unknown server error, please try again later.","Server Error");
				return result;
			}		
		// catch connection error	
		} catch (Exception e) {
			warning("Unable to connect to server.");
			return result;
		}
		return result;
	}
	
	// SHOW POPUP WARNING (network error title)
	private void warning(String message) {
		this.warning(message,"Network Error");
	}
		
	// SHOW POPUP WARNING (custom title)
	private void warning(String message, String title) {
	    AlertDialog ad = new AlertDialog.Builder(this.parent).create();
	    ad.setTitle(title);
	    ad.setMessage(message);
	    ad.setButton("Ignore", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int which) {
	    		return;
	    	}
	    });
	    ad.setButton2("Exit", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int which) {
	    		System.exit(1);
	    	}
	    });
	    ad.show();
	}
	
	// SHOW POPUP ALERT (custom title)
	private void alert(String message, String title) {
		AlertDialog ad = new AlertDialog.Builder(this.parent).create();
	    ad.setTitle(title);
	    ad.setMessage(message);
	    ad.setButton("Dismiss", new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int which) {
	    		return;
	    	}
	    });
	    ad.show();
	}
	
	// SET POPUP PARENT
	public void setParent(Context context) {
		this.parent = context;
	}
	
	// SET AUTH  KEY
	public void setKey(String key) {
		this.ukey = key;
	}
	
	// GET AUTH KEY STATUS
	public boolean hasKey() {
		if ( this.ukey == "" ) return false;
		else return true;
	}
	
	// GET CURRENT_USER ID
	public int getUser() {
		return this.uid;
	}

}
