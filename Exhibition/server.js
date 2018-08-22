#!/usr/bin/node

var express = require('express');
var app = express();
var server = require('http').createServer(app);
var io = require('socket.io')(server);
var exec = require('child_process').exec;

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
     pegDeltas: [ 711, 504, 371, 398, 544, 399, 431 ] }
            };

var test2 = { id: 2,
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


function print_label(pegQ, avg_time){
    execSync("printer/venv/bin/python3 printer/imageGen.py " + pegQ + " " + avg_time, (err, stdout, stderr) => console.log(stdout));
    execSync("./printer/labelPrinter printer/customLabel.png", (err, stdout, stderr) => console.log(stdout));
    execSync("rm printer/customLabel.png", (err, stdout, stderr) => console.log(stdout));
}

// This is a debug flag, should set to false if we want to temporarily turn of printing
const should_print = true;

const STATUS_CONNECTED = 0;
const STATUS_DISCONNECTED = 1;

const PRINT_LABEL = 0;

//Parse application/json
app.use(bodyParser.json());

// Define a few constants

// Port number we are going to listen on
const LISTEN_PORT = 18216;

// Start server on port 4000
server.listen(LISTEN_PORT, function(){
    console.log('Listening to requests on port ' + LISTEN_PORT);
});

// Load in static files
app.use("/epegExhibition", express.static('frontend'));

// List of frontends connected to the server
var frontends = [];

// List of tablets connected to the server
var tablets = [];

io.on('connection', function(socket){
    console.log('Made socket connection', socket.id);

    // Can be either "tablet" or "frontend"
    var client_type = socket.handshake.query.client_type;

    console.log('Socket query information:', client_type);

    if (client_type == "tablet"){
        let tablet_id = tablets.push(socket) - 1;

        // Set up relaying of all player_action messages
        socket.on('player_action', (d) => frontends.forEach((s) => s.emit('player_action', d)));

        // If the tablet disconnects, we remove it from our connection table
        socket.on('disconnect', () => {
            tablets.splice(tablet_id, 1);

            frontends.forEach((s) => s.emit('player_status', {
                id: s.handshake.query.tablet_id,
                status: STATUS_DISCONNECTED
            }));
        });

        // If a tablet connects, we alert every frontend
        frontends.forEach((s) => s.emit("player_status", {
            id: socket.handshake.query.tablet_id,
            status: STATUS_CONNECTED
        }));

    }
    else if (client_type == "frontend"){
        frontends.push(socket);

        // Handle the frontend_action requests
        socket.on("frontend_action", (s) => {
            if (s.action_type == PRINT_LABEL && should_print){
                print_label(s.action_data.pegQ, s.action_data.avg_time)
            }
        })

        // If a frontend connects, we update it with the list of tablets
        tablets.forEach((s) => socket.emit("player_status", {
            id: s.handshake.query.tablet_id,
            status: STATUS_CONNECTED
        }));
    }
    else{
        console.log("Unknown client type: ", client_type);
    }
});
