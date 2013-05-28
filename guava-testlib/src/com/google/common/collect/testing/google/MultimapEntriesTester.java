/*
 * Copyright (C) 2013 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.common.collect.testing.google;

import static com.google.common.collect.testing.features.CollectionSize.ONE;
import static com.google.common.collect.testing.features.CollectionSize.ZERO;
import static com.google.common.collect.testing.features.MapFeature.SUPPORTS_REMOVE;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.collect.Multimap;
import com.google.common.collect.testing.Helpers;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.MapFeature;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * Tester for {@code Multimap.entries}.
 *
 * @author Louis Wasserman
 */
@GwtCompatible
public class MultimapEntriesTester<K, V> extends AbstractMultimapTester<K, V, Multimap<K, V>> {
  @CollectionSize.Require(absent = ZERO)
  @MapFeature.Require(SUPPORTS_REMOVE)
  public void testRemovePropagatesToMultimap() {
    assertTrue(multimap().entries().remove(
        Helpers.mapEntry(sampleKeys().e0, sampleValues().e0)));
    expectMissing(Helpers.mapEntry(sampleKeys().e0, sampleValues().e0));
    assertEquals(getNumElements() - 1, multimap().size());
    assertFalse(multimap().containsEntry(sampleKeys().e0, sampleValues().e0));
  }
  
  @CollectionSize.Require(absent = ZERO)
  @MapFeature.Require(SUPPORTS_REMOVE)
  public void testRemoveAllPropagatesToMultimap() {
    assertTrue(multimap().entries().removeAll(
        Collections.singleton(Helpers.mapEntry(sampleKeys().e0, sampleValues().e0))));
    expectMissing(Helpers.mapEntry(sampleKeys().e0, sampleValues().e0));
    assertEquals(getNumElements() - 1, multimap().size());
    assertFalse(multimap().containsEntry(sampleKeys().e0, sampleValues().e0));
  }
  
  @CollectionSize.Require(absent = ZERO)
  @MapFeature.Require(SUPPORTS_REMOVE)
  public void testRetainAllPropagatesToMultimap() {
    multimap().entries().retainAll(
        Collections.singleton(Helpers.mapEntry(sampleKeys().e0, sampleValues().e0)));
    assertEquals(
        getSubjectGenerator().create(Helpers.mapEntry(sampleKeys().e0, sampleValues().e0)),
        multimap());
    assertEquals(1, multimap().size());
    assertTrue(multimap().containsEntry(sampleKeys().e0, sampleValues().e0));
  }
  
  @CollectionSize.Require(ONE)
  @MapFeature.Require(SUPPORTS_REMOVE)
  public void testIteratorRemovePropagatesToMultimap() {
    Iterator<Entry<K, V>> iterator = multimap().entries().iterator();
    assertEquals(
        Helpers.mapEntry(sampleKeys().e0, sampleValues().e0),
        iterator.next());
    iterator.remove();
    assertTrue(multimap().isEmpty());
  }
  
  @CollectionSize.Require(absent = ZERO)
  @MapFeature.Require(SUPPORTS_REMOVE)
  public void testEntriesRemainValidAfterRemove() {
    Iterator<Entry<K, V>> iterator = multimap().entries().iterator();
    Entry<K, V> entry = iterator.next();
    K key = entry.getKey();
    V value = entry.getValue();
    multimap().removeAll(key);
    assertEquals(key, entry.getKey());
    assertEquals(value, entry.getValue());
  }
  
  /**
   * Returns the {@link Method} instance for the test that assumes the entries iterator supports 
   * {@code remove()}.
   */
  @GwtIncompatible("reflection")
  public static Method getIteratorRemoveMethod() {
    return Helpers.getMethod(MultimapEntriesTester.class,
        "testIteratorRemovePropagatesToMultimap");
  }
}
