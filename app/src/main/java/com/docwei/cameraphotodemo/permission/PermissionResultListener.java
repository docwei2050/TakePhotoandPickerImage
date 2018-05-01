package com.docwei.cameraphotodemo.permission;

import java.util.List;

/**
 *
 */
public interface PermissionResultListener {
    /**
     *同意
     */
    void onGranted();

    /**
     * 拒绝
     */
    void onDenied(List<String> permissions);
}
