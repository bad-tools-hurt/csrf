"use strict";

// this doesn't happen until the DOM is instantiated
$(document).ready(function() {
    update();
    // Refresh values every second.
    window.setInterval(update, 1000);
    $("#buy-apple").on("click", function() { buy(0); })
    $("#sell-apple").on("click", function() { sell(0); })
    $("#buy-orange").on("click", function() { buy(1); })
    $("#sell-orange").on("click", function() { sell(1); })
    $("#buy-banana").on("click", function() { buy(2); })
    $("#sell-banana").on("click", function() { sell(2); })
})

// Update values.
function update() {
    console.log("Updating values from server.");
    // Get current funds.
    $.ajax({
        url: "/funds/",
        type: "GET",
        data: {},
        success: function(data) {
            console.log("success: " + data);
            document.getElementById("funds").textContent = data;
        },
        error: function(data) {
            console.log("error: " + data);
        }
    })
    // Get apple price.
    $.ajax({
        url: "/price/",
        type: "GET",
        data: {"index": 0},
        success: function(data) {
            console.log("apple price: " + data);
            document.getElementById("apple-price").textContent = data;
        },
        error: function(data) {
            console.log("error: " + data);
        }
    })
    // Get orange price.
    $.ajax({
        url: "/price/",
        type: "GET",
        data: {"index": 1},
        success: function(data) {
            console.log("orange price: " + data);
            document.getElementById("orange-price").textContent = data;
        },
        error: function(data) {
            console.log("error: " + data);
        }
    })
    // Get banana price.
    $.ajax({
        url: "/price/",
        type: "GET",
        data: {"index": 2},
        success: function(data) {
            console.log("banana price: " + data);
            document.getElementById("banana-price").textContent = data;
        },
        error: function(data) {
            console.log("error: " + data);
        }
    })
    // Get apple holdings.
    $.ajax({
        url: "/holdings/",
        type: "GET",
        data: {"index": 0},
        success: function(data) {
            console.log("apple holdings: " + data);
            document.getElementById("apple-holdings").textContent = data;
        },
        error: function(data) {
            console.log("error: " + data);
        }
    })
    // Get orange holdings.
    $.ajax({
        url: "/holdings/",
        type: "GET",
        data: {"index": 1},
        success: function(data) {
            console.log("orange holdings: " + data);
            document.getElementById("orange-holdings").textContent = data;
        },
        error: function(data) {
            console.log("error: " + data);
        }
    })
    // Get banana holdings.
    $.ajax({
        url: "/holdings/",
        type: "GET",
        data: {"index": 2},
        success: function(data) {
            console.log("banana holdings: " + data);
            document.getElementById("banana-holdings").textContent = data;
        },
        error: function(data) {
            console.log("error: " + data);
        }
    })
}

var fundBlinks = 0;
function blinkFunds(color) {
    $("#funds").css("color", color);
    ++fundBlinks;
    setTimeout(function() {
        --fundBlinks;
        if (fundBlinks == 0) {
            $("#funds").css("color", "black");
        }
    }, 2000);
}

var holdingsBlinks = [0, 0, 0];
function blinkHoldings(index, color) {
    var id;
    switch (index) {
        case 0: id = "#apple-holdings"; break;
        case 1: id = "#orange-holdings"; break;
        case 2: id = "#banana-holdings"; break;
        default: console.log("error: invalid holdings index in blinkHoldings()"); return;
    }
    $(id).css("color", color);
    ++holdingsBlinks[index];
    setTimeout(function() {
        --holdingsBlinks[index];
        if (holdingsBlinks[index] == 0) {
            $(id).css("color", "black");
        }
    }, 2000);
}

function buy(index) {
    console.log("buying")
    $.ajax({
        url: "/buy/",
        type: "GET",
        data: {"index": index},
        success: function(sufficientFunds) {
            update();
            if (JSON.parse(sufficientFunds)) {
                console.log("Bought fruit.");
                blinkFunds("#f44336");
                blinkHoldings(index, "#4CAF50");
            } else {
                alert("Not enough money!");
            }
        },
        error: function(data) {
            console.log("error: " + data);
        }
    })
    return false;
}

function sell(index) {
    console.log("selling")
    $.ajax({
        url: "/sell/",
        type: "GET",
        data: {"index": index},
        success: function(sufficientFruit) {
            update();
            if (JSON.parse(sufficientFruit)) {
                console.log("Sold fruit.");
                blinkFunds("#4CAF50");
                blinkHoldings(index, "#f44336");
            } else {
                alert("Not enough fruit!");
            }
        },
        error: function(data) {
            console.log("error: " + data);
            update();
        }
    })
    return false;
}
