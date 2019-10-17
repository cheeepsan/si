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
        case "register":
            console.log("registering: " + obj.data)
            registerUser(obj.data.text)
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

function registerUser(data) {
    var boardDiv = $("#user")
    console.log(data)
    boardDiv.append("user")
}