package com.b2bpo.media.geophoto;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import android.util.Log;
import ch.boye.httpclientandroidlib.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.gdata.data.Entry;
import com.google.gdata.data.photos.PhotoEntry;

public class ClientCustomSSL {
	//singleton ClientCustomSSL used as global ?? 
	//mainly for the token and the httpclient
	public static final String ACTION_ALBUMLIST = "com.b2bpo.media.geophoto.action.ALBUMLIST";
	public static final String ACTION_ALBUMLIST2 = "com.b2bpo.media.geophoto.action.ALBUMLIST2";
	public static final String ACTION_CONFIRM = "com.b2bpo.media.geophoto.action.CONFIRM";
	public static final String ACTION_ALBUMFEED = "com.b2bpo.media.geophoto.action.ALBUMFEED";
	public static final String ACTION_UPDATE = "com.b2bpo.media.geophoto.action.UPDATE";
	public static final String ACTION_UPDATE_COMPLETE = "com.b2bpo.media.geophoto.action.UPDATE_COMPLETE";
	  private static ClientCustomSSL  instance;
	  private static int ctr = 0;
	  private static String TAG = "ClientCustomSSL";
	  static final String SCOPE = "https://picasaweb.google.com/data/";
//	  static final String CALLBACK_URL = "urn:ietf:wg:oauth:2.0:oob";
	  static final String CALLBACK_URL = "http://localhost";
	  private static final HttpTransport TRANSPORT = new NetHttpTransport();
	  private static final JsonFactory JSON_FACTORY = new JacksonFactory();

	  // FILL THESE IN WITH YOUR VALUES FROM THE API CONSOLE
	  static final String CLIENT_ID = "707313301757.apps.googleusercontent.com";
			  static final String CLIENT_SECRET = "XXUhpyBlOi0mxWOaEBjEVe2b";	  
//	  static final String CLIENT_ID = "13604934834.apps.googleusercontent.com";
//	  static final String CLIENT_SECRET = "vbdZ2AOZAHzSNjpsnz3xXC93";
//	  private static String token="";
	  static public  PhotoEntry entry = new PhotoEntry(new Entry());
	  //TODO stop this
//	  static DefaultHttpClient httpclient = new DefaultHttpClient();
	  static DefaultHttpClient httpclient;
	  static String oauthTokn;
	  static JsonNode rootNode;
	  //id:albumlink, name:albumTitle below 
	  static HashMap<String, String> albumMap;
	  static String gpxfilepath = "";
	  static X509TrustManager tm = new X509TrustManager() {
	    	 
	    	public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
	    	}
	    	 
