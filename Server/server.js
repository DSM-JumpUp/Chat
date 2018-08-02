const app = require('express')();
const SocketIo = require('socket.io');

const server = app.listen(process.env.PORT, () => {
    console.log(process.env.PORT + '  running...');
});
const io = SocketIo(server);

let rooms = {};
let queue = [];
let names = {};
let allUsers = {};
let locations = {};
let blackList = {};
let randomKeyword = ['익명의 너구리', '익명의 산 개미', '익명의 똥구멍', '익명의 며느리', '익명의 개구리', '익명의 손승용', '익명의 귀요미', '익명의 마마무', '익명의 아몬드', '익명의 다람쥐', '익명의 독수리', '익명의 대머리', '익명의 물고기', '익명의 파랑새', '익명의 호랑이', '익명의 정다은', '익명의 까마귀', '익명의 거북이', '익명의 코뿔소', '익명의 도도새', '익명의 두더지', '익명의 스컹크', '익명의 족제비', '익명의 돌고래', '익명의 오소리', '익명의 원숭이', '익명의 미어캣', '익명의 북극곰', '익명의 흰고래', '익명의 범고래', '익명의 청새치', '익명의 햄스터', '익명의 친칠리', '익명의 산토끼', '익명의 코알라', '익명의 캥거루', '익명의 두꺼비', '익명의 도룡뇽', '익명의 앵무새', '익명의 물총새', '익명의 왜가리', '익명의 독수리', '익명의 호박벌', '익명의 물장군', '익명의 풍뎅이', '익명의 사마귀', '익명의 잠자리', '익명의 왕개미', '익명의 왕거미', '익명의 도마뱀', '익명의 코브라', '익명의 카이만', '익명의 방울뱀', '익명의 보아뱀', '익명의 구렁이', '익명의 고등어', '익명의 주꾸미', '익명의 돌문어']

let controlRoom = (socket) => {

    if(queue.length > 0) {
        for(let i = 0; i < queue.length; i++ ) {
            let peerId = queue[i].id;
            if(calculateDistance(locations[peerId].lat, locations[peerId].lng, locations[socket.id].lat, locations[socket.id].lng) <= (locations[socket.id].length / 1000) && socket.id !== queue[i].id){
                let peer = queue[i];
                queue = queue.splice(i, 1);

                let roomName = socket.id + "#" + peer.id;

                peer.join(roomName);
                socket.join(roomName);

                rooms[peer.id] = roomName;
                rooms[socket.id] = roomName;

                peer.emit('join', {name: names[socket.id], roomName: roomName});
                socket.emit('join', {name: names[peer.id], roomName: roomName});

                break;
            }
            else if(socket.id === queue[i].id) queue = queue.splice(i, 1);
            else if(i === queue.length - 1) queue.push(socket);
            else continue;
        }
    } else {
        queue.push(socket)
    }
}

io.on("connection", socket => { 
    socket.on("ip check", function (data) {
        console.log("ip check : " + data.ip)
        if(blackList[data.ip] >= 5) {
            console.log("block")
            socket.emit("block");
        }
    })
    socket.on("search", function (data) {
        console.log("press search");
        console.log(data);
        names[socket.id] = randomKeyword[Math.floor(Math.random() * (randomKeyword.length + 1))]
        allUsers[socket.id] = socket;
        locations[socket.id] = data
        controlRoom(socket)
    })
    socket.on("pop queue", function() {
        console.log("pop queue");
        if(queue.indexOf(allUsers[socket.id]) > -1)
            queue.splice(queue.indexOf(allUsers[socket.id]))

    })
    socket.on("message", function (data) {
        console.log("get message " + data);
        let room = rooms[socket.id];
        socket.broadcast.to(room).emit('message', data);
    })
    socket.on("leave room", function () {
        let room = rooms[socket.id];
        socket.broadcast.to(room).emit("exit");
        socket.leave(room);
    })
    socket.on("siren", function() {
        let room = rooms[socket.id];
        console.log("siren");
        socket.broadcast.to(room).emit("ip");
        // peer = room.split("#");
        // peer = peer[0] === socket.id ? peer[1] : peer[0];
        // if(report[peer] === undefined) report[peer] = 1
        // else report[peer] = report[peer] + 1
    })
    socket.on("ip", function(data) {
        console.log("ip : " + data.ip);
        if(blackList[data.ip] === undefined) blackList[data.ip] = 1;
        else blackList[data.ip] = blackList[data.ip] + 1;
    })
    socket.on("disconnect", function () {
        let room = rooms[socket.id];
        let UserSocket = allUsers[socket.id];
        if(queue.indexOf(UserSocket > -1)) {
            queue.splice(queue.indexOf(UserSocket), 1)
        } else {
            socket.broadcast.to(room).emit("exit"); 
        }
    })
})


function calculateDistance(lat1, lon1, lat2, lon2) {
    var R = 6371; // km
    var dLat = (lat2-lat1).toRad();
    var dLon = (lon2-lon1).toRad(); 
    var a = Math.sin(dLat/2) * Math.sin(dLat/2) +
            Math.cos(lat1.toRad()) * Math.cos(lat2.toRad()) * 
            Math.sin(dLon/2) * Math.sin(dLon/2); 
    var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
    var d = R * c;
    return d;
  }
  Number.prototype.toRad = function() {
    return this * Math.PI / 180;
  }