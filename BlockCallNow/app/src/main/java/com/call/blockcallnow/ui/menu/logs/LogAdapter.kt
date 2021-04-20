package com.call.blockcallnow.ui.menu.logs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.call.blockcallnow.R
import com.call.blockcallnow.data.room.LogContact
import com.call.blockcallnow.util.Utils
import kotlinx.android.synthetic.main.call_log_item.view.*

class LogAdapter(private val list: List<LogContact>) :
    RecyclerView.Adapter<LogAdapter.LogHolder>() {

    inner class LogHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        LogHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.call_log_item, parent, false)
        )

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: LogHolder, position: Int) {
        val logContact = list[position]

        holder.itemView.apply {
            blockedUser.text = logContact.name?.capitalize() ?: logContact.phoneNumber
            time.text = Utils.getDateTime(logContact.currentTime)
        }
    }
}