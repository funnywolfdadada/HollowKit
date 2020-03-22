package com.funnywolf.hollowkit.recyclerview

import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * 百行代码实现的一个简单高效的 RecyclerView.Adapter
 *
 * @author funnywolf
 * @since 2020/3/15
 */
open class SimpleAdapter(list: List<Any>): RecyclerView.Adapter<SimpleHolder<Any>>() {

    private val infoByDataClass = HashMap<Class<Any>, MutableList<HolderInfo<Any>>>()
    private val infoByType = SparseArray<HolderInfo<Any>?>()

    /**
     * getItemViewType 遇到不支持的数据类型时的出错处理，不设置会抛出异常
     */
    var onGetViewTypeError: ((SimpleAdapter, Int)->Int)? = null

    /**
     * 监听 onCreateViewHolder
     */
    val onCreateListeners = ArrayList<OnCreateHolder<Any>>(2)

    /**
     * onCreateViewHolder 的出错处理，可以返回自定义的 SimpleHolder 来显示错误信息
     * 不设置或者返回 null 会重新抛出异常
     */
    var onCreateError: ((SimpleAdapter, Exception, ViewGroup, Int)->SimpleHolder<Any>?)? = null

    /**
     * 监听 onBindViewHolder
     */
    val onBindListeners = ArrayList<OnBindHolder<Any>>(2)

    /**
     * onBindViewHolder 的出错处理，不设置或者返回 null 会重新抛出异常
     */
    var onBindError: ((SimpleAdapter, Exception, SimpleHolder<Any>, Int)->Unit)? = null

    var list = list
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun addHolderInfo(holderInfo: HolderInfo<*>): SimpleAdapter {
        // HolderInfo 泛型上界是 Any，因此这里强转是安全的
        val info = holderInfo as HolderInfo<Any>
        val list = infoByDataClass[info.dataClass]
            ?: ArrayList<HolderInfo<Any>>().also { infoByDataClass[info.dataClass] = it }
        list.add(holderInfo)
        infoByType.append(info.viewType, info)
        return this
    }

    fun getInfoByPosition(position: Int): HolderInfo<Any>? {
        val data = list[position]
        val dataClass = data.javaClass
        // 先根据数据类型快速查找
        return (infoByDataClass[dataClass]
            // 没找到就遍历查找父类有没有支持
            ?: infoByDataClass.entries.find { it.key.isAssignableFrom(dataClass) }?.value)
            ?.find { it.isSupport(data) }
    }

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int {
        return getInfoByPosition(position)?.viewType
            ?: onGetViewTypeError?.invoke(this, position)
            ?: throw IllegalArgumentException("Unsupported data: ${list[position]}")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleHolder<Any> = try {
        val info = infoByType[viewType] ?: throw IllegalArgumentException("Unknown viewType: $viewType")
        val view = LayoutInflater.from(parent.context).inflate(info.layoutRes, parent, false)
        val holder = (info.holderClass ?: SimpleHolder::class.java)
            .getConstructor(View::class.java)
            // SimpleHolder 泛型上界是 Any，因此这里强转是安全的
            .newInstance(view) as SimpleHolder<Any>
        info.onCreate?.invoke(holder)
        onCreateListeners.forEach { it.invoke(holder) }
        holder
    } catch (e: Exception) {
        onCreateError?.invoke(this, e, parent, viewType) ?: throw e
    }

    override fun onBindViewHolder(holder: SimpleHolder<Any>, position: Int) = try {
        holder.onBind(list[position])
        infoByType[holder.itemViewType]?.onBind?.invoke(holder, list[position])
        onBindListeners.forEach { it.invoke(holder, list[position]) }
    } catch (e: Exception) {
        onBindError?.invoke(this, e, holder, position) ?: throw e
    }

}

typealias OnCreateHolder<T> = (SimpleHolder<T>)->Unit

typealias OnBindHolder<T> = (SimpleHolder<T>, T)->Unit

open class SimpleHolder<T: Any>(v: View) : RecyclerView.ViewHolder(v) {
    /**
     * 缓存 findViewById
     */
    private val viewArray = SparseArray<View?>()

    /**
     * holder 的当前数据
     */
    var data: T? = null

    @CallSuper
    open fun onBind(data: T) {
        this.data = data
    }

    fun find(id: Int): View? = viewArray[id] ?: itemView.findViewById<View>(id)?.also { viewArray.put(id, it) }
    inline fun <reified V: View> v(id: Int): V? = find(id) as? V
}

data class HolderInfo<T: Any> (
    /**
     * 所支持的数据类型
     */
    val dataClass: Class<T>,

    /**
     * 布局文件
     */
    @LayoutRes val layoutRes: Int,

    /**
     * 自定义的 SimpleHolder 类型
     */
    val holderClass: Class<out SimpleHolder<T>>? = null,

    /**
     * 是否支持该数据，用于同一数据类型的区分，不设置就默认支持
     */
    val isSupport: ((T)->Boolean) = { true },

    /**
     * 就是 Adapter 用的那个 viewType，这里暴露出来方便做缓存优化
     */
    val viewType: Int = Objects.hash(dataClass, layoutRes, holderClass),

    /**
     * 该 viewType 类型的 ViewHolder 在 onCreateViewHolder 时的回调
     */
    val onCreate: OnCreateHolder<T>? = null,

    /**
     * 该 viewType 类型的 ViewHolder 在 onBindViewHolder 时的回调
     */
    val onBind: OnBindHolder<T>? = null
)
