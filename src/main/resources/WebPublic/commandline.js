
"use strict";

let savedServerUrl = "/lowercase/";

function setupCommandLine(name, serverUrl) {
    $(document).ready(function () {
        // this doesn't happen until the DOM is instantiated
        printParagraph("<i><u>" + name + " initialized, ready to rock</u></i>");

        $("#goButton").on("click", fetchQuery);
        $("#commandLine").keydown(event => {
            if (event.keyCode === 13) {
                event.preventDefault(); // prevent carriage-return from triggering a page reload
                fetchQuery()
            }
        });

        savedServerUrl = serverUrl
    });
}

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
    console.log("dispatching query: " + input);
    $.ajax( {
        url: savedServerUrl,
        type: "GET",
        data: {'input': input},
        success: data => {
            console.log("success: " + data);
            printParagraph(JSON.parse(data).response)
        },
        error: data => {
            console.log("error: " + data);
            printParagraph('<b>Bah! ' + data + ' error!</b>')
        }
    })
}

