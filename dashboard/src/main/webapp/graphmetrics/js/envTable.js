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

// Table for displaying environment parameters

// Width of environment div
var envDivCanvasWidth = $("#envDiv").width() - 8;

var tableRowHeight = 30;
var tableRowWidth = 170;

// Define the environment chart space
var envSVG = d3.select("#envDiv")
    .append("svg")
    .attr("width", envDivCanvasWidth)
    .attr("height", canvasHeight)
    .attr("class", "envData")

var envTitleBox = envSVG.append("rect")
    .attr("width", envDivCanvasWidth)
    .attr("height", 30)
    .attr("class", "titlebox")

envSVG.append("text")
    .attr("x", 7)
    .attr("y", 15)
    .attr("dominant-baseline", "central")
    .style("font-size", "18px")
    .text("Environment");

var paragraph = envSVG.append("g")
    .attr("class", "envGroup")
    .attr("transform",
        "translate(" + 20 + "," + (margin.top + 10) + ")");


var envTableIsFullScreen = false;

// Add the maximise button
var envResize = envSVG.append("image")
    .attr("x", envDivCanvasWidth - 30)
    .attr("y", 4)
    .attr("width", 24)
    .attr("height", 24)
    .attr("xlink:href","graphmetrics/images/maximize_24_grey.png")
    .attr("class", "maximize")
    .on("click", function(){
        envTableIsFullScreen = !envTableIsFullScreen
        d3.selectAll(".hideable").classed("invisible", envTableIsFullScreen);
        d3.select("#envDiv").classed("fullscreen", envTableIsFullScreen)
            .classed("invisible", false); // remove invisible from this chart
        if(envTableIsFullScreen) {
            d3.select(".envData .maximize").attr("xlink:href","graphmetrics/images/minimize_24_grey.png")
            // Redraw this chart only
            resizeEnvTable();
        } else {
            d3.select(".envData .maximize").attr("xlink:href","graphmetrics/images/maximize_24_grey.png")
            canvasHeight = 250;
            // Redraw all
            resize();
        }
    })
    .on("mouseover", function() {
        if(envTableIsFullScreen) {
            d3.select(".envData .maximize").attr("xlink:href","graphmetrics/images/minimize_24.png")
        } else {
            d3.select(".envData .maximize").attr("xlink:href","graphmetrics/images/maximize_24.png")
        }
    })
    .on("mouseout", function() {
        if(envTableIsFullScreen) {
            d3.select(".envData .maximize").attr("xlink:href","graphmetrics/images/minimize_24_grey.png")
        } else {
            d3.select(".envData .maximize").attr("xlink:href","graphmetrics/images/maximize_24_grey.png")
        }
    });

function populateEnvTable(envRequestData) {
        envData = JSON.parse(envRequestData);
        if (envData == null) return

        function tabulate(data) {

            // create a row for each object in the data
            var rows = paragraph.selectAll('text')
                .data(data)
                .enter()
                .append('text')
                .style('font-size', '14px')
                .attr("transform", function(d, i) {
                    return "translate(0," + (i * tableRowHeight) + ")";
                });

            // create a cell in each row for each column
            var cells = rows.selectAll('tspan')
                .data(function (row) {
                    return ['Parameter', 'Value'].map(function (column) {
                        return {column: column, value: row[column]};
                    });
                })
                .enter()
                .append('tspan')
                .attr("x", function(d, i) {
                    return i * tableRowWidth; // indent second element for each row
                })
                .text(function (d) { return d.value; });
        }

        // render the table(s)
        tabulate(envData); // 2 column table

}

function resizeEnvTable() {
	if(envTableIsFullScreen) {
        envDivCanvasWidth = $("#envDiv").width() - 30; // -30 for margins and borders
        canvasHeight = $("#envDiv").height() - 100;
    } else {
        envDivCanvasWidth = $("#envDiv").width() - 8;
    }
    envResize.attr("x", envDivCanvasWidth - 30).attr("y", 4);
    envSVG.attr("width", envDivCanvasWidth)
        .attr("height", canvasHeight);
    envTitleBox.attr("width", envDivCanvasWidth)
}
