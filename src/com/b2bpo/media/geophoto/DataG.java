package com.b2bpo.media.geophoto;

import java.util.ArrayList;
import javax.xml.datatype.DatatypeConfigurationException;
import org.apache.xerces.jaxp.datatype.DatatypeFactoryImpl;

/**
 * @author rob
 *
 */
public class DataG {
	
	public String lat;
	public String lon;
	public String time;
	public static ArrayList<Gpxitem> geoitems = new ArrayList<Gpxitem>();
	  
    public DataG(String str){
		
    }
  
    public DataG() {
    	//bugfix repeated array onResume()
    	geoitems = new ArrayList<Gpxitem>();
	  lat = "";
	  lon = "";
	  time = "";
    }
	  
    public static void addGeoitem(Gpxitem entry){
    	geoitems.add(entry);
    }
    
    public static ArrayList<Gpxitem> getGeoitems(){
    	return geoitems;
    }
	
	public String getLat() {
		return lat;
	}
	public void setLat(String lat) {
		this.lat = lat;
	}
	public String getLon() {
		return lon;
	}
	public void setLon(String lon) {
		this.lon = lon;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	
	public String toString(){
		return "lat " +getLat() + " lon " +getLon() + " time " +getTime();
	}
	/**
	 * Use the Gpxitem class to organize/reformat the DataG class's data so that the 
	 * resulting data can be used in a comparator.
	 * This gets the data item to put to the array that will be used as a 
	 * KV pair to compare these Gpx Timestamps to 
	 * Timestamps of matching time-zone provided by another data Set. 
	 * time : "yyyy-MM-dd'T'HH:mm:ss'Z'"  is converted to milliseconds since 1970 in DST
	 * lat and long are concat to a single string
	 * time : long { lat+long : String } 
	 * @return
	 */
	public Gpxitem getGeoitem(){
		Gpxitem item = new Gpxitem();
		try {
			// convert string from DST zone to a Date object so the milliseconds can be returned
			// stackovrflo discuss : http://goo.gl/8g29b
			item.setTime(DatatypeFactoryImpl.newInstance().newXMLGregorianCalendar(getTime()).toGregorianCalendar().getTimeInMillis());			
			item.latLong = getLat() +" " +getLon();
			return item;
		} catch (DatatypeConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new Gpxitem();
	}
}
