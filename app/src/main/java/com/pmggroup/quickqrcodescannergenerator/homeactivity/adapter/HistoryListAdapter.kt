package com.pmggroup.quickqrcodescannergenerator.homeactivity.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import androidx.recyclerview.widget.RecyclerView
import com.pmggroup.quickqrcodescannergenerator.databinding.ListitemHistoryBinding
import com.pmggroup.quickqrcodescannergenerator.homeactivity.view.HomeActivity
import java.lang.Exception


class HistoryListAdapter(
    var historyList: ArrayList<String>,
    private val copyClick: (result: String) -> Unit,
    private val shareClick: (result: String) -> Unit,
    private val deletClick: (result: Int) -> Unit
) :
    RecyclerView.Adapter<HistoryListAdapter.DataViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        return DataViewHolder(
            ListitemHistoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return historyList.size
    }

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.bind(position)

        holder.binding.imgCopy.setOnClickListener {
            copyClick.invoke(historyList[position])
        }
        holder.binding.tvOpen.setOnClickListener {
            shareClick.invoke(historyList[position])
        }
        holder.binding.imgDelete.setOnClickListener {
            deletClick.invoke(position)
        }
    }

    inner class DataViewHolder constructor(val binding: ListitemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val value = historyList[position]

            if (isValidValue(value)){
                binding.tvOpen.visibility = View.VISIBLE
            }else{
                binding.tvOpen.visibility = View.INVISIBLE
            }
            binding.tvTitle.text = value
        }
    }

    fun isValidValue(value:String): Boolean {
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(value)
        return i.resolveActivity(HomeActivity.homeActivity.packageManager) != null
    }

}