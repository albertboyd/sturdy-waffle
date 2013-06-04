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

import static java.util.Arrays.asList;

import com.google.common.annotations.GwtCompatible;
import com.google.common.collect.Multiset.Entry;
import com.google.common.collect.testing.google.UnmodifiableCollectionTests;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Common tests for a {@link Multiset}.
 *
 * @author Kevin Bourrillion
 */
@GwtCompatible(emulated = true)
public abstract class AbstractMultisetTest extends AbstractCollectionTest {

  @Override protected abstract <E> Multiset<E> create();

  protected Multiset<String> ms;

  // public for GWT
  @Override public void setUp() throws Exception {
    super.setUp();
    c = ms = create();
  }

  /**
   * Validates that multiset size returned by {@code size()} is the same as the
   * size generated by summing the counts of all multiset entries.
   */
  protected void assertSize(Multiset<String> multiset) {
    long size = 0;
    for (Multiset.Entry<String> entry : multiset.entrySet()) {
      size += entry.getCount();
    }
    assertEquals((int) Math.min(size, Integer.MAX_VALUE), multiset.size());
  }

  protected void assertSize() {
    assertSize(ms);
  }

  @Override protected void assertContents(String... expected) {
    super.assertContents(expected);
    assertSize();
  }
  
  static class WrongType {}

  public void testAddNoneToNone() {
    assertEquals(0, ms.add("a", 0));
    assertContents();
  }

  public void testAddNoneToSome() {
    ms.add("a");
    assertEquals(1, ms.add("a", 0));
    assertContents("a");
  }

  public void testAddSeveralAtOnce() {
    assertEquals(0, ms.add("a", 3));
    assertContents("a", "a", "a");
  }

  public void testAddSomeToSome() {
    ms.add("a", 2);
    assertEquals(2, ms.add("a", 3));
    assertContents("a", "a", "a", "a", "a");
  }

  @Override public void testAddSeveralTimes() {
    assertTrue(ms.add("a"));
    assertTrue(ms.add("b"));
    assertTrue(ms.add("a"));
    assertTrue(ms.add("b"));
    assertContents("a", "b", "a", "b");
  }

  public void testAddNegative() {
    try {
      ms.add("a", -1);
      fail();
    } catch (IllegalArgumentException expected) {
    }
    assertSize();
  }

  @Override public void testEqualsNo() {
    ms.add("a");
    ms.add("b");
    ms.add("b");

    Multiset<String> ms2 = create();
    ms2.add("a", 2);
    ms2.add("b");

    assertFalse(ms.equals(ms2));
    assertSize();
  }

  public void testAddTooMany() {
    ms.add("a", Integer.MAX_VALUE); // so far so good
    ms.add("b", Integer.MAX_VALUE); // so far so good
    try {
      ms.add("a");
      fail();
    } catch (IllegalArgumentException expected) {
    }
    assertSize();
  }

  public void testAddAllEmptySet() {
    c = ms = createSample();
    assertFalse(ms.addAll(Collections.<String>emptySet()));
    assertEquals(createSample(), ms);
    assertSize();
  }

  public void testAddAllEmptyMultiset() {
    c = ms = createSample();
    Multiset<String> empty = create();
    assertFalse(ms.addAll(empty));
    assertEquals(createSample(), ms);
    assertSize();
  }

  public void testAddAllSet() {
    c = ms = createSample();
    Set<String> more = ImmutableSet.of("c", "d", "e");
    assertTrue(ms.addAll(more));
    assertContents("a", "b", "b", "c", "c", "d", "d", "d", "d", "e");
  }

  public void testAddAllMultiset() {
    c = ms = createSample();
    Multiset<String> more = HashMultiset.create(
        asList("c", "c", "d", "d", "e"));
    assertTrue(ms.addAll(more));
    assertContents("a", "b", "b", "c", "c", "c", "d", "d", "d", "d", "d", "e");
  }

  public void testRemoveNoneFromSome() {
    ms.add("a");
    assertEquals(1, ms.remove("a", 0));
    assertContents("a");
  }

