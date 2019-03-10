package com.antonshilov.exchange.util

import com.shopify.livedataktx.LiveDataKtx
import com.shopify.livedataktx.MutableLiveDataKtx

fun <T> MutableLiveDataKtx<T>.immutable(): LiveDataKtx<T> = this