package com.CashTrack.Activities;

import java.text.DecimalFormat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.CashTrack.CustomComponents.*;
import com.CashTrack.R;

public class Balance extends Activity {
	
	private BillServer server;
	private JSONArray all_debts;
	private LocalDb localDb;
	
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);

        this.setContentView(R.layout.balance);
        
        server = (BillServer) this.getApplication();
        
        localDb = new LocalDb(this);
        
        refresh(Constants.SERVER);
	}
	
	public void onResume() {
		super.onResume();
		server.setParent(this);
		refresh(Constants.LOCAL);
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
			case 1:	refresh(Constants.SERVER);
					return true;
			case 2:	showSettings();
					return true;
			case 3: logout();
				    return true;
			default:return super.onOptionsItemSelected(item);
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
		 server.settings_edit.putString(Constants.USERNAME, null);
		 server.settings_edit.putString(Constants.PASSWORD, null);
		 server.settings_edit.commit();

		 Intent login = new Intent(this, Login.class);
		 startActivity(login);
		 this.finish();
	}
	
	private void refresh(int type) {
		
		try {
			this.all_debts = localDb.getAllDebts(type);
		
			setBalanceValues(this.server.getUser());
					
			ListView list = (ListView) this.findViewById(R.id.ListOfItems);	
			list.setAdapter(new DebtAdapter(this, this.all_debts, 
											new String[this.all_debts.length()],
											this.server.getUser()));
			registerForContextMenu(list);
		
		} catch (JSONException e) {
			
			reset();
			
			Log.e(this.getClass().getSimpleName(), e.toString());
			e.printStackTrace();
		} catch (Exception e) {
			Log.e(this.getClass().getSimpleName(), e.toString());
			e.printStackTrace();
		}
	}
	
	private void reset()
	{
		TextView overallBalanceText = (TextView) this.findViewById(R.id.OverallBalance);
        TextView positiveBalanceText = (TextView) this.findViewById(R.id.Positives);
        TextView negativeBalanceText = (TextView) this.findViewById(R.id.Negatives);
		
		overallBalanceText.setText("$0.00");
		positiveBalanceText.setText("$0.00");
		negativeBalanceText.setText("$0.00");
		
		ListView list = (ListView) this.findViewById(R.id.ListOfItems);
		list.setAdapter(null);
		
		return;
	}

	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		try {
			if(all_debts.getJSONObject(((AdapterContextMenuInfo)menuInfo).position).
					getInt(Constants.VAR_PAID) == 0)
			menu.add(0, 0, 0, "Mark as paid");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		menu.add(0, 1, 0, "Edit");
		menu.add(0, 2, 0,  "Delete");
		menu.add(0, 3, 0, "Cancel");
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

		switch (item.getItemId()) {
		case 0:
			markDebtAsPaid((int) info.id);
			return true;
		case 1:
			editDebt((int) info.id);
			return true;
		case 2:
			deleteDebt((int) info.id);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private void editDebt(int index) {	
		try {
			Intent edit_bill = new Intent(this, NewBill.class);

			edit_bill.putExtra("EDIT_DEBT_ID", this.all_debts.getJSONObject(index).getInt(Constants.VAR_DEBT_ID));
			
			this.startActivity(edit_bill);			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void deleteDebt(final int index) {
		AlertDialog.Builder deleteDialog = new AlertDialog.Builder(this);
		deleteDialog.setMessage("Are you sure you want to delete this debt?")
		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
					
				try {
					localDb.deleteDebt(all_debts.getJSONObject(index).getString(Constants.VAR_DEBT_ID));
					refresh(Constants.LOCAL);
					
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

	private void markDebtAsPaid(int index) {
		try {
			localDb.markDebtAsPaid(this.all_debts.getJSONObject(index).getString(Constants.VAR_DEBT_ID));
			refresh(Constants.LOCAL);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return;
	}

	private void setBalanceValues(int user_id) throws Exception
	{
		TextView overallBalanceText = (TextView) this.findViewById(R.id.OverallBalance);
        TextView positiveBalanceText = (TextView) this.findViewById(R.id.Positives);
        TextView negativeBalanceText = (TextView) this.findViewById(R.id.Negatives);
        
        double overall_balance = 0.00;
		double positive_balance = 0.00;
		double negative_balance = 0.00;
	
		JSONObject debt;
		
		for (int i=0; i<this.all_debts.length(); ++i)
		{
			debt = this.all_debts.getJSONObject(i);
			
			if (debt.getInt(Constants.VAR_PAID) == 0)
			{
				if (user_id == debt.getInt(Constants.VAR_LENDER_ID))
				{
					positive_balance += debt.getDouble(Constants.VAR_AMOUNT);
					overall_balance += debt.getDouble(Constants.VAR_AMOUNT);
				}
				else
				{
					negative_balance += debt.getDouble(Constants.VAR_AMOUNT);
					overall_balance -= debt.getDouble(Constants.VAR_AMOUNT);
				}
			}
		}
		
		DecimalFormat precision = new DecimalFormat("#,##0.00");
		
		if(overall_balance > 0)
			overallBalanceText.setTextColor(getResources().getColor(R.color.lime_green));
		else if(overall_balance < 0)
			overallBalanceText.setTextColor(getResources().getColor(R.color.solid_red));
		
		overallBalanceText.setText("$" + precision.format(overall_balance));
		positiveBalanceText.setText("$" + precision.format(positive_balance));
		negativeBalanceText.setText("$" + precision.format(negative_balance));
	}
}
