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

package com.google.common.base;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;

import java.io.Serializable;
import java.util.Iterator;

import javax.annotation.Nullable;

/**
 * A function from {@code A} to {@code B} with an associated <i>reverse</i> function from {@code B}
 * to {@code A}; used for converting back and forth between <i>different representations of the same
 * information</i>.
 *
 * <h3>Invertibility</h3>
 *
 * <p>The reverse operation <b>may</b> be a strict <i>inverse</i> (meaning that {@code
 * converter.reverse().convert(converter.convert(a)).equals(a)} is always true). However, it is
 * very common (perhaps <i>more</i> common) for round-trip conversion to be <i>lossy</i>. Consider
 * an example round-trip using {@link com.google.common.primitives.Doubles#stringConverter}:
 *
 * <ol>
 * <li>{@code stringConverter().convert("1.00")} returns the {@code Double} value {@code 1.0}
 * <li>{@code stringConverter().reverse().convert(1.0)} returns the string {@code "1.0"} --
 *     <i>not</i> the same string ({@code "1.00"}) we started with
 * </ol>
 *
 * <p>Note that it should still be the case that the round-tripped and original objects are
 * <i>similar</i>.
 *
 * <h3>Nullability</h3>
 *
 * <p>A converter always converts {@code null} to {@code null} and non-null references to non-null
 * references. It would not make sense to consider {@code null} and a non-null reference to be
 * "different representations of the same information", since one is distinguishable from
 * <i>missing</i> information and the other is not. The {@link #convert} method handles this null
 * behavior for all converters; implementations of {@link #doForward} and {@link #doBackward} are
 * guaranteed to never be passed {@code null}, and must never return {@code null}.
 *

 * <h3>Common ways to use</h3>
 *
 * <p>Creating a converter:
 *
 * <ul>
 * <li>Extend this class and override {@link #doForward} and {@link #doBackward}
 * </ul>
 *
 * <p>Using a converter:
 *
 * <ul>
 * <li>Convert one instance in the "forward" direction using {@code converter.convert(a)}
 * <li>Convert multiple instances "forward" using {@code converter.convertAll(as)}
 * <li>Convert in the "backward" direction using {@code converter.reverse().convert(b)} or {@code
 *     converter.reverse().convertAll(bs)}
 * <li>Use {@code converter} or {@code converter.reverse()} anywhere a {@link Function} is accepted
 * </ul>
 *
 * @author Mike Ward
 * @author Kurt Alfred Kluever
 * @author Gregory Kick
 * @since 16.0
 */
@Beta
@GwtCompatible
public abstract class Converter<A, B> implements Function<A, B> {
  private final boolean handleNullAutomatically;

  /** Constructor for use by subclasses. */
  protected Converter() {
    this(true);
  }

  /**
   * Constructor used only by {@code LegacyConverter} to suspend automatic null-handling.
   */
  Converter(boolean handleNullAutomatically) {
    this.handleNullAutomatically = handleNullAutomatically;
  }

  // SPI methods (what subclasses must implement)

  /**
   * Returns a representation of {@code a} as an instance of type {@code B}. If {@code a} cannot be
   * converted, an unchecked exception (such as {@link IllegalArgumentException}) should be thrown.
   *
   * @param a the instance to convert; will never be null
   * @return the converted instance; <b>must not</b> be null
   */
  protected abstract B doForward(A a);

  /**
   * Returns a representation of {@code b} as an instance of type {@code A}. If {@code b} cannot be
   * converted, an unchecked exception (such as {@link IllegalArgumentException}) should be thrown.
   *
   * @param b the instance to convert; will never be null
   * @return the converted instance; <b>must not</b> be null
   * @throws UnsupportedOperationException if backward conversion is not implemented; this should be
   *     very rare. Note that if backward conversion is not only unimplemented but
   *     unimplement<i>able</i> (for example, consider a {@code Converter<Chicken, ChickenNugget>}),
   *     then this is not logically a {@code Converter} at all, and should just implement {@link
   *     Function}.
   */
  protected abstract A doBackward(B b);

  // API (consumer-side) methods

  /**
   * Returns a representation of {@code a} as an instance of type {@code B}.
   *
   * @return the converted value; is null <i>if and only if</i> {@code a} is null
   */
  @Nullable public final B convert(@Nullable A a) {
    return correctedDoForward(a);
  }

