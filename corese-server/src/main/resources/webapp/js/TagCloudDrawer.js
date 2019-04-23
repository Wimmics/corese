"use strict";

import {SvgDrawer, SvgDrawerParameters} from "./SvgDrawer.js";
import {Enumeration} from "./Enumeration.js";

export class TagCloudDrawer extends SvgDrawer {

    static createParameters() {
        return new TagCloudParameters();
    }

    constructor(type) {
        super();
        this.type = type;
    }
    setupConfigurationPanel(divId, data) {
        const panel = d3.select(`#${divId}`);
        const div = panel.append("div");
        div.append("label").text("keyname");
        div.append("select").attr("id", "keyname_select").selectAll("option").data(data.head.vars)
            .enter().append("option").text(function(d) {return d;}).attr("value", function(d) { return d;})
        div.append("label").text("size");
        div.append("select").attr("id", "size_select").selectAll("option").data(data.head.vars)
            .enter().append("option").text(function(d) {return d;}).attr("value", function(d) { return d;});
        d3.select("#size_select").property("value", data.head.vars[1])

        div.append("label").text("Spiral Style");
        div.append("select").attr("id", "spiral_style").selectAll("option").data(["archimedean", "rectangular"])
            .enter().append("option").text(function(d) {return d;}).attr("value", function(d) { return d;});
        d3.select("#spiral_style").property("value", "archimedean")

        this.data = data;
    }

    draw(svgId) {
        super.draw(svgId);
        const keyName = this.parameters.label = d3.select("#keyname_select").node().value;
        const valueName = this.parameters.label = d3.select("#size_select").node().value;
        const spiralStyle = this.parameters.label = d3.select("#spiral_style").node().value;
        const freqTable = this.computeFrequency(this.data, keyName, valueName);

        var margin = {top: 30, right: 50, bottom: 30, left: 50};
        var width = 960 - margin.left - margin.right;
        var height =  500 - margin.top - margin.bottom;

        var svg = d3.select(svgId)
            .attr("width", width)
            .attr("height", height);
        svg.selectAll("g").remove();
        var g = svg.append("g")
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
        var color = d3.scaleOrdinal(d3.schemeCategory20);
        var categories = d3.keys(d3.nest().key(function(d) { return d.key; }).map(freqTable));
        const domainFreqTable = [freqTable[0].freq,freqTable[freqTable.length-1].freq];
        var fontSize = d3.scaleLog().domain(domainFreqTable).range([10,80]);

        var wordcloud = g.append("g")
            .attr('class','wordcloud')
            .attr("transform", "translate(" + width/2 + "," + height/2 + ")");
        var layout = d3.layout.cloud()
            .size([width, height])
            .words(freqTable)
            .padding(1)
            .fontSize(function(d,i) { return fontSize(d.freq); })
            .rotate(function() { return -60 + ~~(Math.random() * 5) * 30;})
            .fontWeight(["bold"])
            .text(function(d) { return d.key; })
            .spiral(spiralStyle) // "archimedean" or "rectangular"
            .on("end", (words) => { draw(words); this.setupZoomHandler(svg); } )
            .start();
        g.append("g")
            .attr("class", "axis")
            .attr("transform", "translate(0," + height + ")")
            .selectAll('text')
            .style("font-size","20px")
            .style('fill',function(d) { return color(d); })
            .style('font','sans-serif');

        function draw(words) {
            wordcloud.selectAll("text")
                .data(words)
                .enter().append("text")
                .attr("class","wod")
                .style("fill", function(d, i) { return color(i); })
                .style("font-size", function(d) { return d.size + "px"; })
                .style("font-family", function(d) { return d.font; })
                .attr("text-anchor", "middle")
                .attr("transform", function(d) { return "translate(" + [d.x, d.y] + ")rotate(" + d.rotate + ")"; })
                .text(function(d) { return d.text; })
                .on("click", function(d) {if (d.url) {window.open(d.url.value);}});
        };
    }
    setupZoomHandler(svg) {
        let g = svg.select("g");
        let zoomed = function () {
            g.attr("transform", d3.event.transform);
        };
        // for bug #62 (begin)
        let bbox = {x:0, y:0, width:0, height:0};
        try {
            let bbox = g.node().getBBox();
        } catch (exception) {
            console.warn(`An exception was caught: ${exception}`)
        }
        // for bug #62 (end)
        let extent = [[bbox.x - bbox.width, bbox.y - bbox.height], [bbox.x+2*bbox.width, bbox.y+2*bbox.height]];
        let zoom_handler = d3.zoom()
            .scaleExtent([0.1,10])
            // .translateExtent(extent)
            .on("zoom", zoomed);
        svg.call(zoom_handler);
    }

    computeFrequency(data, keyName, valueName) {
        let freqTable = {};
        for (let i=0; i<data.results.bindings.length; i++) {
            const entry = data.results.bindings[i];
            const key = entry[keyName].value;
            const value = Number(entry[valueName].value);
            console.assert(freqTable[key] === undefined, "Entry %s is not empty.", [entry]);
            freqTable[key] = {
                "value": value
            };
            if (entry.url !== undefined) {
                freqTable[key].url = entry.url;
            }
        }
        let freqArray = Object.keys(freqTable).map(function(key) {
            return {
                "key": key,
                "freq": freqTable[key].value,
                "url": freqTable[key].url
            };
        })
        freqArray.sort((a,b) => a.freq <= b.freq);
        return freqArray;
    }
};

export class TagCloudParameters extends SvgDrawerParameters {
    constructor() {
        super();
        this.parameters["varName"] = "default";
        this.parameters["spiralStyle"] = "rectangular";
    }
    setVarName(varName) {
        this.parameters["varName"] = varName;
        return this;
    }
    getVarName() {
        return this.parameters["varName"];
    }
    setSpiralStyle(spiralStyle) {
        this.parameters["spiralStyle"] = spiralStyle;
        return this;
    }
    getSpiralStyle() {
        return this.parameters["spiralStyle"];
    }
};
