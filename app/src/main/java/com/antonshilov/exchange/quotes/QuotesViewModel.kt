package com.antonshilov.exchange.quotes

import com.antonshilov.exchange.data.Quote
import com.antonshilov.exchange.data.QuotesService
import com.antonshilov.exchange.util.RxViewModel
import com.antonshilov.exchange.util.immutable
import com.shopify.livedataktx.MutableLiveDataKtx
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit


class QuotesViewModel(private val api: QuotesService) : RxViewModel() {
    private val quoteLatencyLimit = 2000L
    private val refreshCallDebounce = 300L
    private lateinit var symbolsList: List<String>
    private val quoteMap = LinkedHashMap<String, Quote>()
    private val liveQuotes = MutableLiveDataKtx<List<Quote>>()
    private var updateDisposable: Disposable? = null

    val quotes
        get() = liveQuotes.immutable()

    fun fetchQuotes() {
        disposables += api.getSymbols()
            .map {
                symbolsList = it
                it
            }
            .flattenAsObservable { it }
            .map {
                val quote = createEmptyQuote(it)
                quoteMap[it] = quote
            }
            .subscribeBy(
                onComplete = {
                    liveQuotes.postValue(quoteMap.values.toList())
                },
                onError = {
                    Timber.e(it)
                }
            )
    }

    fun createEmptyQuote(symbol: String): Quote {
        return Quote(symbol, -1.0, -1.0, -1.0, 0L)
    }


    fun setVisibleItemsStream(visibleItems: Observable<Pair<Int, Int>>) {
        updateDisposable = Observable.interval(0, 5, TimeUnit.SECONDS)
            .flatMap { visibleItems }
            .debounce(refreshCallDebounce, TimeUnit.MILLISECONDS, Schedulers.io())
            .map { symbolsList.subList(it.first, it.second) }
            .map {
                it.map { quoteMap[it]!! }
                    .filter { it.isExpired(quoteLatencyLimit) }
                    .joinToString(",") { it.symbol }
            }
            .flatMap { api.getQuotes(it) }
            .timeInterval(TimeUnit.MILLISECONDS)
            .subscribeBy(
                onNext = {
                    it.value().forEach { quoteMap[it.symbol] = it }
                    liveQuotes.postValue(quoteMap.values.toList())
                },
                onError = {
                    Timber.e(it)
                }
            )
    }

    fun cancelVisibilityUpdates() {
        updateDisposable?.dispose()
    }

    override fun onCleared() {
        super.onCleared()
        updateDisposable?.dispose()
    }
}
