var fs = require('fs');
var app = require('express')();
var http = require('http').Server(app);
var io = require('socket.io')(http);
var SerialPort = require("serialport").SerialPort;
var sp = new SerialPort("/dev/ttyACM0", {
    baudrate: 115200
}, false);

var mutexLock = false;

// ||
// '' "" \n \0
io.on('connection', function(socket){
    console.log('a user connected');
    socket.on("hello", function(msg){
        console.log("Message" + msg);
    });
    socket.on("sensorOnChanged", function(data){
        if(!mutexLock){
            mutexLock = true;
            sp.open(function(){
                sp.write(data, function(){
                    console.log(data);
                    sp.close(function(){
                        mutexLock = false;
                        fs.readFile('./pic.jpg', function(err, buf){
                            socket.emit('image', buf);
                        });
                    });
                });
            });
        }

    });
});


http.listen(3000, function(){
    console.log('listening on *:3000');
});
