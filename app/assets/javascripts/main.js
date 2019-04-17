var socket = null
function start(d) {
    socket = new WebSocket(d)
    console.log(socket)

    socket.onopen = function() {
      console.log("Соединение установлено.");
    };

    socket.onclose = function(event) {
      if (event.wasClean) {
        console.log('Соединение закрыто чисто');
      } else {
        console.log('Обрыв соединения'); // например, "убит" процесс сервера
      }
      console.log('Код: ' + event.code + ' причина: ' + event.reason);
    };

    socket.onmessage = function(event) {
      console.log("Получены данные " + event.data);
    };

    socket.onerror = function(error) {
      console.log("Ошибка " + error.message);
    };
}

function renderData(data) {
    socket.send("load")
}

