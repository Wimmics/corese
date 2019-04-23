"use strict";

export class SvgDrawer {
    setData(data) {
        this.data = data;
        return this;
    }
    setParameters(parameters) {
        this.parameters = parameters;
        return this;
    }
    draw(svgId) {
        this.svgId = svgId;
        return this;
    }

    setupZoomHandler(svg) {
        let g = svg.select("g");
        let zoomed = function () {
            g.attr("transform", d3.event.transform);
        };
        let zoom_handler = d3.zoom()
            .scaleExtent([0.1,10])
            // .translateExtent(extent)
            .on("zoom", zoomed);
        svg.call(zoom_handler);
    }
};
export class SvgDrawerParameters {
    constructor() {
        this.parameters = {};
    }
};