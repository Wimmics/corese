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
        this.NAVBAR_HEADER = "navbar-header";
        this.NAVBAR_CONTENT = "navbar-content";

        this.data = data;
        // First panel, allowing to add a new tab.
        // this.CHOICE_CONFIG_PANEL_ID = "selectDrawerConfigurationPanel";
        // this.SELECTOR_PANEL_ID = "viewerSelector";
        // this.SELECTOR_BUTTON_ID = "selector";
        // d3.select(`#${this.SELECTOR_BUTTON_ID}`)
        //     .selectAll("option")
        //     .data(Object.keys(SelectDrawer.registry))
        //     .enter()
        //     .append('option')
        //     .text(function (d) { return d; }) // text showed in the menu
        //     .attr("value", function (d) { return d; });
        // d3.select(`#${this.SELECTOR_BUTTON_ID}`)
        //     .on("change", function() {this.setupPluginConfigurationPanel()}.bind(this));
        // d3.select(`#${this.SELECTOR_PANEL_ID}`).append("div").attr("id", this.CHOICE_CONFIG_PANEL_ID);
        // this.setupPluginConfigurationPanel();
        // Build the nabtab header
        let firstTab =true;
        for (let title of Object.keys(SelectDrawer.registry)) {
            d3.select(`#${this.NAVBAR_HEADER}`)
                .append("li")
                .html(function (d) {
                    return `<a data-toggle="tab" href="#${title}_tab" aria-expanded="true">${title}</a>`;
                });
            d3.select(`#${this.NAVBAR_CONTENT}`)
                .append("div")
                .attr("id", (d) => `${title}_tab`)
                .attr("class", "tab-pane container fade " + (firstTab ? "active in " : "") )
                .html((d) =>
                    `<button id="${title}-update-view" onclick="window.pageDrawer.update('${title}')">Update View</button>
                     <div id="${title}-viewer-configuration"></div>
                     <div> <svg id="${title}-content"/> </div>`
                );
            let titlePluginConstructor = SelectDrawer.getViewer(title);
            window[title] = new titlePluginConstructor(title);
            window[title].setupConfigurationPanel(`${title}-viewer-configuration`, this.data);
            window[title].setData(this.data);
            let parameters = new SelectDrawerParameters();
            window[title].setParameters(parameters);
            d3.select(`#${title}-content`).html("");
            window[title].draw(`#${title}-content`);
            firstTab = false;
        };

    }

    setupPluginConfigurationPanel() {
        const graphChoice = d3.select(`#${this.SELECTOR_PANEL_ID}`).select(`#${this.SELECTOR_BUTTON_ID}`).node().value;
        let rendererConstructor = SelectDrawer.getViewer(graphChoice);
        this.currentPlugin = new rendererConstructor("global");
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

    update(title) {
        window[title].draw(`#${title}-content`);
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
                    "margin":  50,  // canvas margin
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
    constructor(prefix) {
        super();
        this.type = "barchart";
        this.prefix = prefix || "default";
    }
    setupConfigurationPanel(divId, data) {
        const panel = d3.select(`#${divId}`);
        const div = panel.append("div");
        div.append("label").text("label");
        div.append("select").attr("id", `${this.prefix}-label-select`).selectAll("option").data(data.head.vars)
            .enter().append("option").text(function(d) {return d;}).attr("value", function(d) { return d;})
        d3.select(`#${this.prefix}-label-select`).property("value", data.head.vars[0])

        div.append("label").text("size");
        div.append("select").attr("id", `${this.prefix}-size-select`).selectAll("option").data(data.head.vars)
            .enter().append("option").text(function(d) {return d;}).attr("value", function(d) { return d;});
        d3.select(`#${this.prefix}-size-select`).property("value", data.head.vars[1])
        this.data = data;
    }
    draw(svgId) {
        this.parameters.selector = svgId;//d3.select(svgId).node().parentNode;
        this.parameters.var_x = d3.select(`#${this.prefix}-label-select`).node().value;
        this.parameters.var_y = d3.select(`#${this.prefix}-size-select`).node().value;

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
    constructor(prefix) {
        super();
        this.type = "piechart";
        this.prefix = prefix || "default";
    }
    setupConfigurationPanel(divId, data) {
        const panel = d3.select(`#${divId}`);
        const div = panel.append("div");
        div.append("label").text("label");
        div.append("select").attr("id", `${this.prefix}-label-select`).selectAll("option").data(data.head.vars)
            .enter().append("option").text(function(d) {return d;}).attr("value", function(d) { return d;})
        d3.select(`#${this.prefix}-label-select`).property("value", data.head.vars[0])

        div.append("label").text("size");
        div.append("select").attr("id", `${this.prefix}-size-select`).selectAll("option").data(data.head.vars)
            .enter().append("option").text(function(d) {return d;}).attr("value", function(d) { return d;});
        d3.select(`#${this.prefix}-size-select`).property("value", data.head.vars[1])
        this.data = data;
    }
    draw(svgId) {
        this.parameters.selector = svgId;
        this.parameters.label = d3.select(`#${this.prefix}-label-select`).node().value;
        this.parameters.size = Number( d3.select(`#${this.prefix}-size-select`).node().value );
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
// SelectDrawer.register(SelectDrawer.Type.SCATTERPLOT, TagCloudDrawer);
