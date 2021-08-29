package com.funnywolf.hollowkit.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.funnywolf.hollowkit.app.R
import com.funnywolf.hollowkit.app.fragments.behavior.ScrollBehaviorFragment
import com.funnywolf.hollowkit.app.fragments.douban.DoubanDetailFragment

/**
 * @author https://github.com/funnywolfdadada
 * @since 2021/9/5
 */
class MainFragment: Fragment() {
    var open: ((fragmentClass: Class<out Fragment>)->Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(inflater.context).apply {
            setContent { MainUi(open) }
        }
    }

}

@Preview
@Composable
private fun MainUi(open: ((fragmentClass: Class<out Fragment>)->Unit)? = null) {
    Image(
        painter = painterResource(id = R.drawable.bg_main),
        contentDescription = null,
        contentScale = ContentScale.Crop
    )
    val names = listOf(
        "TEST" to TestFragment::class.java,
        "嵌套滚动" to ScrollBehaviorFragment::class.java,
        "富文本测试" to RichTextFragment::class.java,
        "stateful_layout" to StatefulLayoutFragment::class.java,
        "OkHttp 进度监听" to OkHttpProgressFragment::class.java,
        "权限申请" to PermissionRequestFragment::class.java,
        "豆瓣详情页" to DoubanDetailFragment::class.java,
        "带展开的 TextView" to ExpandableTextFragment::class.java,
    )
    LazyColumn(
        Modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(names.size) {
            NavigatorButton(names[it].first) { open?.invoke(names[it].second) }
        }
    }
}

@Composable
private fun NavigatorButton(text: String, onClick: ()->Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            Color(0x80000000),
            Color.White
        ),
    ) {
        Text(text = text)
    }
}