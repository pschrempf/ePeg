document.addEventListener("DOMContentLoaded", (e) => {
    var rightTest = { id: 1,
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

    var leftTest = { id: 2,
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

    // Player action constants;
    const REQ_NEW_SINGLE_GAME = 0;
    const REQ_NEW_MULTI_GAME = 1;
    const REQ_START_TRIAL = 2;
    const REQ_TRIAL_FINISHED = 3;
    const REQ_DISPLAY_READ = 4;

    const TABLET_ID = "DUMMY_TABLET_ID";
    // Make connection to the server
    // We also advertise the fact that this is a tablet connection.
    // This can be accessed on the server by referring to
    // socket.handshake.query.client_type
    var socket = io({ query: {client_type:"tablet", tablet_id: TABLET_ID }});

    d3.select("#start_single_game").on("click", () => {
        socket.emit("player_action", {
            "sender_id": TABLET_ID,
            "action_type": REQ_NEW_SINGLE_GAME
        });
    });

    d3.select("#display_read").on("click", () => {
        socket.emit("player_action", {
            "sender_id": TABLET_ID,
            "action_type": REQ_DISPLAY_READ
        });
    });

    d3.select("#start_trial").on("click", () => {
        socket.emit("player_action", {
            "sender_id": TABLET_ID,
            "action_type": REQ_DISPLAY_READ
        });
    });

    d3.select("#start_trial").on("click", () => {
        socket.emit("player_action", {
            "sender_id": TABLET_ID,
            "action_type": REQ_START_TRIAL
        });
    });

    d3.select("#stop_left_trial").on("click", () => {
        socket.emit("player_action", {
            "sender_id": TABLET_ID,
            "action_type": REQ_TRIAL_FINISHED,
            "action_data": leftTest
        });
    });

    d3.select("#stop_right_trial").on("click", () => {
        socket.emit("player_action", {
            "sender_id": TABLET_ID,
            "action_type": REQ_TRIAL_FINISHED,
            "action_data": rightTest
        });
    });

    d3.select("#right_peg").on("click", () => {
        socket.emit("player_action", {
            "sender_id": TABLET_ID,
            "action_type": REQ_PEG_PLACED,
            "action_data": rightTest
        });
    });

    d3.select("#left_peg").on("click", () => {
        socket.emit("player_action", {
            "sender_id": TABLET_ID,
            "action_type": REQ_PEG_PLACED,
            "action_data": rightTest
        });
    });

});
