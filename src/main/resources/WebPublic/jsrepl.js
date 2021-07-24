
"use strict";

// this doesn't happen until the DOM is instantiated
$(document).ready(() => {
    printParagraph("<i><u>JavaScript evaluator initialized, ready to rock</u></i>");

    // Engineering note: there are two interesting things going on here that
    // we didn't talk about in the JavaScript lecture. First, the dollar-sign
    // function is actually JQuery. There's nothing particularly special about
    // a dollar-sign, so JQuery does everything with it. Here, it's looking up
    // buttons and widgets in the HTML document object model. (This is described
    // in the Friday, Week 11 lecture.)

    // Also of note: a different form of lambda than the original lambdas from
    // the early days of JavaScript. This form, with the => arrow works similarly
    // to the lambdas that we use in Java. This was introduced in JavaScript's
    // ES6 (EcmaScript), in 2015.

    $("#goButton").on("click", fetchQuery);
    $("#commandLine").keydown(event => {
        if(event.keyCode === 13) {
            event.preventDefault(); // prevent carriage-return from triggering a page reload
            fetchQuery()
        }
    })
});

function fetchQuery() {
    let commandLine = $("#commandLine");
    let savedText = commandLine.val();
    commandLine.val("");

    dispatchQuery(savedText)
}

function printParagraph(text) {
    let textBox = $("#textOutput");
    textBox.append("<p>" + text + "</p>"); // cross-site scripting opportunity!
    textBox.scrollTop(textBox.prop("scrollHeight")); // scroll to the bottom
}

function dispatchQuery(input) {
    let key = $("#accessKey").val();
    console.log("dispatching query: " + input);
    $.ajax( {
        url: "/jseval/",
        type: "GET",
        data: {
            'key': key,
            'input': input
        },
        success: data => {
            console.log("success: " + data);
            printParagraph(JSON.parse(data).response)
        },
        error: data => {
            console.log("error: " + data);
            printParagraph(JSON.parse(data).response)
        }
    })
}

