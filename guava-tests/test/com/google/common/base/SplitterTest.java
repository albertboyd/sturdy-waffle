/*
 * Copyright (C) 2009 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.base;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.collect.Lists;
import com.google.common.testing.NullPointerTester;

/**
 * @author Julien Silland
 */
@GwtCompatible(emulated = true)
public class SplitterTest extends TestCase {

  public void testSplitNullString() {
    try {
      Splitter.on(',').split(null);
      fail();
    } catch (NullPointerException expected) {
    }
  }

  public void testCharacterSimpleSplit() {
    String simple = "a,b,c";
    Iterable<String> letters = Splitter.on(',').split(simple);
    assertContentsInOrder(letters, "a", "b", "c");
  }

  public void testCharacterSimpleSplitWithNoDelimiter() {
    String simple = "a,b,c";
    Iterable<String> letters = Splitter.on('.').split(simple);
    assertContentsInOrder(letters, "a,b,c");
  }

  public void testCharacterSplitWithDoubleDelimiter() {
    String doubled = "a,,b,c";
    Iterable<String> letters = Splitter.on(',').split(doubled);
    assertContentsInOrder(letters, "a", "", "b", "c");
  }

  public void testCharacterSplitWithDoubleDelimiterAndSpace() {
    String doubled = "a,, b,c";
    Iterable<String> letters = Splitter.on(',').split(doubled);
    assertContentsInOrder(letters, "a", "", " b", "c");
  }

  public void testCharacterSplitWithTrailingDelimiter() {
    String trailing = "a,b,c,";
    Iterable<String> letters = Splitter.on(',').split(trailing);
    assertContentsInOrder(letters, "a", "b", "c", "");
  }

  public void testCharacterSplitWithLeadingDelimiter() {
    String leading = ",a,b,c";
    Iterable<String> letters = Splitter.on(',').split(leading);
    assertContentsInOrder(letters, "", "a", "b", "c");
  }

  public void testCharacterSplitWithMulitpleLetters() {
    Iterable<String> testCharacteringMotto = Splitter.on('-').split(
        "Testing-rocks-Debugging-sucks");
    assertContentsInOrder(testCharacteringMotto,
        "Testing", "rocks", "Debugging", "sucks");
  }

  public void testCharacterSplitWithMatcherDelimiter() {
    Iterable<String> testCharacteringMotto = Splitter
        .on(CharMatcher.WHITESPACE)
        .split("Testing\nrocks\tDebugging sucks");
    assertContentsInOrder(testCharacteringMotto,
        "Testing", "rocks", "Debugging", "sucks");
  }

  public void testCharacterSplitWithDoubleDelimiterOmitEmptyStrings() {
    String doubled = "a..b.c";
    Iterable<String> letters = Splitter.on('.')
        .omitEmptyStrings().split(doubled);
    assertContentsInOrder(letters, "a", "b", "c");
  }

  public void testCharacterSplitEmptyToken() {
    String emptyToken = "a. .c";
    Iterable<String> letters = Splitter.on('.').trimResults()
        .split(emptyToken);
    assertContentsInOrder(letters, "a", "", "c");
  }

  public void testCharacterSplitEmptyTokenOmitEmptyStrings() {
    String emptyToken = "a. .c";
    Iterable<String> letters = Splitter.on('.')
        .omitEmptyStrings().trimResults().split(emptyToken);
    assertContentsInOrder(letters, "a", "c");
  }

  public void testCharacterSplitOnEmptyString() {
    Iterable<String> nothing = Splitter.on('.').split("");
    assertContentsInOrder(nothing, "");
  }

  public void testCharacterSplitOnEmptyStringOmitEmptyStrings() {
    assertFalse(
        Splitter.on('.').omitEmptyStrings().split("").iterator().hasNext());
  }

  public void testCharacterSplitOnOnlyDelimiter() {
    Iterable<String> blankblank = Splitter.on('.').split(".");
    assertContentsInOrder(blankblank, "", "");
  }

