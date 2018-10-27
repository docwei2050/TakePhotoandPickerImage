package com.docwei.imageupload_lib.album.type;

import android.support.annotation.StringDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.docwei.imageupload_lib.album.type.UsageTypeConstant.HEAD_PORTRAIT;
import static com.docwei.imageupload_lib.album.type.UsageTypeConstant.OTHER;

@StringDef({HEAD_PORTRAIT, OTHER})
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
public @interface UsageType {

}