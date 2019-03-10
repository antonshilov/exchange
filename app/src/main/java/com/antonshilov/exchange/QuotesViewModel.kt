package com.antonshilov.exchange

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.antonshilov.exchange.data.Quote
import com.antonshilov.exchange.data.QuotesService
import com.shopify.livedataktx.MutableLiveDataKtx
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class QuotesViewModel(private val api: QuotesService) : ViewModel() {
    private lateinit var symbolsList: List<String>
    val quotes = MutableLiveDataKtx<List<Quote>>()

    val map = LinkedHashMap<String, Quote>()

    private val disposables = CompositeDisposable()


    fun fetchQuotes() {
        disposables += api.getSymbols()
            .map {
                symbolsList = it
                it
            }
            .flattenAsObservable { it }
            .map {

                val quote = createEmptyQuote(it)
                map[it] = quote
                quote
            }
            .collectInto(ArrayList<Quote>()) { t1, t2 -> t1.add(t2) }
            .subscribeBy(
                onSuccess = {
                    quotes.postValue(it)
                },
                onError = {
                    it.printStackTrace()
                }
            )
    }

    fun createEmptyQuote(symbol: String): Quote {
        return Quote(symbol, -1.0, -1.0, -1.0, 0L)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    fun setVisibleItemsStream(visibleItems: Observable<Pair<Int, Int>>) {
        disposables += observable(visibleItems)
            .map { symbolsList.subList(it.first, it.second) }
            .map {
                it.map { map[it]!! }.filter { System.currentTimeMillis() - it.timestamp >= 2000 }.map { it.symbol }
                    .joinToString(",")
            }
            .flatMap { api.getQuotes(it) }
            .timeInterval(TimeUnit.MILLISECONDS)
            .subscribe {
                Timber.d("INTERVAL${it.time()}")
                it.value().forEach { map[it.symbol] = it.copy(price = Random.nextDouble(100.9)) }
                quotes.postValue(map.values.toList())
                Timber.d("QUOTES $it")
            }
    }

    private fun observable(visibleItems: Observable<Pair<Int, Int>>): Observable<Pair<Int, Int>> {
        return Observable.interval(0, 5, TimeUnit.SECONDS)
            .flatMap { visibleItems }
            .debounce(600, TimeUnit.MILLISECONDS, Schedulers.io())
    }

}

class QuotesAdapter : ListAdapter<Quote, QuotesAdapter.ViewHolder>(ItemCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_quote, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val quote = getItem(position)!!
        holder.bind(quote)
    }

    internal object ItemCallback : DiffUtil.ItemCallback<Quote>() {
        override fun areItemsTheSame(oldItem: Quote, newItem: Quote): Boolean {
            return oldItem.symbol == newItem.symbol
        }

        override fun areContentsTheSame(oldItem: Quote, newItem: Quote): Boolean {
            return oldItem == newItem
        }

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(quote: Quote) {
            itemView.findViewById<TextView>(R.id.symbol).text = quote.symbol + quote.price
        }
    }
}
