package com.funnywolf.hollowkit.app.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.funnywolf.hollowkit.app.databinding.PageTestBinding
import com.funnywolf.hollowkit.app.utils.higherPictures
import com.funnywolf.hollowkit.app.utils.initHorizontalPictures

/**
 * @author https://github.com/funnywolfdadada
 * @since 2020/5/9
 */
class TestFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = PageTestBinding.inflate(inflater, container, false)
        val rv = binding.rv
        rv.layoutManager = GridLayoutManager(rv.context, 4)
        val list = rv.initHorizontalPictures()
        list.addAll(higherPictures)
        list.addAll(higherPictures)
        list.addAll(higherPictures)
        ItemTouchHelper(object: ItemTouchHelper.Callback() {
            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                return makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT, 0)
            }

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val from = viewHolder.adapterPosition
                val to = target.adapterPosition
                Log.d("zdl", "onMove $from $to")

                list.move(from, to)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                Log.d("zdl", "onSwiped ${viewHolder.adapterPosition} $direction")
            }

        }).attachToRecyclerView(rv)
        return binding.root
    }

}
