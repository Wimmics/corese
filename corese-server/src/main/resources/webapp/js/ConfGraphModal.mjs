import {Observer} from "./Observer.mjs";
/**
 *  Responsible for the graphical management of the configuration.
 */
export class ConfGraphModal extends Observer {
    /**
     *
     * @param id    id given to the DOM node containing the window.
     * @param root  node parent of the configuration window.
     * @param graph Reference to the D3 wrapper responsible for the SVG management.
     */
    constructor(id, root, graph, data, model) {
        super();
        this.id = id;
        this.model = model;
        this.prefix = `${graph.attr("id")}-`;
        this.domNode = root.append("div")
            .attr("id", this.id)
            .attr("class", "modal modal-sm")
            .style("width", "fit-content");
        let html = "<div class='modal-content' style='list-style:none;'>";
        html += this.createLabelsLi(model.getNodeGroups(), model.getEdgeGroups());
        for (let option of this.model.getOptions()) {
            html += `<label>${this.model.getOptionPrettyName(option)}</label>`;
            for (let value of this.model.getOptionRange(option)) {
                let checked = (this.model.getOption(option) === value) ? "checked" : "";
                html += `<input type="radio" id='${this.prefix}${value}' name='${option}' ${checked}>${value}</input>`;
            }
        }
        html += "<br>" +
            `<button id='${this.prefix}configurationGraphCloseButton' class='btn btn-default'>Close</button>` +
            "</div>";
        this.domNode.html(html);


        // event handlers setting.
        d3.select("body")
            .on("keydown", function (that) {
                    return function () {
                        const key = d3.event.key;
                        let numGroup = -1.0;

                        let groupName = ( d3.event.ctrlKey ) ? that.model.ALL_EDGES : that.model.ALL_NODES;
                        // Ctrl = switch the display for edges.
                        // without Ctrl = switch the display for nodes.
                        if (isFinite(key)) { // display/hide the labels of a group of nodes or edges
                            numGroup = parseInt(key) - 1;
                            // Changing for a more natural mapping: 1 -> first element,
                            // 2 -> second, etc. 0 -> 10th element.
                            if (numGroup === -1) {
                                numGroup = 10;
                            }
                            that.model.toggleDisplayGroupNum( groupName, numGroup);
                        } else if (key === "n") {
                            that.model.toggleDisplayAll(groupName);
                        }
                    }
                }(this)
            );
        this.nodesCheckbox = d3.select(`#${this.getCheckboxName(this.model.ALL_NODES, 'all')}`);
        this.edgesCheckbox = d3.select(`#${this.getCheckboxName(this.model.ALL_EDGES, "all")}`);
        this.closeButton = d3.select(`#${this.prefix}configurationGraphCloseButton`);
        this.nodesCheckbox.on("change",
            function(model, checkbox) {
                return function() {
                    model.setDisplayAll("nodes", checkbox.property("checked"));
                }
            }(this.model, this.nodesCheckbox )
        );
        this.edgesCheckbox.on("change",
            function(model, checkbox) {
                return function() {
                    model.setDisplayAll("edges", checkbox.property("checked"));
                }
            }(this.model, this.edgesCheckbox )
        );
        this.setupGroupHandler("nodes", model.getNodeGroups() );
        this.setupGroupHandler("edges", model.getEdgeGroups() );
        for (let option of this.model.getOptions()) {
            for (let value of this.model.getOptionRange(option)) {
                d3.select(`#${this.prefix}${value}`).on("change",
                    function(model) {
                        return function () {
                            model.setOption(option, value);
                            console.log("setting option");
                        };
                    }(this.model)
                );
            }
        }
        this.closeButton
            .on("click", e => {
                this.displayOff();
            });
    }

    createLabelsLi(nodeGroups, edgeGroups) {
        var result =

            `<label><input id='${this.getCheckboxName(this.model.ALL_NODES, "all")}' type='checkbox'/>All Nodes Labels</label>` +
            "<ul>" +
            this.addGroups( "nodes", nodeGroups ) +
            "</ul>" +
            `<p><label><input id='${this.getCheckboxName(this.model.ALL_EDGES, 'all')}' type='checkbox'/>Edges</label>` +
            "<ul>" +
            this.addGroups( "edges", edgeGroups ) +
            "</ul>";
        return result;
    }

    getCheckboxName( groupName, group) {
        return `${this.prefix}${groupName}-${group}Checkbox`;
    }

    addGroups( groupName, groups ) {
        var result = "";
        if (groups !== undefined) {
            var numGroup = 1;
            groups.forEach( group => {
                    const checkboxName = this.getCheckboxName(groupName, group);
                    result += `<ul><label>${numGroup} <input id='${checkboxName}' type='checkbox'/>${group}</label></ul>`;
                    numGroup++;
                }
            );
        }
        return result;
    }

    setupGroupHandler(groupName, groups) {
        if (groups !== undefined) {
            groups.forEach( group => {
                    const checkboxName = this.getCheckboxName(groupName, group);
                    const checkbox = d3.select(`#${checkboxName}`);
                    checkbox.on("change", function(model, checkbox) {
                        return function() {
                            model.setDisplayGroup( groupName, group, checkbox.property("checked"));
                        }
                    }(this.model, checkbox ));
                }
            );
        }
    }

    getGroupCheckbox(groupName, group) {
        return d3.select( '#'+this.getCheckboxName(groupName, group) );
    }

    static createConfigurationPanel(rootConfPanel, graph, data, model) {
        const prefix = `${graph.attr("id")}-`;
        var confPanelId = `${prefix}configurationGraph`;
        var result = d3.select(`#${confPanelId}`);
        if (result.size() === 0) {
            var confGraphModal = new ConfGraphModal( confPanelId, rootConfPanel, graph, data, model);
            return confGraphModal;
        } else {
            return result;
        }
    }

    isDisplayOn() {
        return d3.select(`#${this.id}`)
            .style("display") === "block";
    }
    displayOn() {
        d3.select(`#${this.id}`)
            .style("display", "block")
            .style("top", d3.event.y + "px")
            .style("left", d3.event.x + "px");
    }

    displayOff() {
        d3.select(`#${this.id}`)
            .style("display", "none");
    }


    update(observable, data) {
        this.model.ALL_ELEMENTS.forEach(
            groupName => {
                this.getGroupCheckbox(groupName, "all").property("checked", this.model.getDisplayAll(groupName));
                this.model.getGroups(groupName).forEach(
                    group => this.getGroupCheckbox(groupName, group).property("checked", this.model.getDisplayGroup(groupName, group))
                )
            }
        );
        console.log("ConfGraphModel notified");
    }
}
