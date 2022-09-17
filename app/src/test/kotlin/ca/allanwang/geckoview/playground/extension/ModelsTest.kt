package ca.allanwang.geckoview.playground.extension

import ca.allanwang.geckoview.playground.hilt.MoshiModule
import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.adapter
import org.json.JSONObject
import org.junit.Test

class ModelsTest {

  private val moshi = MoshiModule.moshi()

  @OptIn(ExperimentalStdlibApi::class)
  @Test
  fun serialization() {
    val data = TestModel(message = "test message")

    val adapter = moshi.adapter<ExtensionModel>()
    val jsonObjectAdapter = moshi.adapter<JSONObject>()

    val json = jsonObjectAdapter.fromJsonValue(adapter.toJsonValue(data))

    assertThat(json.toString())
      .isEqualTo(
        JSONObject().put("type", ExtensionType.TEST).put("message", "test message").toString()
      )
  }
}
