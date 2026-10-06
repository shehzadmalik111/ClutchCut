package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ProjectJsonParser
import com.example.data.SampleTemplates
import com.example.engine.KeyframeInterpolator
import com.example.model.ClipKeyframe
import org.junit.Assert.*
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
        assertEquals("NovaCut", appName)
    }

    @Test
    fun `project json serialization and deserialization roundtrip`() {
        val original = SampleTemplates.createGamingHighlightProject()
        val json = ProjectJsonParser.toJson(original)
        assertNotNull(json)
        assertTrue(json.contains("Cyberpunk Gaming Clutches"))

        val restored = ProjectJsonParser.fromJson(json)
        assertEquals(original.id, restored.id)
        assertEquals(original.title, restored.title)
        assertEquals(original.videoClips.size, restored.videoClips.size)
        assertEquals(original.textLayers.size, restored.textLayers.size)
    }

    @Test
    fun `keyframe interpolator calculates values accurately`() {
        val keyframes = listOf(
            ClipKeyframe(timeOffsetMs = 0L, scale = 1.0f, rotation = 0f),
            ClipKeyframe(timeOffsetMs = 1000L, scale = 2.0f, rotation = 90f)
        )

        val midpoint = KeyframeInterpolator.interpolate(keyframes, 500L)
        assertTrue(midpoint.scale > 1.0f && midpoint.scale < 2.0f)
        assertTrue(midpoint.rotation > 0f && midpoint.rotation < 90f)
    }
}
