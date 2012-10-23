/*
 * Copyright (C) 2007 The Guava Authors
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

package com.google.common.collect;

import static com.google.common.collect.testing.Helpers.orderEntriesByKey;
import static org.junit.contrib.truth.Truth.ASSERT;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.collect.testing.Helpers;
import com.google.common.collect.testing.SampleElements;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.MapFeature;
import com.google.common.collect.testing.google.BiMapTestSuiteBuilder;
import com.google.common.collect.testing.google.TestBiMapGenerator;
import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import com.google.common.testing.SerializableTester;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Tests for {@code EnumBiMap}.
 *
 * @author Mike Bostock
 * @author Jared Levy
 */
@GwtCompatible(emulated = true)
public class EnumBiMapTest extends TestCase {
  private enum Currency { DOLLAR, FRANC, PESO, POUND, YEN }
  private enum Country { CANADA, CHILE, JAPAN, SWITZERLAND, UK }

  public static final class EnumBiMapGenerator implements TestBiMapGenerator<Country, Currency> {
    @SuppressWarnings("unchecked")
    @Override
    public BiMap<Country, Currency> create(Object... entries) {
      BiMap<Country, Currency> result = EnumBiMap.create(Country.class, Currency.class);
      for (Object object : entries) {
        Entry<Country, Currency> entry = (Entry<Country, Currency>) object;
        result.put(entry.getKey(), entry.getValue());
      }
      return result;
    }

    @Override
    public SampleElements<Entry<Country, Currency>> samples() {
      return new SampleElements<Entry<Country, Currency>>(
          Helpers.mapEntry(Country.CANADA, Currency.DOLLAR),
          Helpers.mapEntry(Country.CHILE, Currency.PESO),
          Helpers.mapEntry(Country.UK, Currency.POUND),
          Helpers.mapEntry(Country.JAPAN, Currency.YEN),
          Helpers.mapEntry(Country.SWITZERLAND, Currency.FRANC));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Entry<Country, Currency>[] createArray(int length) {
      return new Entry[length];
    }

    @Override
    public Iterable<Entry<Country, Currency>> order(List<Entry<Country, Currency>> insertionOrder) {
      return orderEntriesByKey(insertionOrder);
    }

    @Override
    public Country[] createKeyArray(int length) {
      return new Country[length];
    }

    @Override
    public Currency[] createValueArray(int length) {
      return new Currency[length];
    }
  }

  @GwtIncompatible("suite")
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(BiMapTestSuiteBuilder.using(new EnumBiMapGenerator())
        .named("EnumBiMap")
        .withFeatures(CollectionSize.ANY,
            CollectionFeature.SERIALIZABLE,
            MapFeature.GENERAL_PURPOSE,
            CollectionFeature.KNOWN_ORDER)
        .createTestSuite());
    suite.addTestSuite(EnumBiMapTest.class);
    return suite;
  }

  public void testCreate() {
    EnumBiMap<Currency, Country> bimap =
        EnumBiMap.create(Currency.class, Country.class);
    assertTrue(bimap.isEmpty());
    assertEquals("{}", bimap.toString());
    assertEquals(HashBiMap.create(), bimap);
    bimap.put(Currency.DOLLAR, Country.CANADA);
    assertEquals(Country.CANADA, bimap.get(Currency.DOLLAR));
    assertEquals(Currency.DOLLAR, bimap.inverse().get(Country.CANADA));
  }

  public void testCreateFromMap() {
    /* Test with non-empty Map. */
    Map<Currency, Country> map = ImmutableMap.of(
        Currency.DOLLAR, Country.CANADA,
        Currency.PESO, Country.CHILE,
        Currency.FRANC, Country.SWITZERLAND);
    EnumBiMap<Currency, Country> bimap = EnumBiMap.create(map);
    assertEquals(Country.CANADA, bimap.get(Currency.DOLLAR));
    assertEquals(Currency.DOLLAR, bimap.inverse().get(Country.CANADA));

    /* Map must have at least one entry if not an EnumBiMap. */
    try {
      EnumBiMap.create(Collections.<Currency, Country>emptyMap());
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException expected) {}
    try {
      EnumBiMap.create(
          EnumHashBiMap.<Currency, Country>create(Currency.class));
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException expected) {}

    /* Map can be empty if it's an EnumBiMap. */
    Map<Currency, Country> emptyBimap =
        EnumBiMap.create(Currency.class, Country.class);
    bimap = EnumBiMap.create(emptyBimap);
    assertTrue(bimap.isEmpty());
  }

  public void testEnumBiMapConstructor() {
    /* Test that it copies existing entries. */
    EnumBiMap<Currency, Country> bimap1 =
        EnumBiMap.create(Currency.class, Country.class);
    bimap1.put(Currency.DOLLAR, Country.CANADA);
    EnumBiMap<Currency, Country> bimap2 =
        EnumBiMap.create(bimap1);
    assertEquals(Country.CANADA, bimap2.get(Currency.DOLLAR));
    assertEquals(bimap1, bimap2);
    bimap2.inverse().put(Country.SWITZERLAND, Currency.FRANC);
    assertEquals(Country.SWITZERLAND, bimap2.get(Currency.FRANC));
    assertNull(bimap1.get(Currency.FRANC));
    assertFalse(bimap2.equals(bimap1));

    /* Test that it can be empty. */
    EnumBiMap<Currency, Country> emptyBimap =
        EnumBiMap.create(Currency.class, Country.class);
    EnumBiMap<Currency, Country> bimap3 =
        EnumBiMap.create(emptyBimap);
    assertEquals(bimap3, emptyBimap);
  }

  public void testKeyType() {
    EnumBiMap<Currency, Country> bimap =
        EnumBiMap.create(Currency.class, Country.class);
    assertEquals(Currency.class, bimap.keyType());
  }