	    	public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
	    	}
	    	 
	    	public X509Certificate[] getAcceptedIssuers() {
	    	return null;
	    	}
	    };
	    static DataP _dataP = new DataP();


	    
		public static synchronized ClientCustomSSL getInstance() {
			if (instance == null) {
				instance = new ClientCustomSSL();

			}
			return instance;
		}
		
		/**
		 * callback for a whendone on the oauth dialog 
		 * when we have oauth token in activity context
		 * then do this 
		 * @param args
		 * @throws Exception
		 */		        
       
    /**
     * see the http: get in getAlbums that produces the json consumed here
     * This return object is suitable for HashMap type extension of android.BaseAdapter
     * or any UI component that binds to a Map object for data
     * see http://goo.gl/k3hcg
     * @param root
     * @return map of _id, title for the most recent , 10 albums ( Map does NOT preserve correct order but, we can live with that )
     * https://picasaweb.google.com/data/api/path/user/rowntreerob/albumid/5703943611411433905/precita_5
	 * 
	 * Use the map.entry.key with httpclient to lookup the album
     */
    //TODO chg the entry elementsfrom id, name to 
    // id, name
    // + entry/published/$t/text() :key=pubdate
    // + entry/gphoto$numphotos/$t/text() :key=numphotos
    // + entry/media$group/media$thumbnail[@url]
    public static ArrayList<HashMap<String, String>> getList(JsonNode root){
    	ArrayList<HashMap<String, String>> albums = new ArrayList<HashMap<String, String>>();
    	
    	
    	ArrayNode array = (ArrayNode) root.path("feed").path("entry");  // an array
    	for(JsonNode node :  array){
    		HashMap<String, String> map = new HashMap<String, String>();
    		String[] words = node.path("link").path(0).path("href").getTextValue().split("\\?");
  //  		map.put( node.path("id").path("$t").getTextValue(), node.path("title").path("$t").getTextValue());
    		map.put("id", words[0]);
    		map.put("name", node.path("title").path("$t").getTextValue());
    		map.put("timestamp", node.path("gphoto$timestamp").path("$t").getTextValue());    		   	    
    		map.put("numphotos", String.valueOf(node.path("gphoto$numphotos").path("$t").getLongValue()));
    		map.put("thumbnail", node.path("media$group").path("media$thumbnail").path(0).path("url").getTextValue());
    		albums.add(map); 
//    		if(Trace.debug) Log.d(TAG, "albmList " +map.toString());

    	}    	
    	return albums;
    }
        
    /**
     * Using Gdata api
     * Get picasa Album and get list of album's photos
     * get the gdata response fields needed to correlate the album's photo DataSet to 
     * the Gpx data set using key=timestamp (GMT)
     * the http get on Gdata API for picasa results in following response (XML)
     *
     *NOTE:  below you no longer need 2 fields:
     *    <gphoto:id>5677569292818283634</gphoto:id>
     *    <gphoto:albumid>5677569274306148065</gphoto:albumid>
<feed xmlns='http://www.w3.org/2005/Atom' xmlns:gphoto='http://schemas.google.com/photos/2007'>
  <entry>
    <link rel='edit' type='application/atom+xml'
    href='https://picasaweb.google.com/data/entry/api/user/rowntreerob/albumid/5677569274306148065/photoid/5677569292818283634' />
    <gphoto:id>5677569292818283634</gphoto:id>
    <gphoto:albumid>5677569274306148065</gphoto:albumid>
    <gphoto:timestamp>1321879579000</gphoto:timestamp>  NOTE: all comparator use UTC milliseconds since 1970 , a long d-type
  </entry>
  <entry>  
    <link rel='edit' type='application/atom+xml'
    href='https://picasaweb.google.com/data/entry/api/user/rowntreerob/albumid/5677569274306148065/photoid/5677569322606354754' />
    <gphoto:id>5677569322606354754</gphoto:id>
    <gphoto:albumid>5677569274306148065</gphoto:albumid>
    <gphoto:timestamp>1321879606000</gphoto:timestamp>
  </entry>
  ... 
 <feed />

    
    /**
     * convert the XML file's geo information to a DataG class (see the static DataG.ArrayList)
     * that will be used to compare geo data to photo data
     * @param instrm the GPX input file selected
     */
    
    public static void getDataP(InputStream instrm){
    	// duplicated entries in list of photos to update
    	_dataP.photoitems.clear();
    	SAXParserFactory spf = SAXParserFactory.newInstance(); 
	    SAXParser sp;
		try {
			sp = spf.newSAXParser();
			if (Trace.debug) Log.d(TAG, "Parser type " +sp.getClass().getName());
			
			StringPullHandler handler = new StringPullHandler() {
                @Override
                public void endElement2(String uri, String localName,
                        String qName) {                	                   
	          		  if(qName.equals("entry")) {
	      		    	try {
	      					_dataP.addPhotoitem(_dataP.getPhotoitem());					
	      				} catch (InvalidParameterException e) {
	      					// TODO Auto-generated catch block
	      					e.printStackTrace();
	      				} 
		      		  } else  if(qName.equals("gphoto:timestamp") ) { 
		      		    	_dataP.setTime(getCharacters());		 	    	    		    		    	
		      		  }
                }
                @Override 
            	protected void startElement2(String uri, String localName, String qName,
            			Attributes attributes) throws SAXException {
                	if(qName.equals("link")) { 	      	      
              	      _dataP.setGdataEditLink(attributes.getValue("href")); 
              	    }
            	}
            };
						
		    XMLReader xr = sp.getXMLReader(); 
		    xr.setContentHandler(handler);	    	       	       	                                      
		    xr.parse(new InputSource(instrm));
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 			 
	       	
    }
        
	public static void setToken(String arg){
    	instance.oauthTokn=arg;
    }
    public String getToken(){
    	return instance.oauthTokn;
    }

    public static String getGpxfilepath() {
		return gpxfilepath;
	}

	public static void setGpxfilepath(String gpxfilepath) {
		ClientCustomSSL.gpxfilepath = gpxfilepath;
	}
    public static String getCallback(){
    	return instance.CALLBACK_URL;
    }
    public static String getClientId(){
    	return instance.CLIENT_ID;
    }
    public static String getClientSecret(){
    	return instance.CLIENT_SECRET;
    }
}
