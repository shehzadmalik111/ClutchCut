package com.example.data

import com.example.model.*
import org.json.JSONArray
import org.json.JSONObject

object ProjectJsonParser {

    fun toJson(project: Project): String {
        val root = JSONObject()
        root.put("id", project.id)
        root.put("title", project.title)
        root.put("createdAt", project.createdAt)
        root.put("lastModified", project.lastModified)
        root.put("aspectRatio", project.aspectRatio.name)
        root.put("fps", project.fps)

        // Canvas background
        val bgObj = JSONObject()
        bgObj.put("type", project.canvasBackground.type)
        bgObj.put("color", project.canvasBackground.color)
        bgObj.put("gradientColor2", project.canvasBackground.gradientColor2)
        bgObj.put("blurIntensity", project.canvasBackground.blurIntensity.toDouble())
        root.put("canvasBackground", bgObj)

        // Video clips
        val videoArray = JSONArray()
        for (c in project.videoClips) {
            val co = JSONObject()
            co.put("id", c.id)
            co.put("uri", c.uri ?: "")
            co.put("title", c.title)
            co.put("isPhoto", c.isPhoto)
            co.put("durationMs", c.durationMs)
            co.put("trimInMs", c.trimInMs)
            co.put("trimOutMs", c.trimOutMs)
            co.put("speed", c.speed.toDouble())
            co.put("volume", c.volume.toDouble())
            co.put("isMuted", c.isMuted)
            co.put("isReversed", c.isReversed)
            co.put("isFrozen", c.isFrozen)
            co.put("activeEffect", c.activeEffect.name)
            co.put("transition", c.transition.name)
            co.put("transitionDurationMs", c.transitionDurationMs)
            co.put("previewColor", c.previewColor)

            // Color adjustments
            val adj = JSONObject()
            adj.put("brightness", c.colorAdjustment.brightness.toDouble())
            adj.put("contrast", c.colorAdjustment.contrast.toDouble())
            adj.put("saturation", c.colorAdjustment.saturation.toDouble())
            adj.put("exposure", c.colorAdjustment.exposure.toDouble())
            adj.put("temperature", c.colorAdjustment.temperature.toDouble())
            adj.put("tint", c.colorAdjustment.tint.toDouble())
            adj.put("highlights", c.colorAdjustment.highlights.toDouble())
            adj.put("shadows", c.colorAdjustment.shadows.toDouble())
            adj.put("sharpness", c.colorAdjustment.sharpness.toDouble())
            adj.put("vignette", c.colorAdjustment.vignette.toDouble())
            adj.put("fade", c.colorAdjustment.fade.toDouble())
            co.put("colorAdjustment", adj)

            // Keyframes
            val kfArray = JSONArray()
            for (kf in c.keyframes) {
                val kfObj = JSONObject()
                kfObj.put("timeOffsetMs", kf.timeOffsetMs)
                kfObj.put("posX", kf.posX.toDouble())
                kfObj.put("posY", kf.posY.toDouble())
                kfObj.put("scale", kf.scale.toDouble())
                kfObj.put("rotation", kf.rotation.toDouble())
                kfObj.put("opacity", kf.opacity.toDouble())
                kfObj.put("volume", kf.volume.toDouble())
                kfArray.put(kfObj)
            }
            co.put("keyframes", kfArray)
            videoArray.put(co)
        }
        root.put("videoClips", videoArray)

        // Text layers
        val textArray = JSONArray()
        for (t in project.textLayers) {
            val to = JSONObject()
            to.put("id", t.id)
            to.put("text", t.text)
            to.put("startMs", t.startMs)
            to.put("durationMs", t.durationMs)
            to.put("posX", t.posX.toDouble())
            to.put("posY", t.posY.toDouble())
            to.put("scale", t.scale.toDouble())
            to.put("rotation", t.rotation.toDouble())
            to.put("color", t.color)
            to.put("strokeColor", t.strokeColor)
            to.put("strokeWidth", t.strokeWidth.toDouble())
            to.put("backgroundColor", t.backgroundColor)
            to.put("fontSize", t.fontSize.toDouble())
            to.put("fontFamily", t.fontFamily)
            to.put("opacity", t.opacity.toDouble())
            to.put("animation", t.animation.name)
            textArray.put(to)
        }
        root.put("textLayers", textArray)

        // Sticker layers
        val stickerArray = JSONArray()
        for (s in project.stickerLayers) {
            val so = JSONObject()
            so.put("id", s.id)
            so.put("emojiOrIcon", s.emojiOrIcon)
            so.put("customUri", s.customUri ?: "")
            so.put("category", s.category)
            so.put("startMs", s.startMs)
            so.put("durationMs", s.durationMs)
            so.put("posX", s.posX.toDouble())
            so.put("posY", s.posY.toDouble())
            so.put("scale", s.scale.toDouble())
            so.put("rotation", s.rotation.toDouble())
            so.put("opacity", s.opacity.toDouble())
            so.put("isAnimated", s.isAnimated)
            stickerArray.put(so)
        }
        root.put("stickerLayers", stickerArray)

        // Audio clips
        val audioArray = JSONArray()
        for (a in project.audioClips) {
            val ao = JSONObject()
            ao.put("id", a.id)
            ao.put("title", a.title)
            ao.put("uri", a.uri ?: "")
            ao.put("startMs", a.startMs)
            ao.put("durationMs", a.durationMs)
            ao.put("trimInMs", a.trimInMs)
            ao.put("trimOutMs", a.trimOutMs)
            ao.put("volume", a.volume.toDouble())
            ao.put("fadeInMs", a.fadeInMs)
            ao.put("fadeOutMs", a.fadeOutMs)
            ao.put("speed", a.speed.toDouble())
            ao.put("isVoiceOver", a.isVoiceOver)
            ao.put("isNoiseReduction", a.isNoiseReduction)
            audioArray.put(ao)
        }
        root.put("audioClips", audioArray)

        return root.toString()
    }

