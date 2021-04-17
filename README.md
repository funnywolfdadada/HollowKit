# HollowKit

自己常用的一些工具的合集

### [OkHttp 上传下载的进度监听](https://juejin.cn/post/6952012430251655204/)
实现简单通用的 OkHttp 上传和下载的进度监听。用法：
``` kotlin
// 初始化 client 时，添加下拦截器
OkHttpClient.Builder()
    .addInterceptor(ProgressIntercept)
    .build()
// 上传和下载进度监听
Request.Builder()
    .uploadProgress(object: OkUploadListener {
        override fun upload(curr: Long, contentLength: Long) {
            // 当前上传的长度和需要上传的总长度
            Log.d(tag, "Upload: $curr/$contentLength")
        }
    })
    .downloadProgress(object: OkDownloadListener {
        override fun download(curr: Long, contentLength: Long) {
            // 当前下载的长度和需要下载的总长度
            Log.d(tag, "Download: $curr/$contentLength")
        }
    })
```

### [BehaviorScrollView 帮你解决各种嵌套滚动问题](https://juejin.im/post/5f16f825e51d45346c5117c4)

`BehaviorScrollView` 是对嵌套滚动共性逻辑的封装，实现了对 touch 事件、嵌套滚动和 fling 的拦截和处理的通用逻辑，支持多级嵌套和水平垂直切换。方便复用和扩展，可以十分轻松地实现底部弹层、下拉刷新、下拉二楼、划动删除等嵌套滚动场景。  

- 底部弹层和联动：  
![底部弹层和联动](https://raw.githubusercontent.com/funnywolfdadada/HollowKit/master/screenshot/linkage.gif)  

- 下拉二楼：  
![下拉二楼](https://raw.githubusercontent.com/funnywolfdadada/HollowKit/master/screenshot/second_floor.gif)  

- 滑动展开和删除：  
![滑动展开和删除](https://raw.githubusercontent.com/funnywolfdadada/HollowKit/master/screenshot/jelly.gif)  


### [仿写豆瓣详情页](https://juejin.im/post/6844904137713270797)
记录了自己学习嵌套滚动造轮子的过程，总结产出了嵌套滚动的处理方式，并在此基础上产出了上面的 [BehaviorScrollView](https://juejin.im/post/5f16f825e51d45346c5117c4)。

## Dependency
```
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}

dependencies {
    implementation 'com.github.funnywolfdadada:HollowKit:1.6'
}
```