  public void testCharacterSplitOnOnlyDelimitersOmitEmptyStrings() {
    Iterable<String> empty = Splitter.on('.').omitEmptyStrings().split("...");
    assertContentsInOrder(empty);
  }

  public void testCharacterSplitWithTrim() {
    String jacksons = "arfo(Marlon)aorf, (Michael)orfa, afro(Jackie)orfa, "
        + "ofar(Jemaine), aff(Tito)";
    Iterable<String> family = Splitter.on(',')
        .trimResults(CharMatcher.anyOf("afro").or(CharMatcher.WHITESPACE))
        .split(jacksons);
    assertContentsInOrder(family,
        "(Marlon)", "(Michael)", "(Jackie)", "(Jemaine)", "(Tito)");
  }

  public void testStringSimpleSplit() {
    String simple = "a,b,c";
    Iterable<String> letters = Splitter.on(",").split(simple);
    assertContentsInOrder(letters, "a", "b", "c");
  }

  public void testStringSimpleSplitWithNoDelimiter() {
    String simple = "a,b,c";
    Iterable<String> letters = Splitter.on(".").split(simple);
    assertContentsInOrder(letters, "a,b,c");
  }

  public void testStringSplitWithDoubleDelimiter() {
    String doubled = "a,,b,c";
    Iterable<String> letters = Splitter.on(",").split(doubled);
    assertContentsInOrder(letters, "a", "", "b", "c");
  }

  public void testStringSplitWithDoubleDelimiterAndSpace() {
    String doubled = "a,, b,c";
    Iterable<String> letters = Splitter.on(",").split(doubled);
    assertContentsInOrder(letters, "a", "", " b", "c");
  }

  public void testStringSplitWithTrailingDelimiter() {
    String trailing = "a,b,c,";
    Iterable<String> letters = Splitter.on(",").split(trailing);
    assertContentsInOrder(letters, "a", "b", "c", "");
  }

  public void testStringSplitWithLeadingDelimiter() {
    String leading = ",a,b,c";
    Iterable<String> letters = Splitter.on(",").split(leading);
    assertContentsInOrder(letters, "", "a", "b", "c");
  }

  public void testStringSplitWithMultipleLetters() {
    Iterable<String> testStringingMotto = Splitter.on("-").split(
        "Testing-rocks-Debugging-sucks");
    assertContentsInOrder(testStringingMotto,
        "Testing", "rocks", "Debugging", "sucks");
  }

  public void testStringSplitWithDoubleDelimiterOmitEmptyStrings() {
    String doubled = "a..b.c";
    Iterable<String> letters = Splitter.on(".")
        .omitEmptyStrings().split(doubled);
    assertContentsInOrder(letters, "a", "b", "c");
  }

  public void testStringSplitEmptyToken() {
    String emptyToken = "a. .c";
    Iterable<String> letters = Splitter.on(".").trimResults()
        .split(emptyToken);
    assertContentsInOrder(letters, "a", "", "c");
  }

  public void testStringSplitEmptyTokenOmitEmptyStrings() {
    String emptyToken = "a. .c";
    Iterable<String> letters = Splitter.on(".")
        .omitEmptyStrings().trimResults().split(emptyToken);
    assertContentsInOrder(letters, "a", "c");
  }

  public void testStringSplitWithLongDelimiter() {
    String longDelimiter = "a, b, c";
    Iterable<String> letters = Splitter.on(", ").split(longDelimiter);
    assertContentsInOrder(letters, "a", "b", "c");
  }

  public void testStringSplitWithLongLeadingDelimiter() {
    String longDelimiter = ", a, b, c";
    Iterable<String> letters = Splitter.on(", ").split(longDelimiter);
    assertContentsInOrder(letters, "", "a", "b", "c");
  }

  public void testStringSplitWithLongTrailingDelimiter() {
    String longDelimiter = "a, b, c, ";
    Iterable<String> letters = Splitter.on(", ").split(longDelimiter);
    assertContentsInOrder(letters, "a", "b", "c", "");
  }

