package to.tawk.githubusers.room.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "user")
data class User (
	@PrimaryKey
	@SerializedName("login")
	@ColumnInfo(name = "login")
	val login : String,
	@SerializedName("id")
	@ColumnInfo(name = "id")
	val id : Int,
	@SerializedName("node_id")
	@ColumnInfo(name = "node_id")
	val node_id : String,
	@SerializedName("avatar_url")
	@ColumnInfo(name = "avatar_url")
	val avatar_url : String,
	@SerializedName("gravatar_id")
	@ColumnInfo(name = "gravatar_id")
	val gravatar_id : String,
	@SerializedName("url")
	@ColumnInfo(name = "url")
	val url : String,
	@SerializedName("html_url")
	@ColumnInfo(name = "html_url")
	val html_url : String,
	@ColumnInfo(name = "position")
	var position: Int
): Parcelable