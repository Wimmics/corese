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
};
export class SvgDrawerParameters {
    constructor() {
        this.parameters = {};
    }
};