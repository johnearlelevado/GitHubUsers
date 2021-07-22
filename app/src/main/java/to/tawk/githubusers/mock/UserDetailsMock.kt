package to.tawk.githubusers.mock

import to.tawk.githubusers.room.entities.Details
import to.tawk.githubusers.room.entities.User

/**
 * Mock objects for unit testing and integrated testing
 * */
object UserDetailsMock {
    fun getDetails() = Details(login = "fake-login",
        id = 0,
        node_id = "fake-node-id",
        avatar_url = "fake-avatar-url",
        gravatar_id = "fake-gravatar-id",
        url = "fake-url",
        html_url = "fake-html-url",
        note = ""
    )

    fun getUser() = User(login = "fake-login",
        id = 0,
        node_id = "fake-node-id",
        avatar_url = "fake-avatar-url",
        gravatar_id = "fake-gravatar-id",
        url = "fake-url",
        html_url = "fake-html-url",
        position = 0
    )
}