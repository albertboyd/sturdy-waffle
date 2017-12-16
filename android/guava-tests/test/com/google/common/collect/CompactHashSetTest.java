/*
 * Copyright (C) 2012 The Guava Authors
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

import com.google.common.annotations.GwtIncompatible;
import com.google.common.collect.testing.SetTestSuiteBuilder;
import com.google.common.collect.testing.TestStringSetGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.Feature;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for CompactHashSet.
 *
 * @author Dimitris Andreou
 */
@GwtIncompatible // java.util.Arrays#copyOf(Object[], int), java.lang.reflect.Array
public class CompactHashSetTest extends TestCase {
  public static Test suite() {
    List<Feature<?>> allFeatures = Arrays.<Feature<?>>asList(
        CollectionSize.ANY,
        CollectionFeature.ALLOWS_NULL_VALUES,
        CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION,
        CollectionFeature.GENERAL_PURPOSE,
        CollectionFeature.REMOVE_OPERATIONS,
        CollectionFeature.SERIALIZABLE,
        CollectionFeature.SUPPORTS_ADD,
        CollectionFeature.SUPPORTS_REMOVE);

    TestSuite suite = new TestSuite();
    suite.addTestSuite(CompactHashSetTest.class);
    suite.addTest(SetTestSuiteBuilder.using(new TestStringSetGenerator() {
      @Override protected Set<String> create(String[] elements) {
        return CompactHashSet.create(Arrays.asList(elements));
      }
    }).named("CompactHashSet")
      .withFeatures(allFeatures)
      .createTestSuite());
    suite.addTest(SetTestSuiteBuilder.using(new TestStringSetGenerator() {
      @Override protected Set<String> create(String[] elements) {
        CompactHashSet set = CompactHashSet.create(Arrays.asList(elements));
        for (int i = 0; i < 100; i++) {
          set.add(i);
        }
        for (int i = 0; i < 100; i++) {
          set.remove(i);
        }
        set.trimToSize();
        return set;
      }
    }).named("CompactHashSet#TrimToSize")
      .withFeatures(allFeatures)
      .createTestSuite());
    return suite;
  }
}
