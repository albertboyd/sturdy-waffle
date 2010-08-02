/*
 * Copyright (C) 2008 Google Inc.
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

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.GwtCompatible;
import com.google.common.collect.ImmutableSet.ArrayImmutableSet;
import com.google.common.collect.ImmutableSet.TransformedImmutableSet;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Implementation of {@link ImmutableMap} with two or more entries.
 *
 * @author Jesse Wilson
 * @author Kevin Bourrillion
 * @author Gregory Kick
 */
@GwtCompatible(serializable = true, emulated = true)
final class RegularImmutableMap<K, V> extends ImmutableMap<K, V> {

  // entries in insertion order
  private final transient LinkedEntry<K, V>[] entries;
  // array of linked lists of entries
  private final transient LinkedEntry<K, V>[] table;
  // 'and' with an int to get a table index
  private final transient int mask;
  private final transient int keySetHashCode;

  // TODO: investigate avoiding the creation of ImmutableEntries since we
  // re-copy them anyway.
  RegularImmutableMap(Entry<?, ?>... immutableEntries) {
    int size = immutableEntries.length;
    entries = createEntryArray(size);

    // TODO: try smaller table sizes
    int tableSize = Hashing.chooseTableSize(size);
    table = createEntryArray(tableSize);
    mask = tableSize - 1;

    int keySetHashCodeMutable = 0;
    for (int entryIndex = 0; entryIndex < size; entryIndex++) {
      // each of our 6 callers carefully put only Entry<K, V>s into the array!
      @SuppressWarnings("unchecked")
      Entry<K, V> entry = (Entry<K, V>) immutableEntries[entryIndex];
      K key = entry.getKey();
      int keyHashCode = key.hashCode();
      keySetHashCodeMutable += keyHashCode;
      int tableIndex = Hashing.smear(keyHashCode) & mask;
      @Nullable LinkedEntry<K, V> existing = table[tableIndex];
      // prepend, not append, so the entries can be immutable
      LinkedEntry<K, V> linkedEntry =
          new LinkedEntry<K, V>(key, entry.getValue(), existing);
      table[tableIndex] = linkedEntry;
      entries[entryIndex] = linkedEntry;
      while (existing != null) {
        checkArgument(!key.equals(existing.getKey()), "duplicate key: %s", key);
        existing = existing.next;
      }
    }
    keySetHashCode = keySetHashCodeMutable;
  }

  /**
   * Creates a {@link LinkedEntry} array to hold parameterized entries. The
   * result must never be upcast back to LinkedEntry[] (or Object[], etc.), or
   * allowed to escape the class.
   */
  @SuppressWarnings("unchecked") // Safe as long as the javadocs are followed
  private LinkedEntry<K, V>[] createEntryArray(int size) {
    return new LinkedEntry[size];
  }

  @Immutable
  @SuppressWarnings("serial") // this class is never serialized
  private static final class LinkedEntry<K, V> extends ImmutableEntry<K, V> {
    @Nullable final LinkedEntry<K, V> next;

    LinkedEntry(K key, V value, @Nullable LinkedEntry<K, V> next) {
      super(key, value);
      this.next = next;
    }
  }

  @Override public V get(Object key) {
    if (key == null) {
      return null;
    }
    int index = Hashing.smear(key.hashCode()) & mask;
    for (LinkedEntry<K, V> entry = table[index];
        entry != null;
        entry = entry.next) {
      K candidateKey = entry.getKey();
      // assume that equals uses the == optimization when appropriate
      if (key.equals(candidateKey)) {
        return entry.getValue();
      }
    }
    return null;
  }

  public int size() {
    return entries.length;
  }

  @Override public boolean isEmpty() {
    return false;
  }

  @Override public boolean containsValue(Object value) {
    if (value == null) {
      return false;
    }
    for (Entry<K, V> entry : entries) {
      if (entry.getValue().equals(value)) {
        return true;
      }
    }
    return false;
  }

  // TODO: Serialization of the map views should serialize the map, and
  // deserialization should call entrySet(), keySet(), or values() on the
  // deserialized map. The views are serializable since the Immutable* classes
  // are.

  private transient ImmutableSet<Entry<K, V>> entrySet;

  @Override public ImmutableSet<Entry<K, V>> entrySet() {
    ImmutableSet<Entry<K, V>> es = entrySet;
    return (es == null) ? (entrySet = new EntrySet<K, V>(this)) : es;
  }

  @SuppressWarnings("serial") // uses writeReplace(), not default serialization
  private static class EntrySet<K, V> extends ArrayImmutableSet<Entry<K, V>> {
    final transient RegularImmutableMap<K, V> map;

    EntrySet(RegularImmutableMap<K, V> map) {
      super(map.entries);
      this.map = map;
    }

    @Override public boolean contains(Object target) {
      if (target instanceof Entry) {
        Entry<?, ?> entry = (Entry<?, ?>) target;
        V mappedValue = map.get(entry.getKey());
        return mappedValue != null && mappedValue.equals(entry.getValue());
      }
      return false;
    }
  }

  private transient ImmutableSet<K> keySet;

  @Override public ImmutableSet<K> keySet() {
    ImmutableSet<K> ks = keySet;
    return (ks == null) ? (keySet = new KeySet<K, V>(this)) : ks;
  }

  @SuppressWarnings("serial") // uses writeReplace(), not default serialization
  private static class KeySet<K, V>
      extends TransformedImmutableSet<Entry<K, V>, K> {
    final RegularImmutableMap<K, V> map;

    KeySet(RegularImmutableMap<K, V> map) {
      super(map.entries, map.keySetHashCode);
      this.map = map;
    }

    @Override K transform(Entry<K, V> element) {
      return element.getKey();
    }

    @Override public boolean contains(Object target) {
      return map.containsKey(target);
    }
  }

  private transient ImmutableCollection<V> values;

  @Override public ImmutableCollection<V> values() {
    ImmutableCollection<V> v = values;
    return (v == null) ? (values = new Values<V>(this)) : v;
  }

  @SuppressWarnings("serial") // uses writeReplace(), not default serialization
  private static class Values<V> extends ImmutableCollection<V> {
    final RegularImmutableMap<?, V> map;

    Values(RegularImmutableMap<?, V> map) {
      this.map = map;
    }

    public int size() {
      return map.entries.length;
    }

    @Override public UnmodifiableIterator<V> iterator() {
      return new AbstractIterator<V>() {
        int index = 0;
        @Override protected V computeNext() {
          return (index < map.entries.length)
              ? map.entries[index++].getValue()
              : endOfData();
        }
      };
    }

    @Override public boolean contains(Object target) {
      return map.containsValue(target);
    }
  }

  @Override public String toString() {
    StringBuilder result = new StringBuilder(size() * 16).append('{');
    Collections2.standardJoiner.appendTo(result, entries);
    return result.append('}').toString();
  }

  // This class is never actually serialized directly, but we have to make the
  // warning go away (and suppressing would suppress for all nested classes too)
  private static final long serialVersionUID = 0;
}