  public void testStringSplitWithDelimiterSubstringInValue() {
    String fourCommasAndFourSpaces = ",,,,    ";
    Iterable<String> threeCommasThenTreeSpaces = Splitter.on(", ").split(
        fourCommasAndFourSpaces);
    assertContentsInOrder(threeCommasThenTreeSpaces, ",,,", "   ");
  }

  public void testStringSplitWithEmptyString() {
    try {
      Splitter.on("");
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testStringSplitOnEmptyString() {
    Iterable<String> notMuch = Splitter.on(".").split("");
    assertContentsInOrder(notMuch, "");
  }

  public void testStringSplitOnEmptyStringOmitEmptyString() {
    assertFalse(
        Splitter.on(".").omitEmptyStrings().split("").iterator().hasNext());
  }

  public void testStringSplitOnOnlyDelimiter() {
    Iterable<String> blankblank = Splitter.on(".").split(".");
    assertContentsInOrder(blankblank, "", "");
  }

  public void testStringSplitOnOnlyDelimitersOmitEmptyStrings() {
    Iterable<String> empty = Splitter.on(".").omitEmptyStrings().split("...");
    assertContentsInOrder(empty);
  }

  public void testStringSplitWithTrim() {
    String jacksons = "arfo(Marlon)aorf, (Michael)orfa, afro(Jackie)orfa, "
        + "ofar(Jemaine), aff(Tito)";
    Iterable<String> family = Splitter.on(",")
        .trimResults(CharMatcher.anyOf("afro").or(CharMatcher.WHITESPACE))
        .split(jacksons);
    assertContentsInOrder(family,
        "(Marlon)", "(Michael)", "(Jackie)", "(Jemaine)", "(Tito)");
  }

  @GwtIncompatible("Splitter.onPattern")
  public void testPatternSimpleSplit() {
    String simple = "a,b,c";
    Iterable<String> letters = Splitter.onPattern(",").split(simple);
    assertContentsInOrder(letters, "a", "b", "c");
  }

  @GwtIncompatible("Splitter.onPattern")
  public void testPatternSimpleSplitWithNoDelimiter() {
    String simple = "a,b,c";
    Iterable<String> letters = Splitter.onPattern("foo").split(simple);
    assertContentsInOrder(letters, "a,b,c");
  }

  @GwtIncompatible("Splitter.onPattern")
  public void testPatternSplitWithDoubleDelimiter() {
    String doubled = "a,,b,c";
    Iterable<String> letters = Splitter.onPattern(",").split(doubled);
    assertContentsInOrder(letters, "a", "", "b", "c");
  }

  @GwtIncompatible("Splitter.onPattern")
  public void testPatternSplitWithDoubleDelimiterAndSpace() {
    String doubled = "a,, b,c";
    Iterable<String> letters = Splitter.onPattern(",").split(doubled);
    assertContentsInOrder(letters, "a", "", " b", "c");
  }

  @GwtIncompatible("Splitter.onPattern")
  public void testPatternSplitWithTrailingDelimiter() {
    String trailing = "a,b,c,";
    Iterable<String> letters = Splitter.onPattern(",").split(trailing);
    assertContentsInOrder(letters, "a", "b", "c", "");
  }

  @GwtIncompatible("Splitter.onPattern")
  public void testPatternSplitWithLeadingDelimiter() {
    String leading = ",a,b,c";
    Iterable<String> letters = Splitter.onPattern(",").split(leading);
    assertContentsInOrder(letters, "", "a", "b", "c");
  }

  @GwtIncompatible("Splitter.onPattern")
  public void testPatternSplitWithMultipleLetters() {
    Iterable<String> testPatterningMotto = Splitter.onPattern("-").split(
        "Testing-rocks-Debugging-sucks");
    assertContentsInOrder(testPatterningMotto,
        "Testing", "rocks", "Debugging", "sucks");
  }

  @GwtIncompatible("java.util.regex.Pattern")
  private static Pattern literalDotPattern() {
    return Pattern.compile("\\.");
  }

  @GwtIncompatible("java.util.regex.Pattern")
  public void testPatternSplitWithDoubleDelimiterOmitEmptyStrings() {
    String doubled = "a..b.c";
    Iterable<String> letters = Splitter.on(literalDotPattern())
        .omitEmptyStrings().split(doubled);
    assertContentsInOrder(letters, "a", "b", "c");
  }

  @GwtIncompatible("java.util.regex.Pattern")
  public void testPatternSplitEmptyToken() {
    String emptyToken = "a. .c";
    Iterable<String> letters = Splitter.on(literalDotPattern()).trimResults()
        .split(emptyToken);
    assertContentsInOrder(letters, "a", "", "c");
  }

  @GwtIncompatible("java.util.regex.Pattern")
  public void testPatternSplitEmptyTokenOmitEmptyStrings() {
    String emptyToken = "a. .c";
    Iterable<String> letters = Splitter.on(literalDotPattern())
        .omitEmptyStrings().trimResults().split(emptyToken);
    assertContentsInOrder(letters, "a", "c");
  }

  @GwtIncompatible("java.util.regex.Pattern")
  public void testPatternSplitOnOnlyDelimiter() {
    Iterable<String> blankblank = Splitter.on(literalDotPattern()).split(".");

    assertContentsInOrder(blankblank, "", "");
  }

  @GwtIncompatible("java.util.regex.Pattern")
  public void testPatternSplitOnOnlyDelimitersOmitEmptyStrings() {
    Iterable<String> empty = Splitter.on(literalDotPattern()).omitEmptyStrings()
        .split("...");
    assertContentsInOrder(empty);
  }

  @GwtIncompatible("java.util.regex.Pattern")
  public void testPatternSplitMatchingIsGreedy() {
    String longDelimiter = "a, b,   c";
    Iterable<String> letters = Splitter.on(Pattern.compile(",\\s*"))
        .split(longDelimiter);
    assertContentsInOrder(letters, "a", "b", "c");
  }

  @GwtIncompatible("java.util.regex.Pattern")
  public void testPatternSplitWithLongLeadingDelimiter() {
    String longDelimiter = ", a, b, c";
    Iterable<String> letters = Splitter.on(Pattern.compile(", "))
        .split(longDelimiter);
    assertContentsInOrder(letters, "", "a", "b", "c");
  }

  @GwtIncompatible("java.util.regex.Pattern")
  public void testPatternSplitWithLongTrailingDelimiter() {
    String longDelimiter = "a, b, c/ ";
    Iterable<String> letters = Splitter.on(Pattern.compile("[,/]\\s"))
        .split(longDelimiter);
    assertContentsInOrder(letters, "a", "b", "c", "");
  }

  @GwtIncompatible("java.util.regex.Pattern")
  public void testPatternSplitInvalidPattern() {
    try {
      Splitter.on(Pattern.compile("a*"));
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  @GwtIncompatible("java.util.regex.Pattern")
  public void testPatternSplitWithTrim() {
    String jacksons = "arfo(Marlon)aorf, (Michael)orfa, afro(Jackie)orfa, "
        + "ofar(Jemaine), aff(Tito)";
    Iterable<String> family = Splitter.on(Pattern.compile(","))
        .trimResults(CharMatcher.anyOf("afro").or(CharMatcher.WHITESPACE))
        .split(jacksons);
    assertContentsInOrder(family,
        "(Marlon)", "(Michael)", "(Jackie)", "(Jemaine)", "(Tito)");
  }

  public void testSplitterIterableIsUnmodifiable() {
    assertIteratorIsUnmodifiable(Splitter.on(',').split("a,b").iterator());
    assertIteratorIsUnmodifiable(Splitter.on(",").split("a,b").iterator());
  }

  @GwtIncompatible("java.util.regex.Pattern")
  public void testSplitterIterableIsUnmodifiable_pattern() {
    assertIteratorIsUnmodifiable(
        Splitter.on(Pattern.compile(",")).split("a,b").iterator());
  }

  private void assertIteratorIsUnmodifiable(Iterator<?> iterator) {
    iterator.next();
    try {
      iterator.remove();
      fail();
    } catch (UnsupportedOperationException expected) {
    }
  }

  public void testSplitterIterableIsLazy() {
    assertSplitterIterableIsLazy(Splitter.on(','));
    assertSplitterIterableIsLazy(Splitter.on(","));
  }

  @GwtIncompatible("java.util.regex.Pattern")
  public void testSplitterIterableIsLazy_pattern() {
    assertSplitterIterableIsLazy(Splitter.on(Pattern.compile(",")));
  }

  /**
   * This test really pushes the boundaries of what we support. In general the
   * splitter's behaviour is not well defined if the char sequence it's
   * splitting is mutated during iteration.
   */
  private void assertSplitterIterableIsLazy(Splitter splitter) {
    StringBuilder builder = new StringBuilder();
    Iterator<String> iterator = splitter.split(builder).iterator();

    builder.append("A,");
    assertEquals("A", iterator.next());
    builder.append("B,");
    assertEquals("B", iterator.next());
    builder.append("C");
    assertEquals("C", iterator.next());
    assertFalse(iterator.hasNext());
  }

  public void testAtEachSimpleSplit() {
    String simple = "abcde";
    Iterable<String> letters = Splitter.fixedLength(2).split(simple);
    assertContentsInOrder(letters, "ab", "cd", "e");
  }

  public void testAtEachSplitEqualChunkLength() {
    String simple = "abcdef";
    Iterable<String> letters = Splitter.fixedLength(2).split(simple);
    assertContentsInOrder(letters, "ab", "cd", "ef");
  }

  public void testAtEachSplitOnlyOneChunk() {
    String simple = "abc";
    Iterable<String> letters = Splitter.fixedLength(3).split(simple);
    assertContentsInOrder(letters, "abc");
  }

  public void testAtEachSplitSmallerString() {
    String simple = "ab";
    Iterable<String> letters = Splitter.fixedLength(3).split(simple);
    assertContentsInOrder(letters, "ab");
  }

  public void testAtEachSplitEmptyString() {
    String simple = "";
    Iterable<String> letters = Splitter.fixedLength(3).split(simple);
    assertContentsInOrder(letters, "");
  }

  public void testAtEachSplitEmptyStringWithOmitEmptyStrings() {
    assertFalse(Splitter.fixedLength(3).omitEmptyStrings().split("").iterator()
        .hasNext());
  }

  public void testAtEachSplitIntoChars() {
    String simple = "abcd";
    Iterable<String> letters = Splitter.fixedLength(1).split(simple);
    assertContentsInOrder(letters, "a", "b", "c", "d");
  }

  public void testAtEachSplitZeroChunkLen() {
    try {
      Splitter.fixedLength(0);
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testAtEachSplitNegativeChunkLen() {
    try {
      Splitter.fixedLength(-1);
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  @GwtIncompatible("NullPointerTester")
  public void testNullPointers() throws Exception {
    NullPointerTester tester = new NullPointerTester();
    tester.testAllPublicStaticMethods(Splitter.class);
    tester.testAllPublicInstanceMethods(Splitter.on(","));
    tester.testAllPublicInstanceMethods(Splitter.on(",").trimResults());
  }

  // TODO: use common one when we settle where that is...
  private void assertContentsInOrder(
      Iterable<String> actual, String... expected) {
    assertEquals(Arrays.asList(expected), Lists.newArrayList(actual));
  }

  public void testMapSplitter_trimmedBoth() {
    Map<String, String> m = Splitter.on(',')
        .trimResults()
        .withKeyValueSeparator(Splitter.on(':').trimResults())
        .split("boy  : tom , girl: tina , cat  : kitty , dog: tommy ");
    assertEquals("tom", m.get("boy"));
    assertEquals("tina", m.get("girl"));
    assertEquals("kitty", m.get("cat"));
    assertEquals("tommy", m.get("dog"));
  }

  public void testMapSplitter_trimmedEntries() {
    Map<String, String> m = Splitter.on(',')
        .trimResults()
        .withKeyValueSeparator(":")
        .split("boy  : tom , girl: tina , cat  : kitty , dog: tommy ");
    assertEquals(" tom", m.get("boy  "));
    assertEquals(" tina", m.get("girl"));
    assertEquals(" kitty", m.get("cat  "));
    assertEquals(" tommy", m.get("dog"));
  }

  public void testMapSplitter_trimmedKeyValue() {
    Map<String, String> m = Splitter.on(',')
        .withKeyValueSeparator(Splitter.on(':').trimResults())
        .split("boy  : tom , girl: tina , cat  : kitty , dog: tommy ");
    assertEquals("tom", m.get("boy"));
    assertEquals("tina", m.get("girl"));
    assertEquals("kitty", m.get("cat"));
    assertEquals("tommy", m.get("dog"));
  }

  public void testMapSplitter_notTrimmed() {
    Map<String, String> m = Splitter.on(',')
        .withKeyValueSeparator(":")
        .split(" boy:tom , girl: tina , cat :kitty , dog:  tommy ");
    assertEquals("tom ", m.get(" boy"));
    assertEquals(" tina ", m.get(" girl"));
    assertEquals("kitty ", m.get(" cat "));
    assertEquals("  tommy ", m.get(" dog"));
  }

  public void testMapSplitter_multiCharacterSeparator() {

    // try different delimiters.
    Map<String, String> m = Splitter.on(",")
        .withKeyValueSeparator(":^&")
        .split("boy:^&tom,girl:^&tina,cat:^&kitty,dog:^&tommy");
    assertEquals("tom", m.get("boy"));
    assertEquals("tina", m.get("girl"));
    assertEquals("kitty", m.get("cat"));
    assertEquals("tommy", m.get("dog"));
  }

  public void testMapSplitter_emptySeparator() {
    try {
      Splitter.on(",").withKeyValueSeparator("");
      fail("Should be impossible to use an empty separator.");
    } catch (IllegalArgumentException expected) {
      // Pass
    }
  }

  public void testMapSplitter_malformedEntry() {
    try {
      Splitter.on(",").withKeyValueSeparator("=").split("a=1,b,c=2");
      fail("Shouldn't accept malformed entry \"b\"");
    } catch(IllegalArgumentException expected) {
      // Pass
    }
  }

  public void testMapSplitter_orderedResults() {
    Map<String, String> m = Splitter.on(",")
        .withKeyValueSeparator(":")
        .split("boy:tom,girl:tina,cat:kitty,dog:tommy");
    MoreAsserts.assertContentsInOrder(m.keySet(), "boy", "girl", "cat", "dog");

    assertEquals("tom", m.get("boy"));
    assertEquals("tina", m.get("girl"));
    assertEquals("kitty", m.get("cat"));
    assertEquals("tommy", m.get("dog"));

    // try in a different order
    m = Splitter.on(",")
        .withKeyValueSeparator(":")
        .split("girl:tina,boy:tom,dog:tommy,cat:kitty");
    MoreAsserts.assertContentsInOrder(m.keySet(), "girl", "boy", "dog", "cat");

    assertEquals("tom", m.get("boy"));
    assertEquals("tina", m.get("girl"));
    assertEquals("kitty", m.get("cat"));
    assertEquals("tommy", m.get("dog"));
  }

  public void testMapSplitter_duplicateKeys() {
    try {
      Splitter.on(",").withKeyValueSeparator(":").split("a:1,b:2,a:3");
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException expected) {}
  }
}
