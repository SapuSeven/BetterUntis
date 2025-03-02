package com.sapuseven.untis.data.model.github

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object GitHubApi {
	const val URL_GITHUB_PRIVACY_POLICY = "https://docs.github.com/en/github/site-policy/github-privacy-statement"
	const val URL_GITHUB_REPOSITORY = "https://github.com/SapuSeven/BetterUntis"
	const val URL_GITHUB_REPOSITORY_API = "https://api.github.com/repos/SapuSeven/BetterUntis"
}

@Serializable
data class GitHubUser(
	@SerialName("login")
	val login: String,

	@SerialName("id")
	val id: Int,

	@SerialName("node_id")
	val nodeId: String,

	@SerialName("avatar_url")
	val avatarUrl: String,

	@SerialName("gravatar_id")
	val gravatarId: String,

	@SerialName("url")
	val url: String,

	@SerialName("html_url")
	val htmlUrl: String,

	@SerialName("followers_url")
	val followersUrl: String,

	@SerialName("following_url")
	val followingUrl: String,

	@SerialName("gists_url")
	val gistsUrl: String,

	@SerialName("starred_url")
	val starredUrl: String,

	@SerialName("subscriptions_url")
	val subscriptionsUrl: String,

	@SerialName("organizations_url")
	val organizationsUrl: String,

	@SerialName("repos_url")
	val reposUrl: String,

	@SerialName("events_url")
	val eventsUrl: String,

	@SerialName("received_events_url")
	val receivedEventsUrl: String,

	@SerialName("type")
	val type: String,

	@SerialName("site_admin")
	val siteAdmin: Boolean,

	@SerialName("contributions")
	val contributions: Int
)
