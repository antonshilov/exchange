package com.antonshilov.exchange.quotes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.antonshilov.exchange.R
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.viewModel

class MainActivity : AppCompatActivity() {
    private val vm by viewModel<QuotesViewModel>()
    private val adapter = QuotesAdapter()
    private val visibleItemsListener = VisibleItemsListener()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initQuotesList()
        initViewModel()
    }

    override fun onStart() {
        super.onStart()
        startVisibilityUpdates()
    }

    override fun onStop() {
        super.onStop()
        cancelVisibilityUpdates()
    }

    private fun initQuotesList() {
        quotesList.adapter = adapter
        quotesList.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        quotesList.layoutManager = LinearLayoutManager(this)
        quotesList.itemAnimator = null
        quotesList.addOnScrollListener(visibleItemsListener)
    }

    private fun initViewModel() {
        vm.quotes.observe(this, Observer {
            adapter.submitList(it)
        })
        vm.fetchQuotes()
    }

    private fun startVisibilityUpdates() {
        vm.setVisibleItemsStream(visibleItemsListener.getVisibleItems())
    }

    private fun cancelVisibilityUpdates() {
        vm.cancelVisibilityUpdates()
    }
}