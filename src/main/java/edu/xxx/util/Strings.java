
package edu.xxx.util;

import static org.unbescape.java.JavaEscape.escapeJava;

import javax.annotation.Nullable;
import org.unbescape.java.JavaEscapeLevel;

/** These static utility functions are helpful when converting arbitrary Java objects to strings. */
public interface Strings {
  /**
   * This helper function converts any object to a String, with special handling for null. All the
   * rest just get Object.toString() called on them.
   */
  static String objectToString(@Nullable Object o) {
    if (o == null) {
      return "null";
    } else {
      return o.toString();
    }
  }
}
