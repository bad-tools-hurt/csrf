
package edu.xxx.util;

import static edu.xxx.util.Strings.objectToString;

import com.google.errorprone.annotations.FormatMethod;
import com.google.errorprone.annotations.FormatString;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a simplified version of the Android logging system (<a
 * href="http://developer.android.com/reference/android/util/Log.html">http://developer.android.com/reference/android/util/Log.html</a>)
 * that uses slf4j / <a href="https://logback.qos.ch/">Logback</a> as its backend.
 *
 * <p>Also notable: this code is extremely fast when a log level is disabled. Furthermore, you can
 * delay the computation of a log string by putting it in a lambda which supplies the string. The
 * lambda will only be called if the log level is enabled.
 *
 * <pre>
 * Log.i(TAG, "result of computation: " + result.toString()); // always computes result.toString()
 * Log.i(TAG, () -&gt; "result of computation: " + result.toString()); // more efficient when logging is disabled
 * </pre>
 *
 * <p>Also available is a string-formatting variant ({@link #iformat(String, String, Object...)} and
 * {@link #eformat(String, String, Object...)}) that acts like {@link
 * java.io.PrintStream#printf(String, Object...)} or {@link String#format(String, Object...)},
 * constructing the string to be logged only if the logging level is enabled.
 *
 * <p>There are two ways you can change the logging level. You can call {@link Log#setLogLevel(int)}
 * somewhere in your program, or you can edit the resources/logback.xml configuration, which also
 * allows you to turn on and off logging for any given tag.
 *
 * <p>See the logback configuration manual for details: <a
 * href="http://logback.qos.ch/manual/configuration.html">http://logback.qos.ch/manual/configuration.html</a>
 */
public class Log {
  private Log() {} // this class should never be instantiated

  // We need to maintain one "logger" per "tag". We keep all of that inside this loggerMap.
  private static final Map<String, Logger> loggerMap = new ConcurrentHashMap<>();

  /** logging level: everything goes into the log. */
  public static final int ALL = 1;
  /** logging level: only errors go into the log. */
  public static final int ERROR = 0;
  /** logging level: nothing is logged at all. */
  public static final int NOTHING = -1;

  private static final String TAG = "Log";
  private static int logLevel = ALL;

  static {
    i(TAG, "CompYYY log support ready!");

    var properties =
        List.of(
            "java.version",
            "java.vm.version",
            "java.runtime.name",
            "java.home",
            "java.vendor",
            "java.vm.name",
            "user.dir");

    properties.forEach(
        str -> iformat(TAG, "System property: %-17s -> %s", str, System.getProperty(str)));
  }

  private static Logger logger(String tag) {
    return loggerMap.computeIfAbsent(tag, LoggerFactory::getLogger);
  }

  /**
   * Many of the logging functions let you delay the computation of the log string, such that if
   * logging is turned off, then that computation will never need to happen. That means hiding the
   * computation inside a lambda. So far so good.
   *
   * <p>Normally, we'd just call msgFunc.get() to fetch the string behind the lambda, but what if
   * there's an exception generated in the process of returning that string? We don't want the Log
   * library to ever throw an exception. Solution? We quietly eat exceptions here and, when they do
   * occur, the ultimate log string will reflect that failure, but THE SHOW MUST GO ON!
   */
  private static String safeGet(Supplier<?> msgFunc) {
    try {
      return objectToString(msgFunc.get());
    } catch (Throwable throwable) {
      return String.format("Log string supplier failure!: %s", throwable);
    }
  }

  /**
   * Information logging. Lambda variant allows the string to be evaluated only if needed.
   *
   * @param tag String indicating which code is responsible for the log message
   * @param msgFunc Lambda providing the string or object to be logged
   * @param th Throwable, exception, error, etc. to be included in the log
   */
  public static void i(String tag, Supplier<?> msgFunc, Throwable th) {
    if (logLevel == ALL) {
      var l = logger(tag);
      if (l.isInfoEnabled()) {
        l.info(safeGet(msgFunc), th);
      }
    }
  }

  /**
   * Information logging. Lambda variant allows the string to be evaluated only if needed.
   *
   * @param tag String indicating which code is responsible for the log message
   * @param msgFunc Lambda providing the string or object to be logged
   */
  public static void i(String tag, Supplier<?> msgFunc) {
    if (logLevel == ALL) {
      var l = logger(tag);
      if (l.isInfoEnabled()) {
        l.info(safeGet(msgFunc));
      }
    }
  }

  /**
   * Information logging. Logs the message.
   *
   * @param tag String indicating which code is responsible for the log message
   * @param msg String or object to be logged
   */
  public static void i(String tag, Object msg) {
    if (logLevel == ALL) {
      var l = logger(tag);
      if (l.isInfoEnabled()) {
        l.info(objectToString(msg));
      }
    }
  }

  /**
   * Information logging with string formatting. Uses the same {@link java.util.Formatter} syntax as
   * used in {@link String#format(String, Object...)} or {@link java.io.PrintStream#printf(String,
   * Object...)} for constructing the message to be logged.
   *
   * @param tag String indicating which code is responsible for the log message
   * @param msg Formatting string to be logged
   * @param args Optional formatting arguments
   */
  @FormatMethod
  public static void iformat(String tag, @FormatString String msg, Object... args) {
    if (logLevel == ALL) {
      var l = logger(tag);
      if (l.isInfoEnabled()) {
        l.info(String.format(msg, args));
      }
    }
  }

  /**
   * Error logging. Lambda variant allows the string to be evaluated only if needed.
   *
   * @param tag String indicating which code is responsible for the log message
   * @param msgFunc Lambda providing the string or object to be logged
   */
  public static void e(String tag, Supplier<?> msgFunc) {
    if (logLevel >= ERROR) {
      var l = logger(tag);
      if (l.isErrorEnabled()) {
        l.error(safeGet(msgFunc));
      }
    }
  }

  /**
   * Error logging. Logs the message.
   *
   * @param tag String indicating which code is responsible for the log message
   * @param msg String or object to be logged
   */
  public static void e(String tag, Object msg) {
    if (logLevel >= ERROR) {
      var l = logger(tag);
      if (l.isErrorEnabled()) {
        l.error(objectToString(msg));
      }
    }
  }

  /**
   * Error logging. Lambda variant allows the string to be evaluated only if needed.
   *
   * @param tag String indicating which code is responsible for the log message
   * @param msgFunc Lambda providing the string or object to be logged
   * @param th Throwable, exception, error, etc. to be included in the log
   */
  public static void e(String tag, Supplier<?> msgFunc, Throwable th) {
    if (logLevel >= ERROR) {
      var l = logger(tag);
      if (l.isErrorEnabled()) {
        l.error(safeGet(msgFunc), th);
      }
    }
  }

  /**
   * Error logging. Logs the message.
   *
   * @param tag String indicating which code is responsible for the log message
   * @param msg String or object to be logged
   * @param th Throwable, exception, error, etc. to be included in the log
   */
  public static void e(String tag, Object msg, Throwable th) {
    if (logLevel >= ERROR) {
      var l = logger(tag);
      if (l.isErrorEnabled()) {
        l.error(objectToString(msg), th);
      }
    }
  }
}