    fun fromJson(jsonStr: String): Project {
        val root = JSONObject(jsonStr)
        val id = root.optString("id", "")
        val title = root.optString("title", "Untitled Project")
        val createdAt = root.optLong("createdAt", System.currentTimeMillis())
        val lastModified = root.optLong("lastModified", System.currentTimeMillis())
        val aspectRatioStr = root.optString("aspectRatio", AspectRatioEnum.RATIO_9_16.name)
        val aspectRatio = try {
            AspectRatioEnum.valueOf(aspectRatioStr)
        } catch (_: Exception) {
            AspectRatioEnum.RATIO_9_16
        }
        val fps = root.optInt("fps", 30)

        // Canvas background
        val bgObj = root.optJSONObject("canvasBackground")
        val canvasBg = if (bgObj != null) {
            CanvasBackground(
                type = bgObj.optString("type", "COLOR"),
                color = bgObj.optLong("color", 0xFF0A0910),
                gradientColor2 = bgObj.optLong("gradientColor2", 0xFF1F1D2C),
                blurIntensity = bgObj.optDouble("blurIntensity", 0.5).toFloat()
            )
        } else CanvasBackground()

        // Video clips
        val videoClips = mutableListOf<VideoClip>()
        val videoArr = root.optJSONArray("videoClips")
        if (videoArr != null) {
            for (i in 0 until videoArr.length()) {
                val co = videoArr.getJSONObject(i)
                val cId = co.optString("id")
                val uri = co.optString("uri").ifEmpty { null }
                val cTitle = co.optString("title", "Clip")
                val isPhoto = co.optBoolean("isPhoto", false)
                val dur = co.optLong("durationMs", 5000L)
                val trimIn = co.optLong("trimInMs", 0L)
                val trimOut = co.optLong("trimOutMs", dur)
                val speed = co.optDouble("speed", 1.0).toFloat()
                val vol = co.optDouble("volume", 1.0).toFloat()
                val muted = co.optBoolean("isMuted", false)
                val reversed = co.optBoolean("isReversed", false)
                val frozen = co.optBoolean("isFrozen", false)
                val effStr = co.optString("activeEffect", EffectType.NONE.name)
                val eff = try { EffectType.valueOf(effStr) } catch (_: Exception) { EffectType.NONE }
                val transStr = co.optString("transition", TransitionType.NONE.name)
                val trans = try { TransitionType.valueOf(transStr) } catch (_: Exception) { TransitionType.NONE }
                val transDur = co.optLong("transitionDurationMs", 500L)
                val pColor = co.optLong("previewColor", 0xFF2563EB)

                var colorAdj = ColorAdjustment()
                val adj = co.optJSONObject("colorAdjustment")
                if (adj != null) {
                    colorAdj = ColorAdjustment(
                        brightness = adj.optDouble("brightness", 0.0).toFloat(),
                        contrast = adj.optDouble("contrast", 1.0).toFloat(),
                        saturation = adj.optDouble("saturation", 1.0).toFloat(),
                        exposure = adj.optDouble("exposure", 0.0).toFloat(),
                        temperature = adj.optDouble("temperature", 0.0).toFloat(),
                        tint = adj.optDouble("tint", 0.0).toFloat(),
                        highlights = adj.optDouble("highlights", 0.0).toFloat(),
                        shadows = adj.optDouble("shadows", 0.0).toFloat(),
                        sharpness = adj.optDouble("sharpness", 0.0).toFloat(),
                        vignette = adj.optDouble("vignette", 0.0).toFloat(),
                        fade = adj.optDouble("fade", 0.0).toFloat()
                    )
                }

                val keyframes = mutableListOf<ClipKeyframe>()
                val kfArr = co.optJSONArray("keyframes")
                if (kfArr != null) {
                    for (k in 0 until kfArr.length()) {
                        val kf = kfArr.getJSONObject(k)
                        keyframes.add(
                            ClipKeyframe(
                                timeOffsetMs = kf.optLong("timeOffsetMs", 0L),
                                posX = kf.optDouble("posX", 0.0).toFloat(),
                                posY = kf.optDouble("posY", 0.0).toFloat(),
                                scale = kf.optDouble("scale", 1.0).toFloat(),
                                rotation = kf.optDouble("rotation", 0.0).toFloat(),
                                opacity = kf.optDouble("opacity", 1.0).toFloat(),
                                volume = kf.optDouble("volume", 1.0).toFloat()
                            )
                        )
                    }
                }

                videoClips.add(
                    VideoClip(
                        id = cId,
                        uri = uri,
                        title = cTitle,
                        isPhoto = isPhoto,
                        durationMs = dur,
                        trimInMs = trimIn,
                        trimOutMs = trimOut,
                        speed = speed,
                        volume = vol,
                        isMuted = muted,
                        isReversed = reversed,
                        isFrozen = frozen,
                        colorAdjustment = colorAdj,
                        activeEffect = eff,
                        transition = trans,
                        transitionDurationMs = transDur,
                        keyframes = keyframes,
                        previewColor = pColor
                    )
                )
            }
        }

        // Text layers
        val textLayers = mutableListOf<TextLayer>()
        val textArr = root.optJSONArray("textLayers")
        if (textArr != null) {
            for (i in 0 until textArr.length()) {
                val to = textArr.getJSONObject(i)
                val tId = to.optString("id")
                val text = to.optString("text", "Text")
                val start = to.optLong("startMs", 0L)
                val dur = to.optLong("durationMs", 3000L)
                val px = to.optDouble("posX", 0.5).toFloat()
                val py = to.optDouble("posY", 0.5).toFloat()
                val scale = to.optDouble("scale", 1.0).toFloat()
                val rot = to.optDouble("rotation", 0.0).toFloat()
                val col = to.optLong("color", 0xFFFFFFFF)
                val strCol = to.optLong("strokeColor", 0xFF000000)
                val strW = to.optDouble("strokeWidth", 2.0).toFloat()
                val bgCol = to.optLong("backgroundColor", 0x00000000)
                val fs = to.optDouble("fontSize", 28.0).toFloat()
                val ff = to.optString("fontFamily", "Bold")
                val op = to.optDouble("opacity", 1.0).toFloat()
                val animStr = to.optString("animation", TextAnimType.NONE.name)
                val anim = try { TextAnimType.valueOf(animStr) } catch (_: Exception) { TextAnimType.NONE }

                textLayers.add(
                    TextLayer(
                        id = tId,
                        text = text,
                        startMs = start,
                        durationMs = dur,
                        posX = px,
                        posY = py,
                        scale = scale,
                        rotation = rot,
                        color = col,
                        strokeColor = strCol,
                        strokeWidth = strW,
                        backgroundColor = bgCol,
                        fontSize = fs,
                        fontFamily = ff,
                        opacity = op,
                        animation = anim
                    )
                )
            }
        }

        // Sticker layers
        val stickerLayers = mutableListOf<StickerLayer>()
        val stickerArr = root.optJSONArray("stickerLayers")
        if (stickerArr != null) {
            for (i in 0 until stickerArr.length()) {
                val so = stickerArr.getJSONObject(i)
                stickerLayers.add(
                    StickerLayer(
                        id = so.optString("id"),
                        emojiOrIcon = so.optString("emojiOrIcon", "🔥"),
                        customUri = so.optString("customUri").ifEmpty { null },
                        category = so.optString("category", "Emoji"),
                        startMs = so.optLong("startMs", 0L),
                        durationMs = so.optLong("durationMs", 3000L),
                        posX = so.optDouble("posX", 0.5).toFloat(),
                        posY = so.optDouble("posY", 0.5).toFloat(),
                        scale = so.optDouble("scale", 1.0).toFloat(),
                        rotation = so.optDouble("rotation", 0.0).toFloat(),
                        opacity = so.optDouble("opacity", 1.0).toFloat(),
                        isAnimated = so.optBoolean("isAnimated", false)
                    )
                )
            }
        }

        // Audio clips
        val audioClips = mutableListOf<AudioClip>()
        val audioArr = root.optJSONArray("audioClips")
        if (audioArr != null) {
            for (i in 0 until audioArr.length()) {
                val ao = audioArr.getJSONObject(i)
                audioClips.add(
                    AudioClip(
                        id = ao.optString("id"),
                        title = ao.optString("title", "Audio Track"),
                        uri = ao.optString("uri").ifEmpty { null },
                        startMs = ao.optLong("startMs", 0L),
                        durationMs = ao.optLong("durationMs", 5000L),
                        trimInMs = ao.optLong("trimInMs", 0L),
                        trimOutMs = ao.optLong("trimOutMs", ao.optLong("durationMs", 5000L)),
                        volume = ao.optDouble("volume", 1.0).toFloat(),
                        fadeInMs = ao.optLong("fadeInMs", 0L),
                        fadeOutMs = ao.optLong("fadeOutMs", 0L),
                        speed = ao.optDouble("speed", 1.0).toFloat(),
                        isVoiceOver = ao.optBoolean("isVoiceOver", false),
                        isNoiseReduction = ao.optBoolean("isNoiseReduction", false),
                        waveforms = List(16) { 0.3f + (it % 5) * 0.15f }
                    )
                )
            }
        }

        return Project(
            id = id,
            title = title,
            createdAt = createdAt,
            lastModified = lastModified,
            aspectRatio = aspectRatio,
            videoClips = videoClips,
            textLayers = textLayers,
            stickerLayers = stickerLayers,
            audioClips = audioClips,
            canvasBackground = canvasBg,
            fps = fps
        )
    }
}
