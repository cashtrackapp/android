package com.CashTrack.Activities;

import java.util.HashMap;

import org.json.JSONException;

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
import android.widget.Spinner;

import com.CashTrack.R;
import com.CashTrack.CustomComponents.BillServer;

public class FriendSearch extends Activity {

	static final int SEARCH_ERROR_DIALOG = 0;
	static final int CREATE_ERROR_DIALOG = 1;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        this.setContentView(R.layout.friend_search); 
        Button searchButton = (Button) findViewById(R.id.SearchButton);
        searchButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				onSearchClick();
			}
        });
        Button localUser = (Button) findViewById(R.id.local_user_button);
        localUser.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				onCreateClick();
			}
        });
	}
	
	// get email address and prepare to search
	public void onSearchClick()
	{
		EditText searchField = (EditText) findViewById(R.id.search_input);
		Spinner searchby = (Spinner) findViewById(R.id.searchby_spinner);
        String search = searchField.getText().toString();
		if(search.equals(""))
			showDialog(SEARCH_ERROR_DIALOG);
		else{
			        
	        // send to results page
	        Intent searchResults = new Intent(this, SearchResults.class);
	        searchResults.putExtra("SEARCH", search);
	        searchResults.putExtra("SEARCHBY", searchby.getSelectedItemPosition());
	        this.startActivity(searchResults);
	        this.finish();
		}
	}
	
	public void onCreateClick()
	{
		EditText fname = (EditText) findViewById(R.id.local_fname);
		EditText lname = (EditText) findViewById(R.id.local_lname);

		if(fname.getText().equals("") || lname.getText().equals(""))
			showDialog(SEARCH_ERROR_DIALOG);
		
		else{	        
	        BillServer server = ((BillServer)getApplication());
	        server.setParent(this);
	        HashMap<String, String> data = new HashMap<String, String>();
	        data.put("action", "user_create_local");
	        data.put("fname", fname.getText().toString());
	        data.put("lname", lname.getText().toString());
	        
	        try {
				server.send(data).getJSONObject("error");
			} catch (JSONException e) {
				e.printStackTrace();
			}
	        this.finish();
		}
	}
	
	@Override
	public Dialog onCreateDialog(int id){
		switch(id){
		case SEARCH_ERROR_DIALOG:
	    	return new AlertDialog.Builder(this)
	    	.setMessage("Please enter search criteria")
	    	.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			})
	    	.create();
		case CREATE_ERROR_DIALOG:
	    	return new AlertDialog.Builder(this)
	    	.setMessage("Please fill in name fields")
	    	.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			})
	    	.create();
		default:
			return null;
		}
	}
	
}
