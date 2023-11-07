const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
const host = window.location.host;
const wsURL = `${protocol}//${host}/api/ws`;

const stompClient = new StompJs.Client({
    brokerURL: wsURL
});


stompClient.onConnect = (frame) => {
    setConnected(true);
    console.log('Connected: ' + frame);
    stompClient.subscribe('/topic/liveChatRoom', (liveChat) => {
        showGreeting(JSON.parse(liveChat.body));
    });
};

stompClient.onWebSocketError = (error) => {
    console.error('Error with websocket', error);
};

stompClient.onStompError = (frame) => {
    console.error('Broker reported error: ' + frame.headers['message']);
    console.error('Additional details: ' + frame.body);
};

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
    }
    $("#greetings").html("");
}

function connect() {
    stompClient.activate();
}

function disconnect() {
    stompClient.deactivate();
    setConnected(false);
    console.log("Disconnected");
}



function sendName() {
    stompClient.publish({
        destination: "/app/liveChatRoom",
        body: JSON.stringify({'message': $("#name").val() +' : ' + $("#msg").val()})  //$("#name").val()
    });
}

function showGreeting(data) {
    const userId = data.userId;
    const name = data.username;
    const timestamp = data.timestamp;
    const message = data.message;
    console.log(timestamp, message, name)
    $("#greetings").append("<tr><td>" +message + "시간 : " +timestamp +"</td></tr>");
}

$(function () {
    $("form").on('submit', (e) => e.preventDefault());
    $( "#connect" ).click(() => connect());
    $( "#disconnect" ).click(() => disconnect());
    $( "#send" ).click(() => sendName());
});

