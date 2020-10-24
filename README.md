# HollowKit

自己常用的一些工具的合集

### 1、[BehaviorScrollView 帮你解决各种嵌套滚动问题](https://juejin.im/post/5f16f825e51d45346c5117c4)

`BehaviorScrollView` 是对嵌套滚动共性逻辑的封装，实现了对 touch 事件、嵌套滚动和 fling 的拦截和处理的通用逻辑，支持多级嵌套和水平垂直切换。方便复用和扩展，可以十分轻松地实现底部弹层、下拉刷新、下拉二楼、划动删除等嵌套滚动场景。  

- 底部弹层和联动：  
![image](https://github.com/funnywolfdadada/HollowKit/blob/master/screenshot/linkage.gif)  

- 下拉二楼：  
![image](https://github.com/funnywolfdadada/HollowKit/blob/master/screenshot/second_floor.gif)  

- 滑动展开和删除：  
![image](https://github.com/funnywolfdadada/HollowKit/blob/master/screenshot/jelly.gif)  


### 2、[仿写豆瓣详情页](https://juejin.im/post/6844904137713270797)
记录了自己学习嵌套滚动造轮子的过程，总结产出了嵌套滚动的处理方式，并在此基础上产出了上面的 [BehaviorScrollView](https://juejin.im/post/5f16f825e51d45346c5117c4)。

## Dependency
```
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}

dependencies {
    implementation 'com.github.funnywolfdadada:HollowKit:1.5'
}
```

