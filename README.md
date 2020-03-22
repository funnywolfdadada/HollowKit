# HollowKit

自己常用的一些工具地合集，目前有：
- `SimpleAdapter`：一个简单高效地 `Recycler.Adapter`，demo 及说明可以参考：[一百行代码造一个 RecyclerView.Adapter 轮子](https://github.com/funnywolfdadada/Blog/blob/master/SimpleAdapter/%E4%B8%80%E7%99%BE%E8%A1%8C%E4%BB%A3%E7%A0%81%E9%80%A0%E4%B8%80%E4%B8%AARecyclerView.Adapter%E8%BD%AE%E5%AD%90.md)
- `LiveList.kt`: 封装了列表操作和 `Recycler.Adapter`，变更列表会自动 notity 
- `RecyclerViewLoadMore`: 用于 `RecyclerView` 快到底部时触发「加载更多」
- `LiveDataUtils.kt`: 更新 `MutableLiveData` 的工具
- `ResponseUtils.kt`: `Response` 数据剥离的工具
- `Runner.kt`: 在主线程或者某个 `Lifecycle.Event` 执行 `Runnable`

## Dependency
```
allprojects {
    repositories {
        maven { url "https://raw.githubusercontent.com/funnywolfdadada/repository/master" }
    }
}

dependencies {
    implementation "com.funnywolf:hollowkit:$last_version"
}
```

