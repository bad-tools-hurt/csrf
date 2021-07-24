
package edu.xxx.web;

import static j2html.TagCreator.body;
import static j2html.TagCreator.button;
import static j2html.TagCreator.div;
import static j2html.TagCreator.form;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.head;
import static j2html.TagCreator.header;
import static j2html.TagCreator.input;
import static j2html.TagCreator.link;
import static j2html.TagCreator.meta;
import static j2html.TagCreator.nav;
import static j2html.TagCreator.script;
import static j2html.TagCreator.title;
import static spark.Spark.exception;
import static spark.Spark.notFound;

import edu.xxx.util.Log;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.control.Try;
import j2html.tags.ContainerTag;
import java.security.SecureRandom;
import org.json.JSONArray;
import org.json.JSONObject;
import spark.Request;

/** Useful helper functions for dealing with the SparkJava web server. */
public interface Utils {
  /**
   * Given a URL, tries to launch the real web browser to load that URL. If it fails, this fact is
   * logged, but it's best to treat this command as unreliable. It might work, it might fail. It
   * might log that failure, it might not.
   */
  static void launchBrowser(String url) {
    // if the remote launch fails, this will at least leave a clickable url in the log
    Log.i("SERVER URL", url);

    Try.run(() -> java.awt.Desktop.getDesktop().browse(java.net.URI.create(url)))
        .onFailure(err -> Log.e("Utils.launchBrowser", "failed to launch: " + url, err));
  }

  /** j2html helper used for making web pages that use the MUI javascript library. */
  static ContainerTag muicssHeader(String pageTitle, String jsLibrary, String cssFile) {
    return head()
        .with(
            title(pageTitle),
            meta().attr("charset", "utf-8"),
            meta().attr("http-equiv", "X-UA-Compatible").withContent("IE=edge"),
            meta().withName("viewport").withContent("width=device-width, initial-scale=1"),
            link().withHref("/mui-0.6.0/css/mui.css").withRel("stylesheet").withType("text/css"),
            link().withHref(cssFile).withRel("stylesheet").withType("text/css"),
            script().withType("text/javascript").withSrc("/mui-0.6.0/js/mui.min.js"),
            script().withType("text/javascript").withSrc("/jquery-3.3.1.min.js"))
        .condWith(!jsLibrary.isEmpty(), script().withType("text/javascript").withSrc(jsLibrary));
  }

  /** j2html helper used for making web page bodies that implement a command-line interface. */
  static ContainerTag muicssCommandLineBody(String pageTitle) {
    return muicssCommandLineBody(pageTitle, "hello", "hello");
  }

  /**
   * j2html helper used for making web page bodies that implement a command-line interface,
   * including a hidden key/value pair, using the specified key for the HTML div id and the
   * specified value in that form field.
   */
  static ContainerTag muicssCommandLineBody(
      String pageTitle, String hiddenKey, String hiddenValue) {
    return body()
        .with(
            header()
                .withId("header")
                .with(nav().withId("appbar").withClass("mui-container-fluid").with(h1(pageTitle))),
            div().withId("textOutput"),
            div()
                .withId("goButton")
                .with(button(">").withClass("mui-btn mui-btn--fab mui-btn--primary")),
            div()
                .withId("footer")
                .with(
                    textEntryForm(
                        "commandLine", "type your commands here", hiddenKey, hiddenValue)));
  }

  /**
   * j2html helper used for making text entry forms plus a hidden form field using the specified key
   * for the HTML div id, and the specified value in that form field.
   */
  static ContainerTag textEntryForm(
      String id, String placeHolder, String hiddenKey, String hiddenValue) {
    return form()
        .with(
            div()
                .withClass("mui-textfield")
                .with(
                    input()
                        .withId(id)
                        .withType("text")
                        .attr("autocomplete", "off")
                        .withPlaceholder(placeHolder)),
            input()
                .withId(hiddenKey)
                .withType("text")
                .withValue(hiddenValue)
                .attr("readonly", null) // no value, just "readonly" by itself
                .isHidden()); // not user visible, but still part of the form
  }

