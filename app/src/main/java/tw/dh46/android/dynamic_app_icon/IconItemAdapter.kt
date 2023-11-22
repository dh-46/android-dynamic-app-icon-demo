package tw.dh46.android.dynamic_app_icon

import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import tw.dh46.android.dynamic_app_icon.databinding.ItemIconBinding

/**
 *  Created by DanielHuang on 2023/11/21
 */
class IconItemAdapter(
    private val dataList: List<AppIcon>,
    val onClicked: (icon: AppIcon, index: Int) -> Unit) :
    RecyclerView.Adapter<IconItemAdapter.ViewHolder>() {


    class ViewHolder(private val binding: ItemIconBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setOnClickListener(listener: OnClickListener) {
            binding.root.setOnClickListener(listener)
        }

        fun bindData(icons: AppIcon) {
            binding.ivIcon.setImageResource(icons.iconResId)
            binding.tvName.text = icons.alias
            binding.viewIndicator.isVisible = icons.isActive
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val holder =
            ViewHolder(ItemIconBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        holder.setOnClickListener {
            onClicked(dataList[holder.adapterPosition], holder.adapterPosition)
        }

        return holder
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(dataList[position])
    }
}