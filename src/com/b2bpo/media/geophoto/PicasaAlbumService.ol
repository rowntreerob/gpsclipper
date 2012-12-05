package com.b2bpo.media.geophoto;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.codehaus.jackson.JsonNode;
//import com.b2bpo.media.Fileprocess;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;

public class PicasaAlbumService extends IntentService{

	private static final String TAG="PicasaAlbumService";

	private static Intent intent=null;
	
	
// above go to App.nextprocess at end of current service process
	public PicasaAlbumService() {
	      super("PicasaAlbumService");
	  }
	
	/**
	 * use the handle to figure out what type of action is needed
	 */
    @Override
    protected void onHandleIntent(final Intent intent) {
    	    	
        if (Trace.debug) Log.d(TAG, "Starting PHOTOService.onHandleIntent " +intent.getAction());
            
       //TODO somebody needs to set action 
        if(intent.getAction().equalsIgnoreCase(ACTION_ALBUMLIST2)){
        	 process(intent);
        }
        else if(intent.getAction().equalsIgnoreCase(ACTION_ALBUMFEED)){
        	 process2(intent);        
        } else if (intent.getAction().equalsIgnoreCase(ACTION_UPDATE)){
       	 process4(intent);
       }        
    }
    
    @Override
     public void onDestroy(){
    	if (Trace.debug) Log.d(TAG, " in onDestroy ");    	
    }
    
    /**
     * From the UI selectionList - myAlbums . Get the photoFeed for 
     * Selected Album. Its network GData calls and needs to happen 
     * OFF the main UI thread.
     * @param context
     */
    public static final void startAlbumFeed(final Context context) {    	    	
        intent = new Intent(ACTION_ALBUMFEED, null, context, PicasaAlbumService.class);        
        context.startService(intent);
    }   
 
    /**
     * App first data retreived after oauth dialog is get the list of Albums
     * that could be target for the updates.
     * @param context
     */
    public static final void startAlbumList(final Context context) {
        intent = new Intent(ACTION_ALBUMLIST2, null, context, PicasaAlbumService.class);
        context.startService(intent);        
    }

    /**
     * from UI event on OK button in the UpdateActivity
     * This is the main update action in the app.
     * It will process the GPX data and update the geotags on the photos
     * @param context
     */
    public static final void startAlbumUpdate(final Context context) {
    	    	
        intent = new Intent(ACTION_UPDATE, null, context, PicasaAlbumService.class);
        context.startService(intent);        
    }
    
    /**
     * calls ClientCustomSSL.getAlbums() to return a JSON Blob
     * Blob inserts to a parcel that gets sorted out in AlbumListActivity Receiver
     * Using intent to be started next ,
     * return the parcel/json blob containing the list of albums to the UI thread
     * @param intent
     * @throws IllegalStateException
     */
//bug below on first call - getAlbums not returning an mnode
// if the mnode is null then this will fail instead of just showing an empty list message
// the constructor for the parcel causes NPE when it should just go on and show an  empty list of albums
    private  void process(Intent arg) throws IllegalStateException{
    	ClientCustomSSL.getInstance().registerHttpClient();// prepare libs and client for https usage in threads
    	JsonNode mnode = ClientCustomSSL.getInstance().getAlbums(); 
    	AlbumsParcel parcel = new AlbumsParcel(mnode.toString());
        Bundle b = new Bundle();
        b.putParcelable("com.b2bpo.media.albums", parcel);
//TODO Broadcast the intent
        Intent intent = new Intent(PicasaAlbumService.ACTION_ALBUMLIST);  //sync w/receiver in albumListActivity
        intent.setComponent(new ComponentName(getApplicationContext(),AlbumListActivity.class));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtras(b);
        if (Trace.debug)Log.d(TAG, "process ended " +parcel.toString().substring(0, 35));
//        sendBroadcast(intent);
        startActivity(intent);
    }
    /**
     * Use the ID from the item chosen from SelectionLIst in albumList
     * getAlbumXML is heavy bandwidth data call that gets all the photos in the album,
     * parsing the xml for the what fields will be needed to update all the photos
     * by calling GData Partial updates on every photo in the album
     * This just GETS data to the DataP class static field so it can b used elsewhere
     * @param intent
     * @throws IllegalStateException
     */
    private  void process2(Intent intent) throws IllegalStateException{
    	// get the gdata/picasa feedlink for the album that was selected from the singleton
    	ClientCustomSSL client = ClientCustomSSL.getInstance();
    	String feedUri = client.getAlbumMap().get("id");
    	client.getDataP(client.getAlbumXml(feedUri));
    	if(Trace.debug) { 
			for (Photoitem item : DataP.getPhotoItems()) {
	              Log.d(TAG, "ArPEntry: "
	                                 +item.time);
	        }
    	}
    }
    
    /**
     * process the main update
     * again this involves heavy network calls to GData api's using partial logic
     * @param intent
     * @throws IllegalStateException
     */
    private void process4(Intent intent) throws IllegalStateException{
	
    	   ClientCustomSSL.getInstance().process();
    	   // DONE UPDATE broadcast
    	if (Trace.debug) Log.d(TAG, "update thrd complete, sending broadcast");
    	   Intent broadcastIntent = new Intent();
    	   broadcastIntent.setAction(ACTION_UPDATE_COMPLETE);
    	   broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
//    	   broadcastIntent.putExtra(PARAM_OUT_MSG, resultTxt);
    	   sendBroadcast(broadcastIntent);
    }             
}
