package com.funnywolf.hollowkit.list

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bytedance.scene.Scene
import com.bytedance.scene.ktx.viewModels
import com.funnywolf.hollowkit.R
import com.funnywolf.hollowkit.recyclerview.*
import com.funnywolf.hollowkit.update
import com.funnywolf.hollowkit.utils.toast
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * 列表 demo，会用到 [SimpleAdapter]、[RecyclerViewLoadMore] 和 [LiveList]
 *
 * @author funnywolf
 * @since 2020/3/21
 */
class ListDemoScene: Scene() {

    private val viewModel: ListViewModel by viewModels()
    private var refresh: SwipeRefreshLayout? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.stateLiveData.observe(this, Observer {
            refresh?.isRefreshing = it == LIST_STATE_REFRESHING
        })
        viewModel.refresh()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.scene_list_demo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findViewById<RecyclerView>(R.id.recycler)?.also {
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

            RecyclerViewLoadMore(500) {
                viewModel.loadMore()
            }.setup(it)
        }
        refresh = findViewById(R.id.refresh)
        refresh?.setOnRefreshListener {
            viewModel.refresh()
        }
    }

    private fun initAdapter() = SimpleAdapter(viewModel.liveList.get())
        .addHolderInfo(
            HolderInfo(
                LoadingMore::class.java,
                R.layout.holder_loading_more
            )
        )
        .addHolderInfo(
            HolderInfo(
                TitleLine::class.java,
                R.layout.holder_title_line,
                onCreate = { holder ->
                    holder.itemView.setOnClickListener { v ->
                        val toast = holder.data?.toast ?: return@setOnClickListener
                        v.context.toast(toast)
                    }
                },
                onBind = { holder, data ->
                    holder.v<TextView>(R.id.tv_title)?.text = data.title
                    holder.v<TextView>(R.id.tv_info)?.text = data.info
                }
            )
        )
        .addHolderInfo(
            HolderInfo(
                Movie::class.java,
                R.layout.holder_online_movie,
                OnlineMovieViewHolder::class.java,
                isSupport = { it.isOnline }
            )
        )
        .addHolderInfo(
            HolderInfo(
                Movie::class.java,
                R.layout.holder_upcomming_movie,
                UpcomingMovieViewHolder::class.java,
                isSupport = { !it.isOnline }
            )
        )
        .addHolderInfo(
            HolderInfo(
                Recommend::class.java,
                R.layout.holder_recommend_movie,
                RecommendMovieViewHolder::class.java,
                onCreate = { holder ->
                    holder.itemView.setOnClickListener { v ->
                        v.context.toast(holder.data?.title)
                    }
                }
            )
        )
        .apply {
            onCreateListeners.add {
                Log.d(TAG, "onCreate $it")
            }
            onGetViewTypeError = { adapter, position ->
                sceneContext?.toast("不支持的数据: ${adapter.list[position]}")
                0
            }
            onCreateError = { _, _, parent, viewType ->
                val v = LayoutInflater.from(parent.context).inflate(R.layout.holder_error, parent, false)
                val holder = SimpleHolder<Any>(v)
                holder.v<TextView>(R.id.tv_info)?.text = "不支持的 viewType: $viewType"
                holder
            }
            onBindListeners.add { holder, data ->
                Log.d(TAG, "onBind $holder, $data")
            }
        }

}

class OnlineMovieViewHolder(v: View): SimpleHolder<Movie>(v) {
    private val ivPoster = v<ImageView>(R.id.iv_picture)!!
    private val tvName = v<TextView>(R.id.tv_name)!!
    private val tvDesc = v<TextView>(R.id.tv_desc)!!

    init {
        itemView.setOnClickListener {
            it.context.toast(data?.name)
        }
    }

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

    init {
        itemView.setOnClickListener {
            it.context.toast(data?.name)
        }
    }

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

const val LIST_STATE_IDLE = 0
const val LIST_STATE_REFRESHING = 1
const val LIST_STATE_LOADING_MORE = 2

class ListViewModel: ViewModel() {
    val liveList = LiveList<Any>()

    private var listState = LIST_STATE_IDLE
    private val _stateLiveData = MutableLiveData<Int>()
    val stateLiveData: LiveData<Int> = _stateLiveData

    private var refreshDispose: Disposable? = null
    private var loadMoreDispose: Disposable? = null

    private val loadingMoreItem = LoadingMore()

    fun refresh() {
        updateState(LIST_STATE_REFRESHING)
        // 先关闭加载更多
        loadMoreDispose?.dispose()
        loadMoreDispose = null
        liveList.remove(loadingMoreItem)
        // 关闭正在刷新
        refreshDispose?.dispose()
        refreshDispose = ListRepository.getList()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                liveList.clearAddAll(it)
                updateState(LIST_STATE_IDLE)
            }, {
                updateState(LIST_STATE_IDLE)
            })
    }

    fun loadMore() {
        // 正在刷新或者正在加载，不处理
        if (listState != LIST_STATE_IDLE) { return }

        updateState(LIST_STATE_LOADING_MORE)
        liveList.add(loadingMoreItem)

        loadMoreDispose?.dispose()
        loadMoreDispose = ListRepository.getList()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                liveList.remove(loadingMoreItem)
                updateState(LIST_STATE_IDLE)
                liveList.addAll(it)
            }, {
                liveList.remove(loadingMoreItem)
                updateState(LIST_STATE_IDLE)
            })
    }

    private fun updateState(state: Int) {
        listState = state
        _stateLiveData.update(state)
    }
}

object ListRepository {

    private val list = listOf(
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
        ),
        "我是一个字符串"
    )

    fun getList(): Observable<List<Any>> = Observable.just(list)
        .delay(1000, TimeUnit.MILLISECONDS)
        .subscribeOn(Schedulers.io())

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

class LoadingMore

const val TAG = "ListDemo"