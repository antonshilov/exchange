package com.antonshilov.exchange.data

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


interface QuotesService {

    @GET("symbols")
    fun getSymbols(): Single<List<String>>

    @GET("quotes")
    fun getQuotes(@Query("pairs") pairs: String): Observable<List<Quote>>
}

object QuotesSerficeFactory {
    fun makeService(): QuotesService {
        val retrofit = Retrofit.Builder()
            .client(makeClient())
            .baseUrl("https://forex.1forge.com/1.0.3/")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        return retrofit.create(QuotesService::class.java)
    }

    private fun makeClient(): OkHttpClient {
        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor { chain: Interceptor.Chain ->
            val original = chain.request()
            val originalHttpUrl = original.url()

            val url = originalHttpUrl.newBuilder()
                .addQueryParameter("api_key", "4hZAb32JfTCh7ineyODFhvC8sQHchVWr")
                .build()

            // Request customization: add request headers
            val requestBuilder = original.newBuilder()
                .url(url)

            val request = requestBuilder.build()
            chain.proceed(request)
        }
        httpClient.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        return httpClient.build()
    }
}

data class Quote(
    val symbol: String,
    val price: Double,
    val bid: Double,
    val ask: Double,
    val timestamp: Long
) {
    val isInitialized: Boolean
        get() = timestamp != 0L
}
