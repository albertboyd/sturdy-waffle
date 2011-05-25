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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Collections2.TransformedCollection;
import com.google.common.collect.Table.Cell;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * Provides static methods that involve a {@code Table}.
 *
 * @author Jared Levy
 * @since Guava release 07
 */
@GwtCompatible
@Beta
public final class Tables {
  private Tables() {}

  /**
   * Returns an immutable cell with the specified row key, column key, and
   * value.
   *
   * <p>The returned cell is serializable.
   *
   * @param rowKey the row key to be associated with the returned cell
   * @param columnKey the column key to be associated with the returned cell
   * @param value the value to be associated with the returned cell
   */
  public static <R, C, V> Cell<R, C, V> immutableCell(
      @Nullable R rowKey, @Nullable C columnKey, @Nullable V value) {
    return new ImmutableCell<R, C, V>(rowKey, columnKey, value);
  }

  private static class ImmutableCell<R, C, V>
      extends AbstractCell<R, C, V> implements Serializable {
    final R rowKey;
    final C columnKey;
    final V value;

    ImmutableCell(
        @Nullable R rowKey, @Nullable C columnKey, @Nullable V value) {
      this.rowKey = rowKey;
      this.columnKey = columnKey;
      this.value = value;
    }

    @Override
    public R getRowKey() {
      return rowKey;
    }
    @Override
    public C getColumnKey() {
      return columnKey;
    }
    @Override
    public V getValue() {
      return value;
    }

    private static final long serialVersionUID = 0;
  }

  abstract static class AbstractCell<R, C, V> implements Cell<R, C, V> {
    // needed for serialization
    AbstractCell() {}

    @Override public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (obj instanceof Cell) {
        Cell<?, ?, ?> other = (Cell<?, ?, ?>) obj;
        return Objects.equal(getRowKey(), other.getRowKey())
            && Objects.equal(getColumnKey(), other.getColumnKey())
            && Objects.equal(getValue(), other.getValue());
      }
      return false;
    }

    @Override public int hashCode() {
      return Objects.hashCode(getRowKey(), getColumnKey(), getValue());
    }

