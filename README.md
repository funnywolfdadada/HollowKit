# HollowKit

自己常用的一些工具的合集，目前有：
- `SimpleAdapter`：一个简单高效地 `Recycler.Adapter`，demo 及说明可以参考：[一百行代码造一个 RecyclerView.Adapter 轮子](https://juejin.im/post/5e773cb8518825494822eabd)
- `LiveList.kt`: 封装了列表操作和 `Recycler.Adapter`，变更列表会自动 notity 
- `RecyclerViewLoadMore`: 用于 `RecyclerView` 快到底部时触发「加载更多」
- `LiveDataUtils.kt`: 更新 `MutableLiveData` 的工具
- `ResponseUtils.kt`: `Response` 数据剥离的工具
- `Runner.kt`: 在主线程或者某个 `Lifecycle.Event` 执行 `Runnable`
- `BottomSheetLayout`: 底部弹层，[仿写豆瓣详情页（二）底部浮层](https://juejin.im/post/5ea3fc386fb9a03c7a333830)
- `LinkedScrollView`: 一个上下联动的滚动结构，[仿写豆瓣详情页（三）内容列表](https://juejin.im/post/5ea3ffade51d4546ca30ccec)
- `JellyLayout`: 弹性布局

## Dependency
```
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}

dependencies {
    implementation 'com.github.funnywolfdadada:HollowKit:1.0'
}
```

