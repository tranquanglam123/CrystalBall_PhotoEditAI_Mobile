package com.samhung.crystalball.photoeditor.Help;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.samhung.crystalball.Common.BaseActivity;
import com.samhung.crystalball.photoeditor.Help.fragments.HelpDataFragment;
import com.samhung.crystalball.photoeditor.Help.fragments.HelpMenuFragment;
import com.samhung.crystalball.photoeditor.MainActivity;
import com.samhung.crystalball.photoeditor.R;
import com.samhung.crystalball.photoeditor.Utilis.zipfile.ZipResourceFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class HelpActivity extends BaseActivity {
    public ArrayList<HelpContentsInfo> m_listHelpContents = null;
    ListView listViewContents;
    ViewPager helpViewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        InitHelpContents();
        InitControls();
    }

    void InitHelpContents() {
        m_listHelpContents = new ArrayList<>();
        m_listHelpContents.add(new HelpContentsInfo(0, "1. 프로그람소개","1.htm"));
        m_listHelpContents.add(new HelpContentsInfo(0, "2. 사진합성","2.htm"));
        m_listHelpContents.add(new HelpContentsInfo(1, "    2.1. 인물화상추출","2.1.htm"));
        m_listHelpContents.add(new HelpContentsInfo(1, "    2.2. 배경합성","2.2.htm"));
        m_listHelpContents.add(new HelpContentsInfo(0, "3. 화상가공","3.htm"));
        m_listHelpContents.add(new HelpContentsInfo(1, "    3.1. 화상편집","3.1.htm"));
        m_listHelpContents.add(new HelpContentsInfo(1, "    3.2. 얼굴가공","3.2.htm"));
        m_listHelpContents.add(new HelpContentsInfo(0, "4. 사진양상","4.htm"));
        m_listHelpContents.add(new HelpContentsInfo(0, "5. 장식추가","5.htm"));
    }

    public void InitControls() {
        helpViewPager = (ViewPager)findViewById(R.id.help_viewPager);
        final List<Fragment> fragmentsList = new ArrayList<Fragment>();
        fragmentsList.add(new HelpMenuFragment());
        for(int i=0; i<m_listHelpContents.size(); i++) {
            HelpDataFragment fragment = new HelpDataFragment();
            Bundle args = new Bundle();
            args.putString("filename", m_listHelpContents.get(i).m_fileName);
            fragment.setArguments(args);
            fragmentsList.add(fragment);
        }
        HelpSlidePagerAdapter adapter = new HelpSlidePagerAdapter(getSupportFragmentManager(), fragmentsList);
        helpViewPager.setAdapter(adapter);
       // helpViewPager.setOffscreenPageLimit(2);
        helpViewPager.setCurrentItem(0);

    }

    @Override
    public void onBackPressed() {
        if(helpViewPager.getCurrentItem() != 0){
            helpViewPager.setCurrentItem(0);
        } else {
            super.onBackPressed();
        }
    }

    public void onButtonClick(View view) {
        if(view.getId() == R.id.button_close) {
            if(helpViewPager.getCurrentItem() != 0){
                helpViewPager.setCurrentItem(0);
            } else {
                finish();
            }
        }
    }

    public HelpContentsAdapter makeAdapter() {
        HelpContentsAdapter contentsAdapter = new HelpContentsAdapter(this, R.layout.item_help_contents_row, m_listHelpContents);
        return contentsAdapter;
    }
    public class HelpContentsInfo {
        public String m_strText;
        public int m_nLevel;
        public String m_fileName;
        public HelpContentsInfo(int nLevel, String text, String fileName) {
            m_nLevel = nLevel;
            m_strText = text;
            m_fileName = fileName;
        }
    }

    public AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            helpViewPager.setCurrentItem(position + 1);
        }
    };

    public class HelpContentsAdapter extends BaseAdapter {
        LayoutInflater m_inflate;
        int m_layout;
        ArrayList<HelpContentsInfo> m_listConents;

        //    @RequiresApi(api = Build.VERSION_CODES.M)
        public HelpContentsAdapter(Context context, int alayout, ArrayList<HelpContentsInfo> infoArray) {
            m_inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            m_listConents = infoArray;
            m_layout = alayout;
        }

        @Override
        public int getCount() {
            return m_listConents.size();
        }

        @Override
        public Object getItem(int position) {
            return m_listConents.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            String str;

            if (convertView == null) {
                convertView = m_inflate.inflate(m_layout, parent, false);
            }
            TextView textView = (TextView)convertView.findViewById(R.id.textView_contents);
            textView.setText(m_listConents.get(position).m_strText);
            if(m_listConents.get(position).m_nLevel == 0) {
                textView.setTextAppearance(HelpActivity.this, R.style.boldText);
            }
            return convertView;
        }
    }

    private class HelpSlidePagerAdapter extends FragmentStatePagerAdapter {
        private List<Fragment> mFragments;

        HelpSlidePagerAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            mFragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            if (mFragments == null) {
                return (null);
            }
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }
    }

    public static void copyHelpData() {
        String toPath = "/data/data/" + MainActivity.mainActivity.getPackageName()+"/help/";
        File f = new File(toPath);
        if(f.exists()) return;

        if(false == f.mkdir()) return;

        ZipResourceFile.ZipEntryRO[] entryROS = MainActivity.gZipResourceFile.getList("assets/help");
        for(int i=0; i<entryROS.length; i++) {

            File ff = new File(entryROS[i].mFileName);
            try {
                InputStream is= null;// = MainActivity.gZipResourceFile.getInputStream(entryROS[i].mFileName);
                if(entryROS[i].mFileName.contains("_files")) {

                    File subDir = new File(toPath + ff.getName());
                    boolean bRslt = subDir.mkdir();

                    ZipResourceFile.ZipEntryRO[] subEntryOS = MainActivity.gZipResourceFile.getList("assets/help/"+ff.getName());
                    for(int j=0; j<subEntryOS.length; j++) {
                        File ffs = new File(subEntryOS[j].mFileName);
                        String subFileName = ffs.getName();
                        is = MainActivity.gZipResourceFile.getInputStream(subEntryOS[j].mFileName);
                        CopyFile(is, toPath+ff.getName()+"/"+subFileName);
                    }
                } else {
                    is = MainActivity.gZipResourceFile.getInputStream(entryROS[i].mFileName);
                    CopyFile(is, toPath+ff.getName());
                }
            }catch (Exception ex) {}
        }

    }

    static void CopyFile(InputStream is, String filePath) {
        byte[] data = new byte[102400];
        try{
            FileOutputStream fos = new FileOutputStream(filePath);
            int nRead=0;
            while((nRead = is.read(data, 0, 102400)) > 0) {
                fos.write(data, 0, nRead);
            }
            is.close();
            fos.close();
        }catch (Exception ex) {}
    }
}
