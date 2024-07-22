package com.samhung.crystalball.photoeditor.Help.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.samhung.crystalball.photoeditor.Help.HelpActivity;
import com.samhung.crystalball.photoeditor.MainActivity;
import com.samhung.crystalball.photoeditor.R;
import com.samhung.crystalball.photoeditor.Utilis.GalleryUtils;
import com.samhung.crystalball.photoeditor.Utilis.zipfile.ZipResourceFile;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class HelpDataFragment extends Fragment {
    HelpActivity helpActivity;
    WebView webView;
    String m_fileName = "";
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        helpActivity = (HelpActivity)getActivity();
        Bundle args = getArguments();
        m_fileName = args.getString("filename");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_help_data, container, false);
        webView = (WebView)rootView.findViewById(R.id.contents_frame);
        if(m_fileName!="") {
            String fileName = "data/data/"+getActivity().getPackageName()+"/help/" + m_fileName;
            File f = new File(fileName);
            webView.loadUrl(f.toURI().toString());
//            webView.loadUrl("file:///android_asset/" + fileName);
//            File f = new File(Environment.getExternalStorageDirectory() + "/help/"+m_fileName);

//            try{
//                InputStream is = MainActivity.gZipResourceFile.getInputStream("assets/help/"+m_fileName);
//                ByteArrayOutputStream sb = new ByteArrayOutputStream();
//                int iREad = 0;
//                byte data[]  = new byte[4096];
//                while ((iREad=is.read(data, 0, 4096)) > 0) {
//                    sb.write(data, 0, iREad);
//                }
//                is.close();
//                String ddd = sb.toString("UTF-8");
//                sb.close();
//                webView.loadDataWithBaseURL("file:///android_asset/", ddd,"text/html", "UTF-8", "");
//            }catch (Exception ex) {}

        }
        return rootView;
    }




}
