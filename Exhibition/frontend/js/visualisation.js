// Closure that when called will create a barchart object
function barchart(width, height, num_bars){

    // References we will hold on to so that internal functions can use them
    var vis;
    var bars;

    // Constants for reference
    const bar_width = width / (num_bars + 2);
    const margin = {top: 30, right: 10, bottom: 40, left: 60};


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

        vis.append("g")
            .attr("class", "axis")
            .attr("transform", "translate(0, " + (-height) + ")")
            .call(y_axis);

        // Create a group for the bars
        bars = vis.append("g")
            .attr("class", "bar_group");

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
            .domain([0, 2000])
            .range([0, height]);

        // Now create the axes
        x_axis = d3.axisBottom(x_scale)
            .tickFormat((d) => "Peg " + d);
        y_axis = d3.axisLeft(y_scale)
            .tickFormat((d) => 2000 - d);

    };

    chart.update_chart = function(data){

        var deltas = data.measurements.pegDeltas;
        var hand_used = data.handUsed;

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
