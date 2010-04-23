/*
 * Copyright (C) 2007 Google Inc.
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

import com.google.common.annotations.GwtCompatible;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Implementation of {@code Multimap} whose keys and values are ordered by
 * their natural ordering or by supplied comparators. In all cases, this
 * implementation uses {@link Comparable#compareTo} or {@link
 * Comparator#compare} instead of {@link Object#equals} to determine
 * equivalence of instances.
 *
 * <p><b>Warning:</b> The comparators or comparables used must be <i>consistent
 * with equals</i> as explained by the {@link Comparable} class specification.
 * Otherwise, the resulting multiset will violate the general contract of {@link
 * SetMultimap}, which it is specified in terms of {@link Object#equals}.
 *
 * <p>The collections returned by {@code keySet} and {@code asMap} iterate
 * through the keys according to the key comparator ordering or the natural
 * ordering of the keys. Similarly, {@code get}, {@code removeAll}, and {@code
 * replaceValues} return collections that iterate through the values according
 * to the value comparator ordering or the natural ordering of the values. The
 * collections generated by {@code entries}, {@code keys}, and {@code values}
 * iterate across the keys according to the above key ordering, and for each
 * key they iterate across the values according to the value ordering.
 *
 * <p>The multimap does not store duplicate key-value pairs. Adding a new
 * key-value pair equal to an existing key-value pair has no effect.
 *
 * <p>Depending on the comparators, null keys and values may or may not be
 * supported. The natural ordering does not support nulls. All optional multimap
 * methods are supported, and all returned views are modifiable.
 *
 * <p>This class is not threadsafe when any concurrent operations update the
 * multimap. Concurrent read operations will work correctly. To allow concurrent
 * update operations, wrap your multimap with a call to {@link
 * Multimaps#synchronizedSortedSetMultimap}.
 *
 * @author Jared Levy
 * @since 2 (imported from Google Collections Library)
 */
@GwtCompatible(serializable = true)
public class TreeMultimap<K, V> extends AbstractSortedSetMultimap<K, V> {
  private transient Comparator<? super K> keyComparator;
  private transient Comparator<? super V> valueComparator;

  /**
   * Creates an empty {@code TreeMultimap} ordered by the natural ordering of
   * its keys and values.
   */
  @SuppressWarnings("unchecked") // eclipse doesn't like the raw Comparable
  public static <K extends Comparable, V extends Comparable>
      TreeMultimap<K, V> create() {
    return new TreeMultimap<K, V>(Ordering.natural(), Ordering.natural());
  }

  /**
   * Creates an empty {@code TreeMultimap} instance using explicit comparators.
   * Neither comparator may be null; use {@link Ordering#natural()} to specify
   * natural order.
   *
   * @param keyComparator the comparator that determines the key ordering
   * @param valueComparator the comparator that determines the value ordering
   */
  public static <K, V> TreeMultimap<K, V> create(
      Comparator<? super K> keyComparator,
      Comparator<? super V> valueComparator) {
    return new TreeMultimap<K, V>(checkNotNull(keyComparator),
        checkNotNull(valueComparator));
  }

  /**
   * Constructs a {@code TreeMultimap}, ordered by the natural ordering of its
   * keys and values, with the same mappings as the specified multimap.
   *
   * @param multimap the multimap whose contents are copied to this multimap
   */
  @SuppressWarnings("unchecked") // eclipse doesn't like the raw Comparable
  public static <K extends Comparable, V extends Comparable>
      TreeMultimap<K, V> create(Multimap<? extends K, ? extends V> multimap) {
    return new TreeMultimap<K, V>(Ordering.natural(), Ordering.natural(),
        multimap);
  }

  TreeMultimap(Comparator<? super K> keyComparator,
      Comparator<? super V> valueComparator) {
    super(new TreeMap<K, Collection<V>>(keyComparator));
    this.keyComparator = keyComparator;
    this.valueComparator = valueComparator;
  }

  private TreeMultimap(Comparator<? super K> keyComparator,
      Comparator<? super V> valueComparator,
      Multimap<? extends K, ? extends V> multimap) {
    this(keyComparator, valueComparator);
    putAll(multimap);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Creates an empty {@code TreeSet} for a collection of values for one key.
   *
   * @return a new {@code TreeSet} containing a collection of values for one
   *     key
   */
  @Override SortedSet<V> createCollection() {
    return new TreeSet<V>(valueComparator);
  }

  /**
   * Returns the comparator that orders the multimap keys.
   */
  public Comparator<? super K> keyComparator() {
    return keyComparator;
  }

  public Comparator<? super V> valueComparator() {
    return valueComparator;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Because a {@code TreeMultimap} has unique sorted keys, this method
   * returns a {@link SortedSet}, instead of the {@link Set} specified in the
   * {@link Multimap} interface.
   */
  @Override public SortedSet<K> keySet() {
    return (SortedSet<K>) super.keySet();
  }

  /**
   * {@inheritDoc}
   *
   * <p>Because a {@code TreeMultimap} has unique sorted keys, this method
   * returns a {@link SortedMap}, instead of the {@link java.util.Map} specified
   * in the {@link Multimap} interface.
   */
  @Override public SortedMap<K, Collection<V>> asMap() {
    return (SortedMap<K, Collection<V>>) super.asMap();
  }

  /**
   * @serialData key comparator, value comparator, number of distinct keys, and
   *     then for each distinct key: the key, number of values for that key, and
   *     key values
   */
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    stream.writeObject(keyComparator());
    stream.writeObject(valueComparator());
    Serialization.writeMultimap(this, stream);
  }

  @SuppressWarnings("unchecked") // reading data stored by writeObject
  private void readObject(ObjectInputStream stream)
      throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    keyComparator = checkNotNull((Comparator<? super K>) stream.readObject());
    valueComparator = checkNotNull((Comparator<? super V>) stream.readObject());
    setMap(new TreeMap<K, Collection<V>>(keyComparator));
    Serialization.populateMultimap(this, stream);
  }

  private static final long serialVersionUID = 0;
}
