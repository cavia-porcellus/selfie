/*
 * Copyright (C) 2023-2025 DiffPlug
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.diffplug.selfie

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SnapshotValueReaderTest {
  @Test
  fun noEscapingNeeded() {
    val reader =
        SnapshotValueReader.of(
            """
            ╔═ 00_empty ═╗
            ╔═ 01_singleLineString ═╗
            this is one line
            ╔═ 01a_singleLineLeadingSpace ═╗
             the leading space is significant
            ╔═ 01b_singleLineTrailingSpace ═╗
            the trailing space is significant 
            ╔═ 02_multiLineStringTrimmed ═╗
            Line 1
            Line 2
            ╔═ 03_multiLineStringTrailingNewline ═╗
            Line 1
            Line 2

            ╔═ 04_multiLineStringLeadingNewline ═╗

            Line 1
            Line 2
            ╔═ 05_notSureHowKotlinMultilineWorks ═╗
            """
                .trimIndent())
    reader.peekKeyRaw() shouldBe "00_empty"
    reader.peekKeyRaw() shouldBe "00_empty"
    reader.nextValue().valueString() shouldBe ""
    reader.peekKeyRaw() shouldBe "01_singleLineString"
    reader.peekKeyRaw() shouldBe "01_singleLineString"
    reader.nextValue().valueString() shouldBe "this is one line"
    reader.peekKeyRaw() shouldBe "01a_singleLineLeadingSpace"
    reader.nextValue().valueString() shouldBe " the leading space is significant"
    reader.peekKeyRaw() shouldBe "01b_singleLineTrailingSpace"
    reader.nextValue().valueString() shouldBe "the trailing space is significant "
    reader.peekKeyRaw() shouldBe "02_multiLineStringTrimmed"
    reader.nextValue().valueString() shouldBe "Line 1\nLine 2"
    // note that leading and trailing newlines in the snapshots are significant
    // this is critical so that snapshots can accurately capture the exact number of newlines
    reader.peekKeyRaw() shouldBe "03_multiLineStringTrailingNewline"
    reader.nextValue().valueString() shouldBe "Line 1\nLine 2\n"
    reader.peekKeyRaw() shouldBe "04_multiLineStringLeadingNewline"
    reader.nextValue().valueString() shouldBe "\nLine 1\nLine 2"
    reader.peekKeyRaw() shouldBe "05_notSureHowKotlinMultilineWorks"
    reader.nextValue().valueString() shouldBe ""
  }

  @Test
  fun invalidNames() {
    shouldThrow<ParseException> { SnapshotValueReader.of("╔═name ═╗").peekKeyRaw() }
        .let { it.message shouldBe "L1:Expected to start with '╔═ '" }
    shouldThrow<ParseException> { SnapshotValueReader.of("╔═ name═╗").peekKeyRaw() }
        .let { it.message shouldBe "L1:Expected to contain ' ═╗'" }
    shouldThrow<ParseException> { SnapshotValueReader.of("╔═  name ═╗").peekKeyRaw() }
        .let { it.message shouldBe "L1:Leading spaces are disallowed: ' name'" }
    shouldThrow<ParseException> { SnapshotValueReader.of("╔═ name  ═╗").peekKeyRaw() }
        .let { it.message shouldBe "L1:Trailing spaces are disallowed: 'name '" }
    SnapshotValueReader.of("╔═ name ═╗ comment okay").peekKeyRaw() shouldBe "name"
    SnapshotValueReader.of("╔═ name ═╗okay here too").peekKeyRaw() shouldBe "name"
    SnapshotValueReader.of("╔═ name ═╗ okay  ╔═ ═╗ (it's the first ' ═╗' that counts)")
        .peekKeyRaw() shouldBe "name"
  }

  @Test
  fun escapeCharactersInName() {
    val reader =
        SnapshotValueReader.of(
            """
            ╔═ test with \(square brackets\) in name ═╗
            ╔═ test with \\backslash\\ in name ═╗
            ╔═ test with\nnewline\nin name ═╗
            ╔═ test with \ttab\t in name ═╗
            ╔═ test with \┌\─ ascii art \─\┐ in name ═╗
            """
                .trimIndent())
    reader.peekKeyRaw() shouldBe "test with \\(square brackets\\) in name"
    reader.nextValue().valueString() shouldBe ""
    reader.peekKeyRaw() shouldBe """test with \\backslash\\ in name"""
    reader.nextValue().valueString() shouldBe ""
    reader.peekKeyRaw() shouldBe "test with\\nnewline\\nin name"
    reader.nextValue().valueString() shouldBe ""
    reader.peekKeyRaw() shouldBe "test with \\ttab\\t in name"
    reader.nextValue().valueString() shouldBe ""
    reader.peekKeyRaw() shouldBe "test with \\┌\\─ ascii art \\─\\┐ in name"
    reader.nextValue().valueString() shouldBe ""
  }

  @Test
  fun escapeCharactersInBody() {
    val reader =
        SnapshotValueReader.of(
            """
          ╔═ ascii art okay ═╗
           ╔══╗
          ╔═ escaped iff on first line ═╗
          𐝁══╗
          ╔═ body escape characters ═╗
          𐝃𐝁𐝃𐝃 linear a is dead
        """
                .trimIndent())
    reader.peekKeyRaw() shouldBe "ascii art okay"
    reader.nextValue().valueString() shouldBe """ ╔══╗"""
    reader.peekKeyRaw() shouldBe "escaped iff on first line"
    reader.nextValue().valueString() shouldBe """╔══╗"""
    reader.peekKeyRaw() shouldBe "body escape characters"
    reader.nextValue().valueString() shouldBe """𐝁𐝃 linear a is dead"""
  }

  @Test
  fun skipValues() {
    val testContent =
        """
            ╔═ 00_empty ═╗
            ╔═ 01_singleLineString ═╗
            this is one line
            ╔═ 02_multiLineStringTrimmed ═╗
            Line 1
            Line 2
            ╔═ 05_notSureHowKotlinMultilineWorks ═╗
            """
            .trimIndent()
    assertKeyValueWithSkip(testContent, "00_empty", "")
    assertKeyValueWithSkip(testContent, "01_singleLineString", "this is one line")
    assertKeyValueWithSkip(testContent, "02_multiLineStringTrimmed", "Line 1\nLine 2")
  }
  private fun assertKeyValueWithSkip(input: String, key: String, value: String) {
    val reader = SnapshotValueReader.of(input)
    while (reader.peekKeyRaw() != key) {
      reader.skipValue()
    }
    reader.peekKeyRaw() shouldBe key
    reader.nextValue().valueString() shouldBe value
    while (reader.peekKeyRaw() != null) {
      reader.skipValue()
    }
  }

  @Test
  fun binary() {
    val reader = SnapshotValueReader.of("""╔═ Apple ═╗ base64 length 3 bytes
c2Fk
""")
    reader.peekKeyRaw() shouldBe "Apple"
    reader.nextValue().valueBinary() shouldBe "sad".encodeToByteArray()
  }
}
