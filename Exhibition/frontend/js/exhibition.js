// Start doing things when the page has loaded completely.
document.addEventListener("DOMContentLoaded", (e) => {

    chart1("#vis1");
    chart2("#vis2");

    // Player action constants;
    const REQ_NEW_SINGLE_GAME = 0;
    const REQ_NEW_MULTI_GAME = 1;
    const REQ_START_TRIAL = 2;
    const REQ_TRIAL_FINISHED = 3;

    // The number of tablets we will wait for to connect before we allow
    // the game state to progress further.
    const MAX_PLAYERS = 2;

    // This will store the state of each player.
    var players = {};

    // Make connection to the server
    var socket = io();

    // =========================================================================
    // Assign listeners to the socket
    // =========================================================================

    socket.on('player_status', (player) => {
        var num_players = players.push({"id": player.id});

        // We can add in at most MAX_PLAYERS number of players.
        add_player(player);
    });

    socket.on('player_action', function(data){
        players[data.sender_id].update(data);
    });

    // =========================================================================
    // Initialise the frontend for the players
    // =========================================================================

    var player_assets = [
        {
            "chart": barchart(.3 * window.innerWidth, 600, 7);
        },
        {
            "chart": barchart(.3 * window.innerWidth, 600, 7);
        }
    ];

    // =========================================================================
    // Game logic
    // =========================================================================

    function add_player(player){

        // We can only connect a set number of players
        // If we receive more connections than allowed, they must disconnect and wait.
        if (Object.keys(players).length == MAX_PLAYERS) {
            console.log("Maximum number of players already reached!");

            return;
        }

        // Add the player to the game
        players[player.id] = Player(player);
    }

    function initialise_single_player_game(player_id){

        var player_index = Object.keys(players).indexOf(player_id);
        var player_asset = player_assets[player_index];

        function single_player_game(){
            console.log("Starting single player game!");
        }

        return single_player_game;
    }

    function multi_player_game(){
        console.log("Starting multiplayer game!");
    }


















    // We need to wait for both tablets to connect to the server
    var phase = 0;




    d3.select("#haha")
        .on("click", () => {
            switch(phase){
            case 0:
                d3.select("#header")
                    .transition().duration(750)
                    .style("height", (window.innerHeight) + "px")
                    .on('end', () => {
                        d3.select("#header")
                            .transition().duration(750)
                            .style("height", "100px");
                    });
                    phase = 1;
                break;

            case 1:
                phase = 0;
                break;
            }
        });

});

var Player = function(player){

    // Constructor for the Player closure
    create(){
        console.log("Created player!");
    }

    // Handler for receiving any sort of data
    var update = function(data){

        switch(data.action_type){
        case REQ_NEW_SINGLE_GAME: break;
        case REQ_NEW_MULTI_GAME: break;
        case REQ_START_TRIAL: break;
        case REQ_TRIAL_FINISHED: break;
        default: console.log("Unknown request: " + data.action_type);
        }
        console.log(data);
    };



    return create;
};
