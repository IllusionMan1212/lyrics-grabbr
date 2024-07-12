package com.illusionman1212.lyricsgrabbr.utils

import android.content.Context
import android.text.Annotation
import android.text.SpannedString
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.core.os.LocaleListCompat
import com.illusionman1212.lyricsgrabbr.R
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

// Native stylization is coming soon
// https://issuetracker.google.com/issues/139320238
@Composable
fun annotatedStringResource(
    @StringRes id: Int,
    spanStyles: (Annotation) -> SpanStyle? = { null }
): AnnotatedString {
    val resources = LocalContext.current.resources
    val spannedString = SpannedString(resources.getText(id))
    val resultBuilder = AnnotatedString.Builder()
    resultBuilder.append(spannedString.toString())
    spannedString.getSpans<Annotation>(0, spannedString.length, Annotation::class.java).forEach { annotation ->
        val spanStart = spannedString.getSpanStart(annotation)
        val spanEnd = spannedString.getSpanEnd(annotation)
        resultBuilder.addStringAnnotation(
            tag = annotation.key,
            annotation = annotation.value,
            start = spanStart,
            end = spanEnd
        )
        spanStyles(annotation)?.let { resultBuilder.addStyle(it, spanStart, spanEnd) }
    }
    return resultBuilder.toAnnotatedString()
}

fun Context.getLocaleListFromXml(): LocaleListCompat {
    val tagsList = mutableListOf<CharSequence>()
    try {
        val xpp: XmlPullParser = resources.getXml(R.xml.locales_config)
        while (xpp.eventType != XmlPullParser.END_DOCUMENT) {
            if (xpp.eventType == XmlPullParser.START_TAG) {
                if (xpp.name == "locale") {
                    tagsList.add(xpp.getAttributeValue(0))
                }
            }
            xpp.next()
        }
    } catch (e: XmlPullParserException) {
        e.printStackTrace()
    } catch (e: IOException) {
        e.printStackTrace()
    }

    return LocaleListCompat.forLanguageTags(tagsList.joinToString(","))
}


fun Context.getLangPreferenceDropdownEntries(): Map<String, String> {
    val localeList = getLocaleListFromXml()
    val map = mutableMapOf<String, String>()

    for (a in 0 until localeList.size()) {
        localeList[a].let {
            it?.let { it1 -> map.put(it1.getDisplayName(it), it.toLanguageTag()) }
        }
    }
    return map
}