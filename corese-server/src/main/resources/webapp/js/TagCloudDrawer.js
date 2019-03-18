"use strict";

import {SvgDrawer, SvgDrawerParameters} from "./SvgDrawer.js";

export class TagCloudDrawer extends SvgDrawer {

    static createParameters() {
        return new TagCloudParameters();
    }

    draw(svgId) {
        super.draw(svgId);
        const keyName = this.parameters.getVarName();
        const freqTable = this.computeFrequency(this.data, keyName);

        var margin = {top: 30, right: 50, bottom: 30, left: 50};
        var width =2 * 960 - margin.left - margin.right;
        var height = 2 * 500 - margin.top - margin.bottom;

        var g = d3.select("svg")
            .append("g")
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
        var color = d3.scaleOrdinal(d3.schemeCategory20);
        var categories = d3.keys(d3.nest().key(function(d) { return d.key; }).map(freqTable));
        var fontSize = d3.scalePow().exponent(5).domain([0,1]).range([10,80]);
        var fontStyle = d3.scaleLinear().domain([categories]);

        var layout = d3.layout.cloud()
            .size([width, height])
            .timeInterval(2)
            .words(freqTable)
            // .rotate(function(d) { return 0; })
            .fontSize(function(d,i) { return d.freq*10; })
            //.fontStyle(function(d,i) { return fontSyle(Math.random()); })
            .rotate(function() { return ~~(Math.random() * 4) * 22.5; })
            .fontWeight(["bold"])
            .text(function(d) { return d.key; })
            .spiral(this.parameters.getSpiralStyle()) // "archimedean" or "rectangular"
            .on("end", draw)
            .start();


        var wordcloud = g.append("g")
            .attr('class','wordcloud')
            .attr("transform", "translate(" + width/2 + "," + height/2 + ")");

        g.append("g")
            .attr("class", "axis")
            .attr("transform", "translate(0," + height + ")")
            .selectAll('text')
            .style('font-size','20px')
            .style('fill',function(d) { return color(d); })
            .style('font','sans-serif');

        function draw(words) {
            wordcloud.selectAll("text")
                .data(words)
                .enter().append("text")
                .attr('class','word')
                .style("fill", function(d, i) { return color(i); })
                .style("font-size", function(d) { return d.size + "px"; })
                .style("font-family", function(d) { return d.font; })
                .attr("text-anchor", "middle")
                .attr("transform", function(d) { return "translate(" + [d.x, d.y] + ")rotate(" + d.rotate + ")"; })
                .text(function(d) { return d.text; });
        };
    }

    computeFrequency(data, varName) {
        let columnVarName = undefined;
        for (let i=0; i<data.head.vars.length; i++) {
            if (varName == data.head.vars[i]) {
                columnVarName = i;
                break;
            }
        }
        if (columnVarName === undefined) {
            throw `${columnVarName} not defined in the data.`;
        };
        let freqTable = {};
        for (let i=0; i<data.results.bindings.length; i++) {
            const entry = data.results.bindings[i][varName].value;
            if (freqTable[entry] === undefined) {
                freqTable[entry] = 1;
            } else {
                freqTable[entry]++;
            }
        }
        let freqArray = Object.keys(freqTable).map(function(key) {
            return {"key": key, "freq": freqTable[key]};
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
