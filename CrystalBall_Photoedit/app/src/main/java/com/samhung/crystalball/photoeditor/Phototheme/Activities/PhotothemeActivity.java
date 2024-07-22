package com.samhung.crystalball.photoeditor.Phototheme.Activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.samhung.crystalball.Common.BaseActivity;
import com.samhung.crystalball.Common.FullScreenDialog;
import com.samhung.crystalball.photoeditor.MainActivity;
import com.samhung.crystalball.photoeditor.Phototheme.Dialogs.Dialog_ThemeCalendar;
import com.samhung.crystalball.photoeditor.Phototheme.Dialogs.Dialog_ThemeChooser;
import com.samhung.crystalball.photoeditor.Phototheme.Dialogs.Dialog_ThemeMultiSimple;
import com.samhung.crystalball.photoeditor.Phototheme.Dialogs.Dialog_ThemeSimple;
import com.samhung.crystalball.photoeditor.Phototheme.Dialogs.Dialog_ThemeTile;
import com.samhung.crystalball.photoeditor.Phototheme.Dialogs.Dialog_ThemeWhite;
import com.samhung.crystalball.photoeditor.Phototheme.Utils.PhotothemeAdapter;
import com.samhung.crystalball.photoeditor.R;
import com.samhung.crystalball.photoeditor.Utilis.GalleryUtils;
import com.samhung.crystalball.photoeditor.VisionMix.Activities.VisionmixActivity;
import com.samhung.crystalball.photoeditor.VisionMix.Dialogs.VisionPreviewDialog;

import java.util.ArrayList;
import java.util.List;

public class PhotothemeActivity extends BaseActivity  implements PhotothemeAdapter.OnImageClickListener{