  public void testRemoveOneFromNone() {
    assertEquals(0, ms.remove("a", 1));
    assertContents();
  }

  public void testRemoveOneFromOne() {
    ms.add("a");
    assertEquals(1, ms.remove("a", 1));
    assertContents();
  }

  public void testRemoveSomeFromSome() {
    ms.add("a", 5);
    assertEquals(5, ms.remove("a", 3));
    assertContents("a", "a");
  }

  public void testRemoveTooMany() {
    ms.add("a", 3);
    assertEquals(3, ms.remove("a", 5));
    assertContents();
  }

  public void testRemoveNegative() {
    try {
      ms.remove("a", -1);
      fail();
    } catch (IllegalArgumentException expected) {
    }
    assertSize();
  }

  public void testContainsSeveral() {
    ms.add("a", 3);
    assertTrue(ms.contains(new String("a")));
    assertSize();
  }

  public void testContainsAllNo() {
    ms.add("a", 2);
    ms.add("b", 3);
    assertFalse(ms.containsAll(asList("a", "c")));
    assertSize();
  }

  public void testContainsAllYes() {
    ms.add("a", 2);
    ms.add("b", 3);
    ms.add("c", 4);
    assertTrue(ms.containsAll(asList("a", "c")));
    assertSize();
  }

  public void testRemoveAllOfOne() {
    ms.add("a", 2);
    ms.add("b");
    assertTrue(ms.removeAll(asList("a", "c")));
    assertContents("b");
  }

  public void testRemoveAllOfDisjoint() {
    ms.add("a", 2);
    ms.add("b");
    assertFalse(ms.removeAll(asList("c", "d")));
    assertContents("a", "a", "b");
  }

  public void testRemoveAllOfEverything() {
    ms.add("a", 2);
    ms.add("b");
    assertTrue(ms.removeAll(asList("a", "b")));
    assertContents();
  }

  public void testRetainAllOfOne() {
    ms.add("a", 2);
    ms.add("b");
    assertTrue(ms.retainAll(asList("a", "c")));
    assertContents("a", "a");
  }

  public void testRetainAllOfDisjoint() {
    ms.add("a", 2);
    ms.add("b");
    assertTrue(ms.retainAll(asList("c", "d")));
    assertContents();
  }

  public void testRetainAllOfEverything() {
    ms.add("a", 2);
    ms.add("b");
    assertFalse(ms.retainAll(asList("a", "b")));
    assertContents("a", "a", "b");
  }

  public void testContainsAllVacuousViaElementSet() {
    assertTrue(ms.elementSet().containsAll(Collections.emptySet()));
  }

  public void testContainsAllNoViaElementSet() {
    ms.add("a", 2);
    ms.add("b", 3);
    assertFalse(ms.elementSet().containsAll(asList("a", "c")));
    assertSize();
  }

  public void testContainsAllYesViaElementSet() {
    ms.add("a", 2);
    ms.add("b", 3);
    ms.add("c", 4);
    assertTrue(ms.elementSet().containsAll(asList("a", "c")));
    assertSize();
  }

  public void testRemoveAllVacuousViaElementSet() {
    assertFalse(ms.elementSet().removeAll(Collections.emptySet()));
    assertSize();
  }

  public void testRemoveAllOfOneViaElementSet() {
    ms.add("a", 2);
    ms.add("b");
    assertTrue(ms.elementSet().removeAll(asList("a", "c")));
    assertContents("b");
  }

  public void testRemoveAllOfDisjointViaElementSet() {
    ms.add("a", 2);
    ms.add("b");
    assertFalse(ms.elementSet().removeAll(asList("c", "d")));
    assertContents("a", "a", "b");
  }

  public void testRemoveAllOfEverythingViaElementSet() {
    ms.add("a", 2);
    ms.add("b");
    assertTrue(ms.elementSet().removeAll(asList("a", "b")));
    assertContents();
  }

  public void testRetainAllVacuousViaElementSet() {
    assertFalse(ms.elementSet().retainAll(asList("a")));
    assertContents();
  }

  public void testRetainAllOfNothingViaElementSet() {
    ms.add("a");
    assertTrue(ms.elementSet().retainAll(Collections.emptySet()));
    assertContents();
  }

