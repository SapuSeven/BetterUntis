package com.sapuseven.untis.helpers.issues

import android.content.Context
import android.content.Intent
import android.net.Uri


class GithubIssue(type: Type, log: String) : Issue(type, log) {
	override fun launch(context: Context) {
		val uri = Uri.Builder()
				.scheme("https")
				.authority("github.com")
				.path("/SapuSeven/BetterUntis/issues/new")
				.appendQueryParameter("title", generateTitle())
				.appendQueryParameter("body", generateBody())

		val browserIntent = Intent(Intent.ACTION_VIEW, uri.build())
		context.startActivity(browserIntent)
	}

	private fun generateTitle() = when (type) {
		Type.CRASH -> "Crash Report"
		Type.EXCEPTION -> "Bug Report"
		Type.OTHER -> ""
	}

	private fun generateBody() =
			"<details>\n" +
					"<summary>Crash Log</summary>\n" +
					"\n" +
					"```\n" +
					"$log\n" +
					"```\n" +
					"</details>"
}
