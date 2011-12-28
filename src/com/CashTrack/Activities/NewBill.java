package com.CashTrack.Activities;


import java.sql.Date;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.CashTrack.R;
import com.CashTrack.CustomComponents.BillServer;
import com.CashTrack.CustomComponents.Friend;
import com.CashTrack.CustomComponents.LocalDb;

public class NewBill extends Activity {
	private LocalDb localDb;
	
    private int mSwapState;
    private int edit_debt_id;
    private int userId;
    private boolean dueSet;
    private ArrayList<Integer> selectedFriends;
    private ArrayAdapter<CharSequence> tagOptions;
    private EditText Date;
    private EditText Due;
    private TextView friendsView;
    private EditText amountInput;
    private EditText detailsInput;
    private Button swapButton;
    private Button resetButton;
    private CheckBox paid;
    private RadioButton edit;
    private Spinner tagInput;
    private BillServer server;
    private OnClickListener mClickListener;
    private OnDateSetListener mDateListener;
    private OnDateSetListener mDueListener;
    private OnTouchListener mTouchListener;
    private Date mDate;
    private Date mDueDate;
    
    static final DecimalFormat dollar = new DecimalFormat("#,##0.00");
    static final int DATE_DIALOG = 0;
    static final int DUE_DATE_DIALOG = 1;
    static final int PICK_FRIEND_DIALOG = 2;
    static final int SAVE_DIALOG = 3;
    static final int BILL_SAVE_ERROR_DIALOG = 4;
    static final int NO_FRIENDS_DIALOG = 5;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.new_bill);	
		
		localDb = new LocalDb(this);
		
	    friendsView = (TextView) this.findViewById(R.id.Friend);
	    amountInput = (EditText) this.findViewById(R.id.Amount_Input);
	    Date = (EditText) this.findViewById(R.id.Date_Display);
	    Due = (EditText) this.findViewById(R.id.Due_Date_Display);
	    swapButton = (Button) this.findViewById(R.id.Swap_Button);
	    paid = (CheckBox) this.findViewById(R.id.Paid_Button);
	    edit = (RadioButton) this.findViewById(R.id.Edit_Button);
	    tagInput = (Spinner) this.findViewById(R.id.Tag_Input);
	    detailsInput = (EditText) this.findViewById(R.id.Details_Input);
	    final Button saveButton = (Button) this.findViewById(R.id.Save_Button);
	    resetButton = (Button) this.findViewById(R.id.Reset_Button);
	    tagOptions = ArrayAdapter.createFromResource(
	    		this, R.array.categories, android.R.layout.simple_spinner_item);
	    tagOptions.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    tagInput.setAdapter(tagOptions);
	    server = (BillServer)getApplicationContext();
	    server.setParent(this);
	    userId = server.getUser();
		mDateListener = new OnDateSetListener() {
	    	public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
	    		mDate.setYear(year-1900);
	    		mDate.setMonth(monthOfYear);
	    		mDate.setDate(dayOfMonth);
				update(DATE_DIALOG);
	    	}
		};
		mDueListener = new OnDateSetListener() {
	    	public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
	    		mDueDate.setYear(year-1900);
	    		mDueDate.setMonth(monthOfYear);
	    		mDueDate.setDate(dayOfMonth);
				update(DUE_DATE_DIALOG);
	    	}
		};
		
		mTouchListener = new OnTouchListener(){
			public boolean onTouch(View v, MotionEvent m) {
				if(m.getAction() == 1)
					mClickListener.onClick(v);
				return false;
			}	
		};

	    mClickListener = new OnClickListener() {
	    	public void onClick(View v) {
				switch(v.getId()){
				case R.id.Date_Display:
					showDialog(DATE_DIALOG);
					break;
				case R.id.Due_Date_Display:
					showDialog(DUE_DATE_DIALOG);
					break;
				case R.id.Save_Button:
					save();
					break;
				case R.id.Reset_Button:
					if(edit.isChecked())
						finish();
					else reset();
					break;
				case R.id.Swap_Button:
					if(mSwapState == R.string.owe)
						mSwapState = R.string.owed;
						else mSwapState = R.string.owe;
					swapButton.setText(mSwapState);
					break;
				case R.id.Paid_Button:
					paid.toggle();
					break;
				case R.id.Friend:
					if(!BillServer.friendList.isEmpty())showDialog(PICK_FRIEND_DIALOG);
					else showDialog(NO_FRIENDS_DIALOG);
					break;
				case R.id.Amount_Input:
					if(amountInput.getText().toString().equals(getResources().getString(R.string.emptyAmount)))
						amountInput.setText("");
					break;
				case R.id.Details_Input:
					if(detailsInput.getText().toString().equals(getResources().getString(R.string.defaultDue)))
						detailsInput.setText("");
					break;
				default:
					break;
				}
			}
		};
        Date.setOnClickListener(mClickListener);
        Due.setOnClickListener(mClickListener);
        saveButton.setOnClickListener(mClickListener);
        resetButton.setOnClickListener(mClickListener);
        swapButton.setOnClickListener(mClickListener);
        friendsView.setOnClickListener(mClickListener);
        amountInput.setOnClickListener(mClickListener);
        detailsInput.setOnClickListener(mClickListener);
        Date.setOnTouchListener(mTouchListener);
        Due.setOnTouchListener(mTouchListener);
        amountInput.setOnTouchListener(mTouchListener);
        friendsView.setOnTouchListener(mTouchListener);
        detailsInput.setOnTouchListener(mTouchListener);
        edit_debt_id = getIntent().getIntExtra("EDIT_DEBT_ID", -1);
	    if (edit_debt_id != -1)setFieldsForEdit(edit_debt_id);
	    else {
	    	reset();
	    	if(BillServer.friendList.isEmpty()) showDialog(NO_FRIENDS_DIALOG);
	    }
	}
	@Override
	public void onResume(){
		super.onResume();
		server.setParent(this);
		if(!edit.isChecked())
		update(PICK_FRIEND_DIALOG);
	}
	@Override
	public void onSaveInstanceState(Bundle outState){
		super.onSaveInstanceState(outState);
		outState.putLong("date", mDate.getTime());
		outState.putLong("due", mDueDate.getTime());
		outState.putIntegerArrayList("selectedFriends", selectedFriends);
		outState.putInt("mSwapState", mSwapState);
		outState.putBoolean("dueSet", dueSet);
	}
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState){
		super.onRestoreInstanceState(savedInstanceState);
		mDate.setTime(savedInstanceState.getLong("date"));
		mDueDate.setTime(savedInstanceState.getLong("due"));
		dueSet = savedInstanceState.getBoolean("dueSet");
		if(dueSet) update(DUE_DATE_DIALOG);
		selectedFriends = savedInstanceState.getIntegerArrayList("selectedFriends");
		if(!edit.isChecked())update(PICK_FRIEND_DIALOG);
		mSwapState = savedInstanceState.getInt("mSwapState");
		swapButton.setText(mSwapState);
	}
	
	private void setFieldsForEdit(int debt_id)
	{
		friendsView.setEnabled(false);
    	edit.setChecked(true);
    	selectedFriends = new ArrayList<Integer>(1);
    	resetButton.setText("Cancel");
    	
    	
		//HashMap<String, String> data = new HashMap<String, String>();
		//data.put("action", "debt_details");
		//data.put("debt_id", Integer.toString(debt_id));
		
		try {
			//JSONObject debt_details = server.send(data).getJSONObject("return");
			JSONObject debt_details = localDb.getDebtDetails(debt_id);
			if(debt_details.getInt("paid") == 0)
    			paid.setChecked(false);
    		else paid.setChecked(true);
	    	amountInput.setText(debt_details.getString("amount"));
			detailsInput.setText(debt_details.getString("details"));
			if(detailsInput.getText().toString().equals(""))
				detailsInput.setText(R.string.defaultDue);
			if(debt_details.getInt("lender_id") == userId){
				mSwapState = R.string.owed;
				selectedFriends.add(debt_details.getInt("borrower_id"));
				friendsView.setText(debt_details.getString("borrower_name"));
			}
			else{ 
				mSwapState = R.string.owe;
				selectedFriends.add(debt_details.getInt("lender_id"));
				friendsView.setText(debt_details.getString("lender_name"));
			}
			swapButton.setText(mSwapState);
			tagInput.setSelection(tagOptions.getPosition(debt_details.getString("category")), true);
			mDate = new Date(debt_details.getLong("date")*1000);
			update(DATE_DIALOG);
			mDueDate = new Date(debt_details.getLong("date_due")*1000); 
			if(mDueDate.getTime() != 0) update(DUE_DATE_DIALOG);
			else{
				mDueDate = new Date(debt_details.getLong("date")*1000);
				Due.setText(R.string.defaultDue);
				dueSet = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			finish();
		}
	}
	@Override
	public Dialog onCreateDialog(int id) {
	    switch (id) {
	    case DATE_DIALOG:
	        return  new DatePickerDialog(this, 
	        		mDateListener, 
	        		mDate.getYear()+1900, mDate.getMonth(), mDate.getDate());

	    case DUE_DATE_DIALOG:
	    	return new DatePickerDialog(this, 
	    			mDueListener, 
	    			mDueDate.getYear()+1900, mDueDate.getMonth(), mDueDate.getDate());

	    case PICK_FRIEND_DIALOG:
	    	final boolean[] checked = new boolean[BillServer.friendList.size()];
	    	final String[] names = new String[BillServer.friendList.size()];
	    	final int[] ids = new int[BillServer.friendList.size()];
	    	
	    	for(int x = 0; x< BillServer.friendList.size(); x++){
	    		names[x] = BillServer.friendList.get(x).toString();
	    		ids[x] = BillServer.friendList.get(x).id;
	    		if(selectedFriends.contains(BillServer.friendList.get(x).id)) checked[x] = true;
	    	}
	    	
	    	return new AlertDialog.Builder(this).setTitle("Choose Friends")
	    	.setCancelable(false)
	    	.setMultiChoiceItems(names, checked, new DialogInterface.OnMultiChoiceClickListener(){
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					checked[which] = isChecked;
				}})
	    	.setPositiveButton("Done", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   selectedFriends = new ArrayList<Integer>();
		        	   for(int x=0; x < checked.length; x++){
		        		   if(checked[x]) selectedFriends.add(ids[x]);
		        	   }
		        	   update(PICK_FRIEND_DIALOG);
		               removeDialog(PICK_FRIEND_DIALOG);
		           }
		       })
		       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id){
		        	   removeDialog(PICK_FRIEND_DIALOG);
		           }
		       })
		       .create();

	    case SAVE_DIALOG:
	    	return new AlertDialog.Builder(this)
	    	.setCancelable(false)
	    	.setMessage("      Bill Saved!      ")
	    	.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if(edit.isChecked()) finish();
					else reset();
				}
			})
	    	.create();

	    case BILL_SAVE_ERROR_DIALOG:
	    	return new AlertDialog.Builder(this)
	    	.setMessage("Bill must include at least one other person")
	    	.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
	    	.create();

	    case NO_FRIENDS_DIALOG:
	    	return new AlertDialog.Builder(this)
	    	.setMessage("Add a friend to create a new bills")
	    	.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
	    	.create();
	    default:
	    	return null;
	    }
	}
	private void update(int id) {
		switch(id){
		case DATE_DIALOG:
	        Date.setText(
	            new StringBuilder()
                .append(mDate.getMonth()+1).append("/")
                .append(mDate.getDate()).append("/")
                .append(mDate.getYear()+1900).append(" "));
	        break;
	        
		case DUE_DATE_DIALOG:
			Due.setText(
	            new StringBuilder()
	            .append(mDueDate.getMonth()+1).append("/")
                .append(mDueDate.getDate()).append("/")
                .append(mDueDate.getYear()+1900).append(" "));
			dueSet = true;
			break;
		case PICK_FRIEND_DIALOG:
			Friend tempFriend = null;
			Iterator<Integer> it = selectedFriends.iterator();
			Integer x = null;
			while(it.hasNext()){
				x = it.next();
				tempFriend = new Friend(null, null, null, null, null, x, 0);
				if(!BillServer.friendList.contains(tempFriend))
					it.remove();
			}
			if(selectedFriends.isEmpty()){
				friendsView.setText(R.string.addFriend);
			}
			else if(selectedFriends.size() == 1){
				int loc = BillServer.friendList.indexOf((new Friend(null, null, null, null, null, selectedFriends.get(0), 0)));
				friendsView.setText(BillServer.friendList.get(loc).toString());
			}
			else{
				friendsView.setText(selectedFriends.size()+" Friends Selected");
			}
			break;
		default: 
			break;
		}
	}
	private void save(){
		if(selectedFriends.isEmpty())
			showDialog(BILL_SAVE_ERROR_DIALOG);
		else{
			HashMap<String,String> insert = new HashMap<String, String>();
			if(edit.isChecked()){
				insert.put("action", "debt_edit");
				insert.put("debt_id", Integer.toString(edit_debt_id));
			}
			else insert.put("action", "debt_add"); 
			
			if(paid.isChecked()) 
				insert.put("paid", "1"); 
			else insert.put("paid", "0");
			
			if(!detailsInput.getText().toString().equals(getResources().getString(R.string.defaultDue))
					&& !detailsInput.getText().toString().equals(""))
				insert.put("details", detailsInput.getText().toString()); 
			else
				insert.put("details", "");
			
			insert.put("amount", amountInput.getText().toString());
			insert.put("category", tagInput.getSelectedItem().toString());
			insert.put("date", String.valueOf(mDate.getTime()/1000));
			if(dueSet)
				insert.put("date_due", String.valueOf(mDueDate.getTime()/1000));
			else
				insert.put("date_due", "0");
			
			try{
			if(mSwapState == R.string.owed){
				insert.put("lender_id", String.valueOf(userId));
				for(int x : selectedFriends){
					insert.put("borrower_id", String.valueOf(x));

					if (!localDb.saveDebt(insert))
						return;
					//if (server.send(insert).getString("error").equals("true"))
					//		return;
				}
			}
			else{
				insert.put("borrower_id", String.valueOf(userId));
				for(int x : selectedFriends){
						insert.put("lender_id", String.valueOf(x));

						if (!localDb.saveDebt(insert))
							return;
						//if (server.send(insert).getString("error").equals("true"))
						//		return;
				}
	
			}
			showDialog(SAVE_DIALOG);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	public void reset(){
		edit.setChecked(false);
    	dueSet = false;
    	selectedFriends = new ArrayList<Integer>();
		mSwapState = R.string.owed;
		swapButton.setText(mSwapState);
		Due.setText(R.string.defaultDue);
		detailsInput.setText(R.string.defaultDue);
		amountInput.setText(R.string.emptyAmount);
		friendsView.setText(R.string.addFriend);
		tagInput.setSelection(0);
		paid.setChecked(false);
		Calendar c = Calendar.getInstance();
		mDate = new Date(c.get(Calendar.YEAR)-1900, c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
		mDueDate = new Date(c.get(Calendar.YEAR)-1900, c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
	    update(DATE_DIALOG);
	    this.removeDialog(DATE_DIALOG);
	    this.removeDialog(DUE_DATE_DIALOG);
	}
	
	/* Creates the menu items */
	public boolean onCreateOptionsMenu(Menu menu) {
	    
		MenuItem settings = menu.add(0, 1, 0, "Settings"); 
	    settings.setIcon(android.R.drawable.ic_menu_manage);
		
	    MenuItem logout = menu.add(0, 2, 0, "Logout");
	    logout.setIcon(android.R.drawable.ic_menu_revert);
	   
	    return true;
	}

	/* Handles item selections */
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId())
		{		
			case 1: showSettings();
					return true;
			case 2:  logout();
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
		 server.setKey(null);
		 server.settings_edit.putString("username", null);
		 server.settings_edit.putString("password", null);
		 server.settings_edit.commit();

		 Intent login = new Intent(this, Login.class);
		 startActivity(login);
		 this.finish();
	}
}
