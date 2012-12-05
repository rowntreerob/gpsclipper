package com.b2bpo.media.geophoto;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * in android you get the xerces parse stuff and default handlers built in, in pojo , you have to provide
 * jars for the xerces impl of the parsers
 */
public class XmlReader {
	
	 private Log log = LogFactory.getLog(XmlReader.class);
	
	 public int _parseXml(org.xml.sax.helpers.DefaultHandler dataHandler, String path ) {  
		 
		  // sax parser stuff 
		  try { 
		    SAXParserFactory spf = SAXParserFactory.newInstance(); 
		    SAXParser sp = spf.newSAXParser(); 			 
		    XMLReader xr = sp.getXMLReader(); 			 
		    xr.setContentHandler(dataHandler); 
		    //TODO external field is result of UI selection of thhe GPX file used to apply the geo data
	    	File mydir = new File("c:/temp/f4");       	       	                                      
		    xr.parse(new InputSource(new FileInputStream( new File(mydir, path))));
		    return 1;
		  } catch(ParserConfigurationException pce) { 
		    log.error("sax parse error " +pce);
		    return -1;
		  } catch(SAXException se) { 
		    log.error("sax error " +se);
		    return -1;
		  } catch(IOException ioe) { 
		    log.error("sax parse io error " +ioe);
		    return -1;
		  }		 		  
		}
	 
	 public int _parseXml(org.xml.sax.helpers.DefaultHandler dataHandler, File input ) {  
		 
		  // sax parser stuff 
		  try { 
		    SAXParserFactory spf = SAXParserFactory.newInstance(); 
		    SAXParser sp = spf.newSAXParser(); 			 
		    XMLReader xr = sp.getXMLReader(); 			 
		    xr.setContentHandler(dataHandler); 
       	       	                                      
		    xr.parse(new InputSource(new FileInputStream(input)));
		    return 1;
		  } catch(ParserConfigurationException pce) { 
		    log.error("sax parse error " +pce);
		    return -1;
		  } catch(SAXException se) { 
		    log.error("sax error " +se);
		    return -1;
		  } catch(IOException ioe) { 
		    log.error("sax parse io error " +ioe);
		    return -1;
		  }		 		  
		}

}
