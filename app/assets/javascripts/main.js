var socket = null


function start(d) {
    var status = $("#status")
    socket = new WebSocket(d)
    console.log(socket)

    socket.onopen = function() {
      console.log("Соединение установлено.");
      status.css("color", "green")
    };

    socket.onclose = function(event) {
      if (event.wasClean) {
        console.log('Соединение закрыто чисто');
      } else {
        console.log('Обрыв соединения'); // например, "убит" процесс сервера
      }
      status.css("color", "red")
      console.log('Код: ' + event.code + ' причина: ' + event.reason);
    };

    socket.onmessage = function(event) {
       var obj = JSON.parse(event.data);
       switch (obj.message) {
        case "round":
            drawRound(obj.data)
            break;
        case "removeQuestion":
            console.log("removing: " + obj.data)
            removeQuestion(obj.data.text)
            break;
        default:
            console.log("def")
            break;
       }
    };

    socket.onerror = function(error) {
      console.log("Ошибка " + error.message);
    };
}
function drawRound(data) {
    var boardDiv = $("#board")
    console.log(data)
    boardDiv.append(data.html)
}

function chooseQuestion(username, data) {
    console.log("u " + username)
    console.log(data)
    let user = new User(0, username)
    let siText = new SiText(data)
    let message = new Message("chooseQuestion", user, "SiText", siText)
    socket.send(JSON.stringify(message))
}

function renderData(data) {
    let user = new User(0, "username")
    let siText = new SiText("")
    let message = new Message("start", user, "SiText", siText)
    //socket.send("start");
    console.log(message)
    socket.send(JSON.stringify(message));
}

function removeQuestion(data) {
    console.log(data)
    var q = $("#" + data)
    q.remove();
}
