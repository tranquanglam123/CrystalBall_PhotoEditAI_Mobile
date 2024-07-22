package com.samhung.crystalball.Common;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

public class RefreshProvider extends ListActivity {

    private List<String> list = new ArrayList<String>();
    private final String [] STAR= {"*"};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ListAdapter adapter = createAdapter();
        setListAdapter(adapter);
    }

    /**
     * Creates and returns a list adapter for the current list activity
     * @return
     */
    protected ListAdapter createAdapter()
    {
        // return play-lists
        Uri playlist_uri= MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor= managedQuery(playlist_uri, STAR, null,null,null);
        cursor.moveToFirst();
        for(int r= 0; r<cursor.getCount(); r++, cursor.moveToNext()){
            int i = cursor.getInt(0);
            int l = cursor.getString(1).length();
            if(l>0){
                // keep any playlists with a valid data field, and let me know
                list.add("Keeping : " + cursor.getString(2) + " : id(" + i + ")");
            }else{
                // delete any play-lists with a data length of '0'
                Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, i);
                getContentResolver().delete(uri, null, null);
                list.add("Deleted : " + cursor.getString(2) + " : id(" + i + ")");
            }
        }
        cursor.close();
        // publish list of retained / deleted playlists
        ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);

        return adapter;
    }
}
