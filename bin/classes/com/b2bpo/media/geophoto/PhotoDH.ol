package com.b2bpo.media.geophoto;

import org.xml.sax.helpers.DefaultHandler;
import java.security.InvalidParameterException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import android.util.Log;

//TODO can remove parse events related to photoId, albumId as they are no longer needed in DataP
public class PhotoDH extends DefaultHandler{
	  private boolean  _intime; //  _inphoto, _inalbum;
	  private static String TAG = "PhotoDH"; 

	  // this holds the data 
	  private DataP _data;
	  private StringBuffer sb;
 	  	
	  /** 
	   * This gets called when the xml document is first opened 
	   * 
	   * @throws SAXException 
	   */ 
	  @Override 
	  public void startDocument() throws SAXException { 
	    _data = new DataP();
	    if (Trace.debug) Log.d(TAG, "starting doc");
	  } 	 
	  /** 
	   * Called when it's finished handling the document 
	   * 
	   * @throws SAXException 
	   */ 
	  @Override 
	  public void endDocument() throws SAXException { 

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
		//link get attr=href
		 if(qName.equals("link")) { 	      	      
	      _data.setGdataEditLink(atts.getValue("href")); 
	    } else if(qName.equals("gphoto:timestamp")) { 
	      _intime = true; 
	      sb = new StringBuffer();
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
		  if(qName.equals("entry")) {
		    	try {
					_data.addPhotoitem(_data.getPhotoitem());					
				} catch (InvalidParameterException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
		  } else  if(qName.equals("gphoto:timestamp") ) { 
		    	_data.setTime(sb.toString());
		    	_intime = false; 	    	    		    		    	
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
	    if(_intime){
	    	sb.append(chars);	    	
	    } 
	  }

}
