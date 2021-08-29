package com.funnywolf.hollowkit.app.fragments.douban

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import coil.api.load
import com.funnywolf.hollowkit.app.R
import com.funnywolf.hollowkit.app.databinding.*
import com.funnywolf.hollowkit.recyclerview.*
import com.funnywolf.hollowkit.utils.dp
import com.funnywolf.hollowkit.app.fragments.douban.view.RightDragToOpenView

/**
 * @author https://github.com/funnywolfdadada
 * @since 2020/4/11
 */
class DoubanHeaderHolder(v: View): SimpleHolder<DoubanHeader>(v) {
    private val binding = HolderDoubanHeaderBinding.bind(v)

    override fun onBind(data: DoubanHeader) {
        super.onBind(data)
        binding.ivPoster.setImageResource(data.posterRes)
        binding.tvTitle.text = data.title
        binding.tvSubtitle.text = data.subTitle
        binding.tvInfo.text = data.info
    }
}

class RatingHolder(v: View): SimpleHolder<DoubanRating>(v) {
    private val binding = HolderDoubanRatingBinding.bind(v)

    override fun onBind(data: DoubanRating) {
        super.onBind(data)
        binding.tvRating.text = data.rating.toString()
        binding.ratingBar.rating = data.rating / 2
        binding.tvInfo.text = data.info
    }
}

class TitleHolder(v: View): SimpleHolder<TitleModel>(v) {
    private val binding = HolderDoubanTitleBinding.bind(v)

    override fun onBind(data: TitleModel) {
        super.onBind(data)
        binding.tvTitle.text = data.text
        binding.tvInfo.text = data.info
    }
}

class BriefHolder(v: View): SimpleHolder<Brief>(v) {
    private val binding = HolderDoubanBriefBinding.bind(v)

    override fun onBind(data: Brief) {
        super.onBind(data)
        binding.textView.text = data.text
    }
}

class ActorViewHolder(v: View): SimpleHolder<Actor>(v) {
    private val binding = HolderDoubanActorBinding.bind(v)

    override fun onBind(data: Actor) {
        super.onBind(data)
        binding.ivAvatar.load(data.avatar)
        binding.tvName.text = data.name
        binding.tvInfo.text = data.info
    }
}

class ActorsViewHolder(v: View): SimpleHolder<Actors>(v) {
    private val binding = HolderDoubanInnerListBinding.bind(v)

    private val dragView =
        RightDragToOpenView(v.context)

    private val list = AdapterList<Any>()

    init {
        dragView.layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT).also {
            it.topMargin = 20.dp
            it.bottomMargin = 20.dp
        }
        binding.jelly.setRightView(dragView)
        binding.jelly.setLeftView(View(v.context))
        binding.jelly.onScrollChangedListener = {
            dragView.process = it.currProcess
        }
        binding.recycler.adapter = SimpleAdapter(list)
            .addMapper(
                HolderMapInfo(
                    Actor::class.java,
                    R.layout.holder_douban_actor,
                    ActorViewHolder::class.java
                )
            ).also { list.bind(it) }
        binding.recycler.layoutManager = LinearLayoutManager(v.context).also {
            it.orientation = LinearLayoutManager.HORIZONTAL
        }
    }

    override fun onBind(data: Actors) {
        super.onBind(data)
        list.clear()
        list.addAll(data)
    }
}

class PictureViewHolder(v: View): SimpleHolder<Picture>(v) {
    private val binding = HolderDoubanPictureBinding.bind(v)

    override fun onBind(data: Picture) {
        super.onBind(data)
        binding.imageView.load(data.pictureRes)
        binding.imageView.requestLayout()
    }
}

class PicturesViewHolder(v: View): SimpleHolder<Pictures>(v) {
    private val binding = HolderDoubanInnerListBinding.bind(v)

    private val dragView =
        RightDragToOpenView(v.context)

    private val list = AdapterList<Any>()

    init {
        dragView.layoutParams = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT).also {
            it.topMargin = 20.dp
            it.bottomMargin = 20.dp
        }
        binding.jelly.setRightView(dragView)
        binding.jelly.setLeftView(View(v.context))
        binding.jelly.onScrollChangedListener = {
            dragView.process = it.currProcess
        }
        binding.recycler.adapter = SimpleAdapter(list)
            .addMapper(
                HolderMapInfo(
                    Picture::class.java,
                    R.layout.holder_douban_picture,
                    PictureViewHolder::class.java
                )
            ).also { list.bind(it) }
        binding.recycler.layoutManager = LinearLayoutManager(v.context).also {
            it.orientation = LinearLayoutManager.HORIZONTAL
        }
    }

    override fun onBind(data: Pictures) {
        super.onBind(data)
        list.clear()
        list.addAll(data)
    }
}
