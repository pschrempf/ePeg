// Start doing things when the page has loaded completely.
document.addEventListener("DOMContentLoaded", (e) => {

    var phase = 0;

    var chart1 = barchart(.3 * window.innerWidth, 600, 7);
    var chart2 = barchart(.3 * window.innerWidth, 600, 7);

    chart1("#vis1");
    chart2("#vis2");

    // Make connection
    var socket = io();

    socket.on('peg_info', function(data){
            chart1.update_chart(data);
            chart2.update_chart(data);
    });

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