    private RecyclerView m_gridView = null;
    private ArrayList<Bitmap> m_gridItemArray = new ArrayList<Bitmap>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phototheme);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        RefreshGridItemArray();
        InitControls();
    }

    public void InitControls() {
        m_gridView = (RecyclerView) findViewById(R.id.grid_frame);
        m_gridView.setLayoutManager(new GridLayoutManager(PhotothemeActivity.this, 2));
        PhotothemeAdapter adapter = new PhotothemeAdapter(PhotothemeActivity.this, m_gridItemArray );
        adapter.setOnImageClickListener(this);
        m_gridView.setAdapter(adapter);
        m_gridView.setEnabled(true);

    }

    public void RefreshGridItemArray()
    {
        m_gridItemArray.clear();

        try {
            String dir = "assets/theme";
//            String[] list = getAssets().list(dir);
//            for(int i=0; i<list.length; i++)
            for(int i = 0; i< MainActivity.gZipResourceFile.getList(dir).length; i++)
            {
                String strfileName = String.format("assets/theme/%d/1/thumb.png", i + 1);
                Bitmap ret=null;
                try {
//                    ret = BitmapFactory.decodeStream(getAssets().open(strfileName));
                    ret = BitmapFactory.decodeStream(MainActivity.gZipResourceFile.getInputStream(strfileName));
                    m_gridItemArray.add(ret);
                }catch (Exception ex) {}

            }
        }
        catch (Exception ex){ex.printStackTrace();}
    }

    public void onButtonClick(View view) {
        if(view.getId() == R.id.button_close) {
            finish();
        }
    }

    int main_CategoryID = 0;
    int sub_CategoryID = 0;

    @Override
    public void onRecyclerItemClickListener(Bitmap image, int imageIndex) {
        main_CategoryID = imageIndex;
        Dialog_ThemeChooser dlg = new Dialog_ThemeChooser(this,this,  imageIndex, mThemeDlgItemClickListener);
        dlg.show();
    }

    LoadGalleryListener mLoadGalleryListener = null;
    Dialog.OnClickListener mThemeDlgItemClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if(which == DialogInterface.BUTTON_POSITIVE) {
                int position = ((Dialog_ThemeChooser) dialog).m_nSelectedItem;
                sub_CategoryID = position;

                if (main_CategoryID == 0) {
                    Dialog_ThemeSimple dlg = new Dialog_ThemeSimple(PhotothemeActivity.this, PhotothemeActivity.this,
                            main_CategoryID, sub_CategoryID,((Dialog_ThemeChooser) dialog).m_thumbThemeArray,  mThemeDialogClickListener);
                    mLoadGalleryListener = dlg.mLoadGalleryListener;
                    dlg.show();
                } else if(main_CategoryID == 2) {
                    Dialog_ThemeMultiSimple dlg = new Dialog_ThemeMultiSimple(PhotothemeActivity.this, PhotothemeActivity.this, main_CategoryID, sub_CategoryID,
                            ((Dialog_ThemeChooser) dialog).m_thumbThemeArray, mThemeDialogClickListener);
                    mLoadGalleryListener = dlg.mLoadGalleryListener;
                    dlg.show();
                } else if(main_CategoryID == 5) {
                    Dialog_ThemeWhite dlg = new Dialog_ThemeWhite(PhotothemeActivity.this, PhotothemeActivity.this,
                            main_CategoryID, sub_CategoryID,((Dialog_ThemeChooser) dialog).m_thumbThemeArray,  mThemeDialogClickListener);
                    mLoadGalleryListener = dlg.mLoadGalleryListener;
                    dlg.show();
                } else if(main_CategoryID == 1) {
                    Dialog_ThemeCalendar dlg = new Dialog_ThemeCalendar(PhotothemeActivity.this, PhotothemeActivity.this,
                            main_CategoryID, sub_CategoryID,((Dialog_ThemeChooser) dialog).m_thumbThemeArray,  mThemeDialogClickListener);
                    mLoadGalleryListener = dlg.mLoadGalleryListener;
                    dlg.show();
                } else if(main_CategoryID == 4) {
                    Dialog_ThemeTile dlg = new Dialog_ThemeTile(PhotothemeActivity.this, PhotothemeActivity.this,
                            main_CategoryID, sub_CategoryID,((Dialog_ThemeChooser) dialog).m_thumbThemeArray,  mThemeDialogClickListener);
                    mLoadGalleryListener = dlg.mLoadGalleryListener;
                    dlg.show();
                } else if(main_CategoryID == 3) {
                    Dialog_ThemeMultiSimple dlg = new Dialog_ThemeMultiSimple(PhotothemeActivity.this, PhotothemeActivity.this, main_CategoryID, sub_CategoryID,
                            ((Dialog_ThemeChooser) dialog).m_thumbThemeArray, mThemeDialogClickListener);
                    mLoadGalleryListener = dlg.mLoadGalleryListener;
                    dlg.show();
                }

            }
            dialog.dismiss();
        }
    };

    Dialog.OnClickListener mThemeDialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if(which == VisionPreviewDialog.BTN_PHEDIT || which == VisionPreviewDialog.BTN_STICKER) {
                Intent returnIntent = new Intent();
                String fileName = ((FullScreenDialog)dialog).m_strResultPath;
                returnIntent.putExtra("path", fileName);
                returnIntent.putExtra("sendto", which);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
            dialog.dismiss();
        }
    };

    List<String> imagesEncodedList;
    String imageEncoded;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            if (requestCode == GalleryUtils.GALLERY_INTENT_CALLED
                    || requestCode == GalleryUtils.GALLERY_KITKAT_INTENT_CALLED) {

                String path = "";
                int back_idx = 0;
                if (requestCode == GalleryUtils.GALLERY_INTENT_CALLED) {
                    Uri uri = data.getData();
                    path = GalleryUtils.getFilePathFromUri(this, uri);
                } else if (requestCode == GalleryUtils.GALLERY_KITKAT_INTENT_CALLED) {
                    Uri uri = data.getData();
                    final int takeFlags = data.getFlags()
                            & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    // Check for the freshest data.
                    if (uri != null) {
                        path = GalleryUtils.getFilePathFromUri(this, uri);
                    }
                }
                ///////
                mLoadGalleryListener.onLoadGallery(path);
            }

            if (requestCode == GalleryUtils.GALLERY_INTENT_CALLED_MULTI
                    || requestCode == GalleryUtils.GALLERY_KITKAT_INTENT_CALLED_MULTI) {
                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                imagesEncodedList = new ArrayList<String>();
                if(data.getData()!=null){

                    Uri mImageUri=data.getData();

                    // Get the cursor
                    Cursor cursor = getContentResolver().query(mImageUri,
                            filePathColumn, null, null, null);
                    // Move to first row
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imageEncoded  = cursor.getString(columnIndex);
                    imagesEncodedList.add(imageEncoded);
                    cursor.close();

                }else {
                    if (data.getClipData() != null) {
                        ClipData mClipData = data.getClipData();
                        ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
                        for (int i = 0; i < mClipData.getItemCount(); i++) {

                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri uri = item.getUri();
                            mArrayUri.add(uri);
                            // Get the cursor
                            Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                            // Move to first row
                            cursor.moveToFirst();

                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                            imageEncoded  = cursor.getString(columnIndex);
                            imagesEncodedList.add(imageEncoded);
                            cursor.close();
                        }
                    }
                }

            mLoadGalleryListener.onLoadGalleryMulti(imagesEncodedList);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public interface LoadGalleryListener {
        public void onLoadGallery(String path);
        public void onLoadGalleryMulti(List<String> paths);
    }
}

