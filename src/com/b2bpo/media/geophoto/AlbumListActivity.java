package com.b2bpo.media.geophoto;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import ch.boye.httpclientandroidlib.client.methods.HttpGet;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.util.Log;
import android.view.View;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;



/**
 * start this activity once the Oauth callback is done
 * and once gdata api has been called in another thread ( picasaService.process() ) 
 * call gData to get the list of picasa Albums that belong to the phone's gmail account used in oAuth
 * @author rob
 *
 */
public class AlbumListActivity extends Activity {
		private static String TAG = "AlbumListActivity";
		private static int SELECT_GPX = 1001;
		private Context myctx;
//		private static ProgressDialog progressDialog;
		private  Activity _activity = this;
		private DataG _dataG = new DataG();
        private String _albumName;
	    ListView list;
	    LazyAdapter adapter;
	    ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();
	    
	    static final String KEY_SONG = "song"; // parent node
	    static final String KEY_ID = "id";
	    static final String KEY_NAME = "name";
	    static final String KEY_TIMESTAMP = "timestamp";
	    static final String KEY_NUMPHOTOS = "numphotos";
	    static final String KEY_THUMBNAIL = "thumbnail";
	   @Override
	    public void onCreate(Bundle savedInstanceState)  {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.main);
	        myctx = this.getApplicationContext();