  public void testRetainAllOfOneViaElementSet() {
    ms.add("a", 2);
    ms.add("b");
    assertTrue(ms.elementSet().retainAll(asList("a", "c")));
    assertContents("a", "a");
  }

  public void testRetainAllOfDisjointViaElementSet() {
    ms.add("a", 2);
    ms.add("b");
    assertTrue(ms.elementSet().retainAll(asList("c", "d")));
    assertContents();
  }

  public void testRetainAllOfEverythingViaElementSet() {
    ms.add("a", 2);
    ms.add("b");
    assertFalse(ms.elementSet().retainAll(asList("a", "b")));
    assertContents("a", "a", "b");
  }

  public void testElementSetBasic() {
    ms.add("a", 3);
    ms.add("b", 2);
    ms.add("c", 1);
    HashSet<String> expected = Sets.newHashSet("a", "b", "c");
    Set<String> actual = ms.elementSet();
    assertEquals(expected, actual);
    assertEquals(actual, expected);
    assertSize();
  }

  public void testElementSetIsNotACopy() {
    ms.add("a", 1);
    ms.add("b", 2);
    Set<String> elementSet = ms.elementSet();
    ms.add("c", 3);
    ms.setCount("b", 0);
    assertEquals(Sets.newHashSet("a", "c"), elementSet);
    assertSize();
  }

  public void testRemoveFromElementSetYes() {
    ms.add("a", 1);
    ms.add("b", 2);
    Set<String> elementSet = ms.elementSet();
    assertTrue(elementSet.remove("b"));
    assertContents("a");
  }

  public void testRemoveFromElementSetNo() {
    ms.add("a", 1);
    Set<String> elementSet = ms.elementSet();
    assertFalse(elementSet.remove("b"));
    assertContents("a");
  }

  public void testRemoveFromElementSetNull() {
    assertEquals(false, ms.elementSet().remove(null));
  }

  public void testRemoveFromElementSetWrongType() {
    assertEquals(false, ms.elementSet().remove(new WrongType()));
  }

  public void testCantAddToElementSet() {
    try {
      ms.elementSet().add("a");
      fail();
    } catch (UnsupportedOperationException expected) {
    }
    assertSize();
  }

  public void testClearViaElementSet() {
    ms = createSample();
    ms.elementSet().clear();
    assertContents();
  }

  public void testClearViaEntrySet() {
    ms = createSample();
    ms.entrySet().clear();
    assertContents();
  }

  public void testEntrySet() {
    ms = createSample();
    for (Multiset.Entry<String> entry : ms.entrySet()) {
      assertEquals(entry, entry);
      String element = entry.getElement();
      if (element.equals("a")) {
        assertEquals(1, entry.getCount());
      } else if (element.equals("b")) {
        assertEquals(2, entry.getCount());
      } else if (element.equals("c")) {
        assertEquals(1, entry.getCount());
      } else if (element.equals("d")) {
        assertEquals(3, entry.getCount());
      } else {
        fail();
      }
    }
    assertSize();
  }

  public void testEntrySetEmpty() {
    assertEquals(Collections.emptySet(), ms.entrySet());
  }

  public void testReallyBig() {
    ms.add("a", Integer.MAX_VALUE - 1);
    assertEquals(Integer.MAX_VALUE - 1, ms.size());
    ms.add("b", 3);

    // See Collection.size() contract
    assertEquals(Integer.MAX_VALUE, ms.size());

    // Make sure we didn't forget our size
    ms.remove("a", 4);
    assertEquals(Integer.MAX_VALUE - 2, ms.size());
    assertSize();
  }

  public void testToStringNull() {
    ms.add("a", 3);
    ms.add("c", 1);
    ms.add("b", 2);
    ms.add(null, 4);

    // This test is brittle. The original test was meant to validate the
    // contents of the string itself, but key ordering tended to change
    // under unpredictable circumstances. Instead, we're just ensuring
    // that the string not return null, and implicitly, not throw an exception.
    assertNotNull(ms.toString());
    assertSize();
  }

