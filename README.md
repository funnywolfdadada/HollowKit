# HollowKit

自己常用的一些工具的合集，目前有：
- `SimpleAdapter`：一个简单高效地 `Recycler.Adapter`，demo 及说明可以参考：[一百行代码造一个 RecyclerView.Adapter 轮子](https://juejin.im/post/5e773cb8518825494822eabd)
- `LiveList.kt`: 封装了列表操作和 `Recycler.Adapter`，变更列表会自动 notity 
- `RecyclerViewLoadMore`: 用于 `RecyclerView` 快到底部时触发「加载更多」
- `BottomSheetLayout`: 底部弹层，[仿写豆瓣详情页（二）底部浮层](https://juejin.im/post/5ea3fc386fb9a03c7a333830)
- `LinkedScrollView`: 一个上下联动的滚动结构，[仿写豆瓣详情页（三）内容列表](https://juejin.im/post/5ea3ffade51d4546ca30ccec)
- `JellyLayout`: 弹性布局，[仿写豆瓣详情页（四）弹性布局](https://juejin.im/post/5eb2c471e51d454d980e3db7)
- `BehavioralScrollView`: 帮你解决各种嵌套滚动问题，[BehaviorScrollView 帮你解决各种嵌套滚动问题](https://juejin.im/post/5f16f825e51d45346c5117c4)

## Dependency
```
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}

dependencies {
    implementation 'com.github.funnywolfdadada:HollowKit:1.4'
}
```

