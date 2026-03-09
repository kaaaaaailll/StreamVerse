package com.example.streamverse

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import java.io.File

class ContentAdapter(
    private val items: MutableList<ContentItem>,
    private val onItemClick: (ContentItem) -> Unit
) : RecyclerView.Adapter<ContentAdapter.ViewHolder>() {

    private val displayItems = mutableListOf<ContentItem>().also { it.addAll(items) }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val thumbnail: ImageView = view.findViewById(R.id.IV_Thumbnail)
        val title: TextView = view.findViewById(R.id.TV_ContentTitle)
        val description: TextView = view.findViewById(R.id.TV_ContentDescription)
        val episode: TextView = view.findViewById(R.id.TV_EpisodeInfo)
        val rating: TextView = view.findViewById(R.id.TV_Rating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_content_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = displayItems[position]
        holder.title.text = item.title
        holder.description.text = item.description
        holder.episode.text = item.episode
        holder.rating.text = item.rating

        if (!item.imageUri.isNullOrEmpty()) {
            val file = File(item.imageUri)
            val imageSource: Any = if (file.exists()) file else Uri.parse(item.imageUri)

            Glide.with(holder.thumbnail.context)
                .load(imageSource)
                .apply(
                    RequestOptions()
                        .override(200, 200)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                )
                .into(holder.thumbnail)
        } else {
            Glide.with(holder.thumbnail.context)
                .clear(holder.thumbnail)
            holder.thumbnail.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        Glide.with(holder.thumbnail.context).clear(holder.thumbnail)
    }

    override fun getItemCount() = displayItems.size

    override fun getItemId(position: Int): Long {
        return displayItems[position].id
    }

    fun updateList(filtered: List<ContentItem>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize() = displayItems.size
            override fun getNewListSize() = filtered.size
            override fun areItemsTheSame(oldPos: Int, newPos: Int) =
                displayItems[oldPos].id == filtered[newPos].id
            override fun areContentsTheSame(oldPos: Int, newPos: Int) =
                displayItems[oldPos] == filtered[newPos]
        }
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        displayItems.clear()
        displayItems.addAll(filtered)
        diffResult.dispatchUpdatesTo(this)
    }
}