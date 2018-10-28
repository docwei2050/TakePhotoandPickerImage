
#### 整体思路：把图片上传的逻辑封装到一个透明Activity（ImageSelectProxyActivity）去完成，减少宿主页面逻辑。
##### 使用(两步走)：
###### 1.启动相册、拍照的弹窗：
>  场景一：类似于发朋友圈上传图片：不涉及裁剪图片
          > >```ImageSelectProxyActivity.selectImage(ImageActivity.this, UsageTypeConstant.OTHER, 9); ```
>  场景二：更换头像：裁剪图片
         > > ``` ImageSelectProxyActivity.selectImage(ImageActivity.this, UsageTypeConstant.HEAD_PORTRAIT, 1); ```
###### 2.接收数据：
  ```@Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK == resultCode) {
            if (requestCode == ImageConstant.REQUEST_CODE_IAMGES) {
                ArrayList<String> list = (ArrayList<String>) data.getSerializableExtra(ImageConstant.SELECTED_IAMGES);
                //场景一：发朋友圈上传、评论等上传 这里不裁剪
                if (mType.equals(UsageTypeConstant.OTHER)) {
                    mImagesAdapter.updateDataFromAlbum(list);
                }
  
                if (mType.equals(UsageTypeConstant.HEAD_PORTRAIT)) {
                    //场景二：头像等上传 有裁剪操作
                    if (list != null && list.size() > 0) {
                        GlideApp.with(this).load(list.get(0)).circleCrop().into(mIv_logo);
                    }
                }
            }
        }
    }
 ```





##### 截图如下

<img src="https://github.com/docwei2050/TakePhotoandPickerImage/blob/master/screenshot/Screenshot_20181028-003555.png" width=270 height=480 />  <img src="https://github.com/docwei2050/TakePhotoandPickerImage/blob/master/screenshot/Screenshot_20181028-003559.png" width=270 height=480 />

<img src="https://github.com/docwei2050/TakePhotoandPickerImage/blob/master/screenshot/Screenshot_20181028-003614.png" width=270 height=480 />  <img src="https://github.com/docwei2050/TakePhotoandPickerImage/blob/master/screenshot/Screenshot_20181028-003642.png" width=270 height=480 />


##### 1.选择相册和拍照是分开的；
##### 2选择相册通过通过contentResolver查询系统数据库获取图片的uri
拍照适配最新的7.0和8.0，不同版本区分直接的uri和封装后的uri（fileProvider）；

##### ps：裁剪逻辑 参考 https://github.com/jeasonlzy/ImagePicker
##### 压缩使用第三方，luban压缩
##### 图片加载使用glide
##### 使用到了photoView

