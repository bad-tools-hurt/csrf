
// Engineering note: this file, and the file just like it in each of
// the package directories, specifies annotations that apply to every
// class in the package. (If we could just do this once and have it
// apply everywhere, that would have been even better, but Java
// doesn't work that way. We need to repeat these annotations once per
// Java package.)

// This annotation says that we expect that it's an error for somebody
// to pass us a null for any parameter to any method. If you tried it
// anyway, it would be highlighted by IntelliJ and would also generate
// a compile-time error from ErrrorProne. If we have an argument to a
// method where we explicitly want to override this, and allow a null
// value, we would use the @Nullable annotation.
@javax.annotation.ParametersAreNonnullByDefault

// This annotation says that if a function returns a value, you're
// expected to use it. Ignoring a function return value most commonly
// indicates a bug in your program. Of course, if a function is
// declared "void", then it returns nothing and there's no
// problem. There are also handful of functions that return something
// that is fine to ignore, and those will be tagged individually
// with @CanIgnoreReturnValue. As before, if you go ahead and do it
// anyway, both IntelliJ and ErrorProne will catch the errors.
@javax.annotation.CheckReturnValue
package edu.xxx;
