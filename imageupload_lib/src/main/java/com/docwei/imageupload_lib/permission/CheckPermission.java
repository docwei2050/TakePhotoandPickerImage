package com.docwei.imageupload_lib.permission;

import android.content.Context;

/**
 *
 */
public class CheckPermission {

    private static CheckPermission        mInstance;
    private        CheckPermissionManager mAcpManager;

    public static CheckPermission getInstance(Context context) {
        if (mInstance == null)
            synchronized (CheckPermission.class) {
                if (mInstance == null) {
                    mInstance = new CheckPermission(context.getApplicationContext());
                }
            }
        return mInstance;
    }

    private CheckPermission(Context context) {
        mAcpManager = new CheckPermissionManager(context);
    }

    /**
     * 开始请求
     *
     * @param options
     * @param acpListener
     */
    public void request(PermissionOptions options, PermissionResultListener acpListener) {
        if (options == null) new NullPointerException("PermissionOptions is null...");
        if (acpListener == null) new NullPointerException("PermissionResultListener is null...");
        mAcpManager.request(options, acpListener);
    }

    CheckPermissionManager getAcpManager() {
        return mAcpManager;
    }

}
