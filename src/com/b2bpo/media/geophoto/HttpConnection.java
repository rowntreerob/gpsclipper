package com.b2bpo.media.geophoto;

import java.io.*;
import java.util.concurrent.TimeUnit;

import com.google.gdata.data.ExtensionProfile;
import com.google.gdata.data.photos.PhotoEntry;
import com.google.gdata.util.common.xml.XmlWriter;

import ch.boye.httpclientandroidlib.HttpEntity;
import ch.boye.httpclientandroidlib.HttpException;
import ch.boye.httpclientandroidlib.HttpHost;
import ch.boye.httpclientandroidlib.HttpRequest;
import ch.boye.httpclientandroidlib.HttpRequestInterceptor;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.HttpVersion;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpDelete;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.client.methods.HttpPut;
import ch.boye.httpclientandroidlib.conn.ClientConnectionRequest;
import ch.boye.httpclientandroidlib.conn.ConnectionPoolTimeoutException;
import ch.boye.httpclientandroidlib.conn.params.ConnManagerParams;
import ch.boye.httpclientandroidlib.conn.routing.HttpRoute;
import ch.boye.httpclientandroidlib.entity.BufferedHttpEntity;
import ch.boye.httpclientandroidlib.entity.ContentProducer;
import ch.boye.httpclientandroidlib.entity.EntityTemplate;
import ch.boye.httpclientandroidlib.entity.StringEntity;
import ch.boye.httpclientandroidlib.impl.client.DefaultHttpClient;
import ch.boye.httpclientandroidlib.params.BasicHttpParams;
import ch.boye.httpclientandroidlib.params.HttpConnectionParams;
import ch.boye.httpclientandroidlib.params.HttpParams;
import ch.boye.httpclientandroidlib.params.HttpProtocolParams;
import ch.boye.httpclientandroidlib.protocol.HttpContext;
import ch.boye.httpclientandroidlib.util.EntityUtils;

import android.graphics.*;
import android.os.*;
import android.util.Log;

/**
 * Asynchronous HTTP connections
 * 
 * @author Greg Zavitz & Joseph Roth
 */
public class HttpConnection implements Runnable {

	public static final int DID_START = 0;
	public static final int DID_ERROR = 1;
	public static final int DID_SUCCEED = 2;

	private static final int GET = 0;
	private static final int POST = 1;
	private static final int PUT = 2;
	private static final int DELETE = 3;
	private static final int BITMAP = 4;

	private String url;
	private int method;
	private Handler handler;
	private String data;

	private static String TAG = "HttpConnection";

	private DefaultHttpClient httpClient;

	public HttpConnection() {
		this(new Handler());
	}

	public HttpConnection(Handler _handler) {
		handler = _handler;

	}

	public void create(int method, String url, String data) {
		this.method = method;
		this.url = url;
		this.data = data;	    
	    MyConnectionManager.getInstance().push(this);
		//MyConnectionManager.getInstance().startNext(this);
	}
	

	public void get(String url) {
		create(GET, url, null);
	}

	public void post(String url, String data) {
		create(POST, url, data);
	}	

	public void put(String url, String data) {
		create(PUT, url, data);
	}

	public void delete(String url) {
		create(DELETE, url, null);
	}

	public void bitmap(String url) {
		create(BITMAP, url, null);
	}

	public void run() {
		handler.sendMessage(Message.obtain(handler, HttpConnection.DID_START));
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, 40 * 1000);
		HttpConnectionParams.setSoTimeout(params, 10 * 1000);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		
		httpClient = new DefaultHttpClient(MyConnectionManager.getInstance(), params);
//		HttpConnectionParams.setSoTimeout(httpClient.getParams(), 25000);
	    httpClient.addRequestInterceptor(new HttpRequestInterceptor() {
	        public void process(
	                final HttpRequest request, 
	                final HttpContext context) throws HttpException, IOException {
                if (!request.containsHeader("Authorization")) {
                    request.addHeader("Authorization", "OAuth " +ClientCustomSSL.getInstance().getToken());
                }
	        }
	        
	    }); 
		try {
			HttpResponse response = null;
			switch (method) {
			case GET:
				HttpGet httpGet = new HttpGet(url);
				httpGet.addHeader("GData-Version", "2");
				response = httpClient.execute(httpGet);
				break;
			case PUT:
				HttpPut httpPut = new HttpPut(url);
				httpPut.setEntity(new StringEntity(data));
				response = httpClient.execute(httpPut);
				break;
			case DELETE:
				response = httpClient.execute(new HttpDelete(url));
				break;
			case BITMAP:
				response = httpClient.execute(new HttpGet(url));
				processBitmapEntity(response.getEntity());
				break;
			}
			if (method < BITMAP)
				processEntity(response.getEntity());
		} catch (Exception e) {
			handler.sendMessage(Message.obtain(handler,
					HttpConnection.DID_ERROR, e));
		}
		MyConnectionManager.getInstance().didComplete(this);
	}

	private void processEntity(HttpEntity entity) throws IllegalStateException,
			IOException {		
         if (entity != null) {
             byte[] bytes = EntityUtils.toByteArray(entity);
     		 Message message = Message.obtain(handler, DID_SUCCEED, bytes);
    		 handler.sendMessage(message);
         }		
	}

	private void processBitmapEntity(HttpEntity entity) throws IOException {
		BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
		Bitmap bm = BitmapFactory.decodeStream(bufHttpEntity.getContent());
		handler.sendMessage(Message.obtain(handler, DID_SUCCEED, bm));
	}

}
