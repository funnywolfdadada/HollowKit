package com.funnywolf.hollowkit.app.fragments.douban.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.funnywolf.hollowkit.app.R
import kotlin.math.max

/**
 * 豆瓣详情页的框架视图
 *
 * @author https://github.com/funnywolfdadada
 * @since 2020/4/10
 */
class DoubanDetailView: FrameLayout {

    val toolBar: ToolbarView
    val linkedScrollView: LinkedScrollView
    val bottomSheetLayout: BottomSheetLayout
    val topRecyclerView: RecyclerView

    val bottomLayout: FrameLayout
    var bottomScrollViewProvider: (()->View?)? = null
        set(value) {
            linkedScrollView.setBottomView(bottomLayout, value)
            field = value
        }

    var toolbarHeight = context.resources.getDimension(R.dimen.toolbar_height).toInt()
    var minBottomShowingHeight = toolbarHeight

    var isBottomViewFloating = false
        private set

    private var topScrolledY = 0F

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet?): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    init {
        linkedScrollView = LinkedScrollView(context)
        addView(linkedScrollView)
        bottomSheetLayout = BottomSheetLayout(context)
        addView(bottomSheetLayout, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).apply {
            topMargin = toolbarHeight
        })
        toolBar = ToolbarView(context)
        addView(toolBar, LayoutParams(LayoutParams.MATCH_PARENT, toolbarHeight))

        linkedScrollView.topContainer.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        linkedScrollView.bottomContainer.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).apply {
            bottomMargin = toolbarHeight
        }

        linkedScrollView.setOnScrollChangeListener { _, _, _, _, _ ->
            updateBottomView()
            updateToolbar()
        }
        bottomSheetLayout.onProcessChangedListener = { updateToolbar() }

        topRecyclerView = RecyclerView(context)
        topRecyclerView.setPaddingRelative(0, toolbarHeight, 0, 0)
        topRecyclerView.clipToPadding = false
        topRecyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                topScrolledY += dy
                if (topScrolledY < 0) {
                    topScrolledY = 0F
                }
                updateToolbar()
            }
        })
        linkedScrollView.setTopView(topRecyclerView) { topRecyclerView }

        bottomLayout = FrameLayout(context)
        linkedScrollView.setBottomView(bottomLayout, bottomScrollViewProvider)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        post { updateBottomView() }
    }

    private fun updateBottomView() {
        val bottomY = linkedScrollView.bottomContainer.y - linkedScrollView.scrollY
        val shouldBottomFloating = bottomY > height - minBottomShowingHeight
        if (shouldBottomFloating && !isBottomViewFloating) {
            isBottomViewFloating = true
            linkedScrollView.removeBottomView()
            bottomSheetLayout.setContentView(bottomLayout, minBottomShowingHeight)
        } else if (!shouldBottomFloating && isBottomViewFloating) {
            isBottomViewFloating = false
            bottomSheetLayout.removeContentView()
            linkedScrollView.setBottomView(bottomLayout, bottomScrollViewProvider)
        }
    }

    private fun updateToolbar() {
        toolBar.process = if (bottomSheetLayout.state == BOTTOM_SHEET_STATE_EXTENDED) {
            1F
        } else {
            max(topScrolledY, linkedScrollView.scrollY.toFloat()) / toolbarHeight
        }
    }

}