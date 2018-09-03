#!/usr/bin/node

var express = require('express');
var app = express();
var server = require('http').createServer(app);
var io = require('socket.io')(server);
var exec = require('child_process').exec;

var bodyParser = require('body-parser');

var fs = require('fs');

// This is a debug flag, should set to false if we want to temporarily turn of printing
const should_print = true;

const STATUS_CONNECTED = 0;
const STATUS_DISCONNECTED = 1;

const PRINT_LABEL = 0;
const MULTIPLAYER_PROGRESS = 1;
const SAVE_DATA = 2;

const DYNAMIC_PEGQ_DATA = "frontend/resources/exhibition_data.csv";
const DYNAMIC_PEGQ_BACKUP = "frontend/resources/exhibition_backup.csv";

// =============================================================================
// Helper functions
// =============================================================================

// Create resources file if it doesn't exist already
if (!fs.existsSync(DYNAMIC_PEGQ_DATA)){
    // Write the header
    fs.writeFile(DYNAMIC_PEGQ_DATA, "pegs.ndx, avg_time\n", (err) => {

        if (err) throw err;

        console.log("Wrote header for the dynamic peg data!");
    });
}

function save_data(data){
    console.log(data);

    fs.appendFile(DYNAMIC_PEGQ_DATA, data.stats.pegQ + ", " + data.stats.avg_time + "\n", (err) => {

        if (err) throw err;

        console.log("Recorded: " + data.stats.pegQ + ", " + data.stats.avg_time);
    });

    fs.appendFile(DYNAMIC_PEGQ_BACKUP, JSON.stringify(data.backup) + "\n", (err) => {
        if (err) throw err;

        console.log("Recorded backup data!");
    });
}

function print_label(pegQ, avg_time){
    exec("printer/venv/bin/python3 printer/imageGen.py " + pegQ + " " + avg_time, (err, stdout, stderr) => {
        exec("./printer/labelPrinter printer/customLabel.png", (err, stdout, stderr) => {
            exec("rm printer/customLabel.png", (err, stdout, stderr) => console.log(stdout));
        });
    });
}

// =============================================================================
// App setup
// =============================================================================

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

// =============================================================================
// Socket setup
// =============================================================================

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
		        console.log("frontend_action: ");
		        console.log(s);

            // Printing
            if (s.action_type == PRINT_LABEL && should_print){
                print_label(s.action_data.pegQ, s.action_data.avg_time)
            }

            // Pass on frontend information to the tablets
            if (s.action_type == MULTIPLAYER_PROGRESS){

                tablets.forEach((s) => s.emit("server_action", {action_data: s.action_data}))
            }

            // Save the trial data
            if (s.action_type == SAVE_DATA){
                save_data(s.action_data);
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
