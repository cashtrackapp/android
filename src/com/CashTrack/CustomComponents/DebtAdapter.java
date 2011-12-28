package com.CashTrack.CustomComponents;

import java.sql.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.CashTrack.R;

public class DebtAdapter extends ArrayAdapter<String> {
	Activity context;
	//JSONObject debts;
	JSONArray debts;
	String[] keys;
	int userid;

	public DebtAdapter(Activity context, JSONArray debts, String[] keys, int userid) {
		super(context, R.layout.list_debt_item, keys);
		this.context = context;
		this.debts = debts;
		this.keys = keys;
		this.userid = userid;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View row = null;
		
		try {
			//row = convertView;

			LayoutInflater inflater = context.getLayoutInflater();
			JSONObject cur_debt = this.debts.getJSONObject(position);
			
			if (cur_debt.getInt("paid") == 0)
			{
				row = inflater.inflate(R.layout.list_debt_item, null);
				
				TextView name = (TextView)row.findViewById(R.id.Debt_Name);
				
				TextView amount_pos = (TextView)row.findViewById(R.id.Debt_Amount_Pos);
				TextView amount_neg = (TextView)row.findViewById(R.id.Debt_Amount_Neg);
				
				if (cur_debt.getInt("borrower_id") == userid)
				{
					amount_pos.setText("");
					amount_neg.setText("$" + cur_debt.getString("amount"));
					name.setText(cur_debt.getString("lender_name"));
				}
				else
				{
					amount_pos.setText("$" + cur_debt.getString("amount"));
					amount_neg.setText("");
					name.setText(cur_debt.getString("borrower_name"));
				}
			
				TextView tag = (TextView)row.findViewById(R.id.Debt_Tag);
				tag.setText(cur_debt.getString("category"));
			
				TextView date = (TextView)row.findViewById(R.id.Debt_Date);
				Date format_date = new Date(cur_debt.getLong("date")*1000);
				date.setText(format_date.toString());
			}
			else
			{
				row = inflater.inflate(R.layout.list_debt_item_paid, null);
				
				TextView name = (TextView)row.findViewById(R.id.Debt_Name_Paid);
				
				TextView amount_pos = (TextView)row.findViewById(R.id.Debt_Amount_Pos_Paid);
				TextView amount_neg = (TextView)row.findViewById(R.id.Debt_Amount_Neg_Paid);
				
				if (cur_debt.getInt("borrower_id") == userid)
				{
					amount_pos.setText("");
					amount_neg.setText("$" + cur_debt.getString("amount"));
					name.setText(cur_debt.getString("lender_name"));
				}
				else
				{
					amount_pos.setText("$" + cur_debt.getString("amount"));
					amount_neg.setText("");
					name.setText(cur_debt.getString("borrower_name"));
				}
			
				TextView tag = (TextView)row.findViewById(R.id.Debt_Tag_Paid);
				tag.setText(cur_debt.getString("category"));
			
				TextView date = (TextView)row.findViewById(R.id.Debt_Date_Paid);
				Date format_date = new Date(cur_debt.getLong("date")*1000);
				date.setText(format_date.toString());
			}
			
		} catch (Exception e) {
			Log.e(this.getClass().getSimpleName(), e.toString());
			e.printStackTrace();
		}
		
		return(row);
	}
}