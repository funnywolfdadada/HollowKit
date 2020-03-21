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
    private val infoByType = SparseArray<HolderInfo<Any>>()

    private val listeners = ArrayList<HolderListener<Any>>()

    /**
     * getItemViewType 遇到不支持的数据类型时的出错处理，不设置会抛出异常
     */
    var getItemViewTypeError: ((SimpleAdapter, Int)->Int)? = null

    /**
     * onCreateViewHolder 的出错处理，可以返回自定义的 SimpleHolder 来显示错误信息
     * 不设置或者返回 null 会重新抛出异常
     */
    var onCreateError: ((SimpleAdapter, Exception, ViewGroup, Int)->SimpleHolder<Any>?)? = null

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

    fun addHolderListener(listener: HolderListener<Any>): SimpleAdapter {
        listeners.add(listener)
        return this
    }

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int {
        val data = list[position]
        return getSupportedInfo(data.javaClass)?.viewType
            ?: getItemViewTypeError?.invoke(this, position)
            ?: throw IllegalArgumentException("Unsupported data ${data.javaClass}: $data")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleHolder<Any> = try {
        val info = infoByType[viewType] ?: throw IllegalArgumentException("Unknown viewType: $viewType")
        val view = LayoutInflater.from(parent.context).inflate(info.layoutRes, parent, false)
        val holder = (info.holderClass ?: SimpleHolder::class.java)
            .getConstructor(View::class.java)
            // SimpleHolder 泛型上界是 Any，因此这里强转是安全的
            .newInstance(view) as SimpleHolder<Any>
        info.onCreate(holder)
        listeners.forEach { it.onCreate(holder) }
        holder
    } catch (e: Exception) {
        onCreateError?.invoke(this, e, parent, viewType) ?: throw e
    }

    override fun onBindViewHolder(holder: SimpleHolder<Any>, position: Int) = try {
        holder.onBind(list[position])
        infoByType[holder.itemViewType].onBind(holder, list[position])
        listeners.forEach { it.onBind(holder, list[position]) }
    } catch (e: Exception) {
        onBindError?.invoke(this, e, holder, position) ?: throw e
    }

    private fun getSupportedInfo(data: Any): HolderInfo<Any>? {
        val dataClass = data.javaClass
        return (infoByDataClass[dataClass]
            ?: infoByDataClass.entries.find { it.key.isAssignableFrom(dataClass) }?.value)
            ?.find { it.isSupport(data) }
    }

}

interface HolderListener<T: Any> {

    fun onCreate(holder: SimpleHolder<T>) {
        // for children
    }

    fun onBind(holder: SimpleHolder<T>, data: T) {
        // for children
    }

}

open class SimpleHolder<T: Any>(v: View) : RecyclerView.ViewHolder(v) {

    /**
     * holder 的当前数据
     */
    protected var data: T? = null

    @CallSuper
    open fun onBind(data: T) {
        this.data = data
    }

}

open class HolderInfo<T: Any> (
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
    val holderClass: Class<out SimpleHolder<T>>? = null
): HolderListener<T> {

    /**
     * 就是 Adapter 用的那个 viewType，这里暴露出来方便做缓存优化
     */
    open val viewType: Int = Objects.hash(dataClass, layoutRes, holderClass)

    /**
     * 当同一类型数据对应多种 SimpleHolder 时，需要在这里确定是否支持
     */
    open fun isSupport(data: T): Boolean = true

}
