package com.codebattle.app.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Very lightweight syntax highlighter for Kotlin-like code.
 * Highlights a predefined list of keywords using simple regex matching.
 * This is NOT a full parser, but good enough for visual feedback.
 */
class SyntaxHighlightTransformation(
    private val keywordColor: Color,
    private val keywords: List<String> = listOf(
        "fun", "val", "var", "if", "else", "for", "while",
        "return", "when", "class", "object", "data"
    )
) : VisualTransformation {

    private val keywordRegex = Regex("\\b(${keywords.joinToString("|")})\\b")

    override fun filter(text: AnnotatedString): TransformedText {
        val builder = AnnotatedString.Builder(text)
        keywordRegex.findAll(text.text).forEach { matchResult ->
            builder.addStyle(
                style = SpanStyle(color = keywordColor),
                start = matchResult.range.first,
                end = matchResult.range.last + 1
            )
        }
        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }
}

