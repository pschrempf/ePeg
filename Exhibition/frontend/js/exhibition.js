fun_facts = [
    "Most humans are right-handed and most parrots are left-footed.",
    "The same genes implicated in human handedness control the chirality in shells.",
    "Most animals have sidedness but humans are the only one with a 1:9 left-right ratio.",
    "Many genes control handedness.",
    "Handedness is not just left/right but can be measured along a continuum.",
    "More than 90% parrots prefers their left foot.",
    "Most animals have a preferred side, left/right preference is usually 50:50.",
    "Kangaroos prefer their left side.",
    "Handedness and language hemispheric dominance are correlated.",
    "Handedness is established before we are born.",
    "Genes contribute up to 25% to hand preference.",
    "Left-handers have an advantage in baseball and cricket.",
    "There are very few left-handed golf players",
    "Kurt Cobain was right handed but played a left-handed guitar.",
    "Paul McCartney and Ringo Starr are left handed."
];

// Start doing things when the page has loaded completely.
document.addEventListener("DOMContentLoaded", (e) => {

    // Player status constants
    const STATUS_CONNECTED = 0;
    const STATUS_DISCONNECTED = 1;

    // Player action constants;
    const NEW_GAME = 0;
    const JOIN_GAME = 1;
    const START_TRIAL = 2;
    const TRIAL_FINISHED = 3;
    const DISPLAY_READ = 4;
    const EXPERIMENT_DONE = 5;
    const GAME_RESET = 6;

    // Frontend action constants;
    const RES_PRINT_LABEL = 0;
    const RES_MULTIPLAYER_PROGRESS = 1;
    const RES_SAVE_DATA = 2;
    const RES_GAME_STARTED = 3;
    const RES_GAME_LOCKED = 4;
    const RES_GAME_UNLOCKED = 5;

    // The number of tablets we will wait for to connect before we allow
    // the game state to progress further.
    const MAX_PLAYERS = 2;

    // The number of trials that will be conducted for a player
    // One trial consits of one right hand or one left hand pass
    const NUM_PLAYER_TRIALS = 4;

    // This will store the state of each player.
    var players = {};

    // Register the items on the frontend
    var cover = d3.select(".cover");

    var vis_base = d3.select("#vis");

    var fun_fact_interval;
    var fun_fact_index = 0;

    // Singleton for the current game
    var current_game = undefined;

    // Make connection to the server
    // We also advertise the fact that this is the frontend connection.
    // This can be accessed on the server by referring to
    // socket.handshake.query.client_type
    var socket = io({ query: {client_type:"frontend" }});

    // =========================================================================
    // Assign listeners to the socket
    // =========================================================================

    socket.on('player_status', (data) =>{
        switch (data.status){
        case STATUS_CONNECTED: add_player(data); break;
        case STATUS_DISCONNECTED: pause_player(data); break;
        default: console.log("Received malformed player_status message:" + JSON.stringify(data)); break;
        }
    });

    socket.on('player_action', function(data){


        switch(data.action_type){
        case NEW_GAME:
            // Check if we can start a new game by checking if there is already a game
            if (typeof current_game != "undefined"){
                console.log("Cannot create a new game, there is already one running!");
                return;
            }

            current_game = initialise_game(data.sender_id);
            current_game();
            break;

        case JOIN_GAME:
            if (typeof current_game == "undefined"){
                console.log("There is no game to join!");
                return;
            }
            current_game.join_player(data.sender_id);
            break;

        case DISPLAY_READ:
            if (typeof current_game == "undefined") return;

            current_game.begin_study(data.action_data, data.sender_id);
            break;

        case START_TRIAL:
            if (typeof current_game == "undefined") return;

            current_game.run_trial(data.sender_id);
            break;

        case TRIAL_FINISHED:
            if (typeof current_game == "undefined") return;

            current_game.finish_trial(data.action_data, data.sender_id);
            break;

        case EXPERIMENT_DONE:
            if (typeof current_game == "undefined") return;

            current_game.show_results(data.sender_id);
            break;

        case GAME_RESET:
            if (typeof current_game == "undefined") return;

            // Here we check if we are calling the reset because someone has correctly finished the game and
            // now we should print a reward label.
            if (typeof data.action_data != 'undefined' && data.action_data.reason == "finished"){
                socket.emit('frontend_action', {
			              "action_type": RES_PRINT_LABEL,
			              "action_data": current_game.get_stats(data.sender_id)
		            });
            }

            if(current_game.reset(data.sender_id)){
                current_game = undefined;
            }

            socket.emit('fronend_action', {action_type: RES_GAME_UNLOCKED, action_data: {}});
            break;

        default: console.log("Unknown request: " + data.action_type);
        }

    });

    // =========================================================================
    // Initialise the frontend for the players
    // =========================================================================

    var chart = document.getElementById("vis");
    console.log(chart);
    console.log(chart.offsetWidth);

    var player_assets = [
        {
            "chart": barchart(chart.offsetWidth, window.innerHeight * .8, 7),
            "results": results_vis(chart.offsetWidth, window.innerHeight * .8)
        },
        {
            "chart": barchart(chart.offsetWidth, window.innerHeight * .8, 7),
            "results": results_vis(chart.offsetWidth, window.innerHeight * .8)
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

        d3.select(".cover_info").html("Connected tablet " + (player_index + 1) + "/" + MAX_PLAYERS);

        player["index"] = player_index;
        player["assets"] = player_assets[player_index];

        // Add the player to the game
        players[player.id] = player;

        console.log("Assigned Player ", player_index + 1);
        console.log(players);

        // If every tablet is connected
        if(player_index == MAX_PLAYERS - 1){
            reset_cover();
        }
    }

    function pause_player(player){
        console.log("pausing " + player);
    }

    function initialise_game(player_id){

        var current_players = [players[player_id]];
        var current_players_idx = [player_id];

        var participant_data = {};

        var trials_started = false;

        var study_data = [[], []];

        var stats = {};

        var num_trials_finished = 0;

        var semaphore_counter = 0;

        var semaphore_max = MAX_PLAYERS - 1;


        function game(){
            console.log("Starting new game!");

            socket.emit("frontend_action", {
                action_type: RES_GAME_STARTED,
                action_data: {}
            });

            display_information();
        }

        var id_to_index = function(id){
            return current_players_idx.indexOf(id);
        };

        var display_information = function(){
            d3.select(".cover_info")
                .html('<video width=1600 height=900 autoplay><source src="resources/tutorial.mp4" /></video>');
        };

        game.reset = function(){

            // Check whether we are in a multiplayer game
            if ( current_players.length == 2 ) {
                // If this is the first time this function is called withing the current setting, set up the semaphore so that the
                // function only runs if we have confirmation that everyone has called it.
                semaphore_counter = semaphore_counter == 0 ? semaphore_max : semaphore_counter - 1;
                if(semaphore_counter == 0){
                    socket.emit("frontend_action", {action_type: RES_MULTIPLAYER_PROGRESS, action_data:"move"});
                }
                else{
                    return false;
                }
            }

            d3.select(".cover").transition().duration(1000)
                .style("height", "100%")
                .on("end", () => {reset_cover();});

            return true;
        };

        game.join_player = function(id){

            if( current_players.length == 1 && !trials_started && player_id != id){

                current_players.push(players[id]);
                current_players_idx.push(id);
                console.log(id + " joined!");
            }
            else{
                console.log(id + " cannot join this game!");
            }
        };

        game.get_stats = function (id) {
            console.log("STATS:", stats);
            return stats[id];
        };

        game.show_results = function(id){

            // Check whether we are in a multiplayer game
            if ( current_players.length == 2 ) {
                // If this is the first time this function is called withing the current setting, set up the semaphore so that the
                // function only runs if we have confirmation that everyone has called it.
                semaphore_counter = semaphore_counter == 0 ? semaphore_max : semaphore_counter - 1;
                if(semaphore_counter == 0){
                    socket.emit("frontend_action", {action_type: RES_MULTIPLAYER_PROGRESS, action_data:"move"});
                }
                else{
                    return;
                }
            }

            let index = id_to_index(id);
            let player = current_players[index];

            d3.select(".cover_info").html("");

            d3.select(".cover").transition().duration(1000)
                .style("height", "100%")
                .on("end", () => {
                    d3.select("#vis_chart")
                        .html("")
                        .style("background-color", "#27567b");

                    let offsetWidth = document.getElementById("vis").offsetWidth;

                    current_players_idx.forEach(idx => {

                        let p = players[idx];

                        p["assets"]["results"].width(offsetWidth * (current_players.length == 2 ? .5 : 1));
                        p["assets"]["results"].offsetX(offsetWidth * (current_players.length == 2 ? p["index"] * .5 : 0));

                        // Creating the player histograms will also calculate the statistics we need, so we can retrieve them here.
                        stats[idx] = p["assets"]["results"]("#vis_chart",
                                                            study_data[id_to_index(idx)]);

                        // Have the study data saved
                        socket.emit("frontend_action", {
                            action_type: RES_SAVE_DATA,
                            action_data: {
                                backup: {
                                    participant: participant_data[idx],
                                    trials: study_data[id_to_index(idx)]
                                },
                                stats: stats[idx]
                            }
                        });

                    });

                    d3.select(".cover").transition().duration(1000)
                        .style("height", "0%");


                });
        };

        game.begin_study = function(participant, id){

            participant_data[id] = participant;

            // Check whether we are in a multiplayer game
            if ( current_players.length == 2 ) {
                // If this is the first time this function is called withing the current setting, set up the semaphore so that the
                // function only runs if we have confirmation that everyone has called it.
                semaphore_counter = semaphore_counter == 0 ? semaphore_max : semaphore_counter - 1;
                if(semaphore_counter == 0){
                    socket.emit("frontend_action", {action_type: RES_MULTIPLAYER_PROGRESS, action_data:"move"});
                }
                else{
                    return;
                }
            }
            // If we are beginning the study in single player mode, we will lock the other tablet
            else{
                socket.emit("frontend_action", {action_type: RES_GAME_LOCKED, action_data: {}});
            }

            d3.select(".cover_info").html("");
            console.log(current_players);
            current_players.forEach(player => {
                let offsetWidth = document.getElementById("vis").offsetWidth;
                player["assets"]["chart"].width(offsetWidth * (current_players.length == 2 ? .5 : 1));
                player["assets"]["chart"].offsetX(offsetWidth * (current_players.length == 2 ? player["index"] * .5 : 0));
                player["assets"]["chart"]("#vis_chart");
            });

            d3.select(".cover").transition().duration(1000)
                .style("height", "2%");

        };

        game.run_trial = function(id){
            if(num_trials_finished % 2 == 0){
                num_trials_finished = 0;

                let index = id_to_index(id);
                current_players[index]["assets"]["chart"].clear();
            }
        };

        game.finish_trial = function(data, id){

            // Check whether we are in a multiplayer game
            if ( current_players.length == 2 ) {
                // If this is the first time this function is called withing the current setting, set up the semaphore so that the
                // function only runs if we have confirmation that everyone has called it.
                semaphore_counter = semaphore_counter == 0 ? semaphore_max : semaphore_counter - 1;
                if(semaphore_counter == 0){
                    socket.emit("frontend_action", {action_type: RES_MULTIPLAYER_PROGRESS, action_data:"trial finished"});

                    num_trials_finished++;
                }
            }
            else{
                num_trials_finished++;
            }

            let index = id_to_index(id);
            current_players[index]["assets"]["chart"].update(data);


            // Record the data so that we can use it for the visualisation at the end and for printing
            study_data[index].push(data);

        };

        return game;
    }

    // Singleton instance of a multiplayer game.
    var current_multi_player_game = undefined;

    function initialise_multi_player_game(){

        var num_trials_finished = 0;

	      var stats = {};

        var study_data = [[], []];

        var semaphore_counter = 0;

        var semaphore_max = MAX_PLAYERS - 1;

        function multi_player_game(){
            console.log("Starting multiplayer game!");
            console.log(semaphore_counter);

            // If this is the first time this function is called withing the current setting, set up the semaphore so that the
            // function only runs if we have confirmation that everyone has called it.
            semaphore_counter = semaphore_counter == 0 ? semaphore_max : semaphore_counter - 1;
            console.log(semaphore_counter);

            // If everyone has called this function, the counter will be 0 after the line above.
            if(semaphore_counter == 0){
                socket.emit("frontend_action", {action_type: RES_MULTIPLAYER_PROGRESS, action_data:"move"});
                display_information();
            }
        }

        var display_information = function(){
            Object.keys(players).forEach(k => {
                players[k]["assets"]["cover"].select(".cover_info")
                    .html('<iframe width="560" height="315" src="https://www.youtube.com/embed/NPvMUpcxPSA?rel=0&amp;controls=0" frameborder="0" allow="autoplay; encrypted-media" allowfullscreen></iframe>');
            });
        };

        multi_player_game.get_stats = function (player_id) {
            return stats[player_id];
        };

        multi_player_game.reset = function(player_id){
            console.log("Resetting!");

            let player = players[player_id];

            player["assets"]["cover"].transition().duration(1000)
                .style("height", "100%");

            reset_cover(player);
        };

        multi_player_game.show_results = function(player_id){

	          let player = players[player_id];

	          player["assets"]["cover"].select(".cover_info").html("");

	          player["assets"]["cover"].transition().duration(1000)
                .style("height", "100%")
                .on("end", () => {
                    player["assets"]["vis_base"].select("svg").html("");
                    player["assets"]["vis_base"].select("svg").style("background-color", "#27567b");
                    stats[player_id] = player["assets"]["results"](player["assets"]["vis_id"] + " .chart",
                        study_data[player["index"]]);

                    player["assets"]["cover"].transition().duration(1000)
                .style("height", "0%");
                });
        };

        multi_player_game.begin_study = function(){

            // If this is the first time this function is called withing the current setting, set up the semaphore so that the
            // function only runs if we have confirmation that everyone has called it.
            semaphore_counter = semaphore_counter == 0 ? semaphore_max : semaphore_counter - 1;

            // If everyone has called this function, the counter will be 0 after the line above.
            if(semaphore_counter == 0){
                socket.emit("frontend_action", {action_type: RES_MULTIPLAYER_PROGRESS, action_data:"move"});

                Object.keys(players).forEach(k => {
                    let player = players[k];

                    player["assets"]["cover"].select(".cover_info").html("");
                    player["assets"]["chart"](player["assets"]["vis_id"] + " .chart");

                    player["assets"]["cover"].transition().duration(1000)
                        .style("height", "2%");
                });
            }
        };

        multi_player_game.run_trial = function(player_id){
            if(num_trials_finished % 2 == 0){
                num_trials_finished = 0;

                players[player_id]["assets"]["chart"].clear();
            }
        };

        multi_player_game.finish_trial = function(data, player_id){

            // If this is the first time this function is called withing the current setting, set up the semaphore so that the
            // function only runs if we have confirmation that everyone has called it.
            semaphore_counter = semaphore_counter == 0 ? semaphore_max : semaphore_counter - 1;

            // If everyone has called this function, the counter will be 0 after the line above.
            if(semaphore_counter == 0){
	              // Report that both players have now finished the trial
	              socket.emit("frontend_action", {action_type: RES_MULTIPLAYER_PROGRESS, action_data:"trial finished"});

		            num_trials_finished++;
            }

            players[player_id]["assets"]["chart"].update(data);

            // Record the data so that we can use it for the visualisation at the end and for printing
            study_data[players[player_id]["index"]].push(data);

        };

        // Check if the singleton instance is present already or not
        if (typeof current_multi_player_game == 'undefined'){
            current_multi_player_game = multi_player_game;

            return multi_player_game;
        }
        else{
            return current_multi_player_game;
        }
    }

    function reset_cover(){

        console.log("re1");
        d3.select("#vis_chart")
            .style("background-color", "white")
            .html("");

        // Update the visuals
        d3.select(".cover_info")
            .html("")
            .append("span")
            .html("Come and try our experiment!");

        console.log("re2");
        d3.select(".cover_info")
            .append("p")
            .style("padding-top", "20px")
            //.style("border-top", "2px solid white")
            .html("Did you know?");

        console.log("re3");
        var fact_box = d3.select(".cover_info")
            .append("p")
            .style("font-weight", "bold")
            .style("line-height", "50px")
            .html(fun_facts[0]);

        // Switch the fun facts at regular intervals
        fun_fact_interval = setInterval(() => {
            fact_box.transition().duration(500)
                .style("opacity", 0)
                .on("end", () => {
                    if(fun_fact_index == fun_facts.length) fun_fact_index = 0;

                    fact_box.html(fun_facts[fun_fact_index++]);

                    fact_box.transition().duration(500)
                        .style("opacity", 1);
                });
        }, 10000);
    }
});
