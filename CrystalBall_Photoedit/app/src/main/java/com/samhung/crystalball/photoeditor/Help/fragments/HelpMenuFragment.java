package com.samhung.crystalball.photoeditor.Help.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.samhung.crystalball.photoeditor.Help.HelpActivity;
import com.samhung.crystalball.photoeditor.R;

import java.util.ArrayList;

public class HelpMenuFragment extends Fragment {
    HelpActivity helpActivity;
    ListView listView_menu;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        helpActivity = (HelpActivity)getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_help_menu, container, false);
        listView_menu = (ListView)rootView.findViewById(R.id.contents_frame);
        HelpActivity.HelpContentsAdapter contentsAdapter = helpActivity.makeAdapter();
        listView_menu.setAdapter(contentsAdapter);
        listView_menu.setOnItemClickListener(helpActivity.mItemClickListener);
        return rootView;
        //return super.onCreateView(inflater, container, savedInstanceState);
    }

}
