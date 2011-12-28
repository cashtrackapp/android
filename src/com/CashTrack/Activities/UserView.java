package com.CashTrack.Activities;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

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
import android.widget.TextView;
import android.text.InputType;


public class UserView extends Activity {
	private EditText fname;
	private EditText lname;
	private EditText username;
	private EditText password;
	private EditText email;
	private EditText phone;
	private EditText newPassword;
	private TextView newPasswordText;
	private Button login;
	private BillServer server;
	static final int NEW_USER_ERROR = 0;
	static final int MISSING_DATA_ERROR = 1;
	static final int PHONE_NUMBER_ERROR = 2;
	static final int SAVED = 3;
	static final int PASSWORD_ERROR = 4;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.user_view);
		username = (EditText) findViewById(R.id.username);
		password = (EditText) findViewById(R.id.password);
		fname = (EditText) findViewById(R.id.fname);
		lname = (EditText) findViewById(R.id.lname);
		email = (EditText) findViewById(R.id.email);
		phone = (EditText) findViewById(R.id.phone);
		newPassword = (EditText) findViewById(R.id.newPassword);
		login = (Button) findViewById(R.id.loginButton);
		newPasswordText = (TextView) findViewById(R.id.newPasswordText);
		
		username.setText(getIntent().getStringExtra("USER"));
		password.setText(getIntent().getStringExtra("PASSWORD"));
		
		server = (BillServer)getApplicationContext();
	    server.setParent(this);
	    
	    login.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				onSubmit();	
			}    	
	    });
	    if(getIntent().getBooleanExtra("viewing", false)){
	    	login.setText("Done");
	    	TextView heading = (TextView) findViewById(R.id.user_details_heading);
	    	TextView passwordText = (TextView) findViewById(R.id.passwordText);
	    	heading.setText("User Details");
	    	passwordText.setVisibility(View.GONE);
	    	password.setVisibility(View.GONE);
	    	newPassword.setVisibility(View.GONE);
	    	newPasswordText.setVisibility(View.GONE);
	    	username.setText(getIntent().getStringExtra("username"));
	    	fname.setText(getIntent().getStringExtra("fname"));
	    	lname.setText(getIntent().getStringExtra("lname"));
	    	email.setText(getIntent().getStringExtra("email"));
	    	phone.setText(getIntent().getStringExtra("phone"));
	    	username.setCursorVisible(false);
	    	fname.setCursorVisible(false);
	    	lname.setCursorVisible(false);
	    	email.setCursorVisible(false);
	    	phone.setCursorVisible(false);
	    }
	    else if(getIntent().getBooleanExtra("updating", false)){
	    	login.setText("Submit");
	    	TextView heading = (TextView) findViewById(R.id.user_details_heading);
	    	heading.setText("Update Account");
	    	username.setEnabled(false);
	    	password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD+1);
	    	newPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
	    	fname.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
	    	lname.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
	    	email.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS+1);
	    	phone.setInputType(InputType.TYPE_CLASS_NUMBER);
	    	HashMap<String, String> data = new HashMap<String, String>();
	    	data.put("action", "user_details");
	    	data.put("user_id", String.valueOf(server.getUser()));
	    	try {
				JSONObject userDetails = server.send(data).getJSONObject("return");
				username.setText(userDetails.getString("username"));
		    	fname.setText(userDetails.getString("fname"));
		    	lname.setText(userDetails.getString("lname"));
		    	email.setText(userDetails.getString("email"));
		    	phone.setText(userDetails.getString("phone"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
	    	
	    }
	    else{
	    	newPassword.setVisibility(View.GONE);
	    	newPasswordText.setVisibility(View.GONE);
	    	username.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
	    	password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
	    	fname.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
	    	lname.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
	    	email.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS+1);
	    	phone.setInputType(InputType.TYPE_CLASS_NUMBER);
	    }
	}
	private void onSubmit(){
		if(getIntent().getBooleanExtra("viewing", false)){
	    	finish();
	    	return;
	    }
		HashMap<String, String> newUserData = new HashMap<String, String>();
		if(getIntent().getBooleanExtra("updating", false)){
			newUserData.put("action", "user_update");
			newUserData.put("user_id", String.valueOf(server.getUser()));
			if(!newPassword.getText().toString().equals(""))
				newUserData.put("new_pass", newPassword.getText().toString());
			else
				newUserData.put("new_pass", password.getText().toString());
		}
		
		else{
			newUserData.put("action", "user_create");
		}
		newUserData.put("username", username.getText().toString());
		newUserData.put("password", password.getText().toString());
		newUserData.put("fname", fname.getText().toString());
		newUserData.put("lname", lname.getText().toString());
		newUserData.put("email", email.getText().toString());
		newUserData.put("phone", phone.getText().toString());
		if(username.getText().toString().equals("") || password.getText().toString().equals("")
				|| fname.getText().toString().equals("") || lname.getText().toString().equals("")){
			showDialog(MISSING_DATA_ERROR);
		}
		else if(!phone.getText().toString().equals("") && phone.getText().toString().length() != 10){
			showDialog(PHONE_NUMBER_ERROR);
		}
		else{	
			try {
				JSONObject result = server.send(newUserData);
				String error = result.getString("error");
				if(error.equals("true")){
					if(getIntent().getBooleanExtra("updating", false) && result.getInt("error_code") == 1)
						showDialog(PASSWORD_ERROR);
					else showDialog(NEW_USER_ERROR);
				}
				else{
					if(getIntent().getBooleanExtra("updating", false))
						showDialog(SAVED);
					else if(server.login(username.getText().toString(), password.getText().toString())){
						startActivity(new Intent(this, BillShare.class));
						finish();
					}
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
			.setMessage("Username Taken")
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		               dialog.cancel();
		           }
			})
			.create();
		case MISSING_DATA_ERROR:
			return new AlertDialog.Builder(this)
			.setMessage("Username, password, and name fields required")
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		               dialog.cancel();
		           }
			})
			.create();
		case PHONE_NUMBER_ERROR:
			return new AlertDialog.Builder(this)
			.setMessage("Please enter a 10-digit phone number")
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		               dialog.cancel();
		           }
			})
			.create();
		case SAVED:
			return new AlertDialog.Builder(this)
			.setMessage("      Saved      ")
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		               dialog.cancel();
		               finish();
		           }
			})
			.create();
		case PASSWORD_ERROR:
			return new AlertDialog.Builder(this)
			.setMessage("Incorrect Password")
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		               dialog.cancel();
		               finish();
		           }
			})
			.create();
		default:
			return null;
		}
	}
}