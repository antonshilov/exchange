package com.antonshilov.exchange.quotes

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class VisibleItemsListener : RecyclerView.OnScrollListener() {
    private val visibleItems = BehaviorSubject.create<Pair<Int, Int>>()
    fun getVisibleItems() = visibleItems as Observable<Pair<Int, Int>>
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        val lm = recyclerView.layoutManager as LinearLayoutManager
        val firstVisible = lm.findFirstVisibleItemPosition()
        val lastVisible = lm.findLastVisibleItemPosition()
        visibleItems.onNext(firstVisible to lastVisible + 1)
    }
}