// Start doing things when the page has loaded completely.
document.addEventListener("DOMContentLoaded", (e) => {

    var chart1 = barchart(window.innerWidth - 120, 600, 7);

    chart1("#visualisation");

    // Make connection
    var socket = io();

    socket.on('peg_info', function(data){
        chart1.update_chart(data);
    });

});
