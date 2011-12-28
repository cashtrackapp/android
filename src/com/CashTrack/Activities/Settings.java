package com.CashTrack.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.CashTrack.R;
import com.CashTrack.CustomComponents.BillServer;

public class Settings extends Activity {
	
	public static final String SETTINGS_LOGIN = "Save login information for automatic sign in";
	public static final String SETTINGS_UPDATE = "Click to update user profile";
	private BillServer server;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        this.setContentView(R.layout.settings);
        
        server = (BillServer) this.getApplication();
        server.setParent(this);
        
        TextView loginText = (TextView) this.findViewById(R.id.LoginSettingsText);
        TextView updateText = (TextView) this.findViewById(R.id.UpdateText);
        loginText.setText(SETTINGS_LOGIN);
        updateText.setText(SETTINGS_UPDATE);
        
        CheckBox loginCheck = (CheckBox) this.findViewById(R.id.LoginSettingsCheck);
        Button updateButton = (Button) findViewById(R.id.UpdateUser);
        updateButton.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				Intent updateUser = new Intent(arg0.getContext(), UserView.class);
				updateUser.putExtra("updating", true);
				startActivity(updateUser);
			}
        });
 
        if (server.settings.getBoolean("save_login", false))
        {
        	loginCheck.setChecked(true);
        }
        
        loginCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
				Log.i(this.getClass().getSimpleName(), "isChecked:" + isChecked);
				
				if (isChecked)
				{
					server.settings_edit.putBoolean("save_login", true);
				}
				else
				{
					server.settings_edit.putBoolean("save_login", false);
				}
				
				server.settings_edit.commit();
			}
        	
        });
        
	}
	
	@Override
	public void onResume(){
		super.onResume();
		server.setParent(this);
	}
	
	/* Creates the menu items */
	public boolean onCreateOptionsMenu(Menu menu) {
	    
	    MenuItem logout = menu.add(0, 1, 0, "Logout");
	    logout.setIcon(android.R.drawable.ic_menu_revert);
	   
	    return true;
	}

	/* Handles item selections */
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId())
		{		
			case 1:  logout();
				     return true;
			default:
				     return super.onOptionsItemSelected(item);
		}
	}
	
	private void logout()
	{
		 server.setKey(null);
		 server.settings_edit.putString("username", null);
		 server.settings_edit.putString("password", null);
		 server.settings_edit.commit();

		 Intent login = new Intent(this, Login.class);
		 startActivity(login);
		 this.finish();
	}
}
