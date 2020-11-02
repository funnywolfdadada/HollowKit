package com.funnywolf.hollowkit.recyclerview

import android.content.Context
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.collections.HashSet

/**
 * Make RecyclerView No Adapter
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/10/31
 */

/**
 * 生成可用于更新数据的列表，不过更新数据前记得 [addMapper] 添加数据与视图的映射关系
 */
fun RecyclerView.asList(): MutableList<Any> {
    if (layoutManager == null) {
        layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
    }
    return requestNoAdapter().liveList
}

/**
 * 给 RecyclerView 添加数据到视图的映射信息
 *
 * [D] 类型的数据，在 [filter] 返回 true 的情况下，可能会调用 [viewCreator] 生成视图（会有复用）
 * 并调用 [viewBinder] 进行绑定
 */
inline fun <reified D: Any> RecyclerView.addMapper(
        noinline filter: ((D)->Boolean)? = null,
        noinline viewCreator: ViewCreator,
        noinline viewBinder: ViewBinder<D> = defaultViewBinder()
): RecyclerView {
    return addMapper(NoAdapterMapperImpl(D::class.java, filter, viewCreator, viewBinder))
}

// region RecyclerView 的其他扩展方法

fun RecyclerView.addMapper(mapper: NoAdapterMapper<*>): RecyclerView {
    requestNoAdapter().addMapper(mapper)
    return this
}

fun RecyclerView.addOnCreate(listener: (View)->Unit) {
    requestNoAdapter().onCreateListener.add(listener)
}

fun RecyclerView.removeOnCreate(listener: (View)->Unit) {
    requestNoAdapter().onCreateListener.remove(listener)
}

fun RecyclerView.addOnBind(listener: (Any, View)->Unit) {
    requestNoAdapter().onBindListener.add(listener)
}

fun RecyclerView.removeOnBind(listener: (Any, View)->Unit) {
    requestNoAdapter().onBindListener.remove(listener)
}

fun RecyclerView.requestNoAdapter(): NoAdapter {
    return (adapter as? NoAdapter) ?: NoAdapter().also { adapter = it }
}

// endregion

// region NoAdapter 实现

class NoAdapter: RecyclerView.Adapter<NoViewHolder>() {

    val liveList: MutableList<Any> = LiveList<Any>().apply {
        bind(this@NoAdapter)
    }

    var onCreateListener = HashSet<(View)->Unit>()
    var onBindListener = HashSet<(Any, View)->Unit>()

    private val mapperSet = HashSet<NoAdapterMapper<Any>>()
    private val mapperByType = SparseArray<NoAdapterMapper<Any>>()

    fun addMapper(mapper: NoAdapterMapper<*>) {
        val m = mapper as NoAdapterMapper<Any>
        mapperSet.add(m)
        mapperByType.put(mapper.viewType(), m)
    }

    override fun getItemCount(): Int {
        return liveList.size
    }

    override fun getItemViewType(position: Int): Int {
        val data = liveList[position]
        val mapper = mapperSet.find {
            it.dataClass.isInstance(data) && it.isSupport(data)
        } ?: throw IllegalArgumentException("Unsupported data $data at $position")
        return mapper.viewType()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoViewHolder {
        val v = mapperByType.get(viewType).createView(parent)
        val holder = NoViewHolder(v)
        onCreateListener.forEach { it(v) }
        return holder
    }

    override fun onBindViewHolder(holder: NoViewHolder, position: Int) {
        val data = liveList[position]
        mapperByType.get(holder.itemViewType).bindView(data, holder.itemView)
        onBindListener.forEach { it(data, holder.itemView) }
    }

}

class NoViewHolder(v: View): RecyclerView.ViewHolder(v)

/**
 * 确定数据和视图的映射关系
 */
interface NoAdapterMapper<D: Any> {

    /**
     * 数据的类型
     */
    val dataClass: Class<D>

    /**
     * 是否支持
     */
    fun isSupport(data: D): Boolean = true

    /**
     * 创建该类型映射的视图
     */
    fun createView(parent: ViewGroup): View

    /**
     * 数据和视图绑定
     */
    fun bindView(data: D, v: View)

    /**
     * 用于 adapter 的 viewType
     */
    fun viewType(): Int = hashCode()

}

// endregion

// region 一些为了方便建立数据和视图映射的工具

class NoAdapterMapperImpl<D: Any>(
        override val dataClass: Class<D>,
        private val filter: ((D)->Boolean)? = null,
        private val viewCreator: ViewCreator,
        private val viewBinder: ViewBinder<D>
): NoAdapterMapper<D> {

    override fun isSupport(data: D): Boolean {
        return filter?.invoke(data) ?: super.isSupport(data)
    }

    override fun createView(parent: ViewGroup): View {
        return viewCreator(parent)
    }

    override fun bindView(data: D, v: View) {
        viewBinder(data, v)
    }

    override fun viewType(): Int {
        return Objects.hash(dataClass, filter, viewCreator, viewBinder)
    }

}

/**
 * RecyclerView.Adapter 的 onCreateViewHolder 需要生成视图，但不关心视图怎么生成的
 */
typealias ViewCreator = (parent: ViewGroup)->View

fun classViewCreator(vClass: Class<out View>): ViewCreator = {
    vClass.getConstructor(Context::class.java).newInstance(it.context)
}

fun resViewCreator(@LayoutRes id: Int, init: (View.()->Unit)? = null): ViewCreator = {
    LayoutInflater.from(it.context).inflate(id, it, false).apply {
        init?.invoke(this)
    }
}

/**
 * RecyclerView.Adapter 的 onBindViewHolder 需要数据和视图绑定，但不关心怎么绑定的
 */
typealias ViewBinder<D> = (d: D, View)->Unit

interface DataBinder<D: Any> {
    val dataClass: Class<D>
    var data: D?
}

/**
 * 默认会把绑定过程交给实现了 DataBinder 的视图
 */
fun <D: Any> defaultViewBinder(): ViewBinder<D> = { data: D, v: View ->
    // 由 DataBinder.dataClass 保证泛型和 D 一致，这里类型转换是没有问题的
    val dataBinder = v as? DataBinder<D>
    if (dataBinder != null && dataBinder.dataClass.isInstance(data)) {
        dataBinder.data = data
    }
}

// endregion
