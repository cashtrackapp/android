package com.CashTrack.CustomComponents;

import java.sql.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import com.CashTrack.R;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class AlertAdapter extends ArrayAdapter<String> {
	Activity context;
	JSONArray alerts;
	int userid;

	public AlertAdapter(Activity context, JSONArray alerts, int userid) {
		super(context, R.layout.list_alert_item, new String[alerts.length()]);
		this.context = context;
		this.alerts = alerts;
		this.userid = userid;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = null;
		
		try {
			LayoutInflater inflater = context.getLayoutInflater();
			JSONObject cur_alert = this.alerts.getJSONObject(position);
			
			row = inflater.inflate(R.layout.list_alert_item, null);
			
			TextView subject = (TextView) row.findViewById(R.id.AlertSubject);
			subject.setText(cur_alert.getString("subject"));

			TextView date = (TextView) row.findViewById(R.id.AlertDate);
			Date format_date = new Date(cur_alert.getLong("date_created")*1000);
			date.setText(format_date.toString());
			
			if (cur_alert.getInt("viewed") == 1) {
				row.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.debt_item_paid));
			}
			
		}
		catch (Exception e) {
			Log.e(this.getClass().getSimpleName(), e.toString());
			e.printStackTrace();
		}
		
		return row;	
	}
}
