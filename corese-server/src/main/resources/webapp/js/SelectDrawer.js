import {SvgDrawer, SvgDrawerParameters} from "./SvgDrawer.js";
import {Enumeration} from "./Enumeration.js";

export class SelectDrawer extends SvgDrawer {
    static build(parameters) {
        console.assert(parameters.diagram, "'diagram' field is not defined.");
        switch (parameters.diagram) {
            case SelectDrawer.Type.barchart : {
                return new ChartDrawer(parameters.diagram);
            }
        }
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
