package com.example.admin.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.ArrayList;

import website.xiaoming.CenterSelectedSwipeLibrary.CenterSelectedSwipeLayout;

public class DemoActivity extends AppCompatActivity {

    ArrayList<Integer> mUnSelectedIcons;
    ArrayList<Integer> mSelectedIcons;
    ArrayList<String> mCatalogs;

    CenterSelectedSwipeLayout css;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        initData();
        css = (CenterSelectedSwipeLayout) findViewById(R.id.css);
        css.setData(mUnSelectedIcons, mSelectedIcons, mCatalogs);
    }

    private void initData() {
        mUnSelectedIcons = new ArrayList<>();
        mSelectedIcons = new ArrayList<>();
        mCatalogs = new ArrayList<>();

        mUnSelectedIcons.add(R.drawable.icon_dy0_34px);
        mUnSelectedIcons.add(R.drawable.icon_ty0_34px);
        mUnSelectedIcons.add(R.drawable.icon_wd0_34px);
        mUnSelectedIcons.add(R.drawable.icon_yy0_34px);
        mUnSelectedIcons.add(R.drawable.icon_yyue0_34px);
//        mUnSelectedIcons.add(R.drawable.icon_ty0_34px);


        mSelectedIcons.add(R.drawable.icon_dy1_34px);
        mSelectedIcons.add(R.drawable.icon_ty1_34px);
        mSelectedIcons.add(R.drawable.icon_wd1_34px);
        mSelectedIcons.add(R.drawable.icon_yy1_34px);
        mSelectedIcons.add(R.drawable.icon_yyue1_34px);
//        mSelectedIcons.add(R.drawable.icon_ty1_34px);

        mCatalogs.add("电影");
        mCatalogs.add("投影");
        mCatalogs.add("舞蹈");
        mCatalogs.add("语音");
        mCatalogs.add("音乐");
//        mCatalogs.add("眼睛");
    }

    public void pre(View view) {
        css.previous();
    }

    public void next(View view) {
        css.next();
    }
}
