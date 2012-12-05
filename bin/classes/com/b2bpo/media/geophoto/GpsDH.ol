package com.b2bpo.media.geophoto;

import org.xml.sax.helpers.DefaultHandler;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidParameterException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import android.util.Log;

/**
 * class orig written for android, its a the xml parse implementation for GPX data set captured by a 3rd party app on the phone
 * GPX timestamps in GMT zone is a standard
 * @author rob
 *
 */

public class GpsDH extends DefaultHandler {
	  private boolean _intrkpt, _intime;
	  private static String TAG = "GpsDH"; 
	  
	  private static String Filename = "Gps_time.json";
	  private  File filesDir;
//	  private FileWriter fstream;
//	  private BufferedWriter out;
		 
	  // this holds the data 
	  private DataG _data;
	  public GpsDH(){}
	  public GpsDH(File file){
		  //TODO if the file exists, delete it
		  this.filesDir = file;		  		
	  }	 

	  
	  /** 
	   * Returns the data object 
	   * 
	   * @return 
	   */ 
	  public String getData() { 
	    return _data.toString(); 
	  } 
	 
	  /** 
	   * This gets called when the xml document is first opened 
	   * 
	   * @throws SAXException 
	   */ 
	  @Override 
	  public void startDocument() throws SAXException { 
	    _data = new DataG();
	    if (Trace.debug) Log.d(TAG, "starting doc");
//		try {
//			 _env.getExternalStorageDirectory(); 
//			fstream = new FileWriter(new File(filesDir, Filename));
//			out = new BufferedWriter(fstream);
//		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	  } 
	 
	  /** 
	   * Called when it's finished handling the document 
	   * 
	   * @throws SAXException 
	   */ 
	  @Override 
	  public void endDocument() throws SAXException { 
//		  try {
//			out.close();
//		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	  } 
	 
	  /** 
	   * This gets called at the start of an element. Here we're also setting the booleans to true if it's at that specific tag. (so we 
	   * know where we are) 
	   * 
	   * @param namespaceURI 
	   * @param localName 
	   * @param qName 
	   * @param atts 
	   * @throws SAXException 
	   */ 
	  @Override 
	  public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException { 
		
		 if(qName.equals("trkpt")) { 
	      _intrkpt = true;
	      
	      _data.setLat(atts.getValue("lat"));
	      _data.setLon(atts.getValue("lon")); 
	    } else if(qName.equals("time")) { 
	      _intime = true; 
	    } 
	  } 

	  /** 
	   * Called at the end of the element. Setting the booleans to false, so we know that we've just left that tag. 
	   * Use localName in android , use qName in java APP
	   * @param namespaceURI 
	   * @param localName 
	   * @param qName 
	   * @throws SAXException 
	   */ 
	  @Override 
	  public void endElement(String namespaceURI, String localName, String qName) throws SAXException { 
//	    Log.v("endElement", localName +" " +qName); 
		  if(qName.equals("trkpt")) {
		    	try {
					_data.addGeoitem(_data.getGeoitem());
//					System.out.println("end trkpt " +_data.getGeoitem().latLong);
//					out.write(object.toString() + "\n");
//					Log.v(TAG, "time: " +object.getString("time"));
//					Log.v(TAG, object.toString());
					
				} catch (InvalidParameterException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			  _intrkpt = false;
			  }
		  if(qName.equals("time") ) { 
	      _intime = false; 
	    
//	    	Log.v(TAG, "{ \"id\": \"" +_data.photoId +"\", \"albumId\": \"" +_data.albumId +"\", \"time\": \"" +_data.time +"\"},");	    		    		    	
	    }
	  }
	  
	  /** 
	   * Calling when we're within an element. Here we're checking to see if there is any content in the tags that we're interested in 
	   * and populating it in the Config object. 
	   * 
	   * @param ch 
	   * @param start 
	   * @param length 
	   */ 
	  @Override 
	  public void characters(char ch[], int start, int length) { 
	    String chars = new String(ch, start, length); 
//	    chars = chars.trim();	 
	    if(_intime){
//	    	_data.time = chars;
	    	//TODO handle timezone of locale and convert Date from local to UTC  before convrt to String
	    	_data.setTime(chars);
//	    	if(chars.length() < 20) Log.d(TAG, " 2nd len short " +chars);
	    }
	  }

}
