function barchart(width, height){

    var vis;

    function chart(chart_selection){
        vis = chart_selection;

        vis.attr("width", width)
            .attr("height", height)

        vis.append("rect")
            .attr("width", 200)
            .attr("height", 100);
    }

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
