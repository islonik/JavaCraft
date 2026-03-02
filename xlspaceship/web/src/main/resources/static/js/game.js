// we wait till page is loaded
document.addEventListener("DOMContentLoaded", (event) => {
    console.log("Page loaded.");

    updatePageAfterAiFirstTurn();

});

function updatePageAfterAiFirstTurn() {
    let opponentId = document.getElementById("opponentId").value;
    let playerTurnId = document.getElementById("playerTurnId").value;
    let gameId = document.getElementById("gameId").value;

    if (opponentId === playerTurnId) { // AI made first turn so we need to update our page
        updateMyGrid(gameId);
    }
}

function addShot(event) {
    let userId = document.getElementById("userId").value;
    let playerTurnId = document.getElementById("playerTurnId").value;

    if (userId == playerTurnId) { // ignore mouse clicks while it's not your turn
        let aliveShips = document.getElementById("aliveShips").value;

        let imgId = event.id;

        document.getElementById(imgId).removeAttribute("onclick");
        document.getElementById(imgId).setAttribute("class", "shot");

        // get salvo
        let salvo = document.getElementById("salvo").value;
        salvo = salvo + imgId;

        // get salvoCount
        let salvoCount = parseInt(document.getElementById("salvoCount").value);
        // update salvoCount
        salvoCount = salvoCount + 1;
        // save salvoCount
        document.getElementById("salvoCount").value = salvoCount;

        if (salvoCount >= aliveShips) {
            // send required salvo to backend
            sendFireRequest(salvo);
            // reset salvoCount
            document.getElementById("salvoCount").value = 0;
        } else {
            // update salve
            salvo = salvo + ",";
        }
        // save salvo
        document.getElementById("salvo").value = salvo;
    }
}

function sendFireRequest(salvo) {
    let gameId = document.getElementById("gameId").value;

    let array = new Array();
    array = salvo.split(",");

    let fireRequest = {}
    fireRequest["salvo"] = array;

    let httpRequest = new XMLHttpRequest();

    httpRequest.onreadystatechange = function() {
        if (httpRequest.readyState == XMLHttpRequest.DONE) { // XMLHttpRequest.DONE == 4
            if (httpRequest.status == 200) {
                let fireResponse = JSON.parse(httpRequest.responseText);
                let game = fireResponse.game;
                let salvo = fireResponse.salvo;

                // update opponent grid
                for (const [key, value] of Object.entries(salvo)) {
                    if (value === "hit" || value === "kill") { // from backend
                        document.getElementById(key).setAttribute("class", "sunk");
                    }
                }

                updateGameTurn(gameId, game, false);

                // drop salvo value to empty
                document.getElementById("salvo").value = "";

                if (game.player_turn === 'AI') {
                    updateMyGrid(gameId);
                }
            } else {
                let jsonError = JSON.parse(httpRequest.responseText);
                alert(jsonError.error_message); // show message
                // very crude approach as we are trying to resolve it with page update
                window.location.href = "/gameId/" + gameId;
            }
        }
    };

    httpRequest.open("PUT", "/xl-spaceship/user/game/" + gameId + "/fire", true);
    httpRequest.setRequestHeader('Content-Type', 'application/json; charset=utf-8');
    httpRequest.send(JSON.stringify(fireRequest));
}

function updateMyGrid(gameId) {
    setTimeout(() => {
        fetchGameStatus(gameId);
    }, 50); // update my grid after 50 ms delay
}

function fetchGameStatus(gameId) {
    let httpRequest = new XMLHttpRequest();
    httpRequest.onreadystatechange = function() {
        if (httpRequest.readyState == XMLHttpRequest.DONE) { // XMLHttpRequest.DONE == 4
            if (httpRequest.status == 200) {
                let gameStatus = JSON.parse(httpRequest.responseText);

                let myGrid = document.getElementById('myGrid');
                myGrid.innerHTML = gameStatus.grid;

                updateGameTurn(gameId, gameStatus.game, true);

                if (gameStatus.aliveShips) { // check is not nullish
                    document.getElementById("aliveShips").value = gameStatus.aliveShips;
                }
            } else {
                let jsonError = JSON.parse(httpRequest.responseText);
                alert(jsonError.error_message); // show message
                // very crude approach as we are trying to resolve it with page update
                window.location.href = "/gameId/" + gameId;
            }
        }
    };

    httpRequest.open("GET", "/xl-spaceship/user/game/" + gameId + "/status");
    httpRequest.setRequestHeader('Content-Type', 'application/json; charset=utf-8');
    httpRequest.send();
}

function updateGameTurn(gameId, gameTurn, isNewRequest) {
    //  ╭─ nullish ──────╮ ╭─ not nullish ─────────────────────────────────╮
    // ┌───────────┬──────┬───────┬───┬────┬─────┬──────┬───┬─────────┬─────┐
    // │ undefined │ null │ false │ 0 │ "" │ ... │ true │ 1 │ "hello" │ ... │
    // └───────────┴──────┴───────┴───┴────┴─────┴──────┴───┴─────────┴─────┘
    //  ╰─ falsy ───────────────────────────────╯ ╰─ truthy ───────────────╯
    if (gameTurn.player_turn) { // check is not nullish
        document.getElementById("playerTurnId").value = gameTurn.player_turn;

        if (gameTurn.player_turn === 'AI' && isNewRequest) {
            updateMyGrid(gameId);
        }
    }

    if (gameTurn.won) { // check is not nullish
        document.getElementById("wonId").value = gameTurn.won;
    }

    checkGameOver();
}

function checkGameOver() {
    let userId = document.getElementById("userId").value;
    let opponentId = document.getElementById("opponentId").value;
    let wonId = document.getElementById("wonId").value;

    if (userId === wonId) {
        alert("You won!");
        proposeNewGame();
    } else if (opponentId === wonId) {
        alert("You lost!");
        proposeNewGame();
    }
}

function proposeNewGame() {
    if (confirm('Would you like to start a new game?')) {
        createNewGame();
    } else {
        // load index page
        window.location = "/";
    }
}

function createNewGame() {
    let gameId = document.getElementById("gameId").value;

    let httpRequest = new XMLHttpRequest();

    httpRequest.onreadystatechange = function() {
        if (httpRequest.readyState == XMLHttpRequest.DONE) { // XMLHttpRequest.DONE == 4
            if (httpRequest.status == 200) {
                let id = JSON.parse(httpRequest.responseText).game_id;
                window.location.href = "/gameId/" + id;
            } else {
                let jsonError = JSON.parse(httpRequest.responseText);
                alert(jsonError.error_message); // very crude approach
            }
        }
    };

    httpRequest.open("GET", "/xl-spaceship/user/game/" + gameId + "/new", true);
    httpRequest.setRequestHeader('Content-Type', 'application/json; charset=utf-8');
    httpRequest.send();
}