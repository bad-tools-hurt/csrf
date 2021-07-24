
package edu.xxx.vavr;

import io.vavr.CheckedFunction0;
import io.vavr.control.Try;
import java.util.Objects;
import java.util.function.Function;

/**
 * Helpers for VAVR's {@link io.vavr.control.Try}.
 *
 * <p>Are you looking for a "match method" for a Try? It's already built-in. It's called {@link
 * Try#fold(Function, Function)}, taking two lambdas: one for the <i>success</i> case and one for
 * the <i>failure</i> case.
 */
public interface Tries {
  /**
   * Just like {@link Try#of(CheckedFunction0)}, except if the lambda returns <code>null</code>, the
   * result is {@link Try#failed()}. Use if you're wrapping a lambda where a <code>null</code>
   * result implies a failure.
   *
   * <p>Notably, VAVR's {@link Try} is happy to have a value like <code>success(null)</code> but
   * nulls will only ever arise in CompYYY when they're returned by an external library. None of our
   * built-ins ever use null, so we don't want to allow nulls to find their way into our code.
   */
  static <T> Try<T> tryOfNullable(CheckedFunction0<? extends T> supplier) {
    return Try.narrow(
        Try.of(supplier)
            .filter(
                Objects::nonNull,
                () -> new NullPointerException("expected non-null result from supplier")));
  }
}
