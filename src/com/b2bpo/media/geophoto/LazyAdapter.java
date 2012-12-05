package com.b2bpo.media.geophoto;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
 
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
 
public class LazyAdapter extends BaseAdapter {
 
    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater=null;
    public ImageLoader imageLoader; 
 
    public LazyAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
        activity = a;
        data=d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader=new ImageLoader(activity.getApplicationContext());
    }
 
    public int getCount() {
        return data.size();
    }
 
    public Object getItem(int position) {
        return position;
    }
 
    public long getItemId(int position) {
        return position;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.list_row, null);
 
        TextView name = (TextView)vi.findViewById(R.id.name); // album name
        TextView numphotos = (TextView)vi.findViewById(R.id.numphotos); // in the album       
        TextView timestamp = (TextView)vi.findViewById(R.id.timestamp); // duration
        ImageView thumbnail=(ImageView)vi.findViewById(R.id.list_image); // thumb image
 
        HashMap<String, String> photo = new HashMap<String, String>();
        photo = data.get(position);
 
        // Setting all values in listview
        name.setText(photo.get("name"));
        numphotos.setText(photo.get("numphotos"));       
        Date ml = new Date(Long.parseLong(photo.get("timestamp")));                
        timestamp.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(ml));
        imageLoader.DisplayImage(photo.get("thumbnail"), thumbnail);
        return vi;
    }
}
