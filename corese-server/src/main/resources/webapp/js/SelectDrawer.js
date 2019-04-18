import {SvgDrawer, SvgDrawerParameters} from "./SvgDrawer.js";
import {TagCloudDrawer, TagCloudParameters} from "./TagCloudDrawer.js";
import {Enumeration} from "./Enumeration.js";


export class SelectDrawer extends SvgDrawer {
    /**
     * Create the generic panel to be used by the configuration panel for each choice.
     * @param data
     * @param panelId
     */
    constructor(data, panelId) {
        super();
        this.CHOICE_CONFIG_PANEL_ID = "selectDrawerConfigurationPanel";
        this.SELECTOR_PANEL_ID = "viewerSelector";
        this.SELECTOR_BUTTON_ID = "selector";
        d3.select("#selector").selectAll("option").data(Object.keys(SelectDrawer.registry))
            .enter()
            .append('option')
            .text(function (d) { return d; }) // text showed in the menu
            .attr("value", function (d) { return d; });
        d3.select("#selector").on("change", function() {this.setupPluginConfigurationPanel()}.bind(this));
        d3.select(`${panelId}`).append("div").attr("id", this.CHOICE_CONFIG_PANEL_ID);
        this.data = data;
        this.setupPluginConfigurationPanel();
    }

    setupPluginConfigurationPanel() {
        const graphChoice = d3.select(`#${this.SELECTOR_PANEL_ID}`).select(`#${this.SELECTOR_BUTTON_ID}`).node().value;
        let rendererConstructor = SelectDrawer.getViewer(graphChoice);
        this.currentPlugin = new rendererConstructor();
        d3.select(`#${this.CHOICE_CONFIG_PANEL_ID}`).html("");
        this.currentPlugin.setupConfigurationPanel(this.CHOICE_CONFIG_PANEL_ID, this.data);
    }

    static build(parameters) {
        console.assert(parameters.diagram, "'diagram' field is not defined.");
        return new ChartDrawer(parameters.diagram);
    }

    static manage(data, panelId) {
        const manager = new SelectDrawer(data, panelId);
        return manager;
    }

    addViewer() {
        const graphChoice = d3.select("#viewerSelector").select("#selector").node().value;
        // let rendererConstructor = this.getViewer(graphChoice);
        // const renderer = new rendererConstructor(graphChoice);
        /** @TODO A remplacer par une lecture des résultats de la fenêtre de configuration. */
        let parameters = null;
        switch (graphChoice) {
            case SelectDrawer.Type.piechart: {
                // @TODO to be removed when configuration interface is ok.
                parameters = new SelectDrawerParameters();
                Object.assign(parameters,{
                    "label": "pref",
                    "size": "area",
                    "width":  600,  // canvas width
                    "height": 600,  // canvas height
                    "margin":  10,  // canvas margin
                    "hole":   200,  // doughnut hole: 0 for pie, r > 0 for doughnut
                    "selector": "#result"
                });
                break;
            }
            case SelectDrawer.Type.barchart: {
                // @TODO to be removed when configuration interface is ok.
                parameters = new SelectDrawerParameters();
                Object.assign(parameters,{
                    // "label_x": "Prefecture",
                    // "label_y": "Area",
                    "width":  700,  // canvas width
                    "height": 300,  // canvas height
                    "margin":  80,  // canvas margin
                    "selector": "#result"
                });
                break;
            }
            case SelectDrawer.Type.tagcloud: {
                parameters = new TagCloudParameters();
                Object.assign(parameters.parameters , {
                        "label": "pref",
                        "size": "area",
                        "width":  600,  // canvas width
                        "height": 600,  // canvas height
                        "margin":  10,  // canvas margin
                        "selector": "#result"
                });
                parameters.setVarName("pref");
                break;
            }
        }
        /** Fin du @TODO */
        this.currentPlugin.setData(this.data)
        this.currentPlugin.setParameters(parameters);
        d3.select("#result").html("");
        this.currentPlugin.draw("#result");
    }


