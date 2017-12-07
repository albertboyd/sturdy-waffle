/*
 * Copyright (C) 2008 The Guava Authors
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

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.concurrent.LazyInit;
import com.google.j2objc.annotations.RetainedWith;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map.Entry;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

/**
 * A {@link ListMultimap} whose contents will never change, with many other important properties
 * detailed at {@link ImmutableCollection}.
 *
 * <p>See the Guava User Guide article on <a href=
 * "https://github.com/google/guava/wiki/ImmutableCollectionsExplained"> immutable collections</a>.
 *
 * @author Jared Levy
 * @since 2.0
 */
@GwtCompatible(serializable = true, emulated = true)
public class ImmutableListMultimap<K, V> extends ImmutableMultimap<K, V>
    implements ListMultimap<K, V> {

  /** Returns the empty multimap. */
  // Casting is safe because the multimap will never hold any elements.
  @SuppressWarnings("unchecked")
  public static <K, V> ImmutableListMultimap<K, V> of() {
    return (ImmutableListMultimap<K, V>) EmptyImmutableListMultimap.INSTANCE;
  }

  /** Returns an immutable multimap containing a single entry. */
  public static <K, V> ImmutableListMultimap<K, V> of(K k1, V v1) {
    ImmutableListMultimap.Builder<K, V> builder = ImmutableListMultimap.builder();
    builder.put(k1, v1);
    return builder.build();
  }

  /** Returns an immutable multimap containing the given entries, in order. */
  public static <K, V> ImmutableListMultimap<K, V> of(K k1, V v1, K k2, V v2) {
    ImmutableListMultimap.Builder<K, V> builder = ImmutableListMultimap.builder();
    builder.put(k1, v1);
    builder.put(k2, v2);
    return builder.build();
  }

  /** Returns an immutable multimap containing the given entries, in order. */
  public static <K, V> ImmutableListMultimap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
    ImmutableListMultimap.Builder<K, V> builder = ImmutableListMultimap.builder();
    builder.put(k1, v1);
    builder.put(k2, v2);
    builder.put(k3, v3);
    return builder.build();
  }

  /** Returns an immutable multimap containing the given entries, in order. */
  public static <K, V> ImmutableListMultimap<K, V> of(
      K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
    ImmutableListMultimap.Builder<K, V> builder = ImmutableListMultimap.builder();
    builder.put(k1, v1);
    builder.put(k2, v2);
    builder.put(k3, v3);
    builder.put(k4, v4);
    return builder.build();
  }

  /** Returns an immutable multimap containing the given entries, in order. */
  public static <K, V> ImmutableListMultimap<K, V> of(
      K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
    ImmutableListMultimap.Builder<K, V> builder = ImmutableListMultimap.builder();
    builder.put(k1, v1);
    builder.put(k2, v2);
    builder.put(k3, v3);
    builder.put(k4, v4);
    builder.put(k5, v5);
    return builder.build();
  }

  // looking for of() with > 5 entries? Use the builder instead.

  /**
   * Returns a new builder. The generated builder is equivalent to the builder created by the {@link
   * Builder} constructor.
   */
  public static <K, V> Builder<K, V> builder() {
    return new Builder<>();
  }

  /**
   * A builder for creating immutable {@code ListMultimap} instances, especially {@code public
   * static final} multimaps ("constant multimaps"). Example:
   *
   * <pre>{@code
   * static final Multimap<String, Integer> STRING_TO_INTEGER_MULTIMAP =
   *     new ImmutableListMultimap.Builder<String, Integer>()
   *         .put("one", 1)
   *         .putAll("several", 1, 2, 3)
   *         .putAll("many", 1, 2, 3, 4, 5)
   *         .build();
   * }</pre>
   *
   * <p>Builder instances can be reused; it is safe to call {@link #build} multiple times to build
   * multiple multimaps in series. Each multimap contains the key-value mappings in the previously
   * created multimaps.
   *
   * @since 2.0
   */
  public static final class Builder<K, V> extends ImmutableMultimap.Builder<K, V> {
    /**
     * Creates a new builder. The returned builder is equivalent to the builder generated by {@link
     * ImmutableListMultimap#builder}.
     */
    public Builder() {}

    @CanIgnoreReturnValue
    @Override
    public Builder<K, V> put(K key, V value) {
      super.put(key, value);
      return this;
    }

    /**
     * {@inheritDoc}
     *
     * @since 11.0
     */
    @CanIgnoreReturnValue
    @Override
    public Builder<K, V> put(Entry<? extends K, ? extends V> entry) {
      super.put(entry);
      return this;
    }

    /**
     * {@inheritDoc}
     *
     * @since 19.0
     */
    @CanIgnoreReturnValue
    @Beta
    @Override
    public Builder<K, V> putAll(Iterable<? extends Entry<? extends K, ? extends V>> entries) {
      super.putAll(entries);
      return this;
    }

    @CanIgnoreReturnValue
    @Override
    public Builder<K, V> putAll(K key, Iterable<? extends V> values) {
      super.putAll(key, values);
      return this;
    }

    @CanIgnoreReturnValue
    @Override
    public Builder<K, V> putAll(K key, V... values) {
      super.putAll(key, values);
      return this;
    }

    @CanIgnoreReturnValue
    @Override
    public Builder<K, V> putAll(Multimap<? extends K, ? extends V> multimap) {
      super.putAll(multimap);
      return this;
    }

    /**
     * {@inheritDoc}
     *
     * @since 8.0
     */
    @CanIgnoreReturnValue
    @Override
    public Builder<K, V> orderKeysBy(Comparator<? super K> keyComparator) {
      super.orderKeysBy(keyComparator);
      return this;
    }

    /**
     * {@inheritDoc}
     *
     * @since 8.0
     */
    @CanIgnoreReturnValue
    @Override
    public Builder<K, V> orderValuesBy(Comparator<? super V> valueComparator) {
      super.orderValuesBy(valueComparator);
      return this;
    }

    /** Returns a newly-created immutable list multimap. */
    @Override
    public ImmutableListMultimap<K, V> build() {
      return (ImmutableListMultimap<K, V>) super.build();
    }
  }

  /**
   * Returns an immutable multimap containing the same mappings as {@code multimap}. The generated
   * multimap's key and value orderings correspond to the iteration ordering of the {@code
   * multimap.asMap()} view.
   *
   * <p>Despite the method name, this method attempts to avoid actually copying the data when it is
   * safe to do so. The exact circumstances under which a copy will or will not be performed are
   * undocumented and subject to change.
   *
   * @throws NullPointerException if any key or value in {@code multimap} is null
   */
  public static <K, V> ImmutableListMultimap<K, V> copyOf(
      Multimap<? extends K, ? extends V> multimap) {
    if (multimap.isEmpty()) {
      return of();
    }

    // TODO(lowasser): copy ImmutableSetMultimap by using asList() on the sets
    if (multimap instanceof ImmutableListMultimap) {
      @SuppressWarnings("unchecked") // safe since multimap is not writable
      ImmutableListMultimap<K, V> kvMultimap = (ImmutableListMultimap<K, V>) multimap;
      if (!kvMultimap.isPartialView()) {
        return kvMultimap;
      }
    }

    ImmutableMap.Builder<K, ImmutableList<V>> builder =
        new ImmutableMap.Builder<>(multimap.asMap().size());
    int size = 0;

    for (Entry<? extends K, ? extends Collection<? extends V>> entry :
        multimap.asMap().entrySet()) {
      ImmutableList<V> list = ImmutableList.copyOf(entry.getValue());
      if (!list.isEmpty()) {
        builder.put(entry.getKey(), list);
        size += list.size();
      }
    }

    return new ImmutableListMultimap<>(builder.build(), size);
  }

  /**
   * Returns an immutable multimap containing the specified entries. The returned multimap iterates
   * over keys in the order they were first encountered in the input, and the values for each key
   * are iterated in the order they were encountered.
   *
   * @throws NullPointerException if any key, value, or entry is null
   * @since 19.0
   */
  @Beta
  public static <K, V> ImmutableListMultimap<K, V> copyOf(
      Iterable<? extends Entry<? extends K, ? extends V>> entries) {
    return new Builder<K, V>().putAll(entries).build();
  }

  ImmutableListMultimap(ImmutableMap<K, ImmutableList<V>> map, int size) {
    super(map, size);
  }

  // views

  /**
   * Returns an immutable list of the values for the given key. If no mappings in the multimap have
   * the provided key, an empty immutable list is returned. The values are in the same order as the
   * parameters used to build this multimap.
   */
  @Override
  public ImmutableList<V> get(@NullableDecl K key) {
    // This cast is safe as its type is known in constructor.
    ImmutableList<V> list = (ImmutableList<V>) map.get(key);
    return (list == null) ? ImmutableList.<V>of() : list;
  }

  @LazyInit @RetainedWith private transient ImmutableListMultimap<V, K> inverse;

  /**
   * {@inheritDoc}
   *
   * <p>Because an inverse of a list multimap can contain multiple pairs with the same key and
   * value, this method returns an {@code ImmutableListMultimap} rather than the {@code
   * ImmutableMultimap} specified in the {@code ImmutableMultimap} class.
   *
   * @since 11.0
   */
  @Override
  public ImmutableListMultimap<V, K> inverse() {
    ImmutableListMultimap<V, K> result = inverse;
    return (result == null) ? (inverse = invert()) : result;
  }

  private ImmutableListMultimap<V, K> invert() {
    Builder<V, K> builder = builder();
    for (Entry<K, V> entry : entries()) {
      builder.put(entry.getValue(), entry.getKey());
    }
    ImmutableListMultimap<V, K> invertedMultimap = builder.build();
    invertedMultimap.inverse = this;
    return invertedMultimap;
  }

  /**
   * Guaranteed to throw an exception and leave the multimap unmodified.
   *
   * @throws UnsupportedOperationException always
   * @deprecated Unsupported operation.
   */
  @CanIgnoreReturnValue
  @Deprecated
  @Override
  public ImmutableList<V> removeAll(Object key) {
    throw new UnsupportedOperationException();
  }

  /**
   * Guaranteed to throw an exception and leave the multimap unmodified.
   *
   * @throws UnsupportedOperationException always
   * @deprecated Unsupported operation.
   */
  @CanIgnoreReturnValue
  @Deprecated
  @Override
  public ImmutableList<V> replaceValues(K key, Iterable<? extends V> values) {
    throw new UnsupportedOperationException();
  }

  /**
   * @serialData number of distinct keys, and then for each distinct key: the key, the number of
   *     values for that key, and the key's values
   */
  @GwtIncompatible // java.io.ObjectOutputStream
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    Serialization.writeMultimap(this, stream);
  }

  @GwtIncompatible // java.io.ObjectInputStream
  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    int keyCount = stream.readInt();
    if (keyCount < 0) {
      throw new InvalidObjectException("Invalid key count " + keyCount);
    }
    ImmutableMap.Builder<Object, ImmutableList<Object>> builder = ImmutableMap.builder();
    int tmpSize = 0;

    for (int i = 0; i < keyCount; i++) {
      Object key = stream.readObject();
      int valueCount = stream.readInt();
      if (valueCount <= 0) {
        throw new InvalidObjectException("Invalid value count " + valueCount);
      }

      ImmutableList.Builder<Object> valuesBuilder = ImmutableList.builder();
      for (int j = 0; j < valueCount; j++) {
        valuesBuilder.add(stream.readObject());
      }
      builder.put(key, valuesBuilder.build());
      tmpSize += valueCount;
    }

    ImmutableMap<Object, ImmutableList<Object>> tmpMap;
    try {
      tmpMap = builder.build();
    } catch (IllegalArgumentException e) {
      throw (InvalidObjectException) new InvalidObjectException(e.getMessage()).initCause(e);
    }

    FieldSettersHolder.MAP_FIELD_SETTER.set(this, tmpMap);
    FieldSettersHolder.SIZE_FIELD_SETTER.set(this, tmpSize);
  }

  @GwtIncompatible // Not needed in emulated source
  private static final long serialVersionUID = 0;
}