  B correctedDoForward(A a) {
    if (handleNullAutomatically) {
      // TODO(kevinb): we shouldn't be checking for a null result at runtime. Assert?
      return a == null ? null : checkNotNull(doForward(a));
    } else {
      return doForward(a);
    }
  }

  A correctedDoBackward(B b) {
    if (handleNullAutomatically) {
      // TODO(kevinb): we shouldn't be checking for a null result at runtime. Assert?
      return b == null ? null : checkNotNull(doBackward(b));
    } else {
      return doBackward(b);
    }
  }

  /**
   * Returns an iterable that applies {@code convert} to each element of {@code fromIterable}. The
   * conversion is done lazily.
   *
   * <p>The returned iterable's iterator supports {@code remove()} if the input iterator does. After
   * a successful {@code remove()} call, {@code fromIterable} no longer contains the corresponding
   * element.
   */
  public Iterable<B> convertAll(final Iterable<? extends A> fromIterable) {
    checkNotNull(fromIterable, "fromIterable");
    return new Iterable<B>() {
      @Override public Iterator<B> iterator() {
        return new Iterator<B>() {
          private final Iterator<? extends A> fromIterator = fromIterable.iterator();
          @Override public boolean hasNext() {
            return fromIterator.hasNext();
          }
          @Override public B next() {
            return convert(fromIterator.next());
          }
          @Override public void remove() {
            fromIterator.remove();
          }
        };
      }
    };
  }

  /**
   * Returns the reversed view of this converter, which converts {@code this.convert(a)} back to a
   * value roughly equivalent to {@code a}.
   *
   * <p>The returned converter is serializable if {@code this} converter is.
   */
  public Converter<B, A> reverse() {
    return new ReverseConverter<A, B>(this);
  }

  private static final class ReverseConverter<A, B>
      extends Converter<B, A> implements Serializable {
    final Converter<A, B> original;

    ReverseConverter(Converter<A, B> original) {
      // Rely on backing converter to handle null if desired, not us.
      // Actually, since we override correctedDo*, nothing will use this field now anyway.
      super(false);
      this.original = original;
    }

    /*
     * These gymnastics are a little confusing. Basically this class has neither legacy nor
     * non-legacy behavior; it just needs to let the behavior of the backing converter shine
     * through. So, we override the correctedDo* methods, after which the do* methods should never
     * be reached.
     */

    @Override protected A doForward(@Nullable B b) {
      throw new AssertionError();
    }

    @Override protected B doBackward(@Nullable A a) {
      throw new AssertionError();
    }

    @Override A correctedDoForward(B b) {
      return original.correctedDoBackward(b);
    }

    @Override B correctedDoBackward(A a) {
      return original.correctedDoForward(a);
    }

    @Override public Converter<A, B> reverse() {
      return original;
    }

    @Override public boolean equals(@Nullable Object object) {
      if (object instanceof ReverseConverter) {
        ReverseConverter<?, ?> that = (ReverseConverter<?, ?>) object;
        return this.original.equals(that.original);
      }
      return false;
    }

    @Override public int hashCode() {
      return ~original.hashCode();
    }

    @Override public String toString() {
      return original + ".reverse()";
    }

    private static final long serialVersionUID = 0L;
  }

  /**
   * Returns a converter whose {@code convert} method applies {@code secondConverter} to the result
   * of this converter. Its {@code reverse} method applies the converters in reverse order.
   *
   * <p>The returned converter is serializable if {@code this} converter and {@code secondConverter}
   * are.
   */
  public <C> Converter<A, C> andThen(Converter<B, C> secondConverter) {
    return ConverterComposition.of(this, checkNotNull(secondConverter, "secondConverter"));
  }

