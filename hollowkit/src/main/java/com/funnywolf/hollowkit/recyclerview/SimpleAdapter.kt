package com.funnywolf.hollowkit.recyclerview

import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import java.lang.IllegalArgumentException
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * 百行代码实现的一个简单高效的 RecyclerView.Adapter
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/3/15
 */
open class SimpleAdapter(list: List<Any>): RecyclerView.Adapter<SimpleHolder<Any>>() {

    /**
     * 列表数据
     */
    var list = list
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private val infoByDataClass = HashMap<Class<Any>, MutableList<HolderMapper<Any>>>()
    private val infoByType = SparseArray<HolderMapper<Any>?>()

    /**
     * 监听 onCreateViewHolder
     */
    private val onCreateListeners = ArrayList<OnCreateHolder>(2)

    /**
     * 监听 onBindViewHolder
     */
    private val onBindListeners = ArrayList<OnBindHolder>(2)

    /**
     * 添加 view holder 的映射信息
     */
    fun <T: Any> addMapper(holderMapper: HolderMapper<T>): SimpleAdapter {
        // HolderMapper 泛型上界是 Any，因此这里强转是安全的
        val info = holderMapper as HolderMapper<Any>
        val list = infoByDataClass[info.dataClass]
            ?: ArrayList<HolderMapper<Any>>().also { infoByDataClass[info.dataClass] = it }
        list.add(holderMapper)
        infoByType.append(info.viewType, info)
        return this
    }

    /**
     * 根据位置获取映射信息
     */
    fun getMapperByPosition(position: Int): HolderMapper<Any> {
        val data = list[position]
        val dataClass = data.javaClass
        // 先根据数据类型快速查找
        var supportHolderMapper = infoByDataClass[dataClass]?.find { it.isSupport(data) }
        if (supportHolderMapper != null) {
            return supportHolderMapper
        }
        // 没找到就遍历查找父类有没有支持
        infoByDataClass.entries.forEach {
            if (it.key.isAssignableFrom(dataClass)) {
                supportHolderMapper = it.value.find { m -> m.isSupport(data) }
                if (supportHolderMapper != null) {
                    return@forEach
                }
            }
        }
        return supportHolderMapper ?: throw IllegalArgumentException("Unsupported data $data at $position")
    }

    /**
     * 添加 [onCreateViewHolder] 时的回调
     */
    fun addOnCreateHolderListener(listener: OnCreateHolder): SimpleAdapter {
        onCreateListeners.add(listener)
        return this
    }

    /**
     * 移除 [onCreateViewHolder] 时的回调
     */
    fun removeOnCreateHolderListener(listener: OnCreateHolder): SimpleAdapter {
        onCreateListeners.remove(listener)
        return this
    }

    /**
     * 添加 [onBindViewHolder] 时的回调
     */
    fun addOnBindHolderListener(listener: OnBindHolder): SimpleAdapter {
        onBindListeners.add(listener)
        return this
    }

    /**
     * 移除 [onBindViewHolder] 时的回调
     */
    fun removeOnBindHolderListener(listener: OnBindHolder): SimpleAdapter {
        onBindListeners.remove(listener)
        return this
    }

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int {
        return getMapperByPosition(position).viewType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleHolder<Any> {
        val info = infoByType[viewType] ?: throw IllegalArgumentException("Unknown viewType: $viewType")
        val view = LayoutInflater.from(parent.context).inflate(info.layoutRes, parent, false)
        val holder = info.holderClass.getConstructor(View::class.java).newInstance(view)
        onCreateListeners.forEach { it.invoke(holder) }
        return holder
    }

    override fun onBindViewHolder(holder: SimpleHolder<Any>, position: Int) {
        val data = list[position]
        holder.onBind(data)
        onBindListeners.forEach { it.invoke(holder, list[position]) }
    }

}

/**
 * [RecyclerView.Adapter.onCreateViewHolder] 时的回调
 */
typealias OnCreateHolder = (SimpleHolder<out Any>)->Unit

/**
 * [RecyclerView.Adapter.onBindViewHolder] 时的回调
 */
typealias OnBindHolder = (SimpleHolder<out Any>, Any)->Unit

/**
 * 基础的 view holder
 */
abstract class SimpleHolder<T: Any>(v: View) : RecyclerView.ViewHolder(v) {

    /**
     * holder 的当前数据
     */
    var data: T? = null
        private set

    /**
     * 绑定数据
     */
    @CallSuper
    open fun onBind(data: T) {
        this.data = data
    }

}

/**
 * view holder 的映射关系
 */
interface HolderMapper<T: Any> {
    /**
     * 所支持的数据类型
     */
    val dataClass: Class<T>

    /**
     * 布局文件
     */
    @get:LayoutRes val layoutRes: Int

    /**
     * 自定义的 SimpleHolder 类型
     */
    val holderClass: Class<out SimpleHolder<in T>>

    /**
     * 是否支持该数据，用于同一数据类型的区分，不设置就默认支持
     */
    val isSupport: ((T)->Boolean)

    /**
     * 就是 Adapter 用的那个 viewType，这里暴露出来方便自定义
     */
    val viewType: Int
}

/**
 * view holder 的映射关系实现
 */
open class HolderMapInfo<T: Any>(
    override val dataClass: Class<T>,
    override val layoutRes: Int,
    override val holderClass: Class<out SimpleHolder<in T>>,
    override val isSupport: (T)->Boolean = { true },
    override val viewType: Int = layoutRes
) : HolderMapper<T>
