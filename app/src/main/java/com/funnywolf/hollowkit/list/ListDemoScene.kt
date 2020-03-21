package com.funnywolf.hollowkit.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bytedance.scene.Scene
import com.bytedance.scene.ktx.viewModels
import com.funnywolf.hollowkit.R
import com.funnywolf.hollowkit.recyclerview.*
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

/**
 * 列表 demo，会用到 [SimpleAdapter]、[RecyclerViewLoadMore] 和 [LiveList]
 *
 * @author funnywolf
 * @since 2020/3/21
 */
class ListDemoScene: Scene() {

    private val viewModel: ListViewModel by viewModels()
    private var recyclerView: RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.scene_list_demo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = findViewById<RecyclerView>(R.id.recycler)?.also {
            val adapter = initAdapter()
            viewModel.liveList.bind(adapter)
            it.adapter = adapter
            val layoutManager = GridLayoutManager(it.context, 3)
            layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val holderClass = adapter
                        .getInfoByPosition(position)
                        ?.holderClass
                    return if (holderClass == OnlineMovieViewHolder::class.java
                        || holderClass == UpcomingMovieViewHolder::class.java) { 1 } else { 3 }
                }
            }
            it.layoutManager = layoutManager
        }

    }

    private fun initAdapter() = SimpleAdapter(viewModel.liveList.get())
        .addHolderInfo(
            object: HolderInfo<TitleLine>(TitleLine::class.java, R.layout.holder_title_line) {
                override fun onCreate(holder: SimpleHolder<TitleLine>) {
                    super.onCreate(holder)
                    holder.itemView.setOnClickListener {
                        val toast = holder.data?.toast ?: return@setOnClickListener
                        Toast.makeText(it.context, toast, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onBind(holder: SimpleHolder<TitleLine>, data: TitleLine) {
                    super.onBind(holder, data)
                    holder.v<TextView>(R.id.tv_title)?.text = data.title
                    holder.v<TextView>(R.id.tv_info)?.text = data.info
                }
            }
        )
        .addHolderInfo(
            HolderInfo(Movie::class.java, R.layout.holder_online_movie,
                OnlineMovieViewHolder::class.java) { it.isOnline }
        )
        .addHolderInfo(
            HolderInfo(Movie::class.java, R.layout.holder_upcomming_movie,
                UpcomingMovieViewHolder::class.java) { !it.isOnline }
        )
        .addHolderInfo(
            HolderInfo(Recommend::class.java, R.layout.holder_recommend_movie,
                RecommendMovieViewHolder::class.java)
        )

}

class OnlineMovieViewHolder(v: View): SimpleHolder<Movie>(v) {
    private val ivPoster = v<ImageView>(R.id.iv_picture)!!
    private val tvName = v<TextView>(R.id.tv_name)!!
    private val tvDesc = v<TextView>(R.id.tv_desc)!!

    override fun onBind(data: Movie) {
        super.onBind(data)

        ivPoster.setImageResource(data.poster)
        tvName.text = data.name
        tvDesc.text = data.desc
    }

}

class UpcomingMovieViewHolder(v: View): SimpleHolder<Movie>(v) {
    private val ivPoster = v<ImageView>(R.id.iv_picture)!!
    private val tvName = v<TextView>(R.id.tv_name)!!
    private val tvDesc = v<TextView>(R.id.tv_desc)!!

    override fun onBind(data: Movie) {
        super.onBind(data)

        ivPoster.setImageResource(data.poster)
        tvName.text = data.name
        tvDesc.text = data.desc
    }

}

class RecommendMovieViewHolder(v: View): SimpleHolder<Recommend>(v) {
    private val ivPicture = v<ImageView>(R.id.iv_picture)!!
    private val tvTitle = v<TextView>(R.id.tv_title)!!
    private val tvDesc = v<TextView>(R.id.tv_desc)!!

    override fun onBind(data: Recommend) {
        super.onBind(data)

        ivPicture.setImageResource(data.picture)
        tvTitle.text = data.title
        tvDesc.text = data.desc
    }

}

class ListViewModel: ViewModel() {
    val liveList = LiveList<Any>()

    init {
        liveList.clearAddAll(ListRepository.list)
    }
}

object ListRepository {

    val list = listOf(
        TitleLine("热门电影", "全部 996", "敬请期待"),
        Movie("小妇人", "豆瓣评分 8.1", R.drawable.poster_little_women, true),
        Movie("1917", "豆瓣评分 8.5", R.drawable.poster_1917, true),
        Movie("寄生虫", "豆瓣评分 8.7", R.drawable.poster_parasite, true),
        Movie("黑水", "豆瓣评分 8.5", R.drawable.poster_dark_waters, true),
        Movie("南山的部长们", "豆瓣评分 8.1", R.drawable.poster_the_man_standing_next, true),
        Movie("逃离比勒陀利亚", "豆瓣评分 7.7", R.drawable.poster_escape_from_pretoria, true),

        TitleLine("即将上线", "全部 251", "敬请期待"),
        Movie("流浪地球", "04月上映", R.drawable.poster_the_wandering_earth, false),
        Movie("中国合伙人", "04月上映", R.drawable.poster_american_dreams_in_china, false),
        Movie("何以为家", "04月上映", R.drawable.poster_capernaum, false),
        Movie("乔乔的异想世界", "04月03日上映", R.drawable.poster_jojo_rabbit, false),
        Movie("六人-泰坦尼克上的中国幸存者", "04月04日上映", R.drawable.poster_the_six, false),
        Movie("野性的呼唤", "04月03日上映", R.drawable.poster_the_call_of_the_wild, false),

        TitleLine("热门推荐", "全部 111", "敬请期待"),
        Recommend(
            "西部世界 第三季 预告片1",
            "埃文·蕾切尔·伍德、“小粉”亚伦·保尔主演，3月15日HBO播出！",
            R.drawable.poster_westworld_season_3
        ),
        Recommend(
            "凯特大魔王主演《美国夫人》预告",
            "“大魔王”凯特·布兰切特、萝丝·拜恩、“香蕉姐”莎拉·保罗森主演，聚焦女性平权运动。",
            R.drawable.poster_mrs_america
        ),
        Recommend(
            "迪士尼《花木兰》全新中字预告",
            "Christina Aguilera 时隔22年再次献唱《花木兰》主题曲，《忠勇真Loyal Brave True》超好听！",
            R.drawable.poster_mulan
        )

    )

    fun getList() = Observable.just(list).delay(2000, TimeUnit.MILLISECONDS)

}

data class TitleLine (
    val title: String,
    val info: String,
    val toast: String
)

data class Movie (
    val name: String,
    val desc: String,
    @DrawableRes val poster: Int,
    val isOnline: Boolean
)

data class Recommend (
    val title: String,
    val desc: String,
    @DrawableRes val picture: Int
)
