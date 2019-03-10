package com.antonshilov.exchange.di

import com.antonshilov.exchange.QuotesViewModel
import com.antonshilov.exchange.data.QuotesSerficeFactory
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { QuotesSerficeFactory.makeService() }

    viewModel { QuotesViewModel(get()) }
}