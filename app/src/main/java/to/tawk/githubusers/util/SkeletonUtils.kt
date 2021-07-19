package to.tawk.githubusers.util

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import to.tawk.githubusers.R

object SkeletonUtils {
    fun getSkeletonRowCount(context: Context): Int {
        val pxHeight = getDeviceHeight(context)
        val skeletonRowHeight = context.resources
            .getDimension(R.dimen.row_layout_height).toInt() //converts to pixel
        return Math.ceil((pxHeight / skeletonRowHeight).toDouble()).toInt()
    }

    fun getDeviceHeight(context: Context): Int {
        val resources: Resources = context.resources
        val metrics: DisplayMetrics = resources.displayMetrics
        return metrics.heightPixels
    }
}