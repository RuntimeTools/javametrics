/*******************************************************************************
 * Copyright 2017 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

// Line chart for showing memory data
// Process and system data displayed

// Define graph axes
var mempools_xScale = d3.time.scale().range([0, memPoolsGraphWidth]);
var mempools_yScale = d3.scale.linear().range([graphHeight, 0]);

var mempools_xAxis = d3.svg.axis()
    .scale(mempools_xScale)
    .orient("bottom")
    .ticks(3)
    .tickFormat(getTimeFormat());

var mempools_yAxis = d3.svg.axis()
    .scale(mempools_yScale)
    .orient("left")
    .ticks(8)
    .tickFormat(function(d) {
        return d3.format(".2s")(d * 1024 *1024);
    });

// mempoolsory data storage
var mempoolsData = [];

// Set input domain for both x and y scales
mempools_xScale.domain(d3.extent(mempoolsData, function(d) {
    return d.date;
}));

mempools_yScale.domain([0, Math.ceil(d3.extent(mempoolsData, function(d) {
    return d.system;
})[1] / 100) * 100]);


// Define the used heap  line
var mempools_usedHeapLine = d3.svg.line()
    .x(function(d) {
        return  mempools_xScale(d.date);
    })
    .y(function(d) {
        return mempools_yScale(d.used);
    });

// Define the used native line
var mempools_usedNonHeapLine = d3.svg.line()
    .x(function(d) {
        return mempools_xScale(d.date);
    })
    .y(function(d) {
        return mempools_yScale(d.native);
    });

//Define the total used memory line
var mempools_totalUsedLine = d3.svg.line()
    .x(function(d) {
        return mempools_xScale(d.date);
    })
    .y(function(d) {
        return mempools_yScale(d.total);
    });

//Define the used heap after gc line
var mempools_usedHeapAfterGCLine = d3.svg.line()
    .x(function(d) {
        return mempools_xScale(d.date);
    })
    .y(function(d) {
        return mempools_yScale(d.aftergc);
    });

// Define the mempoolsory SVG
var mempoolsSVG = d3.select("#memPoolsDiv")
    .append("svg")
    .attr("width", memPoolsCanvasWidth)
    .attr("height", canvasHeight)
    .attr("class", "mempoolsChart")

var mempoolsTitleBox = mempoolsSVG.append("rect")
    .attr("width", memPoolsCanvasWidth)
    .attr("height", 30)
    .attr("class", "titlebox")

// Define the mempoolsory Chart
var mempoolsChart = mempoolsSVG.append("g")
    .attr("class", "mempoolsGroup")
    .attr("transform",
        "translate(" + margin.left + "," + margin.top + ")");

// Add the system line path.
mempoolsChart.append("path")
    .attr("class", "usedHeapLine")
    .attr("d", mempools_usedHeapLine(mempoolsData));

// Add the process line path.
mempoolsChart.append("path")
    .attr("class", "usedNonHeapLine")
    .attr("d", mempools_usedNonHeapLine(mempoolsData));

//Add the process line path.
mempoolsChart.append("path")
    .attr("class", "totalUsedLine")
    .attr("d", mempools_totalUsedLine(mempoolsData));

//Add the used after gc path.
mempoolsChart.append("path")
    .attr("class", "usedHeapAfterGCLine")
    .attr("d", mempools_usedHeapAfterGCLine(mempoolsData));

// Add the X Axis
mempoolsChart.append("g")
    .attr("class", "xAxis")
    .attr("transform", "translate(0," + graphHeight + ")")
    .call(mempools_xAxis);

// Add the Y Axis
mempoolsChart.append("g")
    .attr("class", "yAxis")
    .call(mempools_yAxis);

// Add the title
mempoolsChart.append("text")
    .attr("x", 7 - margin.left)
    .attr("y", 15 - margin.top)
    .attr("dominant-baseline", "central")
    .style("font-size", "18px")
    .text("Heap");

// Add the placeholder text
var mempoolsChartPlaceholder = mempoolsChart.append("text")
    .attr("x", memPoolsGraphWidth/2)
    .attr("y", graphHeight/2)
    .attr("text-anchor", "middle")
    .style("font-size", "18px")
    .text("No Data Available");

// Add the used colour box
mempoolsChart.append("rect")
    .attr("x", 0)
    .attr("y", graphHeight + margin.bottom - 15)
    .attr("class", "colourbox1")
    .attr("width", 10)
    .attr("height", 10)

// Add the Used Heap label
var mempoolsUsedLabel = mempoolsChart.append("text")
    .attr("x", 15)
    .attr("y", graphHeight + margin.bottom - 5)
    .attr("text-anchor", "start")
    .attr("class", "lineLabel")
    .text("Used Heap Memory");

// Add the total used colour box
mempoolsChart.append("rect")
    .attr("x", mempoolsUsedLabel.node().getBBox().width + 25)
    .attr("y", graphHeight + margin.bottom - 15)
    .attr("width", 10)
    .attr("height", 10)
    .attr("class", "colourbox2")
    
// Add the native label
var memPoolsNativeLabel = mempoolsChart.append("text")
    .attr("x", mempoolsUsedLabel.node().getBBox().width + 40)
    .attr("y", graphHeight + margin.bottom - 5)
    .attr("class", "lineLabel")
    .text("Used Native Memory");

//Add the native used colour box
mempoolsChart.append("rect")
    .attr("x", mempoolsUsedLabel.node().getBBox().width + 175)
    .attr("y", graphHeight + margin.bottom - 15)
    .attr("width", 10)
    .attr("height", 10)
    .attr("class", "colourbox3")

// Add the total label
var memPoolsTotalLabel = mempoolsChart.append("text")
    .attr("x", mempoolsUsedLabel.node().getBBox().width + 190)
    .attr("y", graphHeight + margin.bottom - 5)
    .attr("class", "lineLabel")
    .text("Total Used Memory");

//Add the used after gc colour box
mempoolsChart.append("rect")
    .attr("x", mempoolsUsedLabel.node().getBBox().width + 325)
    .attr("y", graphHeight + margin.bottom - 15)
    .attr("width", 10)
    .attr("height", 10)
    .attr("class", "colourbox4")

// Add the used after gc label
mempoolsChart.append("text")
    .attr("x", mempoolsUsedLabel.node().getBBox().width + 340)
    .attr("y", graphHeight + margin.bottom - 5)
    .attr("class", "lineLabel")
    .text("Used Heap After GC");


function resizeMemPoolsChart() {
    var chart = d3.select(".mempoolsChart")
    chart.attr("width", memPoolsCanvasWidth);
    mempools_xScale = d3.time.scale().range([0, memPoolsGraphWidth]);
    mempools_xAxis = d3.svg.axis().scale(mempools_xScale)
        .orient("bottom").ticks(3).tickFormat(getTimeFormat());

    mempoolsTitleBox.attr("width", memPoolsCanvasWidth)

    // Redraw lines and axes
    mempools_xScale.domain(d3.extent(mempoolsData, function(d) {
        return d.date;
    }));
    chart.select(".usedHeapLine")
        .attr("d", mempools_usedHeapLine(mempoolsData));
    chart.select(".usedNonHeapLine")
        .attr("d", mempools_usedNonHeapLine(mempoolsData));    
    chart.select(".totalUsedLine")
    	.attr("d", mempools_totalUsedLine(mempoolsData));
    chart.select(".usedHeapAfterGCLine")
		.attr("d", mempools_usedHeapAfterGCLine(mempoolsData));
    chart.select(".xAxis").call(mempools_xAxis);
    chart.select(".yAxis").call(mempools_yAxis);
}

function updateMemPoolsData(mempoolsRequest) {
	// Get the data again
	    data = JSON.parse(mempoolsRequest);  // parses the data into a JSON array
      	if (!data)
	        return

        var d = data;
        d.date = new Date(+d.time);
        d.used  = +d.usedHeap  / (1024 * 1024);
        d.native  = +d.usedNative  / (1024 * 1024);
        d.aftergc = +d.usedHeapAfterGC  / (1024 * 1024);
        d.total = d.used + d.native;

        mempoolsData.push(d)

        if(mempoolsData.length === 2) {
            // second data point - remove "No Data Available" label
            mempoolsChartPlaceholder.attr("visibility", "hidden");
        }

	    // Only keep 30 minutes of data
	    var currentTime = Date.now()
	    var d = mempoolsData[0]
	    if (d === null)
		    return;

        while (d.hasOwnProperty('date') && d.date.valueOf() + maxTimeWindow < currentTime) {
            mempoolsData.shift()
            d = mempoolsData[0]
        }

        // Set the input domain for the axes
        mempools_xScale.domain(d3.extent(mempoolsData, function(d) {
            return d.date;
        }));
        mempools_yScale.domain([0, Math.ceil(d3.extent(mempoolsData, function(d) {
            return d.total;
        })[1] / 100) * 100]);

        mempools_xAxis.tickFormat(getTimeFormat());

        // Select the section we want to apply our changes to
        var selection = d3.select(".mempoolsChart");

        // Make the changes
        selection.select(".usedHeapLine")
        	.attr("d", mempools_usedHeapLine(mempoolsData));
        selection.select(".usedNonHeapLine")
        	.attr("d", mempools_usedNonHeapLine(mempoolsData));    
        selection.select(".totalUsedLine")
    		.attr("d", mempools_totalUsedLine(mempoolsData));
        selection.select(".usedHeapAfterGCLine")
        	.attr("d", mempools_usedHeapAfterGCLine(mempoolsData));
        selection.select(".xAxis")
            .call(mempools_xAxis);
        selection.select(".yAxis")
            .call(mempools_yAxis);

}
