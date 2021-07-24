
package edu.xxx.market;

import static edu.xxx.web.Utils.jsonSparkExceptionHandler;
import static edu.xxx.web.Utils.launchBrowser;
import static edu.xxx.web.Utils.logSparkRequest;
import static j2html.TagCreator.body;
import static j2html.TagCreator.button;
import static j2html.TagCreator.form;
import static j2html.TagCreator.head;
import static j2html.TagCreator.html;
import static j2html.TagCreator.img;
import static j2html.TagCreator.link;
import static j2html.TagCreator.meta;
import static j2html.TagCreator.p;
import static j2html.TagCreator.script;
import static j2html.TagCreator.span;
import static j2html.TagCreator.table;
import static j2html.TagCreator.td;
import static j2html.TagCreator.text;
import static j2html.TagCreator.tr;
import static spark.Spark.get;
import static spark.Spark.redirect;
import static spark.Spark.staticFileLocation;

import edu.xxx.util.Log;
import io.vavr.control.Option;
import java.security.SecureRandom;
import java.util.stream.IntStream;
import spark.Request;
import spark.Response;
import spark.Route;

/** Web server for your online shop. "Run" this, then point your browser at localhost:4567/funds/ */
public class MarketServer {
  private static final String TAG = "MarketServer";

  private static SecureRandom random = new SecureRandom();

  private static int funds = 150;
  private static int nFruits = 3;
  private static int[] prices = IntStream.generate(() -> 100).limit(nFruits).toArray();
  private static int[] holdings = IntStream.generate(() -> 0).limit(nFruits).toArray();

  /** Extracts the fruit index from a request. */
  private static Option<Integer> getIndex(Request request) {
    try {
      int index = Integer.parseInt(request.queryParams("index"));
      if (index < nFruits) {
        return Option.some(index);
      } else {
        Log.e(TAG, "Index parameter out of bounds.");
      }
    } catch (NumberFormatException ex) {
      Log.e(TAG, "Malformed index parameter.");
    }
    return Option.none();
  }

  /** Converts cents to a string representation in dollars. */
  private static String asDollars(int cents) {
    return String.format("$%d.%02d", cents / 100, cents % 100);
  }

  private static class TransactionHandler implements Route {
    boolean buying;

    TransactionHandler(boolean buying) {
      this.buying = buying;
    }

    @Override
    public Object handle(Request request, Response response) {
      logSparkRequest(TAG, request);
      response.header("cache-control", "no-cache"); // because we're regenerating it every time
      Option<Integer> optIndex = getIndex(request);
      return optIndex.fold(
          () -> {
            response.status(404);
            return "Invalid fruit index.";
          },
          index -> {
            response.status(200); // okay!
            int price = prices[index];
            if (buying) {
              if (funds >= price) {
                funds -= price;
                ++holdings[index];
                return "true";
              } else {
                return "false";
              }
            } else {
              if (holdings[index] > 0) {
                funds += price;
                --holdings[index];
                return "true";
              } else {
                return "false";
              }
            }
          });
    }
  }

