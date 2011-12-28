package com.CashTrack.Activities;

import com.CashTrack.R;
import com.CashTrack.CustomComponents.BillServer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class Login extends Activity {
	private BillServer server;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.login);
		
		EditText usernameInput = (EditText) this.findViewById(R.id.UsernameInput);
		usernameInput.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		
		// get bill server
		server = (BillServer)this.getApplication();
        server.setParent(this);
        
        // check for server welcome message
        server.welcome();
        
        // setup preferences
        try {
        	server.settings = getSharedPreferences(BillServer.prefs, 2);
        	server.settings_edit = server.settings.edit();
        } catch (Exception e) {
        	Log.e(this.getClass().getSimpleName(), e.toString());
			e.printStackTrace();
        }
    	
        // set default values
        //server.settings_edit.putBoolean("save_login",false);
        //server.settings_edit.putString("username","abe");
        //server.settings_edit.putString("password","123456");
        //server.settings_edit.commit();
        
        // setup username and password
        String username = "";
        String password = "";
        
        // get saved username and password
        if (server.settings.getBoolean("save_login", false)) {
	        username = server.settings.getString("username", "");
			password = server.settings.getString("password", "");
        }

        // attempt to login using saved information
        if ( server.login(username,password) )
        {
            this.startActivity(new Intent(this, BillShare.class));
            this.finish();
        }
        
        Button login_button = (Button) this.findViewById(R.id.LoginButton);
        Button newUserButton = (Button) findViewById(R.id.NewUser);
        newUserButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				onSubmit(v);
			}
        });
        
        login_button.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				onSubmit(v);
			}}); 
	}
	@Override
	public void onResume(){
		super.onResume();
		server.setParent(this);
	}
	
	public void onSubmit(View v) {
		String username_str;
    	String password_str;
    	
		EditText username = (EditText) this.findViewById(R.id.UsernameInput);
		EditText password = (EditText) this.findViewById(R.id.PasswordInput);
		
		username_str = username.getText().toString();
		password_str = password.getText().toString();
		
		if(v.getId() == R.id.NewUser){
			Intent i = new Intent(this, UserView.class);
			i.putExtra("USER", username_str);
			i.putExtra("PASSWORD", password_str);
			startActivity(i);
		}
		else{
			if ( server.login(username_str, password_str) )
			{
				server.settings_edit.putString("username", username_str);
				server.settings_edit.putString("password", password_str);
				server.settings_edit.commit();
				this.startActivity(new Intent(this, BillShare.class));
				this.finish();
			}
			else
			{
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
				alertDialog.setMessage("Invalid username\nor password")
				.setNegativeButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
	                dialog.cancel();
					};
				});
				AlertDialog alert = alertDialog.create();
				alert.show();
			}
		}
	}
}
