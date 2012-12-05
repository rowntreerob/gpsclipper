package com.b2bpo.media.geophoto;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.google.api.client.auth.oauth2.draft10.AccessTokenResponse;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessTokenRequest.GoogleAuthorizationCodeGrant;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.gdata.data.Entry;
import com.google.gdata.data.geo.impl.GeoRssWhere;
import com.google.gdata.data.photos.PhotoEntry;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * process the gData picasa partial updates for the list of photos in the selected album
 * UI present the verification display showing chosen GPX and chosen photo album
 * Process the OK button and stage the httpClient threads that will do all the POSTS for the partial updates to GeoRssWhere
 * UI present the done Updates display with photo counts
 * @author rob
 *
 */
public class UpdateActivity extends Activity {
	private static String TAG = "UpdateActivity";

	private static ProgressDialog progressDialog;
	private Activity  _act = this;

	private static int ctrU = 0;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_confirm_update);
		//TODO on pause in middle of update may screw up this counter that is key to 
		//registering threads & updates completing 
		ctrU = 0;
		TextView text = (TextView) findViewById(R.id.gpxfilename);
		
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
				return;
				}
		String gpx = extras.getString("com.b2bpo.media.gpx");
		if (gpx != null)
		text.setText(gpx);
		String album = extras.getString("com.b2bpo.media.album");
		TextView t2 = (TextView) findViewById(R.id.albumtitle);
		if (album !=null)
			t2.setText(album);
		
		StringBuffer sb = new StringBuffer();
		long ml = extras.getLong("com.b2bpo.media.gpx.mintime");
		sb.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(ml));
		ml = extras.getLong("com.b2bpo.media.gpx.maxtime");
		sb.append(" : ");
		sb.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(ml));
		TextView t3 = (TextView) findViewById(R.id.gpxtvalues);
		if (sb.length() > 0)  t3.setText(sb.toString());
		
		sb.setLength(0);
		ml = extras.getLong("com.b2bpo.media.photo.mintime");
		sb.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(ml));
		ml = extras.getLong("com.b2bpo.media.photo.maxtime");
		sb.append(" : ");
		sb.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(ml));
		TextView t4 = (TextView) findViewById(R.id.phototvalues);
		if (sb.length() > 0)  t4.setText(sb.toString());						
		
        Button button = (Button) findViewById(R.id.okbutton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	// limit dialog until nbr of updates = nbr of photos in album

            	progressDialog = ProgressDialog.show(
            	        _act,
            	        _act.getString(R.string.processing_title),
            	        _act.getString(R.string.processing_msg),
            	        true);           	        
            	// call update on every photo using set of GPX data that will be iterated to find proper point based on timestamp
            	for (Photoitem photoitem : DataP.getPhotoItems()) {
            		matchedGPX(photoitem, DataG.getGeoitems());
            	}
            	// cleanup resources for connPool used on http for updates :: belo causes bug
            	//MyConnectionManager.getInstance().shutdown();
            }
        });
        
        Button cbutton = (Button) findViewById(R.id.cancelbutton);
        cbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	//bugfix
 
     	        Intent intent = new Intent(ClientCustomSSL.ACTION_ALBUMLIST);
     	        intent.setComponent(new ComponentName(getApplicationContext(),AlbumListActivity.class));
     	        intent.addCategory(Intent.CATEGORY_DEFAULT);
     	        startActivity(intent);
            }
        });
        
        Button hbutton = (Button) findViewById(R.id.helpbutton);
        hbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	
     	        Intent intent = new Intent("HelpActivity");
     	        intent.setComponent(new ComponentName(getApplicationContext(),HelpActivity.class));
     	        intent.addCategory(Intent.CATEGORY_DEFAULT);
     	        startActivity(intent);           	
            }
        });
    }
    /**
     * 
     * @param photodata is item from the list of photos in DataP class     * 
     * @param geoitems full trace of GPS data in list that gets iterated for each photo
     */
   
    private  void matchedGPX(Photoitem photodata, ArrayList<Gpxitem> geoitems){
    	// spin thru all geo timestamps looking for the first one that is greater time than photo time
    	// that gpx value for Lat Long will be used in the partial gdata update called on the photo
    	// which of the 2 geo timestamps is closest to the photo's time stamp
    	// start w/ hold time from 1970 '2731420000'
    	Gpxitem holdG = new Gpxitem();
    	holdG.setTime(Long.parseLong("2731420000"));
    	for (Gpxitem geoitem : geoitems) {
    		if(photodata.getTime() <= geoitem.getTime()){
    			// you have gotten very close to the correct time
    			// find the closer match among the 2 available gpx items with timestamp values to either side of the photo time
    			int rc = update(photodata,geoitem = ((photodata.getTime() - holdG.getTime())  < (geoitem.getTime() - photodata.getTime())) ? holdG : geoitem);    			    	
    			break; // once the appropriate geoItem has been found , abandon the loop
    		} else {
    			holdG = geoitem; // last processed gpx 
    		}
    	}
    }
 
    /*
     * args are the update transaction data defining the photo being updated and the GPS point that will be applied to the photo
     * photo - target of update
     * gpx - data being applied to selected photo
     */
    public  int update(Photoitem photo, Gpxitem gpx){
    	//GData implem class for the Geo  is PhotoEntry, container for latitude/ longitude being applied to the photo
    	PhotoEntry entry = new PhotoEntry(new Entry());
    	if (Trace.debug) Log.d(TAG, "upd link geo " +photo.getEditlink() +" " +gpx.getLatLong());
    	String[] words = gpx.latLong.split("\\s+");
    	//lat is first word, long is second word
        entry.setGeoRssWhere(new GeoRssWhere(Double.parseDouble(words[0]), Double.parseDouble(words[1])));        
	    //thread message handler for runnables that convey the POST updates
        // handler for async responses from the threadpool actually doing the httpClient work on the updates
		   Handler handler = new Handler() {
		       public void handleMessage(Message message) {
		         switch (message.what) {
		         case HttpConnection.DID_START: {
		           Log.d(TAG, "Starting connection...");
		           break;
		         }
		         case HttpConnection.DID_SUCCEED: {
		        	 //message is byte[]
		            byte[] response = (byte[]) message.obj;
		            ctrU++;
		 		   if (ctrU == DataP.getPhotoItems().size()) {
					   finished();
				   }
		           break;
		         }
		         case HttpConnection.DID_ERROR: {
		        	 ctrU++;
		           Exception e = (Exception) message.obj;
		           e.printStackTrace();
		           Log.d(TAG, "Connection failed.");
		           break;
		         }
		       }
		     }
		   };
		   // get new runnable and post it to the Que as type=POST
		   new HttpConnectionPost(handler, entry)
		     .post(photo.getEditlink());	
	   return -1;            	
    }            
    /**
     * update work is done. 
     * refresh the UI
     * Use a thread off the UI thread to do belo
     * Cleanup Network resources from the pool that processed the updates
     */
    public  void finished(){
		   
		   ExecutorService executor = Executors.newCachedThreadPool();
	        Future<?> future = executor.submit(
	            new Runnable() {
	                public void run() {
	         		   MyConnectionManager.getInstance().shutdown();
	                }
	            }
	        );
	        try {
				future.get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        if(Trace.debug)Log.d(TAG, "Http ConnectionMGR Shutdown finished!");
	        executor.shutdown();
		   
		   progressDialog.dismiss();
		   setContentView(R.layout.layout_confirm_update);
		   if (Trace.debug) Log.d(TAG, "finished updates, shutdown network conns");
		   
	       TextView result = (TextView) findViewById(R.id.updmsg);
	       CharSequence c1 = getText(R.string.processing_complete);
	       CharSequence c2 = " " +DataP.getPhotoItems().size() +" photos.";
	       result.setText(TextUtils.concat(c1,c2) );
	       result.invalidate();
    }    
}

