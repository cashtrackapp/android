package com.CashTrack.Activities;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.TabHost;

import com.CashTrack.R;
import com.CashTrack.CustomComponents.BillServer;


public class BillShare extends TabActivity { 
	static int ALERTS;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	    if(savedInstanceState == null){
	    	BillServer server = (BillServer)getApplicationContext();
	    	HashMap<String, String> data = new HashMap<String, String>();
        	data.put("action", "update_check");
	    	JSONObject json_notifications;
			try {
				json_notifications = server.send(data).getJSONObject("return");
				boolean new_friends = json_notifications.getBoolean("new_friend");
				boolean new_bills = json_notifications.getBoolean("new_debt");
				boolean new_messages = json_notifications.getBoolean("new_message");
				
				if (new_messages || new_friends || new_bills) {
					showDialog(ALERTS);
				}
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	   
        this.load_tabs();
    }
    
    // LOAD LOGIN VIEW
    public void load_login() {
    	this.setContentView(R.layout.main);
    	
    	BillServer server = (BillServer)getApplicationContext();
    	server.setParent(this);
    	
    	// setup single tab
        TabHost mTabHost = getTabHost();
        Intent int_login = new Intent(this,Login.class);
        mTabHost.addTab(mTabHost.newTabSpec("login").setIndicator("Login").setContent(int_login));
        mTabHost.setCurrentTab(0);
    }
    
    // LOAD MAIN TABBED VIEW
    public void load_tabs() 
    {
    	this.setContentView(R.layout.main);
        
        // setup intents
        Intent int_balance = new Intent(this,Balance.class);
        Intent int_newbill = new Intent(this,NewBill.class);
        Intent int_friends = new Intent(this,Friends.class);
        Intent int_notifications = new Intent(this,Notifications.class);
        
        // setup tabs
        TabHost mTabHost = getTabHost();
        
        // setup icons
        Drawable draw_balance = getResources().getDrawable(R.drawable.search);
        Drawable draw_newbill = getResources().getDrawable(R.drawable.edit);
        Drawable draw_friends = getResources().getDrawable(R.drawable.share);
        Drawable draw_notifications = getResources().getDrawable(android.R.drawable.ic_menu_send);
        
        // add tabs
        mTabHost.addTab(mTabHost.newTabSpec("balance").setIndicator("Balance",draw_balance).setContent(int_balance));
        mTabHost.addTab(mTabHost.newTabSpec("newbill").setIndicator("New Bill",draw_newbill).setContent(int_newbill));
        mTabHost.addTab(mTabHost.newTabSpec("friends").setIndicator("Friends",draw_friends).setContent(int_friends));
        mTabHost.addTab(mTabHost.newTabSpec("notifications").setIndicator("Alerts",
        											         draw_notifications).setContent(int_notifications));
        mTabHost.setCurrentTab(0);
        
    }
    @Override
    protected Dialog onCreateDialog(int id){
    	if(id == ALERTS){
    		boolean new_friends;
            boolean new_bills;
            boolean new_messages;
            String notifications = new String();        
            BillServer server = (BillServer)getApplicationContext();
        	server.setParent(this);
        	HashMap<String, String> data = new HashMap<String, String>();
        	data.put("action", "update_check");
            try {
    			JSONObject json_notifications = server.send(data).getJSONObject("return");
    			new_friends = json_notifications.getBoolean("new_friend");
    			new_bills = json_notifications.getBoolean("new_debt");
    			new_messages = json_notifications.getBoolean("new_message");
    			if(new_friends || new_bills || new_messages){
    				if(new_friends){
    					notifications = notifications.concat(getResources().getString(R.string.new_friends));
    					notifications = notifications.concat("\n");
    				}
    				if(new_bills){
    					notifications = notifications.concat(getResources().getString(R.string.new_bills));
    					notifications = notifications.concat("\n");
    				}
    				if(new_messages){
    					notifications = notifications.concat(getResources().getString(R.string.new_messages));
    					notifications = notifications.concat("\n");
    				}
    				notifications = notifications.substring(0, notifications.lastIndexOf("\n"));
    				Dialog alerts = new AlertDialog.Builder(this).setMessage(notifications).
    				setPositiveButton("OK", new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialog, int which) {
    						dialog.dismiss();
    					}
    				})
    				.create();
    				return alerts;
    			}
    		} catch (JSONException e) {
    			e.printStackTrace();
    		}
    	}
    	return null;
    }
    @Override
    public void onRestoreInstanceState(Bundle state){
    	super.onRestoreInstanceState(state);
    	removeDialog(ALERTS);
    }
}
