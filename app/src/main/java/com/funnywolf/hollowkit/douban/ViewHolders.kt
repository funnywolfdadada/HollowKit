package com.funnywolf.hollowkit.douban

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.funnywolf.hollowkit.R
import com.funnywolf.hollowkit.recyclerview.HolderInfo
import com.funnywolf.hollowkit.recyclerview.LiveList
import com.funnywolf.hollowkit.recyclerview.SimpleAdapter
import com.funnywolf.hollowkit.recyclerview.SimpleHolder
import com.funnywolf.hollowkit.utils.dp2pix
import com.funnywolf.hollowkit.view.JellyLayout
import com.funnywolf.hollowkit.view.RightDragToOpenView

/**
 * @author https://github.com/funnywolfdadada
 * @since 2020/4/11
 */
class DoubanHeaderHolder(v: View): SimpleHolder<DoubanHeader>(v) {
    private val ivPoster = v<ImageView>(R.id.iv_poster)
    private val tvTitle = v<TextView>(R.id.tv_title)
    private val tvSubtitle = v<TextView>(R.id.tv_subtitle)
    private val tvInfo = v<TextView>(R.id.tv_info)

    override fun onBind(data: DoubanHeader) {
        super.onBind(data)
        ivPoster?.setImageResource(data.posterRes)
        tvTitle?.text = data.title
        tvSubtitle?.text = data.subTitle
        tvInfo?.text = data.info
    }
}

class RatingHolder(v: View): SimpleHolder<DoubanRating>(v) {
    private val tvRating = v<TextView>(R.id.tv_rating)
    private val ratingBar = v<RatingBar>(R.id.rating_bar)
    private val tvInfo = v<TextView>(R.id.tv_info)

    override fun onBind(data: DoubanRating) {
        super.onBind(data)
        tvRating?.text = data.rating.toString()
        ratingBar?.rating = data.rating / 2
        tvInfo?.text = data.info
    }
}

class TitleHolder(v: View): SimpleHolder<TitleModel>(v) {
    private val tvTitle = v<TextView>(R.id.tv_title)
    private val tvInfo = v<TextView>(R.id.tv_info)

    override fun onBind(data: TitleModel) {
        super.onBind(data)
        tvTitle?.text = data.text
        tvInfo?.text = data.info
    }
}

class BriefHolder(v: View): SimpleHolder<Brief>(v) {
    private val textView = v<TextView>(R.id.text_view)

    override fun onBind(data: Brief) {
        super.onBind(data)
        textView?.text = data.text
    }
}

class ActorViewHolder(v: View): SimpleHolder<Actor>(v) {
    private val ivAvatar = v<ImageView>(R.id.iv_avatar)!!
    private val tvName = v<TextView>(R.id.tv_name)
    private val tvInfo = v<TextView>(R.id.tv_info)

    override fun onBind(data: Actor) {
        super.onBind(data)
        Glide.with(ivAvatar).load(data.avatar).into(ivAvatar)
        tvName?.text = data.name
        tvInfo?.text = data.info
    }
}

class ActorsViewHolder(v: View): SimpleHolder<Actors>(v) {
    private val jelly = v<JellyLayout>(R.id.jelly)
    private val recyclerView = v<RecyclerView>(R.id.recycler)

    private val dragView = RightDragToOpenView(v.context)

    private val liveList = LiveList<Any>()

    init {
        dragView.layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT).also {
            it.topMargin = 20.dp2pix(v.context)
            it.bottomMargin = 20.dp2pix(v.context)
        }
        jelly?.setRightView(dragView)
        jelly?.setLeftView(View(v.context))
        jelly?.onScrollChangedListener = {
            dragView.process = it.currProcess
        }
        recyclerView?.adapter = SimpleAdapter(liveList.get())
            .addHolderInfo(HolderInfo(Actor::class.java, R.layout.holder_douban_actor, ActorViewHolder::class.java))
        recyclerView?.layoutManager = LinearLayoutManager(v.context).also {
            it.orientation = LinearLayoutManager.HORIZONTAL
        }
    }

    override fun onBind(data: Actors) {
        super.onBind(data)
        liveList.clearAddAll(data)
    }
}

class PictureViewHolder(v: View): SimpleHolder<Picture>(v) {
    private val imageView = v<ImageView>(R.id.image_view)!!

    override fun onBind(data: Picture) {
        super.onBind(data)
        Glide.with(imageView).load(data.pictureRes).into(imageView)
        imageView.requestLayout()
    }
}

class PicturesViewHolder(v: View): SimpleHolder<Pictures>(v) {
    private val jelly = v<JellyLayout>(R.id.jelly)
    private val recyclerView = v<RecyclerView>(R.id.recycler)

    private val dragView = RightDragToOpenView(v.context)

    private val liveList = LiveList<Any>()

    init {
        dragView.layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT).also {
            it.topMargin = 20.dp2pix(v.context)
            it.bottomMargin = 20.dp2pix(v.context)
        }
        jelly?.setRightView(dragView)
        jelly?.setLeftView(View(v.context))
        jelly?.onScrollChangedListener = {
            dragView.process = it.currProcess
        }
        recyclerView?.adapter = SimpleAdapter(liveList.get())
            .addHolderInfo(HolderInfo(Picture::class.java, R.layout.holder_douban_picture, PictureViewHolder::class.java))
        recyclerView?.layoutManager = LinearLayoutManager(v.context).also {
            it.orientation = LinearLayoutManager.HORIZONTAL
        }
    }

    override fun onBind(data: Pictures) {
        super.onBind(data)
        liveList.clearAddAll(data)
    }
}
