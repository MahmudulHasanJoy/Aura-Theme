package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.ThemeRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Aura Themes", appName)
  }

  @Test
  fun `verify curated repository items`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = ThemeRepository(context)
    assertTrue("Wallpapers should not be empty", repo.wallpapers.isNotEmpty())
    assertTrue("Ringtones should not be empty", repo.ringtones.isNotEmpty())
    assertTrue("Themes should not be empty", repo.themes.isNotEmpty())
  }
}
