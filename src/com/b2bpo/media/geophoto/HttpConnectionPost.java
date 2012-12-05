package com.b2bpo.media.geophoto;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import ch.boye.httpclientandroidlib.HttpEntity;
import ch.boye.httpclientandroidlib.HttpException;
import ch.boye.httpclientandroidlib.HttpRequest;
import ch.boye.httpclientandroidlib.HttpRequestInterceptor;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.HttpVersion;

import ch.boye.httpclientandroidlib.client.methods.HttpPost;

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

import com.google.gdata.data.ExtensionProfile;
import com.google.gdata.data.photos.PhotoEntry;
import com.google.gdata.util.common.xml.XmlWriter;

public class HttpConnectionPost implements Runnable {
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
	private final PhotoEntry entry;
	private static String TAG = "HttpConnectionPost";

	private DefaultHttpClient httpClient;

	public HttpConnectionPost() {
		this(new Handler(), new PhotoEntry());
	}

	public HttpConnectionPost(Handler _handler, PhotoEntry _entry) {
		handler = _handler;
		entry = _entry;
		
	}
// thread bug, the last instance pushed to the worker Q contains the entry field that is the target of every callback on "entry.generate(xml)"
	public void create(int method, String url) {
		this.method = method;
		this.url = url;	    
	    MyConnectionManager.getInstance().push(this);
		//MyConnectionManager.getInstance().startNext(this);
	}
	
	public void get(String url) {
		create(GET, url);
	}

	public void post(String url) {
		create(POST, url);
	}


	public void put(String url, String data) {
		create(PUT, url);
	}

	public void delete(String url) {
		create(DELETE, url);
	}

	public void bitmap(String url) {
		create(BITMAP, url);
	}

	public void run() {
//		Log.d(TAG, "run ENTRY instance"  +entry.toString());
		handler.sendMessage(Message.obtain(handler, HttpConnection.DID_START));
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, 40 * 1000);
		HttpConnectionParams.setSoTimeout(params, 20 * 1000);
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

			case POST:
				HttpPost httpPost = new HttpPost(url);
				if (data != null){
					System.out.println(" post data not null ");
					httpPost.setEntity(new StringEntity(data));
				}
				if (entry != null){
					ContentProducer cp = new ContentProducer() {
					    public void writeTo(OutputStream outstream) throws IOException {
					        	        
						     ExtensionProfile ep = new ExtensionProfile();
							 ep.addDeclarations(entry);
							 XmlWriter xmlWriter = new XmlWriter(new OutputStreamWriter(outstream, "UTF-8"));
							 entry.generate(xmlWriter, ep);
							 xmlWriter.flush();
					    }
					};
					httpPost.setEntity(new EntityTemplate(cp));
				}
			    httpPost.addHeader("GData-Version", "2");
		        httpPost.addHeader("X-HTTP-Method-Override", "PATCH");
		        httpPost.addHeader("If-Match", "*");
		        httpPost.addHeader("Content-Type", "application/xml");
				response = httpClient.execute(httpPost);

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
