package com.dzenis_ska.desk.frag

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dzenis_ska.desk.R
import com.dzenis_ska.desk.act.EditAdsAct
import com.dzenis_ska.desk.databinding.SelectImageFragItemBinding
import com.dzenis_ska.desk.utils.AdapterCallBack
import com.dzenis_ska.desk.utils.ImageManager
import com.dzenis_ska.desk.utils.ImagePicker
import com.dzenis_ska.desk.utils.ItemTouchMoveCallback

class   SelectImageRVAdapter(val adapterCallBack: AdapterCallBack) : RecyclerView.Adapter<SelectImageRVAdapter.ImageHolder>(), ItemTouchMoveCallback.ItemTouchAdapter {
    val mainArray = ArrayList<Bitmap>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val viewBinding = SelectImageFragItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageHolder(viewBinding, parent.context, this)
    }

    override fun getItemCount(): Int {
        return mainArray.size
    }

    override fun onMove(startPos: Int, targetPos: Int) {
        val targetItem = mainArray[targetPos]
        mainArray[targetPos] = mainArray[startPos]
        mainArray[startPos] = targetItem
        notifyItemMoved(startPos, targetPos)
    }

    override fun onClear() {
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        holder.setData(mainArray[position])
    }

    class ImageHolder(private val viewBinding: SelectImageFragItemBinding, val context: Context, val adapter: SelectImageRVAdapter): RecyclerView.ViewHolder(viewBinding.root) {

        fun setData(bitMap: Bitmap){

            viewBinding.apply {
                imDelete.setOnClickListener(){
                    adapter.mainArray.removeAt(adapterPosition)
                    adapter.notifyItemRemoved(adapterPosition)
                    for(n in 0 until adapter.mainArray.size) adapter.notifyItemChanged(n)
                    adapter.adapterCallBack.onItemDelete()
                }
                imEditImage.setOnClickListener(){
                    ImagePicker.launcher(context as EditAdsAct, 1)
                    context.editImagePos = adapterPosition
                }
                tvTitle.text = context.resources.getStringArray(R.array.title_array)[adapterPosition]
                ImageManager.chooseScaleType(viewBinding.imageContent, bitMap)
                imageContent.setImageBitmap(bitMap)
            }


        }
    }
    fun updateAdapter(newList: List<Bitmap>, needClear: Boolean){
        if(needClear) mainArray.clear()
        mainArray.addAll(newList)
        Log.d("!!!!", "$newList")
        notifyDataSetChanged()
    }


}