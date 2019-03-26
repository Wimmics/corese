import {SvgDrawer, SvgDrawerParameters} from "./SvgDrawer.js";
import {Enumeration} from "./Enumeration.js";

export class SelectDrawer extends SvgDrawer {
    static build(parameters) {
        console.assert(parameters.diagram, "'diagram' field is not defined.");
        // switch (parameters.diagram) {
        //     case SelectDrawer.Type.barchart : {
                return new ChartDrawer(parameters.diagram);
            // }
        // }
    }

    static manage(data, panelId) {
        d3.select(`#${panelId}`).append("div").attr("id", "SelectDrawerConfigurationPanel");
        SelectDrawer.data = data;
    }

    static addViewer() {
        const graphChoice = d3.select("#viewerSelector").select("#selector").node().value;
        let render = undefined;
        if (graphChoice === "barchart") {
            render = this.renderBarchart;
        } else if (graphChoice === "piechart") {
            render = this.renderPiechart;
        }
        render(SelectDrawer.data);
    }

    static renderBarchart(json) {
        var config = {
            "label_x": "Prefecture",
            "label_y": "Area",
            "var_x": "pref",
            "var_y": "area",
            "width":  700,  // canvas width
            "height": 300,  // canvas height
            "margin":  80,  // canvas margin
            "selector": "#result"
        };
        d3sparql.barchart(json, config);
    }

    static renderPiechart(json) {
        var config = {
            "label": "pref",
            "size": "area",
            "width":  600,  // canvas width
            "height": 600,  // canvas height
            "margin":  10,  // canvas margin
            "hole":   200,  // doughnut hole: 0 for pie, r > 0 for doughnut
            "selector": "#result"
        }
        d3sparql.piechart(json, config)
    }

    static renderDendogram(json) {
        var config = {
            // for d3sparql.tree()
            "root": "root_name",
            "parent": "parent_name",
            "child": "child_name",
            // for d3sparql.dendrogram()
            "width": 900,
            "height": 4500,
            "margin": 350,
            "radius": 5,
            "selector": "#result"
        }
        d3sparql.dendrogram(json, config)
    }

    static renderRoundTree(json) {
        var config = {
            // for d3sparql.tree()
            "root": "root_name",
            "parent": "parent_name",
            "child": "child_name",
            // for d3sparql.roundtree()
            "diameter": 800,
            "angle": 360,
            "depth": 200,
            "radius": 5,
            "selector": "#result"
        }
        d3sparql.roundtree(json, config)
    }

};

class ChartDrawer extends SvgDrawer {
    constructor(type) {
        super();
        this.type = type;
    }
    draw(svgId) {
        this.parameters.selector = d3.select(svgId).node().parentNode;
        d3sparql[this.type](this.data, this.parameters);
        return this;
    }
}

/** Diagram types supported. */
SelectDrawer.Type = new Enumeration(["barchart", "piechart", "scatterplot", "roundtree", "dendrogram", "treemap", "treemapzoom", "sunburst", "circlepack"]);

export class SelectDrawerParameters extends SvgDrawerParameters {

};
