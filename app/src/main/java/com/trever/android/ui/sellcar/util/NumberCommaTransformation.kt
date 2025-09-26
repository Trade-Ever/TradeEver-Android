package com.trever.android.ui.sellcar.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.DecimalFormat

class NumberCommaTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val formattedText = try {
            val number = originalText.toLong()
            DecimalFormat("#,###").format(number)
        } catch (e: NumberFormatException) {
            // 숫자로 변환할 수 없는 경우, 원본 텍스트를 그대로 반환하거나 오류 처리를 할 수 있습니다.
            return TransformedText(text, OffsetMapping.Identity)
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                // 콤마가 추가되면서 변경된 오프셋 계산
                val commas = formattedText.count { it == ',' }
                return offset + commas
            }

            override fun transformedToOriginal(offset: Int): Int {
                // 콤마를 제외하고 원래 오프셋 계산
                val commas = formattedText.substring(0, offset).count { it == ',' }
                return offset - commas
            }
        }

        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}
