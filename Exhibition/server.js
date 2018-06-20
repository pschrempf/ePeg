var express = require('express');
var app = express();
var server = require('http').createServer(app);
var io = require('socket.io')(server);

var bodyParser = require('body-parser');

var test1 = { id: 1,
  handUsed: 'right',
  success: 'success',
  extras: '',
  measurements: 
   { startTime: 1529421219151,
     endTime: 1529421222510,
     totalTime: 3359,
     sumTime: 3358,
     actualTime: 3359,
     pegsLifted: 
      [ '2018-06-19 15:13:41.799',
        '2018-06-19 15:13:41.295',
        '2018-06-19 15:13:40.924',
        '2018-06-19 15:13:40.526',
        '2018-06-19 15:13:39.982',
        '2018-06-19 15:13:39.583',
        '2018-06-19 15:13:39.151' ],
     pegsReleased: 
      [ '2018-06-19 15:13:42.51',
        '2018-06-19 15:13:41.799',
        '2018-06-19 15:13:41.295',
        '2018-06-19 15:13:40.924',
        '2018-06-19 15:13:40.526',
        '2018-06-19 15:13:39.982',
        '2018-06-19 15:13:39.582' ],
     pegDeltas: [ 711, 504, 371, 398, 544, 399, 431 ] } };

var test2 = { id: 1,
  handUsed: 'left',
  success: 'success',
  extras: '',
  measurements: 
   { startTime: 1529421338565,
     endTime: 1529421341107,
     totalTime: 2542,
     sumTime: 2542,
     actualTime: 2542,
     pegsLifted: 
      [ '2018-06-19 15:15:38.565',
        '2018-06-19 15:15:38.852',
        '2018-06-19 15:15:39.196',
        '2018-06-19 15:15:39.5',
        '2018-06-19 15:15:39.903',
        '2018-06-19 15:15:40.262',
        '2018-06-19 15:15:40.573' ],
     pegsReleased: 
      [ '2018-06-19 15:15:38.852',
        '2018-06-19 15:15:39.196',
        '2018-06-19 15:15:39.5',
        '2018-06-19 15:15:39.903',
        '2018-06-19 15:15:40.262',
        '2018-06-19 15:15:40.573',
        '2018-06-19 15:15:41.107' ],
     pegDeltas: [ 287, 344, 304, 403, 359, 311, 534 ] } };


//Parse application/json
app.use(bodyParser.json());

// Define a few constants

// Port number we are going to listen on
const LISTEN_PORT = 18216;

// Start server on port 4000
server.listen(LISTEN_PORT, function(){
    console.log('Listening to requests on port ' + LISTEN_PORT);
});

// Request handler function
var requestHandler = function (request, response){
    console.log(request.body);

    response.json({"code": 200, "status" : "Data Synchronised"});

    // Emit to all connected frontends the data that we just received from the tablet.
    io.sockets.emit('peg_info', request.body);

};

// We will receive the data from the tablets in the form of a post request.
// We pass the data to the request handler function
app.post("/tabletData", requestHandler);

// Load in static files
app.use("/epegExhibition", express.static('frontend'));

io.on('connection', function(socket){
    console.log('Made socket connection', socket.id);
});




