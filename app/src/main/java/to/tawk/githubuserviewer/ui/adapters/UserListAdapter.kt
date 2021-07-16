package to.tawk.githubuserviewer.ui.adapters;

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.request.RequestOptions
import jp.wasabeef.glide.transformations.CropCircleTransformation
import jp.wasabeef.glide.transformations.gpu.InvertFilterTransformation
import to.tawk.githubuserviewer.databinding.LayoutItemRowBinding
import to.tawk.githubuserviewer.room.dao.DetailsDao
import to.tawk.githubuserviewer.room.entities.User
import to.tawk.githubuserviewer.ui.UserDetailsActivity

class UserListAdapter (private val context: Activity, private val userDetailsDao: DetailsDao) : PagingDataAdapter<User, UserListAdapter.VHolder>(DIFF_CALLBACK) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VHolder {
        val binding = LayoutItemRowBinding.inflate(LayoutInflater.from(context), parent, false)
        return VHolder(binding,userDetailsDao,context)
    }

    override fun onBindViewHolder(viewHolder: VHolder, position: Int) {
        val item = getItem(position)!!
        val isInverted = (position + 1) % 4 == 0
        viewHolder.bind(item, isInverted)
    }

    class VHolder (private val layoutUserBinding: LayoutItemRowBinding,val detailsDao: DetailsDao, val context: Activity) : RecyclerView.ViewHolder(layoutUserBinding.root) {
        fun bind(item: User, isInverted: Boolean){

            layoutUserBinding.tvName.text = "${item.login}"
            layoutUserBinding.tvDetails.text = "${item.html_url}"

            Glide.with(itemView.context).load(item?.avatar_url)
                .apply {
                    val multiTransformation = MultiTransformation (CropCircleTransformation(),InvertFilterTransformation())
                    if (isInverted)
                        apply(RequestOptions.bitmapTransform(multiTransformation))
                    else
                        apply(RequestOptions.bitmapTransform(CropCircleTransformation()))
                }
                .into(layoutUserBinding.imgPic)

            layoutUserBinding.root.setOnClickListener {
                val i = Intent(itemView.context,UserDetailsActivity::class.java)
                i.putExtra("user_item",item.login)
                context.startActivityForResult(i,UserDetailsActivity.REQUEST_CODE)
            }

            val details = detailsDao.getUsersDetail(item.login)
            layoutUserBinding.imgNote.isVisible = !details?.note.isNullOrEmpty() == true
        }
    }
    companion object {
        var DIFF_CALLBACK: DiffUtil.ItemCallback<User> = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem.id === newItem.id
            }
            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem == newItem
            }
        }
    }
}