  public void testValueType() {
    EnumBiMap<Currency, Country> bimap =
        EnumBiMap.create(Currency.class, Country.class);
    assertEquals(Country.class, bimap.valueType());
  }

  public void testIterationOrder() {
    // The enum orderings are alphabetical, leading to the bimap and its inverse
    // having inconsistent iteration orderings.
    Map<Currency, Country> map = ImmutableMap.of(
        Currency.DOLLAR, Country.CANADA,
        Currency.PESO, Country.CHILE,
        Currency.FRANC, Country.SWITZERLAND);
    EnumBiMap<Currency, Country> bimap = EnumBiMap.create(map);

    // forward map ordered by currency
    ASSERT.that(bimap.keySet())
        .hasContentsInOrder(Currency.DOLLAR, Currency.FRANC, Currency.PESO);
    // forward map ordered by currency (even for country values)
    ASSERT.that(bimap.values())
        .hasContentsInOrder(Country.CANADA, Country.SWITZERLAND, Country.CHILE);
    // backward map ordered by country
    ASSERT.that(bimap.inverse().keySet())
        .hasContentsInOrder(Country.CANADA, Country.CHILE, Country.SWITZERLAND);
    // backward map ordered by country (even for currency values)
    ASSERT.that(bimap.inverse().values())
        .hasContentsInOrder(Currency.DOLLAR, Currency.PESO, Currency.FRANC);
  }

  public void testKeySetIteratorRemove() {
    // The enum orderings are alphabetical, leading to the bimap and its inverse
    // having inconsistent iteration orderings.
    Map<Currency, Country> map = ImmutableMap.of(
        Currency.DOLLAR, Country.CANADA,
        Currency.PESO, Country.CHILE,
        Currency.FRANC, Country.SWITZERLAND);
    EnumBiMap<Currency, Country> bimap = EnumBiMap.create(map);

    Iterator<Currency> iter = bimap.keySet().iterator();
    assertEquals(Currency.DOLLAR, iter.next());
    iter.remove();

    // forward map ordered by currency
    ASSERT.that(bimap.keySet())
        .hasContentsInOrder(Currency.FRANC, Currency.PESO);
    // forward map ordered by currency (even for country values)
    ASSERT.that(bimap.values())
        .hasContentsInOrder(Country.SWITZERLAND, Country.CHILE);
    // backward map ordered by country
    ASSERT.that(bimap.inverse().keySet())
        .hasContentsInOrder(Country.CHILE, Country.SWITZERLAND);
    // backward map ordered by country (even for currency values)
    ASSERT.that(bimap.inverse().values())
        .hasContentsInOrder(Currency.PESO, Currency.FRANC);
  }

  public void testValuesIteratorRemove() {
    // The enum orderings are alphabetical, leading to the bimap and its inverse
    // having inconsistent iteration orderings.
    Map<Currency, Country> map = ImmutableMap.of(
        Currency.DOLLAR, Country.CANADA,
        Currency.PESO, Country.CHILE,
        Currency.FRANC, Country.SWITZERLAND);
    EnumBiMap<Currency, Country> bimap = EnumBiMap.create(map);

    Iterator<Currency> iter = bimap.keySet().iterator();
    assertEquals(Currency.DOLLAR, iter.next());
    assertEquals(Currency.FRANC, iter.next());
    iter.remove();

    // forward map ordered by currency
    ASSERT.that(bimap.keySet())
        .hasContentsInOrder(Currency.DOLLAR, Currency.PESO);
    // forward map ordered by currency (even for country values)
    ASSERT.that(bimap.values())
        .hasContentsInOrder(Country.CANADA, Country.CHILE);
    // backward map ordered by country
    ASSERT.that(bimap.inverse().keySet())
        .hasContentsInOrder(Country.CANADA, Country.CHILE);
    // backward map ordered by country (even for currency values)
    ASSERT.that(bimap.inverse().values())
        .hasContentsInOrder(Currency.DOLLAR, Currency.PESO);
  }

  public void testEntrySet() {
    // Bug 3168290
    Map<Currency, Country> map = ImmutableMap.of(
        Currency.DOLLAR, Country.CANADA,
        Currency.PESO, Country.CHILE,
        Currency.FRANC, Country.SWITZERLAND);
    EnumBiMap<Currency, Country> bimap = EnumBiMap.create(map);
    Set<Object> uniqueEntries = Sets.newIdentityHashSet();
    uniqueEntries.addAll(bimap.entrySet());
    assertEquals(3, uniqueEntries.size());
  }

  @GwtIncompatible("serialization")
  public void testSerializable() {
    SerializableTester.reserializeAndAssert(
        EnumBiMap.create(ImmutableMap.of(Currency.DOLLAR, Country.CANADA)));
  }

  @GwtIncompatible("reflection")
  public void testNulls() {
    new NullPointerTester().testAllPublicStaticMethods(EnumBiMap.class);
    new NullPointerTester()
        .testAllPublicInstanceMethods(
            EnumBiMap.create(ImmutableMap.of(Currency.DOLLAR, Country.CHILE)));
  }

  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            EnumBiMap.create(ImmutableMap.of(Currency.DOLLAR, Country.CANADA)),
            EnumBiMap.create(ImmutableMap.of(Currency.DOLLAR, Country.CANADA)))
        .addEqualityGroup(EnumBiMap.create(ImmutableMap.of(Currency.DOLLAR, Country.CHILE)))
        .addEqualityGroup(EnumBiMap.create(ImmutableMap.of(Currency.FRANC, Country.CANADA)))
        .testEquals();
  }

  /* Remaining behavior tested by AbstractBiMapTest. */
}
