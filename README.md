# TakePhotoandPickerImage
图片拍照压缩上传
1.选择相册和拍照是分开的；

2选择相册通过通过contentResolver查询系统数据库获取图片的uri
拍照适配最新的7.0和8.0，不同版本区分直接的uri和封装后的uri（fileProvider）；

3.图片显示使用glide，整个界面仿照微信朋友圈上传，但未对图片进行编辑处理，
压缩使用第三方，luban压缩

4.上传请自行实现


截图如下
<img src="https://github.com/docwei2050/TakePhotoandPickerImage/blob/master/screenshot/Screenshot_20180501-111017.jpg" width=270 height=480 /><img src="https://github.com/docwei2050/TakePhotoandPickerImage/blob/master/screenshot/Screenshot_20180501-112433.jpg" width=270 height=480 />

<img src="https://github.com/docwei2050/TakePhotoandPickerImage/blob/master/screenshot/Screenshot_20180501-111029.jpg" width=270 height=480 /><img src="https://github.com/docwei2050/TakePhotoandPickerImage/blob/master/screenshot/Screenshot_20180501-111050.jpg" width=270 height=480 />
