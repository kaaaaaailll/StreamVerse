package com.example.streamverse

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView

class ContentAdapter(
    private val items: MutableList<ContentItem>,
    private val onItemClick: (ContentItem) -> Unit
) : RecyclerView.Adapter<ContentAdapter.ViewHolder>() {

    private val displayItems = mutableListOf<ContentItem>().also { it.addAll(items) }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val thumbnail: ShapeableImageView = view.findViewById(R.id.IV_Thumbnail)
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

        if (item.imageUri != null) {
            holder.thumbnail.setImageURI(Uri.parse(item.imageUri))
        } else {
            holder.thumbnail.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = displayItems.size

    fun updateList(filtered: List<ContentItem>) {
        displayItems.clear()
        displayItems.addAll(filtered)
        notifyDataSetChanged()
    }
}