window.addEventListener("DOMContentLoaded", (event) => {
    const form = document.getElementById("new-game-form");

    form.addEventListener("submit", newGameForm);

    function newGameForm(event) {
        // prevent page reload
        event.preventDefault();
        // send request to backend
        createNewGameViaAjax(event);
    }
});

function createNewGameViaAjax(event) {
    const data = new FormData(event.target);
    const dataObject = Object.fromEntries(data.entries());

    let sp = {}
    sp["hostname"] = data.get("hostname");
    sp["port"] = data.get("port");

    let jsonData = JSON.stringify(sp)

    sendAjaxRequest(jsonData);
}

function sendAjaxRequest(jsonData) {
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

    httpRequest.open("POST", "/xl-spaceship/user/game/new", true);
    httpRequest.setRequestHeader('Content-Type', 'application/json; charset=utf-8');
    httpRequest.send(jsonData);
}