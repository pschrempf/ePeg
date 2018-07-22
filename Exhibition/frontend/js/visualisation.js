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
    const bar_width = width / (num_bars + 2);
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
        vis = d3.select(container_selector).append("svg")
            .attr("width", margin.left + width + margin.right)
            .attr("height", margin.top + height + margin.bottom)
            .attr("class", "chart")
            .append("g")
            .attr("transform", "translate( " + margin.left + "," + (margin.top + height) + ")");

        // Draw the axes
        vis.append("g")
            .attr("class", "axis")
            .call(x_axis);

        d3.selectAll("g.tick line").remove();
        d3.select(".domain").remove();

        vis.append("g")
            .attr("class", "axis")
            .attr("transform", "translate(0, " + (-height) + ")");
            //.call(y_axis);

        // Create a group for the bars
        bars = vis.append("g")
            .attr("class", "bar_group");

        create_legend();

    }

    /*
     * This function will set up the scale and axis objects
     */
    var create_axes = function () {

        // Create a band scale for the bars (X-axis will be the pegs)
        x_scale = d3.scaleBand()
            .domain([1, 2, 3, 4, 5, 6, 7])
            .rangeRound([0, width])
            .paddingOuter([bar_width/2])
            .paddingInner([10]);

        // Create linear scale for the peg deltas
        y_scale = d3.scaleLinear()
            .domain([0, 1000])
            .range([0, height]);

        // Now create the axes
        x_axis = d3.axisBottom(x_scale)
            .tickFormat((d) => "Peg " + d);

        /* y_axis = d3.axisLeft(y_scale)
            .tickFormat((d) => 2000 - d);
        */

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

        // old_bars.enter().append("rect")
        //     .attr("class", "stat_bar")
        //     .attr("height", 0)
        //     .attr("width", bar_width)
        //     .attr("x", (d, i) => x_scale(i + 1) - bar_width/2)
        //   .transition().duration(750)
        //     .attr("height", (d) => y_scale(d))
        //     .attr("y", (d) => -y_scale(d));

        // old_bars.transition().duration(750)
        //     .attr("height", (d) => y_scale(d))
        //     .attr("y", (d) => -y_scale(d));

        new_bars = new_bars.data(deltas);

        new_bars.enter().append("rect")
            .attr("class", hand_used)
            .attr("height", 0)
            .attr("width", bar_width/2)
            .attr("x", (d, i) => x_scale(i + 1) - (hand_used == "left" ? 1 : 0) * bar_width/2)
            .transition().duration(750)
            .attr("height", (d) => y_scale(d))
            .attr("y", (d) => -y_scale(d));

        new_bars.transition().duration(750)
            .attr("height", (d) => y_scale(d))
            .attr("y", (d) => -y_scale(d));


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
