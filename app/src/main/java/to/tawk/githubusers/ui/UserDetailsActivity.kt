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
    private lateinit var networkStatusUtil: NetworkStatusUtil

    var userDetails: Details? = null
    private var isInverted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userItem = intent.extras?.getParcelable<User>("user_item")
        isInverted = intent.extras?.getBoolean("is_inverted") ?: false
        val username = userItem?.login ?: ""
        initializeActionBar(username)
        viewModel.userDetailsLiveData.observe(this, Observer {
            handleResponse(it)
        })
        userDetails = viewModel.getAppDB().userDetailsDao().getUsersDetail(login = username)
        getUserDetails(username)
        initializeButton()
        initializeNoNetworkHandler(username)
    }

    /**
     * set up the save button
     * */
    private fun initializeButton(){
        binding.btnSave.setOnClickListener {
            lifecycleScope.launch {
                // save the note written in the db
                userDetails?.note = binding.etNotesMultiline.text.toString()
                userDetails?.let { viewModel.getAppDB().userDetailsDao().updateUserDetail(it) }
                // activate a refresh on the list when this activity is closed
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

    /**
     * set up the action bar
     * */
    private fun initializeActionBar(username: String) {
        supportActionBar?.title = "${username?.toUpperCase()}"
        supportActionBar?.show()
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    /**
     * Handle no network scenario.
     * Show an 'offline' message when the network is not available and
     * retrieve data from the API when the network is available
     * */
    private fun initializeNoNetworkHandler(username: String) {
        networkStatusUtil = NetworkStatusUtil(context = this) {
            getUserDetails(username)
        }.apply {
            build(binding.tvNetworkStatusBar)
        }
    }

    /**
     * Retrieves the user details from the db, otherwise,
     * if the details are not yet available in the DB, it is fetched from the API and saved to DB
     * */
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

    /**
     * Handles the different statuses from the API response
     * */
    private fun handleResponse(apiResponse: ApiResponse<Details>?) {
        apiResponse?.let { response->
            when(response.mStatus){
                Status.SUCCESS -> {
                    val item = response.mResponse
                    item?.let {
                        // the details fetched from the API is saved in the DB
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
                    // show a toast message when there is an error from the API
                    apiResponse?.mError?.let { showToast(getString(R.string.something_went_wrong)) }
                }
                Status.FAIL -> {
                    // show a toast message when there is a throwable error encountered
                    apiResponse?.mThrowable?.message?.let { showToast(it) }
                }
                Status.LOADING -> { }
            }
        }
    }

    /**
     * composes the user's details from the Detail object
     * and displays them in the UI
     * */
    private fun updateDetails(details: Details?, isInverted: Boolean) {

        // update the followers and following count
        binding.tvFollowers.text = HtmlCompat.fromHtml(getString(R.string.followers,(details?.followers ?: 0)),HtmlCompat.FROM_HTML_MODE_LEGACY)
        binding.tvFollowing.text = HtmlCompat.fromHtml(getString(R.string.following,(details?.following ?: 0)),HtmlCompat.FROM_HTML_MODE_LEGACY)

        // by order of priority, the changes in the notes before the orientation changes is checked first
        // then, if null, check the note on the details object
        binding.etNotesMultiline.setText(viewModel?.notes ?: details?.note ?: "")

        // compose the details
        val builder = StringBuilder()

        if (!details?.name.isNullOrEmpty()) {
            builder.append(getString(R.string.name,details?.name)).append("<br>")
        }
        if (!details?.company.isNullOrEmpty()) {
            builder.append(getString(R.string.company,details?.company)).append("<br>")
        }
        if (!details?.blog.isNullOrEmpty()) {
            builder.append(getString(R.string.blog,details?.blog)).append("<br>")
        }
        if (!details?.email.isNullOrEmpty()) {
            builder.append(getString(R.string.email,details?.email)).append("<br>")
        }
        if (!details?.location.isNullOrEmpty()) {
            builder.append(getString(R.string.location,details?.location)).append("<br>")
        }

        // show the composed HTML data to the textview
        binding.llUserDetails.text = HtmlCompat.fromHtml(builder.toString(),HtmlCompat.FROM_HTML_MODE_LEGACY)

        // Transform and load the image fetched from the API
        Glide.with(this)
            .load(details?.avatar_url)
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
            // cache the image to load faster and handle offline scenario
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(binding.imgPic)
    }

    /**
     * Saving notes data in the viewModel when configuration changes.
     * Another alternative is to save via
     * @param outstate
     * */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.notes = binding.etNotesMultiline.text.toString()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(networkStatusUtil.br)
    }
}