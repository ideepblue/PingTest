package com.pingtest;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class TableActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_table);
		
		DatabaseOperator dbo = new DatabaseOperator(this);
		
		TableRow.LayoutParams params = new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        TableLayout table = (TableLayout) findViewById(R.id.table);
        
        TableRow row = new TableRow(this);
        TextView text;
        
        text = new TextView(this);
        text.setText("id");
        row.addView(text);
        
        text = new TextView(this);
        text.setText("time");
        row.addView(text);
        
        text = new TextView(this);
        text.setText("status");
        row.addView(text);      
       
        
        List<CellInfo> infos = dbo.queryAll();
        for (int i = 0; i < infos.size(); i++) {
        	row = new TableRow(this);
        	
        	text = new TextView(this);
            text.setText(infos.get(i).id + " ");
            row.addView(text);
            
            text = new TextView(this);
            text.setText(infos.get(i).fTimestamp+ " ");
            row.addView(text);
            
            text = new TextView(this);
            text.setText(infos.get(i).status);
            row.addView(text);            
            
            table.addView(row);
        }
	}
}
