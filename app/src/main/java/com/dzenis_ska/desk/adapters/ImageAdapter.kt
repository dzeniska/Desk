package com.dzenis_ska.desk.adapters

import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.dzenis_ska.desk.R

class ImageAdapter: RecyclerView.Adapter<ImageAdapter.ImageHolder>() {
    val mainArray = ArrayList<Bitmap>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.image_adapter_item, parent, false)
        return ImageHolder(itemView)
    }

    override fun getItemCount(): Int {
        return mainArray.size
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        holder.setDate(mainArray[position])
    }

    class ImageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var imItem: ImageView
        fun setDate(bitmap: Bitmap){
            imItem = itemView.findViewById(R.id.imItem)
            imItem.setImageBitmap(bitmap)
        }
    }
    fun update(newList: ArrayList<Bitmap>){
        mainArray.clear()
        mainArray.addAll(newList)
        notifyDataSetChanged()
    }
}