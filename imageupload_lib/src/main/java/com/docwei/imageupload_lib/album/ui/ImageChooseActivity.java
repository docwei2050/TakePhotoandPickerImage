package com.docwei.imageupload_lib.album.ui;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.SharedElementCallback;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.docwei.imageupload_lib.GlideApp;
import com.docwei.imageupload_lib.R;
import com.docwei.imageupload_lib.album.GriditemDecoration;
import com.docwei.imageupload_lib.album.ImageChooseAdapter;
import com.docwei.imageupload_lib.album.SelectPhotosVH;
import com.docwei.imageupload_lib.album.bean.AlbumInfo;
import com.docwei.imageupload_lib.album.bean.ImageBean;
import com.docwei.imageupload_lib.album.type.UsageType;
import com.docwei.imageupload_lib.album.type.UsageTypeConstant;
import com.docwei.imageupload_lib.constant.ImageConstant;
import com.docwei.imageupload_lib.dialog.DialogPlus;
import com.docwei.imageupload_lib.utils.DensityUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by wk on 2018/4/29.
 * 图片选择
 */

public class ImageChooseActivity extends AppCompatActivity implements ImageChooseAdapter.OnSelectImageListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 0;
    private static final int REQUEST_CODE_PREVIEW = 100;
    private static final String CHOOSE_PHOTO_MAX_COUNT = "param_image_max_count";
    public static String IMAGES = "images";
    private final String QUERY_ORDER = " desc";
    private final String ALL_ALBUM = "所有图片";
    private TextView mTv_count;
    private TextView mTv_title;
    private TextView mTv_select;
    private RecyclerView mRecyclerView;
    private int mMaxImageCount = 0;
    private ImageChooseAdapter mAdapter;
    private boolean isLoadComplete = false;
    //左下角的文件夹列表
    private List<AlbumInfo> mAlbumInfos;
    private LinearLayout mLl_select;
    private FrameLayout mFr_anchor;
    private DialogPlus mDialog;
    private Button mBtn_preview;
    private List<ImageBean> mSelectImages;
    private View mOverlay;
    private LinkedHashMap<String, List<ImageBean>> mAlbumMap = new LinkedHashMap<>();
    private ImageView mIv_back;
    private String mType;
    private FrameLayout mFr_toolbar;

    public static void startForResult(Activity activity, int imageCount, int requestCode, @UsageType String type) {
        Intent intent = new Intent(activity, ImageChooseActivity.class);
        intent.putExtra(CHOOSE_PHOTO_MAX_COUNT, imageCount);
        intent.putExtra(ImageConstant.TYPE, type);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void startUp(Activity activity, int imageCount, @UsageType String type) {
        Intent intent = new Intent(activity, ImageChooseActivity.class);
        intent.putExtra(CHOOSE_PHOTO_MAX_COUNT, imageCount);
        intent.putExtra(ImageConstant.TYPE, type);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //设置状态栏颜色
            window.setStatusBarColor(getResources().getColor(R.color.color_status_bar));
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_select);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        mTv_count = findViewById(R.id.tv_count);
        mTv_title = findViewById(R.id.tv_title);
        mFr_toolbar = findViewById(R.id.fr_toolbar);
        mRecyclerView = findViewById(R.id.recycler_view);
        mTv_select = findViewById(R.id.tv_select);
        mLl_select = findViewById(R.id.ll_select);
        mFr_anchor = findViewById(R.id.fr_anchor);
        mBtn_preview = findViewById(R.id.btn_preview);
        mOverlay = findViewById(R.id.tv_overlay);
        mIv_back = findViewById(R.id.iv_back);
    }

    private void initData() {
        mTv_title.setText(ALL_ALBUM);
        Intent intent = getIntent();
        mMaxImageCount = intent.getIntExtra(CHOOSE_PHOTO_MAX_COUNT, 0);
        mType = intent.getStringExtra(ImageConstant.TYPE);
        mTv_count.setText(R.string.image_complete);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        mRecyclerView.addItemDecoration(new GriditemDecoration(10, 10, Color.WHITE));
        mAdapter = new ImageChooseAdapter(mMaxImageCount, this, mType);
        mRecyclerView.setAdapter(mAdapter);
        getLoaderManager().initLoader(LOADER_ID, null, this);

        if (UsageTypeConstant.HEAD_PORTRAIT.equals(mType)) {
            mFr_toolbar.setVisibility(View.GONE);
            mBtn_preview.setVisibility(View.GONE);
        }

    }

    private void initListener() {
        mTv_count.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int width = mTv_count.getWidth();
                        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mOverlay.getLayoutParams();
                        layoutParams.width = width;
                        layoutParams.height = mTv_count.getHeight();
                        mOverlay.setLayoutParams(layoutParams);
                        mTv_count.getViewTreeObserver()
                                .removeOnGlobalLayoutListener(this);
                    }
                });
        mTv_count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(IMAGES, getImagePath());
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        mLl_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                    return;
                }
                if (isLoadComplete) {
                    if (mAlbumInfos == null) {
                        mAlbumInfos = getAlbumInfos();
                    }
                    show();
                }
            }
        });
        mBtn_preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreviewAllSelectActivity.startActivityForResult(getImagePath(), ImageChooseActivity.this, REQUEST_CODE_PREVIEW);
            }
        });
        mAdapter.setOnImagePreviewListener(new ImageChooseAdapter.OnImagePreviewOrCropListener() {
            @Override
            public void previewImage(String imagePath, View iv) {
                ActivityCompat.setExitSharedElementCallback(ImageChooseActivity.this,
                        new SharedElementCallback() {
                            @Override
                            public void onSharedElementEnd(List<String> sharedElementNames,
                                                           List<View> sharedElements,
                                                           List<View> sharedElementSnapshots) {
                                super.onSharedElementEnd(sharedElementNames,
                                        sharedElements,
                                        sharedElementSnapshots);
                                for (View view : sharedElements) {
                                    if (view instanceof FrameLayout) {
                                        view.setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                        });
                PreviewSingleImageActivity.startActivity(ImageChooseActivity.this, imagePath, iv);
            }

            @Override
            public void cropImage(String imagePath) {
                CropImageActivity.startActivity(ImageChooseActivity.this, imagePath);
                finish();
            }
        });
        mIv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @NonNull
    private ArrayList<String> getImagePath() {
        ArrayList<String> list = new ArrayList<>();
        for (ImageBean info : mSelectImages) {
            list.add(info.getImagePath());
        }
        return list;
    }

    public List<AlbumInfo> getAlbumInfos() {
        List<AlbumInfo> list = new ArrayList<>();
        if (mAlbumMap.size() > 0) {
            for (Map.Entry<String, List<ImageBean>> entry : mAlbumMap.entrySet()) {
                List<ImageBean> images = entry.getValue();
                if (images.size() == 0) {
                    continue;
                }
                AlbumInfo info = new AlbumInfo();
                info.setAlbumName(entry.getKey())
                        .setPhotoCounts(entry.getValue().size())
                        .setFirstPhoto(images.get(0).imagePath)
                        .setSelect(ALL_ALBUM.equals(entry.getKey()));
                list.add(info);
            }
        }
        return list;
    }


    private void show() {
        SelectPhotosVH viewHolder = new SelectPhotosVH(this, mAlbumInfos);

        mDialog = DialogPlus.newDialog(this)
                .setContentHolder(viewHolder)
                .setContentHeight(DensityUtil.dip2px(this, 350))
                .setFromWhichView(false)
                .setAnchorView(mFr_anchor)
                .setCancelable(true)
                .setGravity(Gravity.BOTTOM)
                .create();
        mDialog.show();
        viewHolder.setSelectAlbumListener(new SelectPhotosVH.ISelectAlbumListener() {
            @Override
            public void selectAlbum(AlbumInfo albumInfo) {
                List<ImageBean> list = mAlbumMap.get(albumInfo.getAlbumName());
                mTv_title.setText(albumInfo.getAlbumName());
                mTv_select.setText(albumInfo.getAlbumName());
                mAdapter.updateData(list);
            }
        });
    }

    @Override
    public void selectIsFull() {
        Toast.makeText(this,
                String.format(Locale.getDefault(), getString(R.string.image_select_pic_number_at_most), mMaxImageCount),
                Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public void selectImages(List<ImageBean> images) {

        if (images == null || images.size() == 0) {
            mBtn_preview.setEnabled(false);
            mTv_count.setEnabled(false);
            mOverlay.setVisibility(View.VISIBLE);
            mTv_count.setText(R.string.image_complete);
            mBtn_preview.setText(R.string.image_preview);
        } else {
            mBtn_preview.setEnabled(true);
            mTv_count.setEnabled(true);
            if (mOverlay.getVisibility() != View.GONE) {
                mOverlay.setVisibility(View.GONE);
            }
            mTv_count.setText(String.format(Locale.getDefault(), getString(R.string.image_complete_number), images.size(),
                    mMaxImageCount));
            mBtn_preview.setText(String.format(Locale.getDefault(), getString(R.string.image_preview_number), images.size()));
            mSelectImages = images;
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = new String[]{MediaStore.Images.Media._ID,
                MediaStore.Images.Media.BUCKET_ID,
                //直接包含该文件的文件夹id
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                //直接包含该文件的文件夹名字
                MediaStore.Images.Media.DATA
                //图片的绝对路径
        };
        return new CursorLoader(this, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, "",
                null, MediaStore.Images.ImageColumns.DATE_ADDED.concat(QUERY_ORDER));
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            List<ImageBean> imagePathList = new ArrayList<>();
            int pathColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            do {
                String imagePath = cursor.getString(pathColumn);
                String albumName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME));
                if (TextUtils.isEmpty(imagePath)) {
                    continue;
                }
                File file = new File(imagePath);
                if (file.length() <= 0) {
                    continue;
                }
                /*if (imagePath.endsWith("gif")) {
                    continue;
                }*/
                ImageBean bean = new ImageBean(imagePath);
                if (!TextUtils.isEmpty(albumName)) {
                    //需要添加的图片的文件夹
                    List<ImageBean> images = mAlbumMap.get(albumName);
                    if (images == null) {
                        images = new ArrayList<>();
                        mAlbumMap.put(albumName, images);
                    }
                    images.add(bean);
                }
                mAlbumMap.put(ALL_ALBUM, imagePathList);
                imagePathList.add(bean);
                isLoadComplete = true;
            } while (cursor.moveToNext());
            cursor.close();
            mAdapter.updateData(imagePathList);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PREVIEW && resultCode == RESULT_OK) {
            if (data != null) {
                Intent intent = new Intent(data);
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // ImageSelectProxyActivity是一个透明act，手动触发其onNewIntent调用
        Intent intent = new Intent(ImageChooseActivity.this, ImageSelectProxyActivity.class);
        startActivity(intent);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        GlideApp.get(this).clearMemory();
    }
}
