package to.tawk.githubuserviewer.ui

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.core.text.HtmlCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.ViewTarget
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import to.tawk.githubuserviewer.api.common.ApiResponse
import to.tawk.githubuserviewer.api.common.Status
import to.tawk.githubuserviewer.databinding.ActivityUserDetailsBinding
import to.tawk.githubuserviewer.room.AppDatabase
import to.tawk.githubuserviewer.room.entities.Details
import to.tawk.githubuserviewer.viewmodels.UserDetailsViewModel
import javax.inject.Inject

@AndroidEntryPoint
class UserDetailsActivity : BaseActivity() {

    private val viewModel: UserDetailsViewModel by viewModels()
    private lateinit var binding: ActivityUserDetailsBinding

    @Inject
    lateinit var appDatabase: AppDatabase
    var userDetails: Details? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = intent.extras?.getString("user_item") ?: ""
        supportActionBar?.title = "${username?.toUpperCase()}"
        supportActionBar?.show()
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewModel.userDetailsLiveData.observe(this, Observer {
            handleResponse(it)
        })


        userDetails = appDatabase.userDetailsDao().getUsersDetail(login = username)
        if (userDetails == null) {
            viewModel.getUserDetails(username = username)
        } else {
            updateDetails(userDetails)
        }

        binding.btnSave.setOnClickListener {
            lifecycleScope.launch {
                userDetails?.note = binding.etNotesMultiline.text.toString()
                userDetails?.let { appDatabase.userDetailsDao().updateUserDetail(it) }
                setResult(Activity.RESULT_OK)
                finish()
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun handleResponse(apiResponse: ApiResponse<Details>?) {
        apiResponse?.let { response->
            when(response.mStatus){
                Status.SUCCESS -> {
                    //hideLoading()
                    val item = response.mResponse
                    item?.let {
                        lifecycleScope.launch {
                            appDatabase.userDetailsDao().insertUserDetail(user = item)
                        }
                        userDetails = item
                        updateDetails(item)
                    }
                }
                Status.ERROR -> {
                    //hideLoading()
                    apiResponse?.mError?.let { showToast("Something went wrong...") }
                }
                Status.FAIL -> {
                    //hideLoading()
                    apiResponse?.mThrowable?.message?.let { showToast(it) }
                }
                Status.LOADING -> {
                    //showLoading(this)
                }
            }
        }
    }

    private fun updateDetails(item: Details?) {
        binding.tvFollowers.text = "followers: ${item?.followers ?: 0}"
        binding.tvFollowing.text = "following: ${item?.following ?: 0}"
        binding.etNotesMultiline.setText(item?.note ?: "")

        val builder = StringBuilder()

        if (!item?.name.isNullOrEmpty()) {
            builder.append("<b>Name:</b> ${item?.name}").append("<br>")
        }
        if (!item?.company.isNullOrEmpty()) {
            builder.append("<b>Company:</b> ${item?.company}").append("<br>")
        }
        if (!item?.blog.isNullOrEmpty()) {
            builder.append("<b>Blog:</b> ${item?.blog}").append("<br>")
        }
        if (!item?.email.isNullOrEmpty()) {
            builder.append("<b>Email:</b> ${item?.email}").append("<br>")
        }
        if (!item?.location.isNullOrEmpty()) {
            builder.append("<b>Location:</b> ${item?.location}").append("<br>")
        }

        binding.llUserDetails.setText(HtmlCompat.fromHtml(builder.toString(),HtmlCompat.FROM_HTML_MODE_LEGACY))

        Glide.with(this)
            .load(item?.avatar_url)
            .circleCrop()
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(binding.imgPic)
    }

    companion object {
        const val REQUEST_CODE = 100
    }
}