package com.funnywolf.hollowkit.scenes.douban

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bytedance.scene.group.GroupScene
import com.funnywolf.hollowkit.R
import com.funnywolf.hollowkit.scenes.douban.view.BOTTOM_SHEET_STATE_EXTENDED
import com.funnywolf.hollowkit.utils.*
import com.funnywolf.hollowkit.scenes.douban.view.DoubanDetailView

/**
 * 豆瓣详情页
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/4/10
 */
class DoubanDetailScene : GroupScene() {

    private lateinit var doubanDetailView: DoubanDetailView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle?
    ): ViewGroup {
        doubanDetailView =
            DoubanDetailView(inflater.context)
        return doubanDetailView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        initTopRecycler()
        initBottomLayout()
    }

    private fun initToolbar() {
        doubanDetailView.setBackgroundColor(westWorldHolderBackgroundColor)
        doubanDetailView.toolBar.setup(
            "电视", "西部世界 第三季",
            R.drawable.poster_westworld_season_3,
            westWorldHolderBackgroundColor
        ).setListeners(
            View.OnClickListener { it.context.toast("Click back") },
            View.OnClickListener { it.context.toast("Click more") }
        )
    }

    private fun initTopRecycler() {
        val list = ArrayList<Any>()
        list.add(DoubanHeader(
            R.drawable.poster_westworld_season_3,
            "西部世界 第三季",
            "Westworld Season 3 (2020)",
            """
                导演: 乔纳森·诺兰 / 詹妮弗·盖辛格 / 理查德·J·刘易斯 / 保罗·卡梅隆 / 安娜·福斯特 / 阿曼达·马尔萨利斯 / 海伦·谢费
                编剧: 迈克尔·克莱顿 / 丽莎·乔伊 / 乔纳森·诺兰 / 吉娜·阿特沃特
                主演: 埃文·蕾切尔·伍德 / 坦迪·牛顿 / 泰莎·汤普森 / 杰弗里·怀特 / 艾德·哈里斯 / 更多...
            """.trimIndent()
        ))
        list.add(DoubanRating(9.2F, "9371人看过  1.8万人再看  4.1万人想看"))
        list.add(DoubanTags())
        list.add(TitleModel("简介",null))
        list.add(Brief("""
            2016年由乔纳森·诺兰与丽莎·乔伊夫妻档联合开发的剧集，故事改编自作家迈克尔·克莱顿的同名科幻电影，围绕着一个未来主题乐园展开，乐园里有很多机器人，可以帮助人们实现自己的白日梦。然而一直运转良好的机器人中途突然出了问题，局势逐渐失去控制。 
        """.trimIndent()))
        list.add(TitleModel("演职员", "全部 52"))
        list.add(Actors().apply {
            add(Actor(R.drawable.avatar_1, "乔纳森·诺兰", "导演"))
            add(Actor(R.drawable.avatar_2, "詹妮弗·盖辛格", "导演"))
            add(Actor(R.drawable.avatar_3, "埃文·蕾切尔·伍德", "饰 Dolores Abernathy"))
            add(Actor(R.drawable.avatar_4, "坦迪·牛顿", "饰 Maeve Millay"))
            add(Actor(R.drawable.avatar_5, "泰莎·汤普森", "饰 Charlotte Hale"))
            add(Actor(R.drawable.avatar_6, "杰弗里·怀特", "饰 Bernard Lowe"))
        })
        list.add(TitleModel("预告片/剧照", "全部 691"))
        list.add(Pictures().apply {
            add(Picture(R.drawable.picture_1))
            add(Picture(R.drawable.picture_2))
            add(Picture(R.drawable.picture_3))
            add(Picture(R.drawable.picture_4))
            add(Picture(R.drawable.picture_5))
            add(Picture(R.drawable.picture_6))
        })
        list.addAll(getRandomStrings(20))
        doubanDetailView.topRecyclerView.adapter = SimpleAdapter(
            list
        )
            .addHolderInfo(
                HolderInfo(
                    DoubanHeader::class.java,
                    R.layout.holder_douban_header,
                    DoubanHeaderHolder::class.java
                )
            )
            .addHolderInfo(
                HolderInfo(
                    DoubanRating::class.java,
                    R.layout.holder_douban_rating,
                    RatingHolder::class.java
                )
            )
            .addHolderInfo(
                HolderInfo(
                    DoubanTags::class.java,
                    R.layout.holder_douban_tags
                )
            )
            .addHolderInfo(
                HolderInfo(
                    TitleModel::class.java,
                    R.layout.holder_douban_title,
                    TitleHolder::class.java
                )
            )
            .addHolderInfo(
                HolderInfo(
                    Brief::class.java,
                    R.layout.holder_douban_brief,
                    BriefHolder::class.java
                )
            )
            .addHolderInfo(
                HolderInfo(
                    Actors::class.java,
                    R.layout.holder_douban_inner_list,
                    ActorsViewHolder::class.java
                )
            )
            .addHolderInfo(
                HolderInfo(
                    Pictures::class.java,
                    R.layout.holder_douban_inner_list,
                    PicturesViewHolder::class.java
                )
            )
            .addHolderInfo(createSimpleStringHolderInfo(0   ))
        doubanDetailView.topRecyclerView.layoutManager = LinearLayoutManager(view.context)
    }

    private fun initBottomLayout() {
        doubanDetailView.bottomLayout.setBackgroundColor(Color.WHITE)
        LayoutInflater.from(doubanDetailView.bottomLayout.context).inflate(R.layout.view_douban_bottom_content, doubanDetailView.bottomLayout)
        doubanDetailView.bottomLayout.findViewById<ViewPager>(R.id.view_pager)?.apply {
            val titles = listOf("标题 1", "标题 2")
            val views = SparseArray<View?>()
            adapter = object: PagerAdapter() {

                override fun instantiateItem(container: ViewGroup, position: Int): Any {
                    return views[position] ?: container.context.getRecyclerView().also {  rv ->
                        views.put(position, rv)
                        container.addView(rv)
                    }
                }

                private fun Context.getRecyclerView() = RecyclerView(this).apply {
                    simpleInit(50, 0x40000000)
                }

                override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                    val v = `object` as? View ?: return
                    container.removeView(v)
                }

                override fun getPageTitle(position: Int): CharSequence? {
                    return titles.getOrNull(position)
                }

                override fun isViewFromObject(view: View, `object`: Any): Boolean {
                    return view == `object`
                }

                override fun getCount(): Int = titles.size
            }
            addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageSelected(position: Int) {
                    if (doubanDetailView.isBottomViewFloating
                        && doubanDetailView.bottomSheetLayout.state != BOTTOM_SHEET_STATE_EXTENDED
                    ) {
                        doubanDetailView.bottomSheetLayout.setProcess(1F, true)
                    }
                }

                override fun onPageScrollStateChanged(state: Int) {
                }
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                }
            })
            doubanDetailView.bottomScrollViewProvider = { views[currentItem] }
        }
    }

}

fun createSimpleStringHolderInfo(color: Int = 0xFFF89798.toInt()): HolderInfo<String> {
    return HolderInfo(String::class.java,
            R.layout.holder_simple_view,
            onCreate = { holder ->
                holder.itemView.setBackgroundColor(color)
                holder.itemView.setOnClickListener {
                    Toast.makeText(it.context, "Clicked ${holder.data}", Toast.LENGTH_SHORT).show()
                }
            },
            onBind = { holder, data ->
                holder.v<TextView>(R.id.content)?.text = data
            })
}
