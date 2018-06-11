var express = require('express');
var socket = require('socket.io');

// App setup
var app = express();

// App will listen on port 4000
var server = app.listen(4000, function(){
    console.log('listening to requests on port 4000');
});

// Load in static files
app.use(express.static('frontend'));

// Socket setup
var io = socket(server);

io.on('connection', function(socket){
    console.log('made socket connection', socket.id);

    socket.on('chat', function(data){
        io.sockets.emit('chat', data);
    });
});
