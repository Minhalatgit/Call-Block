package com.blockcallnow.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blockcallnow.R
//import com.blockcallnow.app.GlideApp
import com.blockcallnow.data.room.BlockContact
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_block_call.view.*

class BlockListAdapter :
    RecyclerView.Adapter<BlockListAdapter.VH>() {
    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView)

    var contactsList: MutableList<BlockContact>? = null

    var listener: UnBlockListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            LayoutInflater.from(parent.context).inflate(R.layout.item_block_call, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return contactsList?.size ?: 0
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val contact = contactsList?.get(holder.adapterPosition)
        contact?.blockStatus?.let {
            holder.itemView.tv_block_status?.text = "$it Block".capitalize()
        }

        holder.itemView.tv_name?.text = contact?.name ?: "Unknown"
        holder.itemView.tv_phone_no?.text = contact?.number
        Glide.with(holder.itemView.iv_profile)
            .load(contact?.uri)
            .placeholder(R.drawable.img_dummy)
            .error(R.drawable.img_dummy)
            .into(holder.itemView.iv_profile)
        holder.itemView.iv_unblock?.setOnClickListener {
            listener?.onUnBlock(contact)
        }
        holder.itemView.setOnClickListener {
            listener?.showDetail(contact)
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    interface UnBlockListener {
        fun onUnBlock(blockContact: BlockContact?)
        fun showDetail(blockContact: BlockContact?)
    }
}