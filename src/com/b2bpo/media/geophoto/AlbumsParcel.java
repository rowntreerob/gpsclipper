package com.b2bpo.media.geophoto;

import android.os.Parcel;
import android.os.Parcelable;


public class AlbumsParcel implements Parcelable {
	  public String blob;
	  
	    public AlbumsParcel() {
	        blob = new  String();
	    }
	 
	    public AlbumsParcel(Parcel in) {
	        blob = new String();
	        readFromParcel(in);
	    }
	    
	    public AlbumsParcel(String arg) {
	    	this.blob=arg;
	    }
	 
	    public static final Parcelable.Creator<?> CREATOR = new Parcelable.Creator() {
	        public AlbumsParcel createFromParcel(Parcel in) {
	            return new AlbumsParcel(in);
	        }

			@Override
			public AlbumsParcel[] newArray(int size) {
				// TODO Auto-generated method stub
				return new AlbumsParcel[size];
			}	 
	    };
	 
	    @Override
	    public int describeContents() {
	        return 0;
	    }
	 
	    @Override
	    public void writeToParcel(Parcel dest, int flags) {
	        dest.writeString(blob);

	    }
	 
	    public void readFromParcel(Parcel in) {
	        String arg = in.readString();
	        blob = arg;
	    }
	 

}
