'use strict';

window.onload = function () {

    let html = document.getElementsByTagName('html')[0];
    let clientName = document.getElementById('alias');
    let roomName = document.getElementById('room');
    let joinButton = document.getElementById('join');

    joinButton.addEventListener('click', function () {
        if(clientName.value && roomName.value){
            let userName = clientName.value;
            let room = roomName.value;
            let xhr = new XMLHttpRequest();
            xhr.open("GET", "chatRoom.html");

            xhr.addEventListener('load', function () {
                html.innerHTML = this.responseText;

                document.getElementsByTagName('h1')[0].textContent = room;

                let cMessage = document.getElementById('messageBox');
                let sendButton = document.getElementById('sendMessage');

                sendButton.addEventListener('click', function () {
                    console.log("Send BUtton");
                    let date = new Date();
                    let today = date.getMonth().toLocaleString()+"/"+date.getDate().toLocaleString();
                    if(cMessage.value) {
                        console.log(cMessage.value);
                        let fullMessage = {usr:userName, msg:cMessage.value, time:(today+" "+date.toLocaleTimeString())};
                        let jsonMessage = JSON.stringify(fullMessage);
                        var cSocket;
                        cSocket.send(jsonMessage);
                        document.getElementById("messageBox").value = "";
                    }
                });

                document.getElementById("messageBox").addEventListener('keypress', function (event) {
                    let key = event.which || event.keyCode;
                    if(key === 13) {
                        event.preventDefault();
                        if(event.defaultPrevented){
                            sendButton.click();
                        }
                    }
                });


            });

            let cSocket = new WebSocket("ws://" + location.host);
            cSocket.onopen = function () {
                console.log("HANDSHAKE ACCEPTED");
                cSocket.send("join "+room);
            };

            cSocket.onmessage = function (event) {
                console.log("message received");
                console.log("Message: "+event.data.toString());

                let messageServ = JSON.parse(event.data);
                let messageText = document.createTextNode(': '+messageServ.msg);
                let t = document.createTextNode(messageServ.time);

                let bold = document.createElement("b");
                bold.appendChild(document.createTextNode(messageServ.usr));

                let par = document.createElement("p");
                par.appendChild(bold);
                par.appendChild(messageText);

                let messageDiv = document.createElement("div");
                messageDiv.className = "message";

                let mTime = document.createElement("div");
                mTime.className = "time";
                mTime.appendChild(t)

                messageDiv.appendChild(par);
                messageDiv.appendChild(t);

                let chatBox = document.getElementById("chatBox");
                chatBox.appendChild(messageDiv);
                messageDiv.scrollIntoView(false);


            };

            xhr.send();
        }

    });


};