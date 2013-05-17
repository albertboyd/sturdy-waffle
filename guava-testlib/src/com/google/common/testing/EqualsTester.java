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

package com.google.common.testing;

import static com.google.common.base.Preconditions.checkNotNull;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.testing.RelationshipTester.RelationshipAssertion;

import java.util.List;

/**
 * Tester for equals() and hashCode() methods of a class.
 *
 * <p>To use, create a new EqualsTester and add equality groups where each group
 * contains objects that are supposed to be equal to each other, and objects of
 * different groups are expected to be unequal. For example:
 * <pre>
 * new EqualsTester()
 *     .addEqualityGroup("hello", "h" + "ello")
 *     .addEqualityGroup("world", "wor" + "ld")
 *     .addEqualityGroup(2, 1 + 1)
 *     .testEquals();
 * </pre>
 * This tests:
 * <ul>
 * <li>comparing each object against itself returns true
 * <li>comparing each object against null returns false
 * <li>comparing each object against an instance of an incompatible class
 *     returns false
 * <li>comparing each pair of objects within the same equality group returns
 *     true
 * <li>comparing each pair of objects from different equality groups returns
 *     false
 * <li>the hash codes of any two equal objects are equal
 * </ul>
 *
 * <p>When a test fails, the error message labels the objects involved in
 * the failed comparison as follows:
 * <ul>
 *   <li>"{@code [group }<i>i</i>{@code , item }<i>j</i>{@code ]}" refers to the
 *       <i>j</i><sup>th</sup> item in the <i>i</i><sup>th</sup> equality group,
 *       where both equality groups and the items within equality groups are
 *       numbered starting from 1.  When either a constructor argument or an
 *       equal object is provided, that becomes group 1.
 * </ul>
 *
 * @author Jim McMaster
 * @author Jige Yu
 * @since 10.0
 */
@Beta
@GwtCompatible
public final class EqualsTester {
  private static final int REPETITIONS = 3;

  private final List<List<Object>> equalityGroups = Lists.newArrayList();
  private RelationshipTester.ItemReporter itemReporter = new RelationshipTester.ItemReporter();

  /**
   * Constructs an empty EqualsTester instance
   */
  public EqualsTester() {}

  /**
   * Adds {@code equalityGroup} with objects that are supposed to be equal to
   * each other and not equal to any other equality groups added to this tester.
   */
  public EqualsTester addEqualityGroup(Object... equalityGroup) {
    checkNotNull(equalityGroup);
    equalityGroups.add(ImmutableList.copyOf(equalityGroup));
    return this;
  }

  /**
   * Run tests on equals method, throwing a failure on an invalid test
   */
  public EqualsTester testEquals() {
    RelationshipTester<Object> delegate = new RelationshipTester<Object>(
        new RelationshipAssertion<Object>() {
          @Override public void assertRelated(Object item, Object related) {
            assertEquals("$ITEM must be Object#equals to $RELATED", item, related);
            int itemHash = item.hashCode();
            int relatedHash = related.hashCode();
            assertEquals("the Object#hashCode (" + itemHash + ") of $ITEM must be equal to the "
                + "Object#hashCode (" + relatedHash +") of $RELATED", itemHash, relatedHash);
          }

          @Override public void assertUnrelated(Object item, Object unrelated) {
            // TODO(cpovirk): should this implementation (and
            // RelationshipAssertions in general) accept null inputs?
            assertTrue("$ITEM must not be Object#equals to $UNRELATED",
              !Objects.equal(item, unrelated));
          }
        }, itemReporter);
    for (List<Object> group : equalityGroups) {
      delegate.addRelatedGroup(group);
    }
    for (int run = 0; run < REPETITIONS; run++) {
      testItems();
      delegate.test();
    }
    return this;
  }

  EqualsTester setItemReporter(RelationshipTester.ItemReporter reporter) {
    this.itemReporter = checkNotNull(reporter);
    return this;
  }

  private void testItems() {
    for (Object item : Iterables.concat(equalityGroups)) {
      assertTrue(item + " must not be Object#equals to null", !item.equals(null));
      assertTrue(item + " must not be Object#equals to an arbitrary object of another class",
          !item.equals(NotAnInstance.EQUAL_TO_NOTHING));
      assertEquals(item + " must be Object#equals to itself", item, item);
      assertEquals("the Object#hashCode of " + item + " must be consistent",
          item.hashCode(), item.hashCode());
    }
  }

  /**
   * Class used to test whether equals() correctly handles an instance
   * of an incompatible class.  Since it is a private inner class, the
   * invoker can never pass in an instance to the tester
   */
  private enum NotAnInstance {
    EQUAL_TO_NOTHING;
  }
}
