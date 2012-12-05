package com.b2bpo.media.geophoto;


import android.app.Activity;
import android.os.Bundle;

import android.widget.TextView;
import android.widget.Toast;


public class HelpActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_help);
				
		TextView textview = (TextView) findViewById(R.id.helpView1);
		TextView textvw2 = (TextView) findViewById(R.id.helpView2);
		TextView textvw3 = (TextView) findViewById(R.id.helpView3);
		TextView textvw4 = (TextView) findViewById(R.id.helpView4);
		TextView textvw5 = (TextView) findViewById(R.id.helpView5);
		TextView textvw6 = (TextView) findViewById(R.id.helpView6);
		TextView textvw7 = (TextView) findViewById(R.id.helpView7);


		textview.setText(getText(R.string.helpn1));
		textvw2.setText(getText(R.string.helpn2));
		textvw3.setText(getText(R.string.helpn3));
		textvw4.setText(getText(R.string.helpn4));
		textvw5.setText(getText(R.string.helpn5));
		textvw6.setText(getText(R.string.helpn6));
		textvw7.setText(getText(R.string.helpn7));
		
    }
    
    private void message(){
    	Toast.makeText(this,"Finished updates on  " +DataP.getPhotoItems().size() +" photos" ,Toast.LENGTH_SHORT).show(
    	);
    }
}
