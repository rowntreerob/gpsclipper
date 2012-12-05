package com.b2bpo.media.geophoto;

import android.content.Intent;

public interface AuthManager {
	 /**
	   * Initializes the login process. The user should be asked to login if they
	   * haven't already. The {@link Runnable} provided will be executed when the
	   * auth token is successfully fetched.
	   *
	   * @param whenFinished A {@link Runnable} to execute when the auth token
	   *        has been successfully fetched and is available via
	   *        {@link #getAuthToken()}
	   */
	  public abstract void doLogin(Runnable whenFinished, Object o);

	  /**
	   * The {@link android.app.Activity} owner of this class should call this
	   * function when it gets {@link android.app.Activity#onActivityResult} with
	   * the request code passed into the constructor. The resultCode and results
	   * should come directly from the {@link android.app.Activity#onActivityResult}
	   * function. This function will return true if an auth token was successfully
	   *  fetched or the process is not finished.
	   *
	   * @param resultCode The result code passed in to the
	   *        {@link android.app.Activity}'s
	   *        {@link android.app.Activity#onActivityResult} function
	   * @param results The data passed in to the {@link android.app.Activity}'s
	   *        {@link android.app.Activity#onActivityResult} function
	   * @return True if the auth token was fetched or we aren't done fetching
	   *         the auth token, or False if there was an error or the request was
	   *         canceled
	   */
	  public abstract boolean authResult(int resultCode, Intent results);

	  /**
	   * Returns the current auth token. Response may be null if no valid auth
	   * token has been fetched.
	   *
	   * @return The current auth token or null if no auth token has been
	   *         fetched
	   */
	  public abstract String getAuthToken();

	  /**
	   * Invalidates the existing auth token and request a new one. The
	   * {@link Runnable} provided will be executed when the new auth token is
	   * successfully fetched.
	   *
	   * @param whenFinished A {@link Runnable} to execute when a new auth token
	   *        is successfully fetched
	   */
	  public abstract void invalidateAndRefresh(Runnable whenFinished);


}
