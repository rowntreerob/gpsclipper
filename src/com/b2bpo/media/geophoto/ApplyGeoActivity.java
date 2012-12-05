package com.b2bpo.media.geophoto;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.client.CredentialsProvider;


import com.google.api.client.auth.oauth2.draft10.AccessTokenResponse;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessProtectedResource;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAuthorizationRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessTokenRequest.GoogleAuthorizationCodeGrant;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ListView;
import android.widget.TextView;

public class ApplyGeoActivity extends Activity
{
	private final static String TAG = "ApplyGeoActivity";

    /** Called when the default main activity is first created. 
     *  This runs the UI for the Google Oauth dialog to grant access 
     *  to the relevant Gdata resource ( picasa / google plus photos ) 
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    	authorize(); 
     }
 
    public void onResume(){
    	super.onResume();
    }
    
    /**
     * oauth2 authorize using a webview in order to present the callback
     * where the user OKs the usage of picasa scoped data
     * accesstoken from oauth2 protocol is saved to the ClientCustomSSL singleton 
     * Once the UI dialog complete and oauth access granted,
     * Use a service to start the next bit of network access that 
     * Gets the AlbumList
     * NOTE: the clientCustomSSL uses singleton for app level data that is NOT Garanteed to always be around??
     *   this is the typical static data, global problem w/ Android
     */
    
    private void authorize(){
    	if (Trace.debug) Log.d(TAG, "authorize start");
        WebView webview = new WebView(this);       
        webview.getSettings().setJavaScriptEnabled(true);        
        webview.setVisibility(View.VISIBLE);        
        setContentView(webview);        
        /* WebViewClient must be set BEFORE calling loadUrl! */
        webview.setWebViewClient(new WebViewClient() {

        	public void onPageFinished(WebView view, String url)  {       		
        		//TODO client is now a singleton and could disappear with this activity's shutdown
            	if (url.startsWith(ClientCustomSSL.getInstance().getCallback())) {
            		if (url.indexOf("code=")!=-1) {
						try {
							getOauthToken(extractCodeFromUrl(url));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ExecutionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
  		      //TODO review belo																		
//      		      CredentialsProvider credentialStore = new SharedPreferencesCredentialStore(prefs);
//      		      credentialStore.write(accessTokenResponse);
					view.setVisibility(View.INVISIBLE);
//					PicasaAlbumService.startAlbumList(getApplicationContext());
			        Intent intent = new Intent(ClientCustomSSL.ACTION_ALBUMLIST);  //sync w/receiver in albumListActivity
			        intent.setComponent(new ComponentName(getApplicationContext(),AlbumListActivity.class));
			        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			        startActivity(intent);
					}

            	} //endif
            }
        });
 
        String authorizationUrl = new GoogleAuthorizationRequestUrl(ClientCustomSSL.CLIENT_ID, ClientCustomSSL.CALLBACK_URL, ClientCustomSSL.SCOPE).build();
        if (Trace.debug) Log.d(TAG, "webview oauth load");
        webview.loadUrl(authorizationUrl);    	
    }
    
    private String extractCodeFromUrl(String url){
    	String[] words = url.split("=");
    	return words[1];
    }

    /**
     * Network calls involved in the oauth dialog so this needs to be OFF the UI thread
     * Use a runnable to get separate thread for the network calls.
     * @param url
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    private void getOauthToken(String url) throws IOException, InterruptedException, ExecutionException{
    	final String _url = url;
 	        ExecutorService executor = Executors.newCachedThreadPool();
 	        Future<?> future = executor.submit(
 	            new Runnable() {
 	                public void run() {
 	                    try {
 	           			AccessTokenResponse accessTokenResponse = new GoogleAuthorizationCodeGrant(
 	        					new NetHttpTransport(),
 	        					new JacksonFactory(),
 	        					ClientCustomSSL.getInstance().getClientId(),
 	        					ClientCustomSSL.getInstance().getClientSecret(),
 	        					_url,
 	        					ClientCustomSSL.CALLBACK_URL).execute(); 			
 	        			ClientCustomSSL.getInstance().setToken(accessTokenResponse.accessToken); 
 	        			if (Trace.debug) Log.d(TAG, "pstCall tokn " +accessTokenResponse.accessToken); 
 	                    }  catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
 	                }
 	            }
 	        );
 	        future.get();
 	        if(Trace.debug)Log.d(TAG, "getOauthTokn Task finished!");
 	        executor.shutdown();
    }
}
