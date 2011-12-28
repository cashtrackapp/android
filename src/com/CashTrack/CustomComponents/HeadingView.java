package com.CashTrack.CustomComponents;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class HeadingView extends RelativeLayout {

	public HeadingView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		createViews(context);
	}
	
	public HeadingView(Context context, AttributeSet attrs ) {
		super(context, attrs);
		createViews(context);
	}
	
	public HeadingView(Context context) {
		super(context);
		createViews(context);
	}
	
	public void createViews(Context context)
	{
		TextView heading = new TextView(context);
		
		LayoutParams params = new LayoutParams(0, 0);
		params.width = LayoutParams.WRAP_CONTENT;
		params.height = LayoutParams.WRAP_CONTENT;

		heading.setLayoutParams(params);
		heading.setTextSize(25);
		heading.setGravity(CENTER_HORIZONTAL);
		heading.setText("Chris Home");
		heading.setEnabled(true);
		heading.setId(NO_ID);
	}
}
/*
<RelativeLayout
 
 android:id="@+id/RelativeLayout02"
 android:layout_height="wrap_content"
 style="@style/Headings_Relative_Layout"
 android:layout_width="fill_parent">
	
	<TextView
	  android:id="@+id/TextView01"
	  android:text="Home"
	  android:layout_width="wrap_content"
	  android:layout_height="wrap_content"
	  style="@style/Headings_Text">
	</TextView>
	

</RelativeLayout>
*/