fun_facts = [
    "most humans are right-handed and most parrots are left-footed",
    "the same genes implicated in human handedness control the chirality in shells",
    "most animals have sidedness but humans are the only one with a 1left:9right ratio",
    "many genes controls handedness",
    "handedness is not just left/right but can be measures along a continuum"
];

// Start doing things when the page has loaded completely.
document.addEventListener("DOMContentLoaded", (e) => {

    // Player status constants
    const STATUS_CONNECTED = 0;
    const STATUS_DISCONNECTED = 1;

    // Player action constants;
    const REQ_NEW_SINGLE_GAME = 0;
    const REQ_NEW_MULTI_GAME = 1;
    const REQ_START_TRIAL = 2;
    const REQ_TRIAL_FINISHED = 3;
    const REQ_DISPLAY_READ = 4;

    // The number of tablets we will wait for to connect before we allow
    // the game state to progress further.
    const MAX_PLAYERS = 2;

    // The number of trials that will be conducted for a player
    // One trial consits of one right hand or one left hand pass
    const NUM_PLAYER_TRIALS = 2000;

    // This will store the state of each player.
    var players = {};

    // Register the items on the frontend
    var covers = [ d3.select("#cover1")
                 , d3.select("#cover2")
                 ];
    var vis_bases = [ d3.select("#vis1")
                    , d3.select("#vis2")
                    ];

    // Make connection to the server
    // We also advertise the fact that this is the frontend connection.
    // This can be accessed on the server by referring to
    // socket.handshake.query.client_type
    var socket = io({ query: {client_type:"frontend" }});

    // =========================================================================
    // Assign listeners to the socket
    // =========================================================================

    socket.on('player_status', add_player);

    socket.on('player_action', function(data){

        switch(data.action_type){

        case REQ_NEW_SINGLE_GAME:
            players[data.sender_id].game = initialise_single_player_game(data.sender_id);

            // Start the game by calling the constructor of the closure
            players[data.sender_id].game();
            break;

        case REQ_NEW_MULTI_GAME:
            players[data.sender_id].game = initialise_multi_player_game();

            // Start the game by calling the constructor of the closure
            players[data.sender_id].game();
            break;

        case REQ_DISPLAY_READ:
            players[data.sender_id].game.begin_study();
            break;

        case REQ_START_TRIAL:
            players[data.sender_id].game.run_trial();
            break;

        case REQ_TRIAL_FINISHED:
            players[data.sender_id].game.finish_trial(data.action_data);
            break;

        default: console.log("Unknown request: " + data.action_type);
        }

    });

    // =========================================================================
    // Initialise the frontend for the players
    // =========================================================================

    var chart1 = document.getElementById("vis1");
    var chart2 = document.getElementById("vis2");

    var player_assets = [
        {
            "chart": barchart(chart1.offsetWidth, window.innerHeight * .8, 7),
            "cover": covers[0],
            "vis_base": vis_bases[0],
            "vis_id": "#vis1"
        },
        {
            "chart": barchart(chart1.offsetWidth, window.innerHeight * .8, 7),
            "cover": covers[1],
            "vis_base": vis_bases[1],
            "vis_id": "#vis2"
        }
    ];

    // =========================================================================
    // Game logic
    // =========================================================================

    function add_player(player){

        var player_index = Object.keys(players).length;
        // We can only connect a set number of players
        // If we receive more connections than allowed, they must disconnect and wait.
        if (player_index == MAX_PLAYERS) {
            console.log("Maximum number of players already reached!");

            return;
        }
        player["index"] = player_index;
        player["assets"] = player_assets[player_index];

        // Add the player to the game
        players[player.id] = player;

        // Update the visuals
        player["assets"]["cover"].select(".cover_info")
            .html("Tablet connected!");

        console.log("Assigned Player ", player_index + 1);
        console.log(players);
    }

    function initialise_single_player_game(player_id){

        var player = players[player_id];

        var study_data = [];

        var num_trials_finished = 0;

        function game(){
            console.log("Starting single player game!");

            reset();

            display_information();
        }

        var reset = function(){
            player["assets"]["cover"].transition().duration(1000)
                .style("height", "100%");
        };

        var display_information = function(){
            player["assets"]["cover"].select(".cover_info")
                .html('<iframe width="560" height="315" src="https://www.youtube.com/embed/NPvMUpcxPSA?rel=0&amp;controls=0" frameborder="0" allow="autoplay; encrypted-media" allowfullscreen></iframe>');
        };

        var show_results = function(){
            player["assets"]["cover"].select(".cover_info").html("");

            player["assets"]["cover"].transition().duration(1000)
                .style("height", "100%")
                .on("end", () => {
                    player["assets"]["vis_base"].select("svg").html("");
                    player["assets"]["vis_base"].select("svg").html("<rect height='200' width='200' x='0' y='0' style='fill:red'></rect>");

                    player["assets"]["cover"].transition().duration(1000)
                        .style("height", "0%");
                });
        };

        game.begin_study = function(){
            player["assets"]["chart"](player["assets"]["vis_id"] + " .chart");

            player["assets"]["cover"].transition().duration(1000)
                .style("height", "0%");
        };

        game.run_trial = function(){
        };

        game.finish_trial = function(data){
            player["assets"]["chart"].update_chart(data);

            num_trials_finished++;

            // Record the data so that we can use it for the visualisation at the end and for printing
            study_data.push(data);

            // If the player has finished every trial, show the results after 2 seconds.
            if (num_trials_finished == NUM_PLAYER_TRIALS){
                setTimeout(show_results, 2000);
            }
        };

        return game;
    }

    // Singleton instance of a multiplayer game.
    var current_multi_player_game = undefined;

    function initialise_multi_player_game(){

        function multi_player_game(){
            console.log("Starting multiplayer game!");
        }

        // Check if the singleton instance is present already or not
        return typeof current_multi_player_game == 'undefined' ?
            multi_player_game :
            current_multi_player_game;
    }
















    // We need to wait for both tablets to connect to the server
    var phase = 0;




    d3.select("#haha")
        .on("click", () => {
            switch(phase){
            case 0:
                d3.select("#cover1")
                    .transition().duration(1500)
                    .style("height", "0px");
                    phase = 1;
                break;

            case 1:
                phase = 0;
                break;
            }
        });

});