  /** Main method to initialize the web server; args are ignored. */
  public static void main(String[] args) {
    Log.i(TAG, "Starting!");

    staticFileLocation("/WebPublic");
    jsonSparkExceptionHandler(TAG); // set up an exception handler
    launchBrowser("http://localhost:4567/"); // help users find our server

    redirect.get("/", "/market/");

    // Generates the form to view, buy, and sell.
    get(
        "/market/",
        (request, response) -> {
          logSparkRequest(TAG, request);
          response.status(200); // okay!
          response.header("cache-control", "no-cache"); // because we're regenerating it every time

          var head =
              head()
                  .with(
                      meta().withCharset("utf-8"),
                      link().withHref("market.css").withRel("stylesheet").withType("text/css"),
                      script().withType("text/javascript").withSrc("../jquery-3.3.1.min.js"),
                      script().withType("text/javascript").withSrc("market.js"));
          var body =
              body()
                  .with(
                      p().withClass("box")
                          .with(
                              text("Current funds: "),
                              span().withId("funds").withText(asDollars(funds))),
                      form()
                          .with(
                              table()
                                  .attr("cellpadding", "10")
                                  .with(
                                      // Apples
                                      tr().with(
                                              td().attr("rowspan", "3")
                                                  .with(img().withSrc("apples.jpg")),
                                              td().with(
                                                      text("x "),
                                                      span()
                                                          .withId("apple-holdings")
                                                          .withText(
                                                              Integer.toString(holdings[0])))),
                                      tr().with(
                                              td().with(
                                                      span()
                                                          .withId("apple-price")
                                                          .withText(asDollars(prices[0])),
                                                      text(" per lb."))),
                                      tr().with(
                                              td().with(
                                                      button()
                                                          .withType("button")
                                                          .withId("buy-apple")
                                                          .withClass("buy-button")
                                                          .withText("Buy")),
                                              td().with(
                                                      button()
                                                          .withType("button")
                                                          .withId("sell-apple")
                                                          .withClass("sell-button")
                                                          .withText("Sell"))),
                                      // Oranges
                                      tr().with(
                                              td().attr("rowspan", "3")
                                                  .with(img().withSrc("oranges.jpg")),
                                              td().with(
                                                      text("x "),
                                                      span()
                                                          .withId("orange-holdings")
                                                          .withText(
                                                              Integer.toString(holdings[1])))),
                                      tr().with(
                                              td().with(
                                                      span()
                                                          .withId("orange-price")
                                                          .withText(asDollars(prices[1])),
                                                      text(" per lb."))),
                                      tr().with(
                                              td().with(
                                                      button()
                                                          .withType("button")
                                                          .withId("buy-orange")
                                                          .withClass("buy-button")
                                                          .withText("Buy")),
                                              td().with(
                                                      button()
                                                          .withType("button")
                                                          .withId("sell-orange")
                                                          .withClass("sell-button")
                                                          .withText("Sell"))),
                                      // Bananas
                                      tr().with(
                                              td().attr("rowspan", "3")
                                                  .with(img().withSrc("bananas.jpg")),
                                              td().with(
                                                      text("x "),
                                                      span()
                                                          .withId("banana-holdings")
                                                          .withText(
                                                              Integer.toString(holdings[2])))),
                                      tr().with(
                                              td().with(
                                                      span()
                                                          .withId("banana-price")
                                                          .withText(asDollars(prices[2])),
                                                      text(" per lb."))),
                                      tr().with(
                                              td().with(
                                                      button()
                                                          .withType("button")
                                                          .withId("buy-banana")
                                                          .withClass("buy-button")
                                                          .withText("Buy")),
                                              td().with(
                                                      button()
                                                          .withType("button")
                                                          .withId("sell-banana")
                                                          .withClass("sell-button")
                                                          .withText("Sell"))))));
          return html().with(head, body).renderFormatted();
        });

    // Get the user's current funds.
    get(
        "/funds/",
        (request, response) -> {
          response.status(200); // okay!
          response.header("cache-control", "no-cache"); // because we're regenerating it every time
          return asDollars(funds);
        });

    // Get the current price of a certain fruit.
    get(
        "/price/",
        (request, response) -> {
          response.header("cache-control", "no-cache"); // because we're regenerating it every time
          Option<Integer> optIndex = getIndex(request);
          return optIndex.fold(
              () -> {
                response.status(404);
                return "Invalid fruit index.";
              },
              index -> {
                response.status(200); // okay!
                return asDollars(prices[index]);
              });
        });

    // Get the current number of a certain fruit.
    get(
        "/holdings/",
        (request, response) -> {
          response.header("cache-control", "no-cache"); // because we're regenerating it every time
          Option<Integer> optIndex = getIndex(request);
          return optIndex.fold(
              () -> {
                response.status(404);
                return "Invalid fruit index.";
              },
              index -> {
                response.status(200); // okay!
                return holdings[index];
              });
        });

    // Buy a fruit.
    get("/buy/", new TransactionHandler(true));

    // Sell a fruit.
    get("/sell/", new TransactionHandler(false));

    // Change prices randomly once per second.
    //noinspection InfiniteLoopStatement
    while (true) { // Keep updating the price as long as the server is running.
      try {
        for (int i = 0; i < prices.length; ++i) {
          prices[i] = 90 + random.nextInt(20);
        }
        Thread.sleep(1000);
      } catch (InterruptedException ex) {
        Log.e(TAG, ex.getMessage());
      }
    }
  }
}