  private static final class ConverterComposition<A, B, C>
      extends Converter<A, C> implements Serializable {
    final Converter<A, B> first;
    final Converter<B, C> second;

    ConverterComposition(Converter<A, B> first, Converter<B, C> second) {
      // Rely on backing converter to handle null if desired, not us.
      // Actually, since we override correctedDo*, nothing will use this field now anyway.
      super(false);
      this.first = first;
      this.second = second;
    }

    /*
     * These gymnastics are a little confusing. Basically this class has neither legacy nor
     * non-legacy behavior; it just needs to let the behaviors of the backing converters shine
     * through (which might even differ from each other!). So, we override the correctedDo* methods,
     * after which the do* methods should never be reached.
     */

    @Override protected C doForward(@Nullable A a) {
      throw new AssertionError();
    }

    @Override protected A doBackward(@Nullable C c) {
      throw new AssertionError();
    }

    @Override C correctedDoForward(@Nullable A a) {
      return second.correctedDoForward(first.correctedDoForward(a));
    }

    @Override A correctedDoBackward(@Nullable C c) {
      return first.correctedDoBackward(second.correctedDoBackward(c));
    }

    @Override public boolean equals(@Nullable Object object) {
      if (object instanceof ConverterComposition) {
        ConverterComposition<?, ?, ?> that = (ConverterComposition<?, ?, ?>) object;
        return this.first.equals(that.first)
            && this.second.equals(that.second);
      }
      return false;
    }

    @Override public int hashCode() {
      return 31 * first.hashCode() + second.hashCode();
    }

    @Override public String toString() {
      return first + ".andThen(" + second + ")";
    }

    static <A, B, C> Converter<A, C> of(Converter<A, B> first, Converter<B, C> second) {
      return new ConverterComposition<A, B, C>(first, second);
    }

    private static final long serialVersionUID = 0L;
  }

  /**
   * @deprecated Provided to satisfy the {@code Function} interface; use {@link #convert} instead.
   */
  @Deprecated
  @Override
  @Nullable public final B apply(@Nullable A a) {
    return convert(a);
  }

  /**
   * Indicates whether another object is equal to this converter.
   *
   * <p>Most implementations will have no reason to override the behavior of {@link Object#equals}.
   * However, an implementation may also choose to return {@code true} whenever {@code object} is a
   * {@link Converter} that it considers <i>interchangeable</i> with this one. "Interchangeable"
   * <i>typically</i> means that {@code Objects.equal(this.convert(a), that.convert(a))} is true for
   * all {@code a} of type {@code A} (and similarly for {@code reverse}). Note that a {@code false}
   * result from this method does not imply that the converters are known <i>not</i> to be
   * interchangeable.
   */
  @Override
  public boolean equals(@Nullable Object object) {
    return super.equals(object);
  }

  // Static converters

  private static final class FunctionBasedConverter<A, B>
      extends Converter<A, B> implements Serializable {
    private final Function<? super A, ? extends B> forwardFunction;
    private final Function<? super B, ? extends A> backwardFunction;

    private FunctionBasedConverter(
        Function<? super A, ? extends B> forwardFunction,
        Function<? super B, ? extends A> backwardFunction) {
      this.forwardFunction = checkNotNull(forwardFunction);
      this.backwardFunction = checkNotNull(backwardFunction);
    }

    @Override protected B doForward(A a) {
      return forwardFunction.apply(a);
    }

    @Override protected A doBackward(B b) {
      return backwardFunction.apply(b);
    }

    @Override public boolean equals(@Nullable Object object) {
      if (object instanceof FunctionBasedConverter) {
        FunctionBasedConverter<?, ?> that = (FunctionBasedConverter<?, ?>) object;
        return this.forwardFunction.equals(that.forwardFunction)
            && this.backwardFunction.equals(that.backwardFunction);
      }
      return false;
    }

    @Override public int hashCode() {
      return forwardFunction.hashCode() * 31 + backwardFunction.hashCode();
    }

    @Override public String toString() {
      return "Converter.from(" + forwardFunction + ", " + backwardFunction + ")";
    }
  }

  /**
   * Returns a serializable converter that always converts or reverses an object to itself.
   */
  @SuppressWarnings("unchecked") // implementation is "fully variant"
  public static <T> Converter<T, T> identity() {
    return (IdentityConverter<T>) IdentityConverter.INSTANCE;
  }

  /**
   * A converter that always converts or reverses an object to itself. Note that T is now a
   * "pass-through type".
   */
  private static final class IdentityConverter<T> extends Converter<T, T> implements Serializable {
    static final IdentityConverter INSTANCE = new IdentityConverter();

    @Override protected T doForward(@Nullable T t) {
      return t;
    }

    @Override protected T doBackward(@Nullable T t) {
      return t;
    }

    @Override public IdentityConverter<T> reverse() {
      return this;
    }

    @Override public <S> Converter<T, S> andThen(Converter<T, S> otherConverter) {
      return checkNotNull(otherConverter, "otherConverter");
    }

    /*
     * We *could* override convertAll() to return its input, but it's a rather pointless
     * optimization and opened up a weird type-safety problem.
     */

    @Override public String toString() {
      return "Converter.identity()";
    }

    private Object readResolve() {
      return INSTANCE;
    }

    private static final long serialVersionUID = 0L;
  }
}
