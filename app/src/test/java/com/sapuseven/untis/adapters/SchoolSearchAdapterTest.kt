package com.sapuseven.untis.adapters

import android.view.View
import com.sapuseven.untis.models.UntisSchoolInfo
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test


class SchoolSearchAdapterTest {
	@Test
	fun addAndRemoveItemsTest() {
		val listener = View.OnClickListener {}
		val adapter = SchoolSearchAdapter(listener)

		assertThat(adapter.itemCount, `is`(0))

		val item1 = SchoolSearchAdapterItem(UntisSchoolInfo(
				server = "server",
				useMobileServiceUrlAndroid = true,
				useMobileServiceUrlIos = false,
				address = "123",
				displayName = "school display name",
				loginName = "LOGIN_NAME",
				schoolId = 123,
				serverUrl = "http://",
				mobileServiceUrl = "http://"
		))
		adapter.addToDataset(item1)
		assertThat(adapter.itemCount, `is`(1))
		assertThat(adapter.getDatasetItem(0), `is`(item1))

		adapter.clearDataset()
		assertThat(adapter.itemCount, `is`(0))
	}
}
