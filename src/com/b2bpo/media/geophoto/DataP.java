package com.b2bpo.media.geophoto;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.util.Log;


//TODO can remove albumID , photoId because you are getting the full rel=edit link on each photo
// from the process that consumes the response to the call for the album feed
public class DataP {
	private static String TAG = "DataP";
	private static long offset =  TimeZone.getTimeZone(Calendar.getInstance().getTimeZone().getID()).getRawOffset();
	public static String albumId;
	public String time;
	public String photoId;
	public String gdata_edit_link;
	public static ArrayList<Photoitem> photoitems = new ArrayList<Photoitem>();
	
    public DataP() {
    	//bugfix onResume was repeating the array
    	photoitems = new ArrayList<Photoitem>();
	  time = "";
	  photoId = "";
	  gdata_edit_link = "";	 
    }
    
    public static void addPhotoitem(Photoitem entry){
    	photoitems.add(entry);
    }
    
    public static ArrayList<Photoitem> getPhotoItems(){
    	return photoitems;
    }
    
    public void setTime(String arg){
    	time = arg;
    }
    
//    public void setPhotoid(String arg){
//    	photoId = arg;
 //   }
    
    public void setGdataEditLink(String arg){
    	gdata_edit_link = arg;
    }
    
    public String getTime(){
    	return time;
    }
    
//    public String getPhotoid(){
//    	return photoId;
//    }
    
    public String getGdataEditLink(){
    	return gdata_edit_link;
    }
    
	  /**
	   * BUG here in scenario where move on/off DST occurs BETWEEN the time that the EXIF data on cameera was set and
	   * the time when this program runs.
	   * @param date
	   * @return
	   */
		private  Date cvtToGmt( Date date )
		{
		   TimeZone tz = TimeZone.getDefault();
		   Date ret = new Date( date.getTime() - tz.getRawOffset() );

		   // if we are now in DST, back off by the delta.  Note that we are checking the GMT date, this is the KEY.
		   if ( tz.inDaylightTime( ret ))
		   {
		      Date dstDate = new Date( ret.getTime() - tz.getDSTSavings() );

		      // check to make sure we have not crossed back into standard time
		      // this happens when we are on the cusp of DST (7pm the day before the change for PDT)
		      if ( tz.inDaylightTime( dstDate ))
		      {
		         ret = dstDate;
		      }
		   }

		   return ret;
		}

		/**
		 * Called by the parse operation on the album feed
		 * The adjustment here is critical in reconciling TimeZones
		 * All the GPX data is converted on the device to UTC.
		 * The data from the camera , when parsed is going to 
		 * be milliseconds since 1970 in the LOCAL timezone.
		 * So, you have to make an adjustment to get both data sets ( GPX< PHOTO )
		 * on the same LOCALE. 
		 * @return
		 */
	public Photoitem getPhotoitem(){
		
		// from <gphoto:timestamp />
		// convert string of millisecond since 1970 expressed in local zone to equivalent of the milliseconds over in GMT
		// camera that took the pics that were uploaded needs to be in same Zone at the time of upload as the PC used 
		// and in same Zone as the phone that recorded the GPX track
		Photoitem item = new Photoitem();
    	Date tmp = new Date(Long.parseLong(getTime())); // local date   	    	
    	Date newdte =  cvtToGmt(tmp);
    	if (Trace.debug) Log.d(TAG, "local dte UTC dte " +getTime() +" " +newdte.getTime());
    	item.setTime(newdte.getTime());                 // converted to GMT so it will match the zone of GPX data
		item.setEditlink(getGdataEditLink());
		return item;
	}
}
