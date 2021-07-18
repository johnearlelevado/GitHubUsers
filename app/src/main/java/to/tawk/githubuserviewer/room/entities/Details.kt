package to.tawk.githubuserviewer.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "details")
data class Details (

	@PrimaryKey
	@SerializedName("login")
	@ColumnInfo(name = "login_details")
	val login : String,
	@SerializedName("id")
	@ColumnInfo(name = "id_details")
	val id : Int,
	@SerializedName("node_id")
	@ColumnInfo(name = "node_id_details")
	val node_id : String? = null,
	@SerializedName("avatar_url")
	@ColumnInfo(name = "avatar_url_details")
	val avatar_url : String? = null,
	@SerializedName("gravatar_id")
	@ColumnInfo(name = "gravatar_id_details")
	val gravatar_id : String? = null,
	@SerializedName("url")
	@ColumnInfo(name = "url_details")
	val url : String? = null,
	@SerializedName("html_url")
	@ColumnInfo(name = "html_url_details")
	val html_url : String? = null,
	@SerializedName("followers_url")
	@ColumnInfo(name = "followers_url_details")
	val followers_url : String? = null,
	@SerializedName("following_url")
	@ColumnInfo(name = "following_url_details")
	val following_url : String? = null,
	@SerializedName("gists_url")
	@ColumnInfo(name = "gists_url_details")
	val gists_url : String? = null,
	@SerializedName("starred_url")
	@ColumnInfo(name = "starred_url_details")
	val starred_url : String? = null,
	@SerializedName("subscriptions_url")
	@ColumnInfo(name = "subscriptions_url_details")
	val subscriptions_url : String? = null,
	@SerializedName("organizations_url")
	@ColumnInfo(name = "organizations_url_details")
	val organizations_url : String? = null,
	@SerializedName("repos_url")
	@ColumnInfo(name = "repos_url_details")
	val repos_url : String? = null,
	@SerializedName("events_url")
	@ColumnInfo(name = "events_url_details")
	val events_url : String? = null,
	@SerializedName("received_events_url")
	@ColumnInfo(name = "received_events_url_details")
	val received_events_url : String? = null,
	@SerializedName("type")
	@ColumnInfo(name = "type_details")
	val type : String? = null,
	@SerializedName("site_admin")
	@ColumnInfo(name = "site_admin_details")
	val site_admin : Boolean? = null,
	@SerializedName("name")
	@ColumnInfo(name = "name_details")
	val name : String? = null,
	@SerializedName("company")
	@ColumnInfo(name = "company_details")
    var company : String? = null,
	@SerializedName("blog")
	@ColumnInfo(name = "blog_details")
	val blog : String? = null,
	@SerializedName("location")
	@ColumnInfo(name = "location_details")
	val location : String? = null,
	@SerializedName("email_details")
	@ColumnInfo(name = "email_details")
	val email : String? = null,
	@SerializedName("hireable_details")
	@ColumnInfo(name = "hireable_details")
	val hireable : String? = null,
	@SerializedName("bio")
	@ColumnInfo(name = "bio_details")
	val bio : String? = null,
	@SerializedName("twitter_username")
	@ColumnInfo(name = "twitter_username_details")
	val twitter_username : String? = null,
	@SerializedName("public_repos")
	@ColumnInfo(name = "public_repos_details")
	val public_repos : Int? = null,
	@SerializedName("public_gists")
	@ColumnInfo(name = "public_gists_details")
	val public_gists : Int? = null,
	@SerializedName("followers")
	@ColumnInfo(name = "followers_details")
	val followers : Int? = null,
	@SerializedName("following")
	@ColumnInfo(name = "following_details")
	val following : Int? = null,
	@SerializedName("created_at")
	@ColumnInfo(name = "created_at_details")
	val created_at : String? = null,
	@SerializedName("updated_at")
	@ColumnInfo(name = "updated_at_details")
	val updated_at : String? = null,
	@ColumnInfo(name = "note_details")
    var note : String? = null,
)