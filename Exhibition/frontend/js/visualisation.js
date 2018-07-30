/* School of Medicine colours:

   blue: #5f819e
   light blue: #6dbcdb
   gray: #757575

   Bathing in the Sun
   dark orange: #c14b22	(193,75,34)
   light orange: #f28143	(242,129,67)
   yellow: #fde033	(253,224,51)
   deep sea: #106f7c	(16,111,124)
   light blue: #2199c3	(33,153,195)

   Muted neutral tones
   navy: #27567b	(39,86,123)
   dark red: #b63b3b	(182,59,59)
   yellow: #f9c414	(249,196,20)
   beige: #f7ebd8	(247,235,216)
   dark gray: #505050	(80,80,80)
*/


// Closure that when called will create a barchart object
function barchart(width, height, num_bars, palette){

    // References we will hold on to so that internal functions can use them
    var vis;
    var bars;

    // Constants for reference
    const bar_width = height / (2 * (num_bars + 2));
    const margin = {top: 30, right: 10, bottom: 40, left: 60};

    const default_palette = {background: "#f7ebd8",
                             right: "#27567b",
                             left: "#b63b3b",
                             frame: "#505050",
                             emphasis: "#f9c414"
                            };

    // If the user has not provided us with a palette, use the default one
    palette = palette || default_palette;

    var x_scale;
    var y_scale;

    var x_axis;
    var y_axis;

    function chart(container_selector){

        create_axes();

        // Create the visualisation group
        vis = d3.select(container_selector).append("g");

        // Append the separator for the bars
        vis.append("rect")
            .attr("width", "4px")
            .attr("height", (height * .8))
            .attr("y", height * .05)
            .attr("x", width * .5 - 2)
            .style("fill", "black")
            .style("stroke", "none");

        // Create a group for the bars
        bars = vis.append("g")
            .attr("class", "bar_group")
            .attr("transform", "translate(" + width * .5 + ", " + height * .05 + ")");

        //create_legend();

    }

    /*
     * This function will set up the scale and axis objects
     */
    var create_axes = function () {

        // Create a band scale for the bars (Y-axis will be the pegs)
        y_scale = d3.scaleBand()
            .domain([1, 2, 3, 4, 5, 6, 7])
            .rangeRound([0, height * .8])
            .paddingInner([(height * .8 - num_bars * bar_width) / (num_bars - 1)]);

        // Create linear scale for the peg deltas
        x_scale = d3.scaleLinear()
            .domain([0, 2000])
            .range([0, width]);

    };

    var create_legend = function (){

        var legend_box = vis.append("g")
            .attr("transform", "translate(" + 80 + "," + -height + ")");

        legend_box.append("svg:image")
            .attr("width", 40)
            .attr("height", 100)
            .attr("xlink:href", "img/left_hand.png");

        legend_box.append("text")
            .attr("x", 60)
            .attr("y", 60)
            .text(() => "Left Hand");

        legend_box.append("svg:image")
            .attr("x", 250)
            .attr("width", 40)
            .attr("height", 100)
            .attr("xlink:href", "img/right_hand.png");

        legend_box.append("text")
            .attr("x", 310)
            .attr("y", 60)
            .text(() => "Right Hand");

    };

    chart.update_chart = function(data){

        var deltas = data.measurements.pegDeltas;
        var hand_used = data.handUsed;

        console.log(deltas);
        console.log(hand_used);

        var new_bars = bars.selectAll("rect." + hand_used);

        var old_bars = bars.selectAll("rect.old_" + hand_used)
            .data(new_bars.data());

            new_bars = new_bars.data(deltas);

        new_bars.enter().append("rect")
            .attr("class", hand_used)
            .attr("height", bar_width)
            .attr("width", 0)
            .attr("x", (d) => hand_used == "left" ? "-2" : "2")
            .attr("y", (d, i) => y_scale(i + 1))
            .transition().duration(750)
            .attr("width", (d) => x_scale(d))
            .attr("x", (d) => hand_used == "left" ? -x_scale(d) - 2 : 2);

        new_bars.transition().duration(750)
            .attr("width", (d) => x_scale(d))
            .attr("x", (d) => hand_used == "left" ? -x_scale(d) - 2 : 2);

    };

    chart.width = function(value){
        if(!arguments.length) return width;
        width = value;
        return chart;
    };

    chart.height = function(value){
        if(!arguments.length) return height;
        height = value;
        return height;
    };

    return chart;
}

/*
 * Closure for the results page visualisation.
 */
var results_vis = function(width, height){

    function results(container_selector){
    }

    results.width = function(value){
        if(!arguments.length) return width;
        width = value;
        return chart;
    };

    results.height = function(value){
        if(!arguments.length) return height;
        height = value;
        return height;
    };

    return results;
};