  public void testEntryAfterRemove() {
    ms.add("a", 8);
    Multiset.Entry<String> entry = ms.entrySet().iterator().next();
    assertEquals(8, entry.getCount());
    ms.remove("a");
    assertEquals(7, entry.getCount());
    ms.remove("a", 4);
    assertEquals(3, entry.getCount());
    ms.elementSet().remove("a");
    assertEquals(0, entry.getCount());
    ms.add("a", 5);
    assertEquals(5, entry.getCount());
  }

  public void testEntryAfterClear() {
    ms.add("a", 3);
    Multiset.Entry<String> entry = ms.entrySet().iterator().next();
    ms.clear();
    assertEquals(0, entry.getCount());
    ms.add("a", 5);
    assertEquals(5, entry.getCount());
  }

  public void testEntryAfterEntrySetClear() {
    ms.add("a", 3);
    Multiset.Entry<String> entry = ms.entrySet().iterator().next();
    ms.entrySet().clear();
    assertEquals(0, entry.getCount());
    ms.add("a", 5);
    assertEquals(5, entry.getCount());
  }

  public void testEntryAfterEntrySetIteratorRemove() {
    ms.add("a", 3);
    Iterator<Multiset.Entry<String>> iterator = ms.entrySet().iterator();
    Multiset.Entry<String> entry = iterator.next();
    iterator.remove();
    assertEquals(0, entry.getCount());
    try {
      iterator.remove();
      fail();
    } catch (IllegalStateException expected) {}
    ms.add("a", 5);
    assertEquals(5, entry.getCount());
  }

  public void testEntryAfterElementSetIteratorRemove() {
    ms.add("a", 3);
    Multiset.Entry<String> entry = ms.entrySet().iterator().next();
    Iterator<String> iterator = ms.elementSet().iterator();
    iterator.next();
    iterator.remove();
    assertEquals(0, entry.getCount());
    ms.add("a", 5);
    assertEquals(5, entry.getCount());
  }

  public void testEntrySetContains() {
    ms.add("a", 3);
    Set<Entry<String>> es = ms.entrySet();
    assertTrue(es.contains(Multisets.immutableEntry("a", 3)));
    assertFalse(es.contains(null));
    assertFalse(es.contains(Maps.immutableEntry("a", 3)));
    assertFalse(es.contains(Multisets.immutableEntry("a", 2)));
    assertFalse(es.contains(Multisets.immutableEntry("b", 3)));
    assertFalse(es.contains(Multisets.immutableEntry("b", 0)));
  }

  public void testEntrySetRemove() {
    ms.add("a", 3);
    Set<Entry<String>> es = ms.entrySet();
    assertFalse(es.remove(null));
    assertFalse(es.remove(Maps.immutableEntry("a", 3)));
    assertFalse(es.remove(Multisets.immutableEntry("a", 2)));
    assertFalse(es.remove(Multisets.immutableEntry("b", 3)));
    assertFalse(es.remove(Multisets.immutableEntry("b", 0)));
    assertEquals(3, ms.count("a"));
    assertTrue(es.remove(Multisets.immutableEntry("a", 3)));
    assertEquals(0, ms.count("a"));
  }

  public void testEntrySetToArray() {
    ms.add("a", 3);
    Set<Multiset.Entry<String>> es = ms.entrySet();
    Entry<?>[] array = new Entry<?>[3];
    assertSame(array, es.toArray(array));
    assertEquals(Multisets.immutableEntry("a", 3), array[0]);
    assertNull(array[1]);
  }

  public void testUnmodifiableMultiset() {
    ms.add("a", 3);
    ms.add("b");
    ms.add("c", 2);
    Multiset<Object> unmodifiable = Multisets.<Object>unmodifiableMultiset(ms);
    UnmodifiableCollectionTests.assertMultisetIsUnmodifiable(unmodifiable, "a");
  }

  @Override protected Multiset<String> createSample() {
    Multiset<String> ms = create();
    ms.add("a", 1);
    ms.add("b", 2);
    ms.add("c", 1);
    ms.add("d", 3);
    return ms;
  }
}

