package com.docwei.imageupload_lib.album.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.docwei.imageupload_lib.R;
import com.docwei.imageupload_lib.album.ImagePageAdapter;
import com.docwei.imageupload_lib.constant.ImageConstant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by wk on 2018/4/29.
 */

public class PreviewAllSelectActivity extends AppCompatActivity {
    private static final String SELECTED_PHOTOS = "selected_photos";
    private ViewPager mViewPager;
    private ImageView mIv_back;
    private TextView mTv_count;
    private View mTv_overlay;
    private CheckBox mCheckBox;
    private HashMap<String, Boolean> mStatus;
    private ArrayList<String> mList;
    private TextView mTv_order;
    private ImagePageAdapter mAdapter;


    public static void startActivityForResult(ArrayList<String> list, Activity src, int request_code) {
        Intent intent = new Intent(src, PreviewAllSelectActivity.class);
        intent.putExtra(SELECTED_PHOTOS, list);
        src.startActivityForResult(intent, request_code);
    }

    public static void startActivity(ArrayList<String> list, Activity src) {
        Intent intent = new Intent(src, PreviewAllSelectActivity.class);
        intent.putExtra(SELECTED_PHOTOS, list);
        src.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //设置状态栏颜色
            window.setStatusBarColor(getResources().getColor(R.color.color_status_bar));
        }
        setContentView(R.layout.activity_preview_all);
        initView();
        initData();
        initEvent();
    }

    private void initView() {
        mViewPager = findViewById(R.id.view_pager);
        mIv_back = findViewById(R.id.iv_back);
        mTv_count = findViewById(R.id.tv_count);
        mTv_overlay = findViewById(R.id.tv_overlay);
        mCheckBox = findViewById(R.id.cb_select);
        mTv_order = findViewById(R.id.tv_order);
    }


    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            mList = (ArrayList<String>) intent.getSerializableExtra(SELECTED_PHOTOS);
        }
        if (mList == null || mList.size() < 1) {
            return;
        }
        mStatus = new HashMap<>(mList.size());
        for (String path : mList) {
            mStatus.put(path, true);
        }
        //初始化
        mCheckBox.setTag(0);
        mTv_order.setText(String.format(Locale.getDefault(), getString(R.string.image_order), 1, mList.size()));
        mAdapter = new ImagePageAdapter(mList, this);
        mViewPager.setAdapter(mAdapter);
        mTv_count.setText(getString(R.string.image_complete));

    }

    private void initEvent() {
        mTv_count.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int width = mTv_count.getWidth();
                        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mTv_overlay.getLayoutParams();
                        layoutParams.width = width;
                        mTv_overlay.setLayoutParams(layoutParams);
                        mTv_count.setText(String.format(Locale.getDefault(), getString(R.string.image_complete_number), mList.size(), mList.size()));
                        mTv_count.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mTv_order.setText(String.format(Locale.getDefault(), getString(R.string.image_order), position + 1, mList.size()));
                for (Map.Entry<String, Boolean> entry : mStatus.entrySet()) {
                    if (mList.get(position).equals(entry.getKey())) {
                        mCheckBox.setTag(position);
                        mCheckBox.setChecked(entry.getValue());
                        return;
                    }
                }
            }
        });
        mAdapter.setOnPageClickListener(new ImagePageAdapter.OnPageClickListener() {
            @Override
            public void clickPage() {
                finish();
            }
        });
        mIv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int position = (int) buttonView.getTag();
                for (Map.Entry<String, Boolean> entry : mStatus.entrySet()) {
                    if (mList.get(position).equals(entry.getKey())) {
                        entry.setValue(isChecked);
                        updateCountShow();
                        return;
                    }
                }
            }
        });
        mTv_count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> list = new ArrayList<>();
                for (Map.Entry<String, Boolean> entry : mStatus.entrySet()) {
                    if (entry.getValue()) {
                        list.add(entry.getKey());
                    }
                }
                Intent intent = new Intent();
                intent.putExtra(ImageConstant.SELECTED_IAMGES, list);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    private void updateCountShow() {
        // 每次点击checkbox需要更新显示完成的图片
        int count = 0;
        for (Map.Entry<String, Boolean> entry : mStatus.entrySet()) {
            if (entry.getValue()) {
                count++;
            }
        }
        if (count == 0) {
            mTv_overlay.setVisibility(View.VISIBLE);
            mTv_count.setText(getString(R.string.image_complete));
            mTv_count.setEnabled(false);
        } else {
            mTv_count.setEnabled(true);
            if (mTv_overlay.getVisibility() != View.GONE) {
                mTv_overlay.setVisibility(View.GONE);
            }
            mTv_count.setText(String.format(Locale.getDefault(), getString(R.string.image_complete_number), count, mList.size()));
        }
    }

}
