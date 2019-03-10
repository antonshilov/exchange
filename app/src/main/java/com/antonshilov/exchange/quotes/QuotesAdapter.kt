package com.antonshilov.exchange.quotes

import android.os.Handler
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.antonshilov.exchange.R
import com.antonshilov.exchange.data.Quote

class QuotesAdapter : ListAdapter<Quote, QuotesAdapter.ViewHolder>(ItemCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_quote,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val quote = getItem(position)
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
        private val handler = Handler()
        private val lastUpdateRefreshDelay = 1000L

        fun bind(quote: Quote) {
            if (quote.isInitialized) {
                bindQuote(quote)
            } else {
                bindUninitializedQuote(quote)
            }
        }

        private fun bindQuote(quote: Quote) {
            itemView.findViewById<TextView>(R.id.symbol).text = quote.symbol
            itemView.findViewById<TextView>(R.id.price).text = itemView.context.getString(R.string.price, quote.price)
            itemView.findViewById<TextView>(R.id.ask).text = itemView.context.getString(R.string.ask, quote.ask)
            itemView.findViewById<TextView>(R.id.bid).text = itemView.context.getString(R.string.bid, quote.bid)
            setUpdateTime(quote)
        }

        private fun setUpdateTime(quote: Quote) {
            setUpdateTimeRelative(quote)
            handler.removeCallbacksAndMessages(null)
            val updateRunnable = object : Runnable {
                override fun run() {
                    setUpdateTimeRelative(quote)
                    handler.postDelayed(this, lastUpdateRefreshDelay)
                }
            }
            handler.postDelayed(updateRunnable, lastUpdateRefreshDelay)
        }

        private fun bindUninitializedQuote(quote: Quote) {
            itemView.findViewById<TextView>(R.id.symbol).text = quote.symbol
            itemView.findViewById<TextView>(R.id.price).text = itemView.context.getString(R.string.empty_price)
            itemView.findViewById<TextView>(R.id.ask).text = itemView.context.getString(R.string.empty_ask)
            itemView.findViewById<TextView>(R.id.bid).text = itemView.context.getString(R.string.empty_bid)
            itemView.findViewById<TextView>(R.id.lastUpdate).text = itemView.context.getString(
                R.string.not_updated
            )
        }

        private fun setUpdateTimeRelative(quote: Quote) {
            itemView.findViewById<TextView>(R.id.lastUpdate).text = itemView.context.getString(
                R.string.updated,
                DateUtils.getRelativeTimeSpanString(
                    quote.timestamp * lastUpdateRefreshDelay,
                    System.currentTimeMillis(),
                    DateUtils.SECOND_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
                )
            )
        }
    }
}
