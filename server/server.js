var express = require('express');
var mysql = require('mysql');
var bodyParser = require('body-parser');

var app = express();

//Taken from http://stackoverflow.com/questions/10005939/how-to-consume-json-post-data-in-an-express-application

//Parse application/json
app.use(bodyParser.json());

// Defining server port to operate on.
const PORT = 43210;

const FIELD_DATA = "study_results";
const FIELD_IV = "initialisation_vector";
const FIELD_SECRETKEY = "symmetric_key";
const FIELD_RECORD_TIMESTAMP = "time_recorded";
const FIELD_DEVICE_ID = "device_identifier";
const FIELD_RESEARCHER_ID = "trial_conductor";


// Creates connection pool, so that many async DB accesses may be possible
var pool = mysql.createPool({

  "connectionLimit" : 10, // Maximum number of connections at a time
  "host"            : "gf38.host.cs.st-andrews.ac.uk",
  "user"            : "gf38",
  "password"        : "uSc.Zdb2SBar8j",
  "database"        : "gf38_ePegDB",
  "debug"           : "false"

});


//Handler function for HTTP requests
function requestHandler(request, response){

  // Get the connection to the Mysql database
  pool.getConnection(function(err, connection){

    //If an error occurs, release connection, respond with HTTP 100 : Continue response
    if(err){
      connection.release();
      response.json({"code" : 100, "status" : "Internal Database error!"});
      return;
    }

    // Log connection
    console.log("Connected as id #" + connection.threadId);

    var sql_query = "INSERT INTO studies (" +
                                            FIELD_DATA + ", " +
                                            FIELD_SECRETKEY + ", " +
                                            FIELD_IV + ", " +
                                            FIELD_RECORD_TIMESTAMP + ", " +
                                            FIELD_DEVICE_ID + ", " +
                                            FIELD_RESEARCHER_ID + ") " +
                                        "VALUES ('"+
                                            request.body.data + "', '" +
                                            request.body.secretkey + "', '" +
                                            request.body.iv + "', '" +
                                            request.body.timestamp + "', '" +
                                            request.body.device_id + "', '" +
                                            request.body.researcher + "' )";

    // Insert received data into the DB
    connection.query( sql_query, function(err, rows){
      connection.release();
      if( !err ){
        response.json({"code": 200, "status" : "Data Synchronised"});
      }
    });

    // Bind error listener
    connection.on( "error", function(err){
      response.json({"code" : 100, "status" : "Internal Database error!"});
      return;
    });

  });
}

//Create Server on default route "/"
app.post("/epeg", function(request, response){
	requestHandler(request, response);
});

// Start server
app.listen(PORT);

console.log("Server running at localhost:" + PORT);
