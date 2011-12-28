package com.CashTrack.Activities;

import java.util.HashMap;

import org.json.JSONException;

import com.CashTrack.R;
import com.CashTrack.CustomComponents.BillServer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;


public class NewUser extends Activity {
	private EditText fname;
	private EditText lname;
	private EditText username;
	private EditText password;
	private EditText email;
	private EditText phone;
	private Button login;
	private BillServer server;
	static final int NEW_USER_ERROR = 0;
	static final int MISSING_DATA_ERROR = 1;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//this.setContentView(R.layout.new_user); <-- MISSING VIEWS
		fname = (EditText) findViewById(R.id.fname);
		lname = (EditText) findViewById(R.id.lname);
		username = (EditText) findViewById(R.id.username);
		password = (EditText) findViewById(R.id.password);
		email = (EditText) findViewById(R.id.email);
		phone = (EditText) findViewById(R.id.phone);
		login = (Button) findViewById(R.id.loginButton);
		
		username.setText(getIntent().getStringExtra("USER"));
		password.setText(getIntent().getStringExtra("PASSWORD"));
		
		server = (BillServer)getApplicationContext();
	    server.setParent(this);
	    
	    login.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				onSubmit();	
			}    	
	    });    
	}
	private void onSubmit(){
		HashMap<String, String> newUserData = new HashMap<String, String>();
	    newUserData.put("action", "user_create");
		newUserData.put("username", username.getText().toString());
		newUserData.put("password", password.getText().toString());
		newUserData.put("fname", fname.getText().toString());
		newUserData.put("lname", lname.getText().toString());
		newUserData.put("email", email.getText().toString());
		newUserData.put("phone", phone.getText().toString());
		if(username.getText().toString().equals("") || password.getText().toString().equals("")
				|| fname.getText().toString().equals("")){
			showDialog(MISSING_DATA_ERROR);
		}
		else{	
			try {
				String error = server.send(newUserData).getString("error");
				if(error.equals("true"))
					showDialog(NEW_USER_ERROR);
				else{
					if(server.login(username.getText().toString(), password.getText().toString()))
						startActivity(new Intent(this, BillShare.class));
						finish();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	@Override
	public Dialog onCreateDialog(int id) {
		switch(id){
		case NEW_USER_ERROR:
			return new AlertDialog.Builder(this)
			.setTitle("Username Taken")
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		               dialog.cancel();
		           }
			})
			.create();
		case MISSING_DATA_ERROR:
			return new AlertDialog.Builder(this)
			.setTitle("Username, password, and first name required")
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		               dialog.cancel();
		           }
			})
			.create();
		default:
			return null;
		}
	}
}