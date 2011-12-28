package com.CashTrack.Activities;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.CashTrack.R;
import com.CashTrack.CustomComponents.Friend;
import com.CashTrack.CustomComponents.BillServer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.*;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.util.Log;

public class SearchResults extends ListActivity {
	
	private BillServer server;
	protected ArrayList<Friend> users = new ArrayList<Friend>();
	
	static final int ALREADY_FRIENDS = 0;
	static final int CANNOT_FRIEND_SELF = 1;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		server = ((BillServer)getApplication());
		server.setParent(this);
	}
	
	public void onResume() {
		super.onResume();
		this.load_results();
	}
	
	public void load_results() {
		
		this.setContentView(R.layout.searchresults);
		//TextView header = (TextView) findViewById(R.id.ResultHeader);
		// get email to search for
        String search_term = getIntent().getStringExtra("SEARCH");
        
        int searchBy = getIntent().getIntExtra("SEARCHBY", -1);
        
     // send query
        HashMap<String, String> request = new HashMap<String, String>();
        
        if(searchBy == 4){ //SEARCH ALL
        	request.put("action", "user_search");
        	request.put("search", search_term);
        }
        else{
        	request.put("action", "user_search_fields");
        	if(searchBy == 0) // e-mail
        		request.put("email", search_term);
        	else if(searchBy ==1) //phone number
        		request.put("phone", search_term);
        	else if(searchBy ==2) //name
        		request.put("name", search_term);
        	else if(searchBy ==3) //username
        		request.put("username", search_term);
        	else Log.e("Search Code", ""+searchBy);
        }
        
		JSONObject json = server.send(request);
		
		try {
			if ( !json.getBoolean("error") ) {
				JSONArray users_array = json.getJSONArray("return");
				this.users.clear();
			
				// loop through all users
				for( int count = 0; count < users_array.length(); count++)
				{
					JSONObject user = users_array.getJSONObject(count);
					Friend list_item = new Friend(user.getString("username"), user.getString("fname"),
							user.getString("lname"), user.getString("email"), user.getString("phone"),
							user.getInt("user_id"), 2);
					if(list_item.id != server.getUser() && !server.friendList.contains(list_item))
					this.users.add(list_item);
				}
				
				// setup list
				getListView().setAdapter(new ArrayAdapter<Friend>(this,
					       android.R.layout.simple_list_item_1, this.users));
				registerForContextMenu(getListView());
				//if(list.getCount() == 0)
				//	header.setText("No Results Found");
				//else header.setText(list.getCount()+" Matches");
			}
		} catch ( Exception e ) {
			Log.e(this.getClass().getSimpleName(), e.toString());
			e.printStackTrace();
		}
	
	}
	
	// setup context menu
	public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("Add user to friends list?");
		menu.add(0, 0, 0, "Yes");
		menu.add(0, 1, 0, "No");
	}
	
	// context menu actions
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case 0:
			// add friend
			try {
				Friend user = (Friend) getListView().getAdapter().getItem(menuInfo.position);
				HashMap<String,String> data = new HashMap<String, String>();
				data.put("action", "friend_add");
				data.put("friend_id", Integer.toString(user.id));
				JSONObject json = server.send(data);
				String error = json.getString("error");
				if(error.equals("false")){
					server.refreshFriendList();
					finish();
				}
			} catch (Exception e) {
				Log.e(this.getClass().getSimpleName(), e.toString());
				e.printStackTrace();
			}
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
	
	// get array of all users
	public static String[] setupUsersList(JSONArray json) throws Exception
	{
		String[] users = new String[json.length()];
		
		// loop though all users
		for( int count = 0; count < json.length(); count++)
		{
			JSONObject user = json.getJSONObject(count);
			users[count] = user.getString("fname")+" "+user.getString("lname");
		}
		return users;
	}
	
}
