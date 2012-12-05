package com.b2bpo.media.geophoto;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import ch.boye.httpclientandroidlib.HttpHost;
import ch.boye.httpclientandroidlib.conn.routing.HttpRoute;
import ch.boye.httpclientandroidlib.conn.scheme.PlainSocketFactory;
import ch.boye.httpclientandroidlib.conn.scheme.Scheme;
import ch.boye.httpclientandroidlib.conn.scheme.SchemeRegistry;
import ch.boye.httpclientandroidlib.conn.ssl.SSLSocketFactory;
import ch.boye.httpclientandroidlib.impl.conn.tsccm.ThreadSafeClientConnManager;

/**
 * Simple connection manager to throttle connections
 * 
 * @author Greg Zavitz
 */
public class MyConnectionManager extends ThreadSafeClientConnManager{
	
	public static final int MAX_CONNECTIONS = 10;

	private ArrayList<Runnable> active = new ArrayList<Runnable>(2);
	private ArrayList<Runnable> queue = new ArrayList<Runnable>();

	private static MyConnectionManager instance;
	
	static X509TrustManager tm = new X509TrustManager() {
	    	 
	    	public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
	    	}
	    	 
	    	public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
	    	}
	    	 
	    	public X509Certificate[] getAcceptedIssuers() {
	    	return null;
	    	}
	};
	
	MyConnectionManager(SchemeRegistry scheme){
		super(scheme);
	}

	public static MyConnectionManager getInstance() {
		if (instance == null){

		    SSLContext ctx=null;
			try {
				ctx = SSLContext.getInstance("TLS");
				ctx.init(null, new TrustManager[]{tm}, null);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (KeyManagementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	    			
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register( new Scheme("http", 80,PlainSocketFactory.getSocketFactory()));
			schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));
		    instance = new MyConnectionManager(schemeRegistry);
			 // Increase max total connection to 200
			 instance.setMaxTotal(15);
			 // Increase default max connection per route to 20
			 instance.setDefaultMaxPerRoute(15);
			 // Increase max connections for localhost:80 to 50
			 HttpHost localhost = new HttpHost("picasaweb.google.com", 443);
			 instance.setMaxForRoute(new HttpRoute(localhost), 10);
		}
		return instance;
	}

	public void push(Runnable runnable) {
		queue.add(runnable);
		if (active.size() < MAX_CONNECTIONS)
			startNext();
	}

	private void startNext() {
		if (!queue.isEmpty()) {
			Runnable next = queue.get(0);
			queue.remove(0);
			active.add(next);

			Thread thread = new Thread(next);
			thread.start();
		}
	}
	
	public void startNext(Runnable next){
		Thread thread = new Thread(next);
		thread.start();		
	}

	public void didComplete(Runnable runnable) {
		active.remove(runnable);
		startNext();
	}

}