  /**
   * Given a Spark web server request, this converts it to a JSON format, suitable for subsequent
   * processing, or just making it easier to print the whole request in one go.
   */
  static String requestToJson(Request request) {
    return new JSONObject()
        .put("method", request.requestMethod())
        .put("url", request.url())
        .put("body", request.body())

        // The SparkJava library gives us "parameters" and "query
        // parameters". The former are elements from the URL before
        // the ? as specified with colons in the get() command
        // pattern. The latter are the set of ?foo=x,bar=y things that
        // go at the end of the URL. If you're wondering where to look
        // for your input, printing / logging of this will be helpful
        // to understanding what's going on.

        .put(
            "params",
            new JSONObject(HashMap.ofAll(request.params()).toList()))

        // The expression below is a bit convoluted. SparkJava gives
        // us a java.util.Set of queryParam strings, so we convert to
        // an iterator which is easy to convert to a list, and then we
        // can finally make a map out of that list, which we can then
        // convert back to a list of jpairs. We could probably get
        // away without the intermediate map, but it will deal with
        // any weird cases, like the same parameter showing up more
        // than once.

        .put(
            "queryParams",
            new JSONObject(List.ofAll(request.queryParams())
                .toMap(param -> param, request::queryParams)))
        .toString();
  }

  /**
   * Every hit on a Spark web server gives the lambda two arguments: a "request" and a "response".
   * The Request has lots of stuff in it. This will use the standard edu.xxx.util.Log
   * infrastructure to print out the request, which is helpful when you're trying to decipher what's
   * in a request so you can process it properly.
   *
   * @param tag The string to use for tagging the log
   * @param request The request from a Spark web server lambda
   * @see Log#i(String, Object)
   */
  static void logSparkRequest(String tag, Request request) {
    // dump the full request into the log as a JSON object, because why not?
    Log.i(tag, () -> "Spark request:\n" + requestToJson(request));
  }

  /**
   * Sets up a Spark web server exception handler. If some web request handler blows up with an
   * exception, this will cause the Spark server to (1) log the exception using the standard
   * edu.xxx.util.Log service and (2) return a JSON structure with the exception, which may be
   * useful to see from the web client.
   *
   * @param tag The string to use for tagging the log
   * @see Log#e(String, Object)
   */
  static void jsonSparkExceptionHandler(String tag) {
    exception(
        Exception.class,
        (e, request, response) -> {
          response.status(404);
          Log.e(tag, "Spark web lambda failed!", e);

          // we're going to return a JSON array where the elements
          // correspond to the stack backtrace (i.e., files and line
          // numbers from Java). Not that anybody on the browser side
          // is necessarily going to have any use for this, but it
          // might be helpful if you're looking in a browser debugger.

          response.body(
              new JSONObject()
                      .put(
                          "exception-stack-trace",
                          new JSONArray()
                              .put(List.of(e.getStackTrace())))
                      .put("exception-description", e.toString())
                  .toString());
        });

    // might as well use this excuse to put in a handler for requests that we don't understand
    notFound(
        (req, res) -> {
          logSparkRequest("Utils.notFound", req);
          res.type("application/json");
          return new JSONObject().put("message", "404 not found").toString();
        });
  }

  /**
   * Generates a large random number, delivered as a string of base-10 characters (0-9), of the
   * desired number of digits. This number will be "secure" inasmuch as the underlying {@link
   * SecureRandom} class returns numbers that are "secure" against guessing and whatnot.
   */
  static String bigRandom(int digits) {
    // Note: SecureRandom().ints() returns a Java Stream, not a VAVR Stream.
    var digitStream = new SecureRandom().ints(0, 10).limit(digits).boxed();

    // We then convert to a VAVR list, so we've got goodies like mkString()
    return List.ofAll(digitStream).mkString("");
  }
}
