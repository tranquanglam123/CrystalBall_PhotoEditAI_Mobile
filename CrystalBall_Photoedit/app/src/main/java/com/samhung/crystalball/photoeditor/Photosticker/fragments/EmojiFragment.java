package com.samhung.crystalball.photoeditor.Photosticker.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.samhung.crystalball.photoeditor.Photosticker.PhotostickerActivity;
import com.samhung.crystalball.photoeditor.R;

import java.util.ArrayList;
import java.util.Collections;

public class EmojiFragment extends Fragment implements EmojiAdapter.OnEmojiClickListener {

    private ArrayList<String> emojiIds;
    private PhotostickerActivity stickerActivity;
    public RecyclerView emojiRecyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stickerActivity = (PhotostickerActivity) getActivity();

        String[] emojis = stickerActivity.getResources().getStringArray(R.array.photo_editor_emoji);

        emojiIds = new ArrayList<>();
        Collections.addAll(emojiIds, emojis);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_photo_edit_emoji, container, false);

        emojiRecyclerView = (RecyclerView) rootView.findViewById(R.id.fragment_main_photo_edit_emoji_rv);
        emojiRecyclerView.setHasFixedSize(true);
        emojiRecyclerView.setLayoutManager(new GridLayoutManager(stickerActivity, 4));
        EmojiAdapter adapter = new EmojiAdapter(stickerActivity, emojiIds);
        adapter.setOnEmojiClickListener(this);
        emojiRecyclerView.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onEmojiClickListener(String emojiName) {
        stickerActivity.addEmoji(emojiName);
    }
}