	        downloadGdataAlbums(); // gets data, refreshes UI	        
        	
//	        list=(ListView)findViewById(R.id.list);		  
	     }
	   
	   /**
	    * GPX File Chooser Dialog completes and calls this
	    * All data has been selected
	    * Call the Intent to review the selections - ACTION_CONFIRM
	    * store in intent extra the following:
	    *   GPX fileName
	    *   Picasa/ google+ album name
	    *   gpx Date range Hi/Lo
	    *   Album photo timestamp Hi/Lo
	    */  
	   @Override
	   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		   
		   
	      if(requestCode == SELECT_GPX){	   
		     if(resultCode==RESULT_OK){
		    	 //reset the array used in the photo update
		    	 _dataG.geoitems.clear();
				StringPullHandler handler = new StringPullHandler() {
	                @Override
	                public void endElement2(String uri, String localName,
	                        String qName) {                	                   
	          		  if(qName.equals("trkpt")) {
	      		    	try {
	      					_dataG.addGeoitem(_dataG.getGeoitem());	      					
	      				} catch (InvalidParameterException e) {
	      					// TODO Auto-generated catch block
	      					e.printStackTrace();
	      				} 
	      			  } else  if(qName.equals("time") ) { 
			      			_dataG.setTime(getCharacters());		 	    	    		    		    	
			      		  }
	                }
	                @Override 
	            	protected void startElement2(String uri, String localName, String qName,
	            			Attributes attributes) throws SAXException {
		           		 if(qName.equals("trkpt")) { 		           	      
		           	      _dataG.setLat(attributes.getValue("lat"));
		           	      _dataG.setLon(attributes.getValue("lon")); 
		           	    } 
	            	}
	            };
		    	 
//			      ClientCustomSSL.getInstance().setGpxfilepath(data.getData().getPath());
			      if (Trace.debug) Log.d(TAG, "activityResult gpx b4 parse " +data.getData().getPath());
			      // path to GPX file selected in the chooser
//			      File myfil = new File(ClientCustomSSL.getInstance().getGpxfilepath());
			      File myfil = new File(data.getData().getPath());
			      int rc = new XmlReader()._parseXml(handler, myfil);
			    	//log the gpx data below
			      long GtimeMax = 0;
			      long GtimeMin = Long.parseLong("9999999999999");
				  for (Gpxitem item : DataG.getGeoitems()) {
					  GtimeMin = (GtimeMin > item.time) ? item.time : GtimeMin;
					  GtimeMax = (GtimeMax < item.time) ? item.time : GtimeMax;
			          if (Trace.debug) Log.d(TAG, "ArGEntry is: " + item.time +" " +item.latLong);
			      }
			      long PtimeMax = 0;
			      long PtimeMin = Long.parseLong("9999999999999");
				  for (Photoitem item : DataP.getPhotoItems()) {
					  PtimeMin = (PtimeMin > item.time) ? item.time : PtimeMin;
					  PtimeMax = (PtimeMax < item.time) ? item.time : PtimeMax;
			      }
				  
		  	      Intent intent = new Intent(ClientCustomSSL.ACTION_CONFIRM);
		  	      intent.setComponent(new ComponentName(getApplicationContext(), UpdateActivity.class));
		  	      intent.addCategory(Intent.CATEGORY_DEFAULT);
		  	      if (Trace.debug) Log.d(TAG, "activityResult intent xtra gpx " +data.getData().getPath());
		  	      intent.putExtra("com.b2bpo.media.gpx", data.getData().getPath());
		  	      //bug: 
		  	      //store the albumName locally
		  	      if (Trace.debug) Log.d(TAG, "activityResult intent xtra photo " +_albumName);
		  	      intent.putExtra("com.b2bpo.media.album", _albumName);
		  	      intent.putExtra("com.b2bpo.media.photo.mintime", PtimeMin);
		  	      intent.putExtra("com.b2bpo.media.photo.maxtime", PtimeMax);
		  	      intent.putExtra("com.b2bpo.media.gpx.mintime", GtimeMin);
		  	      intent.putExtra("com.b2bpo.media.gpx.maxtime", GtimeMax);
		  	      startActivity(intent);
		     }	    	     
	      }
	   }
	   
	   public void downloadGdataAlbums() {
		   String text;
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
		            JsonNode node;		            
					try {
						node = new ObjectMapper().readValue(new ByteArrayInputStream(response), JsonNode.class);
						mylist = ClientCustomSSL.getList(node);
					} catch (JsonParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JsonMappingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			          
	//		           refresh the UI with the list
					
			        list=(ListView)findViewById(R.id.list);			        
				    adapter=new LazyAdapter(_activity, mylist);
				    list.setAdapter(adapter);			 			        
			        list.setOnItemClickListener(new OnItemClickListener() {
			        	 


					@Override
		            public void onItemClick(AdapterView<?> parent, View view,
		                    int position, long id) {
//		            	 super.onItemClick(parent, view, position, id);
		            	if(Trace.debug)Log.d(TAG, "AlbmSelect pos " +position);
//		            	HashMap<String, String> map = mylist.get(position);
		            	_albumName = mylist.get(position).get("name");
		            	
		            	
		            	getAlbumXml(mylist.get(position).get("id"));
		            	
//		            	ClientCustomSSL.getInstance().setAlbumMap(map);
//		            	PicasaAlbumService.startAlbumFeed(myctx); 
	                    Intent intent = new Intent();
	                    intent.setType("*/*");
	                
	                    intent.setAction(Intent.ACTION_GET_CONTENT);
	                    startActivityForResult(Intent.createChooser(intent,
	                            "Select GPX file"), SELECT_GPX);	 
		            }
			        });	
		           break;
		         }
		         case HttpConnection.DID_ERROR: {
		           Exception e = (Exception) message.obj;
		           e.printStackTrace();
		           Log.d(TAG, "Connection failed.");
		           break;
		         }
		       }
		     }
		   };
		   StringBuffer sbf = new StringBuffer();
	    	sbf.append("https://picasaweb.google.com"); //domain
	    	sbf.append("/data/feed/api/user/default"); //path
	    	sbf.append("?fields=entry%2Ftitle%2Centry%2Flink%5B%40rel%3D%22http%3A%2F%2Fschemas.google.com%2Fg%2F2005%23feed%22%5D%2Centry%2Fgphoto%3Anumphotos%2Centry%2Fgphoto%3Atimestamp%2Centry%2Fmedia%3Agroup%2Fmedia%3Athumbnail%5B%40url%5D");
	    	sbf.append("&start-index=1&max-results=15&alt=json"); // max 10 entries in json		   
		   new HttpConnection(handler)
		     .get(sbf.toString());
		 }
	   
	   public void getAlbumXml(String albumid){
		   
		   String text;
		   Handler handler = new Handler() {
		       public void handleMessage(Message message) {
		         switch (message.what) {
		         case HttpConnection.DID_START: {
		           Log.d(TAG, "Starting connection...");
		           break;
		         }
		         case HttpConnection.DID_SUCCEED: {
	        	   byte[] response = (byte[]) message.obj;
	        	   ClientCustomSSL.getInstance().getDataP(new ByteArrayInputStream(response));
	            	if(Trace.debug) { 
	        			for (Photoitem item : DataP.getPhotoItems()) {
	        	              Log.d(TAG, "ArPEntry: "
	        	                                 +item.editlink);
	        	        }
	            	}
		           break;
		         }
		         case HttpConnection.DID_ERROR: {
		           Exception e = (Exception) message.obj;
		           e.printStackTrace();
		           Log.d(TAG, "Connection failed.");
		           break;
		         }
		       }
		     }
		   };
	    	StringBuffer sbuff = new StringBuffer();

	    	sbuff.append(albumid);
	    	sbuff.append("?fields=entry%2Flink%5B%40rel%3D%22edit%22%5D%2Centry%2Fgphoto%3Atimestamp");	   
		   new HttpConnection(handler)
		     .get(sbuff.toString());
		   
	   }
}
