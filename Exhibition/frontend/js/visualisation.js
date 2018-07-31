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

    const default_palette = {background: "#f7ebd8",
                             right: "#27567b",
                             left: "#b63b3b",
                             frame: "#505050",
                             emphasis: "#f9c414"
                            };

    const separator_width = 4;
    const hsw = separator_width / 2; // Half separator width

    const separator_height = height * .8;

    const separator_offset = {
        x: width * .5 - hsw,
        y: height * .05
    };

    const bar_width = separator_height / (2 * num_bars);

    const text_offset = 30;

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
            .attr("width", separator_width)
            .attr("height", separator_height)
            .attr("y", separator_offset.y)
            .attr("x", separator_offset.x)
            .style("fill", "black")
            .style("stroke", "none");

        // Create a group for the bars
        bars = vis.append("g")
            .attr("class", "bar_group")
            .attr("transform", "translate(" + (separator_offset.x + hsw) + ", " + separator_offset.y + ")");

        //create_legend();

    }

    /*
     * This function will set up the scale and axis objects
     */
    var create_axes = function () {

        // Create a band scale for the bars (Y-axis will be the pegs)
        y_scale = d3.scaleBand()
            .domain([1, 2, 3, 4, 5, 6, 7])
            .rangeRound([0, separator_height])
            .paddingInner([0.5]);

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

    chart.clear = function(){
        bars.selectAll("g text").remove();

        bars.selectAll("g").transition().duration(750)
            .remove()
            .select("rect")
            .attr("width", 0)
            .attr("x", 0);

    };

    chart.update = function(data){

        var deltas = data.measurements.pegDeltas;
        var hand_used = data.handUsed;

        console.log(deltas);
        console.log(hand_used);

        var bar_selection = bars.selectAll("g." + hand_used)
            .data(deltas);

        var selection_enter = bar_selection.enter()
            .append("g")
            .attr("class", hand_used)
            .attr("transform", (d, i) =>
                  "translate(" +
                  (hand_used == "left" ? -hsw : hsw) +
                  "," +
                  y_scale(i + 1) +
                  ")");

        selection_enter.append("rect")
            .attr("height", bar_width)
            .attr("width", 0)
            .transition().duration(750)
            .attr("width", (d) => x_scale(d))
            .attr("x", (d) => hand_used == "left" ? -x_scale(d) : 0);

        selection_enter.append("text")
            .attr("text-anchor", () => hand_used == "left" ? "end": "start")
            .attr("alignment-baseline", "middle")
            .attr("x", () => hand_used == "left" ? -text_offset : text_offset)
            .attr("y", bar_width / 2 )
            .text((d) => d + "ms")
            .transition().duration(750)
            .attr("x", (d) => hand_used == "left" ? -x_scale(d) - text_offset : x_scale(d) + text_offset);

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

    // Constants
    const handedness_scale_width = width * .6;
    const handedness_scale_height = 40;


    var scene;
    var handedness_scale;
    var histogram_group;

    var stats;

    function results(container_selector, peg_data){

        scene = d3.select(container_selector).append("g");

        handedness_scale = scene.append("g")
            .attr("class", "handedness_scale")
            .attr("transform", "translate(" + width * .1 + "," + 200 + ")");

        histogram_group = scene.append("g");

        stats = calculate_statistics(peg_data);

        make_handedness_scale(handedness_scale);

        make_histogram(histogram_group);
    }

    var calculate_statistics = function(peg_data){
        var left_avg =
            peg_data.filter(data => data.handUsed == "left")
            .map(data => data.measurements.sumTime)
            .reduce((x, y) => x + y) / peg_data.length * 2;

        var right_avg =
            peg_data.filter(data => data.handUsed == "right")
            .map(data => data.measurements.sumTime)
            .reduce((x, y) => x + y) / peg_data.length * 2;

        // The peg quotient formula is 2(R-L)/(R+L)
        var pegQ = 2 * (right_avg - left_avg) / (right_avg + left_avg);

        console.log(left_avg);
        console.log(right_avg);
        console.log(pegQ);

        return {
            pegQ: pegQ
        };
    };

    var make_handedness_scale = function(scale_container){

        var svgDefs = scale_container.append('defs');
        var mainGradient = svgDefs.append('linearGradient')
            .attr('id', 'mainGradient');

        // Create the stops of the main gradient. Each stop will be assigned
        // a class to style the stop using CSS.

        mainGradient.append('stop')
            .attr('class', 'stop-left')
            .attr('offset', '0');

        mainGradient.append('stop')
            .attr('class', 'stop-right')
            .attr('offset', '1');

        scale_container.append("rect")
            .classed("filled", true)
            .attr("x", 60)
            .attr("y", 40)
            .attr("width", handedness_scale_width)
            .attr("height", handedness_scale_height);

        scale_container.append("svg:image")
            .attr("x", handedness_scale_width + 80)
            .attr("width", 40)
            .attr("height", 100)
            .attr("xlink:href", "img/right_hand.png");

        scale_container.append("svg:image")
            .attr("x", 0)
            .attr("width", 40)
            .attr("height", 100)
            .attr("xlink:href", "img/left_hand.png");

        var tick_scale = d3.scaleLinear()
            .domain([-2, 2])
            .range([0, handedness_scale_width]);

        scale_container.append("text")
            .attr("text-anchor", "middle")
            .attr("alignment-baseline", "hanging")
            .attr("x", 60 + tick_scale(stats.pegQ))
            .attr("y", 40 + handedness_scale_height)
            .html(() => "&#9650");
    };

    var make_histogram = function(histogram_container){
    };

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
