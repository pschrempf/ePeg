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

    console.log(width, height, num_bars, palette);

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

    const separator_height = height * .6;

    let separator_offset = {
        x: width * .5 - hsw,
        y: height * .2
    };

    let offsetX = 0;

    const bar_width = height * .4 / num_bars;

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
            .attr("x", offsetX + separator_offset.x)
            .style("fill", "black")
            .style("stroke", "none");

        // Create a group for the bars
        bars = vis.append("g")
            .attr("class", "bar_group")
            .attr("transform", "translate(" + (offsetX + separator_offset.x + hsw) + ", " + separator_offset.y + ")");

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
            .style("font-weight", "bold")
            .transition().duration(750)
            .attr("x", (d) => hand_used == "left" ? -x_scale(d) - text_offset : x_scale(d) + text_offset);

    };

    chart.width = function(value){
        if(!arguments.length) return width;
        width = value;
        separator_offset.x = width * .5 - hsw;
        return chart;
    };

    chart.height = function(value){
        if(!arguments.length) return height;
        height = value;
        return chart;
    };

    chart.offsetX = function(value){
        if(!arguments.length) return offsetX;
        offsetX = value;
        console.log(value);
        return chart;
    };

    return chart;
}

/*
 * Closure for the results page visualisation.
 */
var results_vis = function(width, height){


    // Constants

    const STATIC_PEGQ_DATA = "resources/alspac_pegboard_20180711.csv";
    const DYNAMIC_PEGQ_DATA = "resources/exhibition_data.csv";

    const NUM_HIST_BINS = 50;

    var handedness_scale_width = width * .7;
    const handedness_scale_height = 20;

    const handedness_scale_margin = 20;
    const handedness_scale_offset = {
        x: 60,
        y: 60
    };

    const histogram_height = height * .5;

    var offsetX = 0;

    var scene;
    var handedness_scale;
    var static_histogram_group;
    var dynamic_histogram_group;

    var histogram;
    var peg_index_scale;

    var stats;

    function results(container_selector, peg_data){

        peg_index_scale = d3.scaleLinear()
            .domain([-1.5, 1.5])
            .range([0, handedness_scale_width]);

        scene = d3.select(container_selector).append("g");

        handedness_scale = scene.append("g")
            .attr("class", "handedness_scale")
            .attr("transform", "translate(" + (offsetX + width * .1) + "," + (100 + histogram_height) + ")");

        static_histogram_group = scene.append("g")
            .attr("class", "histogram_group")
            .attr("transform", "translate(" + (offsetX + width * .1 + 60 ) + "," + 150 + ")");

        dynamic_histogram_group = scene.append("g")
            .attr("class", "histogram_group")
            .attr("transform", "translate(" + (offsetX + width * .1 + 60 ) + "," + 150 + ")");


        stats = calculate_statistics(peg_data);

        make_handedness_scale(handedness_scale);

        make_histogram(static_histogram_group, dynamic_histogram_group);

        return stats;
    }

    var calculate_statistics = function(peg_data){
        console.log(peg_data);
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

        var avg_time =
            peg_data.map(data => data.measurements.sumTime)
            .reduce((x, y) => x + y) / peg_data.length / 7; // Divide the total time by the total number of pegs, which is 7.

        console.log(peg_data);
        return {
            pegQ: pegQ,
            avg_time: avg_time
        };
    };

    var make_handedness_scale = function(scale_container){

        var svgDefs = scale_container.append('defs');
        var mainGradient = svgDefs.append('linearGradient')
            .attr('id', 'mainGradient');

        // =====================================================================
        // Create the stops of the main gradient. Each stop will be assigned
        // a class to style the stop using CSS.
        // =====================================================================
        mainGradient.append('stop')
            .attr('class', 'stop-left')
            .attr('offset', '0.3');

        mainGradient.append('stop')
            .attr('class', 'stop-center')
            .attr('offset', '0.5');

        mainGradient.append('stop')
            .attr('class', 'stop-right')
            .attr('offset', '0.7');

        // =====================================================================
        // Draw the scale
        // =====================================================================
        scale_container.append("rect")
            .classed("filled", true)
            .attr("x", handedness_scale_offset.x)
            .attr("y", handedness_scale_offset.y)
            .attr("width", handedness_scale_width)
            .attr("height", handedness_scale_height);

        scale_container.append("svg:image")
            .attr("x", handedness_scale_width + handedness_scale_offset.x + handedness_scale_margin)
            .attr("width", 40)
            .attr("height", 100)
            .attr("xlink:href", "img/right_hand.png");

        scale_container.append("text")
            .attr("x", handedness_scale_width + handedness_scale_offset.x + handedness_scale_margin)
            .attr("y", 110)
            .style("font-weight", "bold")
            .style("font-size", "30px")
            .style("fill", "white")
            .attr("text-anchor", "middle")
            .text("Right");


        scale_container.append("svg:image")
            .attr("x", 0)
            .attr("width", handedness_scale_offset.x - handedness_scale_margin)
            .attr("height", 100)
            .attr("xlink:href", "img/left_hand.png");

        scale_container.append("text")
            .attr("x", handedness_scale_offset.x - handedness_scale_margin)
            .attr("text-anchor", "middle")
            .style("fill", "white")
            .style("font-weight", "bold")
            .style("font-size", "30px")
            .attr("y", 110)
            .text("Left");

        // =====================================================================
        // Draw the indicators
        // =====================================================================

        scale_container.append("rect")
            .attr("width", "3")
            .attr("height", handedness_scale_height)
            .style("fill", "black")
            .attr("x", handedness_scale_offset.x + peg_index_scale(stats.pegQ) - 1)
            .attr("y", handedness_scale_offset.y );

        scale_container.append("text")
            .attr("text-anchor", "middle")
            .attr("alignment-baseline", "hanging")
            .attr("x", handedness_scale_offset.x + peg_index_scale(stats.pegQ))
            .attr("y", handedness_scale_offset.y + handedness_scale_height + 5)
            .style("fill", "white")
            .style("font-size", "30pt")
            .html(() => "&#8593");

        scale_container.append("text")
            .attr("text-anchor", "middle")
            .attr("alignment-baseline", "hanging")
            .attr("x", handedness_scale_offset.x + peg_index_scale(stats.pegQ))
            .attr("y", handedness_scale_offset.y + handedness_scale_height + 45)
            .style("fill", "white")
            .style("font-size", "25pt")
            .html(() => "Your laterality score: " + stats.pegQ.toFixed(2));


        // Triangle: #9650
        // Arrow: #8593
    };

    var make_histogram = function(static_histogram_container, dynamic_histogram_container){

        var y_scale = d3.scaleLinear()
            .range([0, histogram_height]);

        var color_scale = d3.scaleLinear()
            .domain([-0.3, 0, 0.3])
            .range(["#2199c3", "#eeeeee", "#ffd700"]); // "#1D9B30"

        histogram = d3.histogram()
            .value((d) => d["pegs.ndx"])
            .domain(peg_index_scale.domain())
            .thresholds(peg_index_scale.ticks([NUM_HIST_BINS]));

        // Create the histogram from the static data
        d3.csv(STATIC_PEGQ_DATA).then( (data) => {
            var bins = histogram(data);

            y_scale.domain([0, d3.max(bins, (d) => d.length)]);

            static_histogram_container.selectAll("rect")
                .data(bins).enter().append("rect")
                .attr("class", "bar")
                .attr("x", 1)
                .style("fill", (d) => color_scale((d.x1 + d.x0) / 2))
                .attr("transform", (d) =>
                      "translate(" + peg_index_scale(d.x0) + "," + (histogram_height - y_scale(d.length)) + ")")
                .attr("width", (d) =>
                      peg_index_scale(d.x1) - peg_index_scale(d.x0))
                //.attr("height", 0).transition().duration(4000)
                .attr("height", (d)=>y_scale(d.length));
        });

        // Create the histogram form the data collected from the people who
        // Performed the experiment at the exhibition.
        d3.csv(DYNAMIC_PEGQ_DATA).then( (data) => {
            var bins = histogram(data);

            y_scale.domain([0, d3.max(bins, (d) => d.length)]);

            dynamic_histogram_container.selectAll("rect")
                .data(bins).enter().append("rect")
                .attr("class", "bar")
                .attr("x", 1)
                .style("fill", "red")
                .attr("opacity", 0.5)
                .attr("transform", (d) =>
                      "translate(" + peg_index_scale(d.x0) + "," + (histogram_height - y_scale(d.length)) + ")")
                .attr("width", (d) =>
                      peg_index_scale(d.x1) - peg_index_scale(d.x0))
            //.attr("height", 0).transition().duration(4000)
                .attr("height", (d)=>y_scale(d.length));
        });
    };

    results.width = function(value){
        if(!arguments.length) return width;
        width = value;
        handedness_scale_width = width * .7;
        return results;
    };

    results.height = function(value){
        if(!arguments.length) return height;
        height = value;
        return results;
    };

    results.offsetX = function(value){
        if(!arguments.length) return offsetX;
        offsetX = value;
        return results;
    };


    return results;
};
