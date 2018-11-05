@file:JvmName("NumberToVietnamese")

package com.hoc.lib

/**
 * @author Hoc
 * Created on Sunday, November 4, 2018
 * Convert number [Long] to Vietnamese words [String]
 */

object NumberToVietnamese {
  private val zeroLeftPadding = arrayOf("", "00", "0")
  private val digits = arrayOf(
    "không",
    "một",
    "hai",
    "ba",
    "bốn",
    "năm",
    "sáu",
    "bảy",
    "tám",
    "chín"
  )
  private val multipleThousand = arrayOf(
    "",
    "nghìn",
    "triệu",
    "tỷ",
    "nghìn",
    "triệu",
    "tỷ"
  )

  private fun readTriple(triple: String, showZeroHundred: Boolean): String {
    val (a, b, c) = triple.map { it - '0' }
    return when {
      a == 0 && b == 0 && c == 0 -> ""
      a == 0 && b == 0 -> digits[c]
      a == 0 && showZeroHundred -> "không trăm " + readPair(b, c)
      a == 0 && b != 0 -> readPair(b, c)
      else -> digits[a] + " trăm " + readPair(b, c)
    }
  }

  private fun readPair(b: Int, c: Int): String {
    return when (b) {
      0 -> when (c) {
        0 -> ""
        else -> " lẻ " + digits[c]
      }
      1 -> "mười " + when (c) {
        0 -> ""
        5 -> "lăm"
        else -> digits[c]
      }
      else -> digits[b] + " mươi " + when (c) {
        0 -> ""
        1 -> "mốt"
        4 -> "tư"
        5 -> "lăm"
        else -> digits[c]
      }
    }
  }

  @JvmStatic
  fun convert(n: Long): String {
    return when {
      n == 0L -> "Không"
      n < 0L -> "Âm " + convert(-n)
      else -> {
        val s = n.toString()
        val groups = "${zeroLeftPadding[s.length % 3]}$s".chunked(3)

        groups.foldIndexed("") { index, acc, e ->
          val readTriple = readTriple(e, groups.size > 1)
          "$acc $readTriple ${when {
            readTriple.isNotBlank() || (groups.size >= 5 && groups.size - 1 - index == 3) -> multipleThousand.getOrNull(
              groups.size - 1 - index
            ).orEmpty()
            else -> ""
          }} "
        }
      }
    }.replace("""\s+""".toRegex(), " ")
      .trim()
      .toLowerCase()
      .capitalize()
  }
}