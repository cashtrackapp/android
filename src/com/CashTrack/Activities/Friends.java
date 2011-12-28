package com.CashTrack.Activities;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;
//import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.CashTrack.R;
import com.CashTrack.CustomComponents.BillServer;
import com.CashTrack.CustomComponents.Friend;

public class Friends extends Activity {
	
	private BillServer server;
	protected ListView list;
	private Button addFriend;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		server = (BillServer)this.getApplication();
		this.setContentView(R.layout.friends);
		addFriend = (Button) findViewById(R.id.NewButton);
        addFriend.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				load_search();
			}
        });
        list = (ListView) this.findViewById(R.id.FriendsList);
	}
	
	public void onResume() {
		super.onResume();
        server.setParent(this);
        load_home();
	}
	
	
	// load friends home
	private void load_home() 
	{
		list.setAdapter(new ArrayAdapter<Friend>(this,
			       android.R.layout.simple_list_item_1, BillServer.friendList));
		registerForContextMenu(list);
	}
	
	/* Creates the menu items */
	public boolean onCreateOptionsMenu(Menu menu) {
	    
		MenuItem refresh = menu.add(0, 1, 0, "Refresh"); 
	    refresh.setIcon(android.R.drawable.ic_menu_rotate);
	    
	    MenuItem settings = menu.add(0, 2, 0, "Settings"); 
	    settings.setIcon(android.R.drawable.ic_menu_manage);
	    
	    MenuItem logout = menu.add(0, 3, 0, "Logout");
	    logout.setIcon(android.R.drawable.ic_menu_revert);
	   
	    return true;
	}

	/* Handles item selections */
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId())
		{		
			case 1:	 
				server.refreshFriendList();
				load_home();
				return true;
			
			case 2:	 
				showSettings();
				return true;
				
			case 3:  
				logout();
				return true;
				
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private void showSettings() {
		Intent i = new Intent(this, Settings.class);
		startActivity(i);
	}
	
	private void logout()
	{
		BillServer server = ((BillServer) this.getApplication());
		
		 server.setKey(null);
		 server.settings_edit.putString("username", null);
		 server.settings_edit.putString("password", null);
		 server.settings_edit.commit();

		 Intent login = new Intent(this, Login.class);
		 startActivity(login);
		 this.finish();
	}
	
	// setup context menu
	public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		Friend friend = ((Friend)list.getItemAtPosition(((AdapterContextMenuInfo)menuInfo).position));
		menu.setHeaderTitle("Friend Options");
		if(friend.confirmed == 2)
			menu.add(0, 0, 0, "View Details");
		if(friend.confirmed == 0)
			menu.add(0, 1, 0, "Confirm Friend");
		menu.add(0, 2, 0, "Remove Friend");
		menu.add(0, 3, 0, "Cancel");
		
	}
	
	// context menu actions
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case 0:
			Friend viewFriend = (Friend) list.getAdapter().getItem(menuInfo.position);
			Intent viewUser = new Intent(this, UserView.class);
			HashMap<String, String> details = new HashMap<String, String>();
			details.put("action", "user_details");
			details.put("user_id", String.valueOf(viewFriend.id));
			try {
				JSONObject results = server.send(details).getJSONObject("return");
				viewUser.putExtra("viewing", true);
				viewUser.putExtra("fname", viewFriend.fname);
				viewUser.putExtra("lname", viewFriend.lname);
				viewUser.putExtra("username", results.getString("username"));
				viewUser.putExtra("email", results.getString("email"));
				viewUser.putExtra("phone", results.getString("phone"));
				startActivity(viewUser);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return true;
		case 1:
			// confirm friend
			Friend confirmFriend = (Friend) list.getAdapter().getItem(menuInfo.position);
			HashMap<String,String> data = new HashMap<String,String>();
			data.put("action", "friend_confirm");
			data.put("friend_id", String.valueOf(confirmFriend.id));
			try {
				if(server.send(data).getBoolean("error") == true)
					return true;
				else {
					confirmFriend.confirmed = 2;
					load_home();
					return true;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		case 2:
			// remove friend
			deleteFriend((int)menuInfo.position);
			return true;
		default:
			return false;
		}
	}
	private void deleteFriend(final int index) {
		AlertDialog.Builder deleteDialog = new AlertDialog.Builder(this);
		deleteDialog.setMessage("Are you sure you want to remove this friend?")
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				try {
					
					Friend friend = (Friend) list.getAdapter().getItem(index);
					HashMap<String,String> data = new HashMap<String, String>();
					data.put("action", "friend_remove");
					data.put("friend_id", Integer.toString(friend.id)); 
					if(server.send(data).getBoolean("error") == false){
						BillServer.friendList.remove(index);
						load_home();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				dialog.dismiss();
			}
		})
		.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			};
		});
		
		AlertDialog alert = deleteDialog.create();
		alert.show();
	
		return;
	}
	
	// load friends search form
	private void load_search()
	{
		this.startActivity(new Intent(this, FriendSearch.class));
	}
}


