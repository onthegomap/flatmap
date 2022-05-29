package com.onthegomap.planetiler.collection;

import java.util.Iterator;
import java.util.function.Supplier;

/**
 * A {@link Supplier} that returns {@code null} when there are no elements left, with an {@link Iterable} view to
 * support for each loop.
 *
 * @param <T> Type of element returned
 */
public interface IterableOnce<T> extends Iterable<T>, Supplier<T> {

  @Override
  default Iterator<T> iterator() {
    return new SupplierIterator<>(this);
  }
}
