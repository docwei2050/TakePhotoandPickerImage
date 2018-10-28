package com.docwei.imageupload_lib.album.ui;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;

import com.docwei.imageupload_lib.R;
import com.docwei.imageupload_lib.album.PermissionDialog;
import com.docwei.imageupload_lib.album.TakePhotoVH;
import com.docwei.imageupload_lib.album.type.UsageType;
import com.docwei.imageupload_lib.album.type.UsageTypeConstant;
import com.docwei.imageupload_lib.constant.ImageConstant;
import com.docwei.imageupload_lib.dialog.DialogPlus;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * 此为透明act
 * 弹窗和选择图片的回调
 */
public class ImageSelectProxyActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_WRITE = 127;//读写权限申请
    private static final int REQUEST_CODE_CAMERA = 128;//相机权限
    private static final int REQUEST_CODE_SETTING = 0x39;
    private Uri mImageUri;
    private int TAKE_PHOTO = 100;
    private int SELECT_ALBUM = 101;
    private String mType;
    private int mCount;

    /**
     * @param type    使用类型
     * @param context
     * @param count   上传的数量
     */
    public static void selectImage(Activity context, @UsageType String type, int count) {
        Intent intent = new Intent(context, ImageSelectProxyActivity.class);
        intent.putExtra(ImageConstant.TYPE, type);
        intent.putExtra(ImageConstant.COUNT, count);
        context.startActivityForResult(intent, ImageConstant.REQUEST_CODE_IAMGES);
        context.overridePendingTransition(0, 0);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_select_proxy);
        Intent intent = getIntent();
        mType = intent.getStringExtra(ImageConstant.TYPE);
        mCount = intent.getIntExtra(ImageConstant.COUNT, 9);
        show(mCount);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            setResult(RESULT_OK, intent);
        }
        finish();
    }

    private void show(final int count) {

        TakePhotoVH viewHolder = new TakePhotoVH(this);

        DialogPlus dialog = DialogPlus.newDialog(this)
                .setContentHolder(viewHolder)
                .setCancelable(false)
                .setMargin(0, 100, 0, 0)
                .setGravity(Gravity.BOTTOM)
                .create();
        dialog.show();
        viewHolder.setCameraAndPhotoListener(new TakePhotoVH.ICameraAndPhotoListener() {
            @Override
            public void takePhoto() {
                //使用相机拍照显示图片，这里保存在该app的关联目录--缓存目录下，所以无需进行外置SD卡的读取权限
                //注意：调用系统相机拍照无需使用相机权限,但是清单文件声明相机权限了就一定要检测权限
                //但是由于4.4以前的系统访问关联目录--缓存目录需要sd卡权限，我们为了兼容老版本加上。。
                checkCameraPermissionBeforeTakePhoto();
            }

            @Override
            public void selectAlbum() {
                selectImageFromGallery(count);
            }
        });
    }
    private void checkCameraPermissionBeforeTakePhoto() {
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
            requestSinglePermission(Manifest.permission.CAMERA,  REQUEST_CODE_CAMERA);
        } else {
            handleTakePhoto();
        }
    }

    private void handleTakePhoto() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_" + ".jpg";
        File outputImage = new File(getExternalCacheDir(), imageFileName);
        try {
            if (outputImage.exists()) {
                outputImage.delete();
            }
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= 24) {
            mImageUri = FileProvider.getUriForFile(ImageSelectProxyActivity.this,
                    "com.docwei.imageupload_lib.fileprovider",
                    outputImage);
        } else {
            mImageUri = Uri.fromFile(outputImage);
        }
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }


    private void selectImageFromGallery(int count) {
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            requestSinglePermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,  REQUEST_CODE_WRITE);
        } else {
            selectImage(count);
        }
    }

    private void selectImage(int count) {
        //使用非系统的实现
        if (mType.equals(UsageTypeConstant.HEAD_PORTRAIT)) {
            ImageChooseActivity.startUp(ImageSelectProxyActivity.this, count, mType);

        } else {
            ImageChooseActivity.startForResult(ImageSelectProxyActivity.this, count, SELECT_ALBUM, mType);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SETTING) {
            // 从设置页面返回再次检测权限是否获取
            selectImageFromGallery(mCount);
            return;
        }
        if (RESULT_OK == resultCode) {
            ArrayList<String> images;
            Intent intent = new Intent();
            if (requestCode == TAKE_PHOTO) {
                //头像类型，需要裁剪
                if (mType.equals(UsageTypeConstant.HEAD_PORTRAIT)) {
                    CropImageActivity.startActivity(this, mImageUri.toString());
                    return;
                } else {
                    //其他类型，一律不裁剪
                    images = new ArrayList<>(1);
                    images.add(mImageUri.toString());
                    intent.putExtra(ImageConstant.SELECTED_IAMGES, images);
                }
            } else if (requestCode == SELECT_ALBUM) {
                if (data != null) {
                    intent = data;
                }
            }
            setResult(RESULT_OK, intent);
        }
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }


    //-------------------------------------- 处理读取sd卡权限-----------start-----------------
    public void requestSinglePermission(String permission,  int requestCode) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            showRationaleDialog(permission, requestCode);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
    }

    /**
     *  解释性对话框
     */
    private void showRationaleDialog(final String permission, final int requestCode) {

        PermissionDialog rationaleVh=new PermissionDialog(this);
        rationaleVh.show();
        if(requestCode==REQUEST_CODE_WRITE) {
            rationaleVh.setTitle(getString(R.string.dialog_msg_storage_title)).setContent(getString(R.string.dialog_msg_rationale_storage_content));
        }else if(requestCode==REQUEST_CODE_CAMERA){
            rationaleVh.setTitle(getString(R.string.dialog_msg_camera_title)).setContent(getString(R.string.dialog_msg_rationale_camera_content));
        }
        rationaleVh.setPermissionDialogListener(new PermissionDialog.SimplePermissionDialog() {
            @Override
            public void rightButtonEvent() {
                ActivityCompat.requestPermissions(ImageSelectProxyActivity.this, new String[]{permission}, requestCode);
            }
            @Override
            public void leftButtonEvent() {
                finish();
            }
        });


    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_WRITE:
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectImage(mCount);
                } else {
                    performUnGrant(requestCode, permissions[0]);
                }
                break;
            case REQUEST_CODE_CAMERA:
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    handleTakePhoto();
                } else {
                    performUnGrant(requestCode, permissions[0]);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    private void performUnGrant(int requestCode, String permission) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            showRationaleDialog(permission, requestCode);
        } else {
            //用户点击禁止会不再询问，就不会再弹出，请用户在设置界面再进行权限打开
            showDenyDialog(requestCode);
        }
    }

    /**
     *  用户点击不再提示，禁止后的对话框
     */
    private void showDenyDialog(int requestCode) {

        PermissionDialog dialog=new PermissionDialog(this);
        dialog.show();
        if(requestCode==REQUEST_CODE_WRITE) {
            dialog.setTitle(getString(R.string.dialog_msg_storage_title)).setContent(getString(R.string.dialog_msg_deny_storage_content))
                    .setLeftText(getString(R.string.dialog_deny)).setRightText(getString(R.string.dialog_go_setting));
        }else if(requestCode==REQUEST_CODE_CAMERA){
            dialog.setTitle(getString(R.string.dialog_msg_camera_title)).setContent(getString(R.string.dialog_msg_deny_camera_content))
                    .setLeftText(getString(R.string.dialog_deny)).setRightText(getString(R.string.dialog_go_setting));
        }

        dialog.setPermissionDialogListener(new PermissionDialog.SimplePermissionDialog() {
            @Override
            public void rightButtonEvent() {
                startSetting();
            }
            @Override
            public void leftButtonEvent() {
                finish();//因为ImageSelectProxyAct是一个透明ACT
            }
        });


    }



    //***************************适配不同手机去做跳转****************start*******************************************************
    private static final String MARK = Build.MANUFACTURER.toLowerCase();

    /**
     * 跳转到设置界面
     */
    private void startSetting() {
        Intent intent;
        if (MARK.contains("huawei")) {
            intent = huaweiApi(this);
        } else if (MARK.contains("xiaomi")) {
            intent = xiaomiApi(this);
        } else if (MARK.contains("oppo")) {
            intent = oppoApi(this);
        } else if (MARK.contains("vivo")) {
            intent = vivoApi(this);
        } else if (MARK.contains("meizu")) {
            intent = meizuApi(this);
        } else {
            intent = defaultApi(this);
        }
        try {
            startActivityForResult(intent, REQUEST_CODE_SETTING);
        } catch (Exception e) {
            go2SettingWithDefault();
        }
    }

    private void go2SettingWithDefault() {
        try {
            Intent orginalintent = defaultApi(this);
            startActivityForResult(orginalintent, REQUEST_CODE_SETTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Intent defaultApi(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", context.getPackageName(), null));
        return intent;
    }

    private static Intent huaweiApi(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return defaultApi(context);
        }
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity"));
        return intent;
    }

    private static Intent xiaomiApi(Context context) {
        Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
        intent.putExtra("extra_pkgname", context.getPackageName());
        return intent;
    }

    private static Intent vivoApi(Context context) {
        Intent intent = new Intent();
        intent.putExtra("packagename", context.getPackageName());
        intent.setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.SoftPermissionDetailActivity"));
        if (hasActivity(context, intent)) return intent;

        intent.setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.safeguard.SoftPermissionDetailActivity"));
        return intent;
    }

    private static Intent oppoApi(Context context) {
        Intent intent = new Intent();
        intent.putExtra("packageName", context.getPackageName());
        intent.setComponent(new ComponentName("com.color.safecenter", "com.color.safecenter.permission.PermissionManagerActivity"));
        return intent;
    }

    private static Intent meizuApi(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return defaultApi(context);
        }
        Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
        intent.putExtra("packageName", context.getPackageName());
        intent.setComponent(new ComponentName("com.meizu.safe", "com.meizu.safe.security.AppSecActivity"));
        return intent;
    }

    private static boolean hasActivity(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        return packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0;
    }
    //***************************适配不同手机去做跳转****************endt*******************************************************


    //-------------------------------------- 处理读取sd卡权限-----------end-----------------
}
