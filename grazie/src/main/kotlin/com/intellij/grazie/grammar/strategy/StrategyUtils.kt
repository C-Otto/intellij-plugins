// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.grammar.strategy

import com.intellij.grazie.grammar.strategy.impl.ReplaceNewLines
import com.intellij.grazie.utils.LinkedSet
import com.intellij.grazie.utils.Text


object StrategyUtils {
  /**
   * Delete leading and trailing quotes with spaces
   *
   * @return deleted leading offset
   */
  fun trimLeadingQuotesAndSpaces(str: StringBuilder): Int = with(str) {
    var offset = quotesOffset(this)

    setLength(length - offset) // remove closing quotes and whitespaces
    while (isNotEmpty() && get(length - 1).isWhitespace()) deleteCharAt(length - 1)

    while (offset < length && get(offset).isWhitespace()) offset++
    repeat(offset) { deleteCharAt(0) } // remove opening quotes and whitespace

    return offset
  }

  /**
   * Convert double spaces into one after removing absorb/stealth elements
   *
   * @param position position in StringBuilder
   * @return true if deleted
   */
  fun deleteRedundantSpace(str: StringBuilder, position: Int): Boolean = with(str) {
    if (position in 1 until length) {
      if (get(position - 1) == ' ' && (Text.isPunctuation(get(position)) || get(position) == ' ')) {
        deleteCharAt(position - 1)
        return true
      }
    }

    return false
  }

  /**
   * Finds indent indexes for each line (indent of specific [chars])
   * NOTE: If you use this method in [GrammarCheckingStrategy.getStealthyRanges],
   * make sure that [GrammarCheckingStrategy.getReplaceCharRules] doesn't contain a [ReplaceNewLines] rule!
   *
   * @param str source text
   * @param chars characters, which considered as indentation
   * @return list of IntRanges for such indents
   */
  fun indentIndexes(str: CharSequence, chars: Set<Char>): LinkedSet<IntRange> {
    val result = LinkedSet<IntRange>()
    var from = -1
    for ((index, char) in str.withIndex()) {
      if ((Text.isNewline(char) || (index == 0 && char in chars)) && from == -1) {
        // for first line without \n
        from = index + if (Text.isNewline(char)) 1 else 0
      } else {
        if (from != -1) {
          if (char !in chars) {
            if (index > from) result.add(IntRange(from, index - 1))
            from = if (Text.isNewline(char)) index + 1 else -1
          }
        }
      }
    }

    if (from != -1) result.add(IntRange(from, str.length - 1))

    return result
  }

  private fun quotesOffset(str: CharSequence): Int {
    var index = 0
    while (index < str.length / 2) {
      if (str[index] != str[str.length - index - 1] || !Text.isQuote(str[index])) {
        return index
      }
      index++
    }

    return index
  }
}