    @Override public String toString() {
      return "(" + getRowKey() + "," + getColumnKey() + ")=" + getValue();
    }
  }

  /**
   * Creates a transposed view of a given table that flips its row and column
   * keys. In other words, calling {@code get(columnKey, rowKey)} on the
   * generated table always returns the same value as calling {@code
   * get(rowKey, columnKey)} on the original table. Updating the original table
   * changes the contents of the transposed table and vice versa.
   *
   * <p>The returned table supports update operations as long as the input table
   * supports the analogous operation with swapped rows and columns. For
   * example, in a {@link HashBasedTable} instance, {@code
   * rowKeySet().iterator()} supports {@code remove()} but {@code
   * columnKeySet().iterator()} doesn't. With a transposed {@link
   * HashBasedTable}, it's the other way around.
   */
  public static <R, C, V> Table<C, R, V> transpose(Table<R, C, V> table) {
    return (table instanceof TransposeTable)
        ? ((TransposeTable<R, C, V>) table).original
        : new TransposeTable<C, R, V>(table);
  }

  private static class TransposeTable<C, R, V> implements Table<C, R, V> {
    final Table<R, C, V> original;

    TransposeTable(Table<R, C, V> original) {
      this.original = checkNotNull(original);
    }

    @Override
    public void clear() {
      original.clear();
    }

    @Override
    public Map<C, V> column(R columnKey) {
      return original.row(columnKey);
    }

    @Override
    public Set<R> columnKeySet() {
      return original.rowKeySet();
    }

    @Override
    public Map<R, Map<C, V>> columnMap() {
      return original.rowMap();
    }

    @Override
    public boolean contains(
        @Nullable Object rowKey, @Nullable Object columnKey) {
      return original.contains(columnKey, rowKey);
    }

    @Override
    public boolean containsColumn(@Nullable Object columnKey) {
      return original.containsRow(columnKey);
    }

    @Override
    public boolean containsRow(@Nullable Object rowKey) {
      return original.containsColumn(rowKey);
    }

    @Override
    public boolean containsValue(@Nullable Object value) {
      return original.containsValue(value);
    }

    @Override
    public V get(@Nullable Object rowKey, @Nullable Object columnKey) {
      return original.get(columnKey, rowKey);
    }

    @Override
    public boolean isEmpty() {
      return original.isEmpty();
    }

    @Override
    public V put(C rowKey, R columnKey, V value) {
      return original.put(columnKey, rowKey, value);
    }

    @Override
    public void putAll(Table<? extends C, ? extends R, ? extends V> table) {
      original.putAll(transpose(table));
    }

    @Override
    public V remove(@Nullable Object rowKey, @Nullable Object columnKey) {
      return original.remove(columnKey, rowKey);
    }

    @Override
    public Map<R, V> row(C rowKey) {
      return original.column(rowKey);
    }

    @Override
    public Set<C> rowKeySet() {
      return original.columnKeySet();
    }

    @Override
    public Map<C, Map<R, V>> rowMap() {
      return original.columnMap();
    }

    @Override
    public int size() {
      return original.size();
    }

    @Override
    public Collection<V> values() {
      return original.values();
    }

    @Override public boolean equals(@Nullable Object obj) {
      if (obj == this) {
        return true;
      }
      if (obj instanceof Table) {
        Table<?, ?, ?> other = (Table<?, ?, ?>) obj;
        return cellSet().equals(other.cellSet());
      }
      return false;
    }

    @Override public int hashCode() {
      return cellSet().hashCode();
    }

    @Override public String toString() {
      return rowMap().toString();
    }

    // Will cast TRANSPOSE_CELL to a type that always succeeds
    private static final Function<Cell<?, ?, ?>, Cell<?, ?, ?>> TRANSPOSE_CELL =
        new Function<Cell<?, ?, ?>, Cell<?, ?, ?>>() {
          @Override
          public Cell<?, ?, ?> apply(Cell<?, ?, ?> cell) {
            return immutableCell(
                cell.getColumnKey(), cell.getRowKey(), cell.getValue());
          }
        };

    CellSet cellSet;

    @Override
    public Set<Cell<C, R, V>> cellSet() {
      CellSet result = cellSet;
      return (result == null) ? cellSet = new CellSet() : result;
    }

    class CellSet extends TransformedCollection<Cell<R, C, V>, Cell<C, R, V>>
        implements Set<Cell<C, R, V>> {
      // Casting TRANSPOSE_CELL to a type that always succeeds
      @SuppressWarnings("unchecked")
      CellSet() {
        super(original.cellSet(), (Function) TRANSPOSE_CELL);
      }

      @Override public boolean equals(Object obj) {
        if (obj == this) {
          return true;
        }
        if (!(obj instanceof Set)) {
          return false;
        }
        Set<?> os = (Set<?>) obj;
        if (os.size() != size()) {
          return false;
        }
        return containsAll(os);
      }

      @Override public int hashCode() {
        return Sets.hashCodeImpl(this);
      }

      @Override public boolean contains(Object obj) {
        if (obj instanceof Cell) {
          Cell<?, ?, ?> cell = (Cell<?, ?, ?>) obj;
          return original.cellSet().contains(immutableCell(
              cell.getColumnKey(), cell.getRowKey(), cell.getValue()));
        }
        return false;
      }

      @Override public boolean remove(Object obj) {
        if (obj instanceof Cell) {
          Cell<?, ?, ?> cell = (Cell<?, ?, ?>) obj;
          return original.cellSet().remove(immutableCell(
              cell.getColumnKey(), cell.getRowKey(), cell.getValue()));
        }
        return false;
      }
    }
  }

  /**
   * Returns a view of a table where each value is transformed by a function.
   * All other properties of the table, such as iteration order, are left
   * intact.
   *
   * <p>Changes in the underlying table are reflected in this view. Conversely,
   * this view supports removal operations, and these are reflected in the
   * underlying table.
   *
   * <p>It's acceptable for the underlying table to contain null keys, and even
   * null values provided that the function is capable of accepting null input.
   * The transformed table might contain null values, if the function sometimes
   * gives a null result.
   *
   * <p>The returned table is not thread-safe or serializable, even if the
   * underlying table is.
   *
   * <p>The function is applied lazily, invoked when needed. This is necessary
   * for the returned table to be a view, but it means that the function will be
   * applied many times for bulk operations like {@link Table#containsValue} and
   * {@code Table.toString()}. For this to perform well, {@code function} should
   * be fast. To avoid lazy evaluation when the returned table doesn't need to
   * be a view, copy the returned table into a new table of your choosing.
   *
   * @since Guava release 10
   */
  public static <R, C, V1, V2> Table<R, C, V2> transformValues(
      Table<R, C, V1> fromTable, Function<? super V1, V2> function) {
    return new TransformedTable<R, C, V1, V2>(fromTable, function);
  }

  private static class TransformedTable<R, C, V1, V2> implements Table<R, C, V2> {
    final Table<R, C, V1> fromTable;
    final Function<? super V1, V2> function;

    TransformedTable(Table<R, C, V1> fromTable, Function<? super V1, V2> function) {
      this.fromTable = checkNotNull(fromTable);
      this.function = checkNotNull(function);
    }

    @Override public boolean contains(Object rowKey, Object columnKey) {
      return fromTable.contains(rowKey, columnKey);
    }

    @Override public boolean containsRow(Object rowKey) {
      return fromTable.containsRow(rowKey);
    }

    @Override public boolean containsColumn(Object columnKey) {
      return fromTable.containsColumn(columnKey);
    }

    @Override public boolean containsValue(Object value) {
      return values().contains(value);
    }

    @Override public V2 get(Object rowKey, Object columnKey) {
      // The function is passed a null input only when the table contains a null
      // value.
      return contains(rowKey, columnKey)
          ? function.apply(fromTable.get(rowKey, columnKey)) : null;
    }

    @Override public boolean isEmpty() {
      return fromTable.isEmpty();
    }

    @Override public int size() {
      return fromTable.size();
    }

    @Override public void clear() {
      fromTable.clear();
    }

    @Override public V2 put(R rowKey, C columnKey, V2 value) {
      throw new UnsupportedOperationException();
    }

    @Override public void putAll(Table<? extends R, ? extends C, ? extends V2> table) {
      throw new UnsupportedOperationException();
    }

    @Override public V2 remove(Object rowKey, Object columnKey) {
      return contains(rowKey, columnKey)
          ? function.apply(fromTable.remove(rowKey, columnKey)) : null;
   }

    @Override public Map<C, V2> row(R rowKey) {
      return Maps.transformValues(fromTable.row(rowKey), function);
    }

    @Override public Map<R, V2> column(C columnKey) {
      return Maps.transformValues(fromTable.column(columnKey), function);
    }

    Function<Cell<R, C, V1>, Cell<R, C, V2>> cellFunction() {
      return new Function<Cell<R, C, V1>, Cell<R, C, V2>>() {
        @Override public Cell<R, C, V2> apply(Cell<R, C, V1> cell) {
          return immutableCell(
              cell.getRowKey(), cell.getColumnKey(), function.apply(cell.getValue()));
        }
      };
    }

    class CellSet extends TransformedCollection<Cell<R, C, V1>, Cell<R, C, V2>>
        implements Set<Cell<R, C, V2>> {
      CellSet() {
        super(fromTable.cellSet(), cellFunction());
      }
      @Override public boolean equals(Object obj) {
        return Sets.equalsImpl(this, obj);
      }
      @Override public int hashCode() {
        return Sets.hashCodeImpl(this);
      }
      @Override public boolean contains(Object obj) {
         if (obj instanceof Cell) {
           Cell<?, ?, ?> cell = (Cell<?, ?, ?>) obj;
           return Objects.equal(cell.getValue(), get(cell.getRowKey(), cell.getColumnKey()))
               && (cell.getValue() != null
                   || fromTable.contains(cell.getRowKey(), cell.getColumnKey()));
         }
         return false;
      }
      @Override public boolean remove(Object obj) {
        if (contains(obj)) {
          Cell<?, ?, ?> cell = (Cell<?, ?, ?>) obj;
          fromTable.remove(cell.getRowKey(), cell.getColumnKey());
          return true;
        }
        return false;
      }
    }

    CellSet cellSet;

    @Override public Set<Cell<R, C, V2>> cellSet() {
      return (cellSet == null) ? cellSet = new CellSet() : cellSet;
    }

    @Override public Set<R> rowKeySet() {
      return fromTable.rowKeySet();
    }

    @Override public Set<C> columnKeySet() {
      return fromTable.columnKeySet();
    }

    Collection<V2> values;

    @Override public Collection<V2> values() {
      return (values == null)
          ? values = Collections2.transform(fromTable.values(), function)
          : values;
    }

    Map<R, Map<C, V2>> createRowMap() {
      Function<Map<C, V1>, Map<C, V2>> rowFunction = new Function<Map<C, V1>, Map<C, V2>>() {
        @Override public Map<C, V2> apply(Map<C, V1> row) {
          return Maps.transformValues(row, function);
        }
      };
      return Maps.transformValues(fromTable.rowMap(), rowFunction);
    }

    Map<R, Map<C, V2>> rowMap;

    @Override public Map<R, Map<C, V2>> rowMap() {
      return (rowMap == null) ? rowMap = createRowMap() : rowMap;
    }

    Map<C, Map<R, V2>> createColumnMap() {
      Function<Map<R, V1>, Map<R, V2>> columnFunction = new Function<Map<R, V1>, Map<R, V2>>() {
        @Override public Map<R, V2> apply(Map<R, V1> column) {
          return Maps.transformValues(column, function);
        }
      };
      return Maps.transformValues(fromTable.columnMap(), columnFunction);
    }

    Map<C, Map<R, V2>> columnMap;

    @Override public Map<C, Map<R, V2>> columnMap() {
      return (columnMap == null) ? columnMap = createColumnMap() : columnMap;
    }

    @Override public boolean equals(@Nullable Object obj) {
      if (obj == this) {
        return true;
      }
      if (obj instanceof Table) {
        Table<?, ?, ?> other = (Table<?, ?, ?>) obj;
        return cellSet().equals(other.cellSet());
      }
      return false;
    }

    @Override public int hashCode() {
      return cellSet().hashCode();
    }

    @Override public String toString() {
      return rowMap().toString();
    }
  }
}
