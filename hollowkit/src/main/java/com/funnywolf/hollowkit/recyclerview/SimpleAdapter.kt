package com.funnywolf.hollowkit.recyclerview

import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * 百行代码实现的一个简单高效的 RecyclerView.Adapter
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/3/15
 */
open class SimpleAdapter(list: List<Any>): RecyclerView.Adapter<SimpleHolder<Any>>() {

    var list = list
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private val infoByDataClass = HashMap<Class<Any>, MutableList<HolderMapper<Any>>>()
    private val infoByType = SparseArray<HolderMapper<Any>?>()
    private val onCreateListenerByHolderMapper = HashMap<HolderMapper<Any>, OnCreateHolder<Any>?>()
    private val onBindListenerByHolderMapper = HashMap<HolderMapper<Any>, OnBindHolder<Any>?>()

    /**
     * 监听 onCreateViewHolder
     */
    private val onCreateListeners = ArrayList<OnCreateHolder<Any>>(2)

    /**
     * 监听 onBindViewHolder
     */
    private val onBindListeners = ArrayList<OnBindHolder<Any>>(2)

    fun <T: Any> addMapper(
        holderMapper: HolderMapper<T>,
        onCreateListener: OnCreateHolder<T>? = null,
        onBindListener: OnBindHolder<T>? = null
    ): SimpleAdapter {
        // HolderMapper 泛型上界是 Any，因此这里强转是安全的
        val info = holderMapper as HolderMapper<Any>
        val list = infoByDataClass[info.dataClass]
            ?: ArrayList<HolderMapper<Any>>().also { infoByDataClass[info.dataClass] = it }
        list.add(holderMapper)
        infoByType.append(info.viewType, info)
        onCreateListenerByHolderMapper[holderMapper] = onCreateListener as? OnCreateHolder<Any>
        onBindListenerByHolderMapper[holderMapper] = onBindListener as? OnBindHolder<Any>
        return this
    }

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

    fun addOnCreateHolderListener(listener: OnCreateHolder<Any>): SimpleAdapter {
        onCreateListeners.add(listener)
        return this
    }

    fun removeOnCreateHolderListener(listener: OnCreateHolder<Any>): SimpleAdapter {
        onCreateListeners.remove(listener)
        return this
    }

    fun addOnBindHolderListener(listener: OnBindHolder<Any>): SimpleAdapter {
        onBindListeners.add(listener)
        return this
    }

    fun removeOnBindHolderListener(listener: OnBindHolder<Any>): SimpleAdapter {
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
        val holder = (info.holderClass ?: SimpleHolder::class.java)
            .getConstructor(View::class.java)
            // SimpleHolder 泛型上界是 Any，因此这里强转是安全的
            .newInstance(view) as SimpleHolder<Any>
        onCreateListenerByHolderMapper[info]?.invoke(holder)
        onCreateListeners.forEach { it.invoke(holder) }
        return holder
    }

    override fun onBindViewHolder(holder: SimpleHolder<Any>, position: Int) {
        val data = list[position]
        holder.onBind(data)
        infoByType[holder.itemViewType]?.also {
            onBindListenerByHolderMapper[it]?.invoke(holder, data)
        }
        onBindListeners.forEach { it.invoke(holder, list[position]) }
    }

}

typealias OnCreateHolder<T> = (SimpleHolder<T>)->Unit

typealias OnBindHolder<T> = (SimpleHolder<T>, T)->Unit

open class SimpleHolder<T: Any>(v: View) : RecyclerView.ViewHolder(v) {

    /**
     * holder 的当前数据
     */
    var data: T? = null
        private set

    @CallSuper
    open fun onBind(data: T) {
        this.data = data
    }

    fun <V: View> find(id: Int): V? = itemView.findViewById(id)
}

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
    val holderClass: Class<out SimpleHolder<T>>?

    /**
     * 是否支持该数据，用于同一数据类型的区分，不设置就默认支持
     */
    val isSupport: ((T)->Boolean)

    /**
     * 就是 Adapter 用的那个 viewType，这里暴露出来方便自定义
     */
    val viewType: Int
}

class HolderMapInfo<T: Any>(
    override val dataClass: Class<T>,
    override val layoutRes: Int,
    override val holderClass: Class<out SimpleHolder<T>>? = null,
    override val isSupport: (T)->Boolean = { true },
    override val viewType: Int = Objects.hash(dataClass, layoutRes, holderClass)
) : HolderMapper<T>
