package to.tawk.githubusers.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.glide.transformations.CropCircleTransformation
import jp.wasabeef.glide.transformations.gpu.InvertFilterTransformation
import kotlinx.coroutines.launch
import to.tawk.githubusers.R
import to.tawk.githubusers.api.common.ApiResponse
import to.tawk.githubusers.api.common.Status
import to.tawk.githubusers.databinding.ActivityUserDetailsBinding
import to.tawk.githubusers.room.entities.Details
import to.tawk.githubusers.room.entities.User
import to.tawk.githubusers.util.NetworkStatusUtil
import to.tawk.githubusers.viewmodels.UserDetailsViewModel

@AndroidEntryPoint
class UserDetailsActivity : BaseActivity() {

    private val viewModel: UserDetailsViewModel by viewModels()
    private lateinit var binding: ActivityUserDetailsBinding

    var userDetails: Details? = null
    private var isInverted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userItem = intent.extras?.getParcelable<User>("user_item")
        isInverted = intent.extras?.getBoolean("is_inverted") ?: false
        val username = userItem?.login ?: ""
        val position = userItem?.position ?: 0
        initializeActionBar(username)
        viewModel.userDetailsLiveData.observe(this, Observer {
            handleResponse(it)
        })
        userDetails = viewModel.getAppDB().userDetailsDao().getUsersDetail(login = username)
        getUserDetails(username)
        initializeButton(position)
        initializeNoNetworkHandler(username)
    }

    private fun initializeButton(position:Int){
        binding.btnSave.setOnClickListener {
            lifecycleScope.launch {
                userDetails?.note = binding.etNotesMultiline.text.toString()
                userDetails?.let { viewModel.getAppDB().userDetailsDao().updateUserDetail(it) }
                setResult(Activity.RESULT_OK, Intent().apply {
                    bundleOf("position" to position)
                })
                finish()
            }
        }
    }

    private fun initializeActionBar(username: String) {
        supportActionBar?.title = "${username?.toUpperCase()}"
        supportActionBar?.show()
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initializeNoNetworkHandler(username: String) {
        NetworkStatusUtil(context = this) {
            getUserDetails(username)
        }.apply {
            build(binding.tvNetworkStatusBar)
        }
    }

    private fun getUserDetails(username: String) {
        if (userDetails?.html_url == null) {
            viewModel.getUserDetails(username = username)
        } else {
            updateDetails(userDetails, isInverted)
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
                    val item = response.mResponse
                    item?.let {
                        lifecycleScope.launch {
                            viewModel
                                .getAppDB()
                                .userDetailsDao()
                                .insertUserDetail(details = item)
                        }
                        userDetails = item
                        updateDetails(item,isInverted)
                    }
                }
                Status.ERROR -> {
                    apiResponse?.mError?.let { showToast(getString(R.string.something_went_wrong)) }
                }
                Status.FAIL -> {
                    apiResponse?.mThrowable?.message?.let { showToast(it) }
                }
                Status.LOADING -> { }
            }
        }
    }

    private fun updateDetails(item: Details?, isInverted: Boolean) {
        binding.tvFollowers.text = HtmlCompat.fromHtml(getString(R.string.followers,(item?.followers ?: 0)),HtmlCompat.FROM_HTML_MODE_LEGACY)
        binding.tvFollowing.text = HtmlCompat.fromHtml(getString(R.string.following,(item?.following ?: 0)),HtmlCompat.FROM_HTML_MODE_LEGACY)
        binding.etNotesMultiline.setText(item?.note ?: "")

        val builder = StringBuilder()

        if (!item?.name.isNullOrEmpty()) {
            builder.append(getString(R.string.name,item?.name)).append("<br>")
        }
        if (!item?.company.isNullOrEmpty()) {
            builder.append(getString(R.string.company,item?.company)).append("<br>")
        }
        if (!item?.blog.isNullOrEmpty()) {
            builder.append(getString(R.string.blog,item?.blog)).append("<br>")
        }
        if (!item?.email.isNullOrEmpty()) {
            builder.append(getString(R.string.email,item?.email)).append("<br>")
        }
        if (!item?.location.isNullOrEmpty()) {
            builder.append(getString(R.string.location,item?.location)).append("<br>")
        }

        binding.llUserDetails.text = HtmlCompat.fromHtml(builder.toString(),HtmlCompat.FROM_HTML_MODE_LEGACY)

        Glide.with(this)
            .load(item?.avatar_url)
            .apply {
                val multiTransformation = MultiTransformation (
                    CropCircleTransformation(),
                    InvertFilterTransformation()
                )
                if (isInverted)
                    apply(RequestOptions.bitmapTransform(multiTransformation))
                else
                    apply(RequestOptions.bitmapTransform(CropCircleTransformation()))
            }
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(binding.imgPic)
    }

    companion object {
        const val REQUEST_CODE = 100
    }
}