    // Registry management functions
    static register(name, constructor) {
        if (SelectDrawer.registry[name] !== undefined) {
            throw  `An entry was already added for ${name}`;
        } else {
            SelectDrawer.registry[name] = constructor;
        }
    }

    static getViewer(name) {
        if (SelectDrawer.registry[name] === undefined) {
            throw  `No viewer was registered for ${name}`;
        } else {
            return SelectDrawer.registry[name];
        }
    }
};
SelectDrawer.registry  = {};


class BarChartDrawer extends SvgDrawer {
    constructor() {
        super();
        this.type = "barchart";
    }
    setupConfigurationPanel(divId, data) {
        const panel = d3.select(`#${divId}`);
        const div = panel.append("div");
        div.append("label").text("var_x");
        div.append("select").attr("id", "var_x_select").selectAll("option").data(data.head.vars)
            .enter().append("option").text(function(d) {return d;}).attr("value", function(d) { return d;})
        div.append("label").text("var_y");
        div.append("select").attr("id", "var_y_select").selectAll("option").data(data.head.vars)
            .enter().append("option").text(function(d) {return d;}).attr("value", function(d) { return d;});
        d3.select("#var_y_select").property("value", data.head.vars[1])
        // div.append("div").attr("class", "custom-control custom-switch ")
        //     .append("label").text("")
        //     "<div class=\"custom-control custom-switch\">\n" +
        //     "  <input type=\"checkbox\" class=\"custom-control-input\" id=\"customSwitches\">\n" +
        //     "  <label class=\"custom-control-label\" for=\"customSwitches\">Toggle this switch element</label>\n" +
        //     "</div>")
        this.data = data;
    }
    draw(svgId) {
        this.parameters.selector = svgId;//d3.select(svgId).node().parentNode;
        this.parameters.var_x = d3.select("#var_x_select").node().value;
        this.parameters.var_y = d3.select("#var_y_select").node().value;

        let nbColumns = this.data.results.bindings.length;
        this.parameters.width = Math.min((nbColumns+1)*50, this.parameters.width);
        let range = d3.extent(this.data.results.bindings, function (d) {
            return parseInt(d[this.parameters.var_y].value)
        }.bind(this));
        range[0] = range[0] - 0.05*(range[1] - range[0])
        this.parameters.custom_extent = range;

        d3sparql[this.type](this.data, this.parameters);
        return this;
    }
}

class PieChartDrawer extends SvgDrawer {
    constructor() {
        super();
        this.type = "piechart";
    }
    setupConfigurationPanel(divId, data) {
        const panel = d3.select(`#${divId}`);
        const div = panel.append("div");
        div.append("label").text("label");
        div.append("select").attr("id", "label_select").selectAll("option").data(data.head.vars)
            .enter().append("option").text(function(d) {return d;}).attr("value", function(d) { return d;})
        div.append("label").text("size");
        div.append("select").attr("id", "size_select").selectAll("option").data(data.head.vars)
            .enter().append("option").text(function(d) {return d;}).attr("value", function(d) { return d;});
        d3.select("#size_select").property("value", data.head.vars[1])
        this.data = data;
    }
    draw(svgId) {
        this.parameters.selector = svgId;
        this.parameters.label = d3.select("#label_select").node().value;
        this.parameters.size = Number( d3.select("#size_select").node().value );
        d3sparql[this.type](this.data, this.parameters);
        return this;
    }
}

/** Diagram types supported. */
SelectDrawer.Type = new Enumeration(["barchart", "piechart", "scatterplot", "roundtree", "dendrogram", "treemap", "treemapzoom", "sunburst", "circlepack","tagcloud"]);

export class SelectDrawerParameters extends SvgDrawerParameters {

};

SelectDrawer.register(SelectDrawer.Type.BARCHART, BarChartDrawer);
SelectDrawer.register(SelectDrawer.Type.PIECHART, PieChartDrawer);
SelectDrawer.register(SelectDrawer.Type.TAGCLOUD, TagCloudDrawer);
SelectDrawer.register(SelectDrawer.Type.SCATTERPLOT, TagCloudDrawer);
