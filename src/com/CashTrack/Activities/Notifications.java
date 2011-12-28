package com.CashTrack.Activities;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.CashTrack.R;
import com.CashTrack.CustomComponents.AlertAdapter;
import com.CashTrack.CustomComponents.BillServer;
import com.CashTrack.CustomComponents.Constants;
import com.CashTrack.CustomComponents.Friend;
import com.CashTrack.CustomComponents.LocalDb;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class Notifications extends Activity {
	
	private BillServer server;
	private Dialog new_message;
	private ArrayList<Integer> selectedFriends;
	private Button choose_recipients;
	private JSONArray alerts;
	private boolean showingMessageDetails;
	private LocalDb db;
	private long last_refreshed;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
		server = ((BillServer) this.getApplication());
		selectedFriends = new ArrayList<Integer>();
		db = new LocalDb(this);
		last_refreshed = System.currentTimeMillis();
		
		refresh(Constants.SERVER);
	}
	
	public void onResume() {
		super.onResume();
		
		long cur_time = System.currentTimeMillis();
		
		if (cur_time-last_refreshed >= Constants.MESSAGE_REFRESH_TIME_MS) {
			refresh(Constants.SERVER);
		}
		
		return;
	}
	
	private void refresh(int type) {
		try {
			this.setContentView(R.layout.notifications);
			
			showingMessageDetails = false;
			alerts = db.getAllMessages(type);
			
			ListView alert_list = (ListView) this.findViewById(R.id.AlertList);
			alert_list.setAdapter(new AlertAdapter(this, alerts, this.server.getUser()));
			
			alert_list.setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long index) {
					setContentView(R.layout.message_details);

					JSONObject message;
					try {
						showingMessageDetails = true;
						message = alerts.getJSONObject((int) index);
						
						String message_type = message.getString("topic");
						
						db.markMessageAsRead(message.getInt("message_id"));
					
						TextView from = (TextView) findViewById(R.id.MessageDetailFrom);
						Friend friend = new Friend(message.getInt("from_id"));
						int friend_index = BillServer.friendList.indexOf(friend);
						if (friend_index != -1) {
							Friend friend2 = BillServer.friendList.get(friend_index);
							from.setText(friend2.fname + " " + friend2.lname);
						}
						
						TextView create_date = (TextView) findViewById(R.id.MessageDetailCreated);
						Date format_date = new Date(message.getLong("date_created")*1000);
						create_date.setText(format_date.toString());
						
						TextView subject = (TextView) findViewById(R.id.MessageDetailSubject);
						subject.setText(message.getString("subject"));
						
						TextView body = (TextView) findViewById(R.id.MessageDetailBody);
						body.setText(message.getString("body"));
						
						TextView expire_date = (TextView) findViewById(R.id.MessageDetailExpires);
						format_date = new Date(message.getLong("date_expire")*1000);
						
						if (message.getLong("date_expire") == 0) {
							expire_date.setText("Does not expire");
						}
						else {
							expire_date.setText(format_date.toString());
						}
												
						if (message_type.equals(Constants.MESSAGE_NEW_FRIEND)) {
							int status = checkConfirmed(message.getInt("from_id"));
							if (status == 0 || status == 1) {
								Notifications.this.newFriendDisplay(message.getInt("from_id"));
							}
						}
											
					} catch (JSONException e) {
						e.printStackTrace();
					}

					Button message_detail_done = (Button) findViewById(R.id.MessageDetailDone);
					message_detail_done.setOnClickListener(new OnClickListener() {

						public void onClick(View v) {				
							refresh(Constants.LOCAL);	
						}
					});
				}	
			});
			
			last_refreshed = System.currentTimeMillis();
			
		} catch (JSONException e) {
			Log.e(this.getClass().getSimpleName(), e.toString());
			e.printStackTrace();
		} catch (Exception e) {
			Log.e(this.getClass().getSimpleName(), e.toString());
			e.printStackTrace();
		}
		
		Button new_alert_but = (Button) this.findViewById(R.id.NewMessageButton);
		new_alert_but.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {				
				showPopup();	
			}
		});
		
		return;
	}
	
	private int checkConfirmed(int id) throws JSONException {
		HashMap<String, String> data = new HashMap<String, String>();
		
		data.put("action", "friend_check");
		data.put("friend_id", String.valueOf(id));
		
		JSONObject rtn = server.send(data);
		if (!rtn.getBoolean("error")) {
			return rtn.getInt(Constants.VAR_RETURN);
		}
		
		return 4;
	}
	
	private void newFriendDisplay(int id) throws JSONException {
		Button confirm_but = (Button) this.findViewById(R.id.MessageConfirmButton);
		Button reject_but = (Button) this.findViewById(R.id.MessageRejectButton);
		
		confirm_but.setVisibility(0);
		reject_but.setVisibility(0);
		
		final int friend_id = id;
		
		confirm_but.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				friendRequest(Constants.CONFIRM_FRIEND, friend_id);
				refresh(Constants.LOCAL);
				showDialog("Friend added!");
			}
		});
		
		reject_but.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				friendRequest(Constants.REJECT_FRIEND, friend_id);
				refresh(Constants.LOCAL);
				showDialog("Friend request rejected");
			}
		});
		
		return;
	}
	
	private void friendRequest(String response, int u2) {
		HashMap<String, String> data = new HashMap<String, String>();
		
		data.put("action", response);
		data.put("friend_id", String.valueOf(u2));
		
		server.send(data);
		
		return;
	}
	
	private void showPopup() {
		View popup = this.getLayoutInflater().inflate(R.layout.new_message, null);
		
		this.new_message = new Dialog(this);
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(280, 300);
		new_message.addContentView(popup, params);
		new_message.setTitle("New Message");
		//new_message.setContentView(popup);
		new_message.setCancelable(true);
		new_message.show();
		
		Button message_submit_but = (Button) this.new_message.findViewById(R.id.NewMessageButton);
		message_submit_but.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				for (int i=0; i<selectedFriends.size(); ++i) {
					submitNewMessage(i);
				}
				refresh(Constants.LOCAL);
			}
		});
		
		Button message_cancel_but = (Button) this.new_message.findViewById(R.id.CancelMessageButton);
		message_cancel_but.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				new_message.dismiss();
			}
		});
		
		this.choose_recipients = (Button) this.new_message.findViewById(R.id.NewMessageTo);
		this.choose_recipients.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				showFriendsList();
			}
		});
	}
	
	private void showFriendsList() {
		final boolean[] checked = new boolean[BillServer.friendList.size()];
    	final String[] names = new String[BillServer.friendList.size()];
    	final int[] ids = new int[BillServer.friendList.size()];
    	
    	for(int x = 0; x< BillServer.friendList.size(); x++){
    		names[x] = BillServer.friendList.get(x).toString();
    		ids[x] = BillServer.friendList.get(x).id;
    		if(selectedFriends.contains(BillServer.friendList.get(x).id)) checked[x] = true;
    	}
    	
    	AlertDialog friends_list = new AlertDialog.Builder(this).setTitle("Choose Friends")
    	.setCancelable(false)
    	.setMultiChoiceItems(names, checked, new DialogInterface.OnMultiChoiceClickListener(){
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				checked[which] = isChecked;
			}})
    	.setPositiveButton("Done", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	   String friends = "";
	        	   
	        	   selectedFriends.clear();
	        	   for(int x=0; x < checked.length; x++){
	        		   if(checked[x]) {
	        			   selectedFriends.add(ids[x]);
	        			   if (friends.length() == 0)
	        				   friends += names[x];
	        			   else
	        				   friends += ", " + names[x];
	        		   }
	        	   }
	        	   
	        	   choose_recipients.setText(friends);
	        	   
	        	   dialog.dismiss();
	           }
	       })
	       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id){
	        	   dialog.cancel();
	           }
	       })
	       .create();
    	
    	friends_list.show();
	}

	private void submitNewMessage(int friend) {	
		String message_subject = ((EditText) this.new_message.findViewById(R.id.NewMessageSubject)).getText().toString();
		String message_body = ((EditText) this.new_message.findViewById(R.id.NewMessageBody)).getText().toString();
		
		if (message_subject.length() == 0 || message_body.length() == 0) {
			return;
		}
		
		try {
			db.submitNewMessage(this.selectedFriends.get(friend).toString(), message_subject, message_body);
			new_message.dismiss();
			
			showDialog("Message sent");
			
		} catch (JSONException e) {
			new_message.dismiss();
			showDialog("Error sending message");
			e.printStackTrace();
		} catch (Exception e) {
			new_message.dismiss();
			showDialog("Error sending message");
			e.printStackTrace();
		}

		return;
	}
	
	private void showDialog(String message) {
		Dialog d = new Dialog(this);
		d.setTitle(message);
		d.show();

		//d.dismiss();
	}
	
	public void onBackPressed() {
		if (showingMessageDetails) {
			refresh(Constants.LOCAL);
		}
		else {
			super.onBackPressed();
		}
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
			case 1: refresh(Constants.SERVER);
					return true;
			case 2: showSettings();
					return true;
			case 3:	logout();
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
		 server = ((BillServer) this.getApplication());
		
		 server.setKey(null);
		 server.settings_edit.putString("username", null);
		 server.settings_edit.putString("password", null);
		 server.settings_edit.commit();

		 Intent login = new Intent(this, Login.class);
		 startActivity(login);
		 this.finish();
	}
	
}
