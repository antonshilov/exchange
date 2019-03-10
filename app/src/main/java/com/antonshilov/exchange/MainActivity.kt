package com.antonshilov.exchange

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.viewModel


class MainActivity : AppCompatActivity() {
    val vm by viewModel<QuotesViewModel>()
    val adapter = QuotesAdapter()
    val visibleItemsListener = VisibleItemsListener()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        quotesList.adapter = adapter
        quotesList.layoutManager = LinearLayoutManager(this)
        quotesList.addOnScrollListener(visibleItemsListener)
        initViewModel()
    }

    private fun initViewModel() {
        vm.quotes.observe(this, Observer {
            adapter.submitList(it)
        })
        vm.fetchQuotes()
        vm.setVisibleItemsStream(visibleItemsListener.visibleItems)
    }
}

class VisibleItemsListener : RecyclerView.OnScrollListener() {
    val visibleItems = BehaviorSubject.create<Pair<Int, Int>>()
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        val lm = recyclerView.layoutManager as LinearLayoutManager
        val first = lm.findFirstVisibleItemPosition()
        val last = lm.findLastVisibleItemPosition()
        visibleItems.onNext(first to last)
    }

}