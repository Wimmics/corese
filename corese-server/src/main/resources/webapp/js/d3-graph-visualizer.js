import {GraphModel} from "./GraphModel.js";
import {Observer} from "./Observer.mjs";

/**
 *  Responsible for the graphical management of the configuration.
 */
class ConfGraphModal extends Observer {
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
            html += `<label>${option}</label>`;
            for (let value of this.model.getOptionRange(option)) {
                let checked = (this.model.getOption(option) === value) ? "checked" : "";
                html += `<input type="radio" id='${value}' name='${option}' ${checked}>${value}</input>`;
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
                d3.select(`#${value}`).on("change",
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
        var prefix = `${graph.attr("id")}-`;
        var confPanelId = `${this.prefix}configurationGraph`;
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


export class D3GraphVisualizer extends Observer {
    constructor( data ) {
        super();
        this.model = new GraphModel( data );
        // this.model.displayAllEdgeLabels = false;
        // this.model.displayAllNodeLabels = false;
        this.model.addObserver(this);
        var sheet = document.createElement('style');

        document.head.appendChild(sheet); // Bug : does not support multiple graphs in the same page.
    }

    update(observable, data) {
        super.update(observable, data);
        this.graph.ticked();
    }

    dragstarted(_simulation) {
        return function(d) {
            if (!d3.event.active) _simulation.alphaTarget(0.3).restart();
            d.fx = d.x;
            d.fy = d.y;
        }
    }

    dragged(d) {
        d.fx = d3.event.x;
        d.fy = d3.event.y;
    }

    dragended( _simulation ) {
        return function(d) {
            if (!d3.event.active) _simulation.alphaTarget(0);
            d.fx = null;
            d.fy = null;
        }
    }

    // To be used with text for edges, in order to obtain text no upside-down.
    buildPathFromEdge(scale) {
        return links => {
            return (edge, i, edges) => {
                var dx = edge.source.x - edge.target.x;
                var dy = edge.source.y - edge.target.y;
                var r = 10 * Math.sqrt(dx*dx + dy*dy);
                var dr = r /  (2 * edge.linknum);

                // var dr = 100/edge.linknum * scale;  //linknum is defined above
                var lefterpoint, righterpoint;
                var sourceLeft = edge.source.x <= edge.target.x;
                [lefterpoint, righterpoint] = (sourceLeft) ? [edge.source, edge.target] : [edge.target, edge.source];
                var leftx = lefterpoint.x * scale;
                var lefty = lefterpoint.y * scale;
                var rightx = righterpoint.x * scale;
                var righty = righterpoint.y * scale;
                var sweep = (sourceLeft) ? "1" : "0";
                return `M ${leftx},${lefty} A ${dr * scale},${dr * scale} 0 0,${sweep} ${rightx},${righty}`
            };
        }
    }


    /**
     * \param _results : json representation of the graph.
     * \param svgId : id of the svg element to draw the graph (do not forget the #).
     */
    static drawRdf(_results, svgId) {
        var results = _results;
        var visualizer = new D3GraphVisualizer( _results );
        var confGraphModal;
        visualizer.graph = d3.select(svgId);

        // @Todo make a function counting the edges between same nodes.
        // To be able to detect edges between same nodes.
        results.edges.sort(function(a,b) {
            if (a.source > b.source) {return 1;}
            else if (a.source < b.source) {return -1;}
            else {
                if (a.target > b.target) {return 1;}
                if (a.target < b.target) {return -1;}
                else {return 0;}
            }

        });
        // counting the edges between the same edges.
        for (var i=0; i<results.edges.length; i++) {
            if (i != 0 &&
                results.edges[i].source === results.edges[i-1].source &&
                results.edges[i].target === results.edges[i-1].target) {
                results.edges[i].linknum = results.edges[i-1].linknum + 1;
            }
            else {results.edges[i].linknum = 1;};
        };


        visualizer.graph.zoomed = function () {
            var copieTransform = new d3.event.transform.constructor(d3.event.transform.k, d3.event.transform.x, d3.event.transform.y);
            copieTransform.k = 1;
            g.attr("transform", copieTransform);
            visualizer.graph.ticked(d3.event.transform.k);
        };

        var scale = 1;
        var fo = visualizer.graph.append('foreignObject').attr("width", "40px").attr("height", "34px");
        var button = fo.append("xhtml:button")
            .attr("class", "btn btn-info")
            .attr("id", "configurationButton")
            .on("click", e => {
                if (confGraphModal.isDisplayOn()) {
                    confGraphModal.displayOff()
                } else {
                    confGraphModal.displayOn();
                }
            });
        button.append("xhtml:span")
            .attr("class", "glyphicon glyphicon-cog");
        results.links = results.edges;

        var rootConfPanel = d3.select(d3.select(svgId).node().parentNode, visualizer.graph);

        visualizer.graph.ticked = function (s) {
            scale = (s === undefined) ? scale : s;
            links.attr("d",
                function(model) {
                    return function(edge, i, edges) {
                        if (model.getOption(model.ARROW_STYLE) === "curve") {
                            var dx = edge.source.x - edge.target.x;
                            var dy = edge.source.y - edge.target.y;
                            var r = 10 * Math.sqrt(dx * dx + dy * dy);
                            var dr = r / (2 * edge.linknum);
                            d3.select(this).attr("marker-end", "url(#arrowhead)");
                            return `M ${edge.source.x * scale},${edge.source.y * scale} A ${dr * scale},${dr * scale} 0 0,1 ${edge.target.x * scale},${edge.target.y * scale}`
                        } else {
                            var dx = edge.source.x - edge.target.x;
                            var dy = edge.source.y - edge.target.y;
                            var dr = 0;
                            d3.select(this).attr("marker-end", "url(#arrowhead)");
                            return `M ${edge.source.x * scale},${edge.source.y * scale} A ${dr * scale},${dr * scale} 0 0,1 ${edge.target.x * scale},${edge.target.y * scale}`
                        }
                    }
                }(visualizer.model)
                );

            nodes
                .attr("cx", function (d) {
                    return d.x * scale;
                })
                .attr("cy", function (d) {
                    return d.y * scale;
                })
                .each(
                    (d, i, nodes) => {
                        var current = d3.select(nodes[i]);
                        var father = d3.select(nodes[i].parentNode);
                        var image = father.select("image");
                        if (image !== undefined) {
                            var width = current.attr("r") * Math.sqrt(2);
                            image.attr("x", d => (d.x * scale - width / 2));
                            image.attr("y", d => (d.y * scale - width / 2));
                        }
                    }
                );

            if (true) { // @TODO : à réécrire pour parcourir les groupes à afficher.
                textNodes
                    .attr("x",
                        (d) => {
                            const r = Number( d3.select(d.node).attr("r") );
                            const result = d.x * scale + r;
                            return result;
                        }
                    )
                    .attr("y",
                        (d) => {
                            const r = Number( d3.select(d.node).attr("r") );
                            return (d.y * scale + r);
                        }
                    ).attr("visibility",
                    (d) => confGraphModal.model.getDisplayGroup(confGraphModal.model.ALL_NODES, d.group) ? "visible" : "hidden"
                );
            }
            if (true) {// @TODO : à réécrire pour parcourir les groupes à afficher.
                textEdges
                    .attr("d", visualizer.buildPathFromEdge(scale)(results.links))
                    .attr("visibility",
                        (d) => confGraphModal.model.getDisplayGroup(confGraphModal.model.ALL_EDGES, d.group) ? "visible" : "hidden"
                    );
            }
        };


        confGraphModal = ConfGraphModal.createConfigurationPanel(rootConfPanel, visualizer.graph, results, visualizer.model);
        confGraphModal.model.addObserver(confGraphModal);
        visualizer.graph.updateConfiguration = function () {
            var updateSet = function(groupName, text) {
                const nodesDisplayCriteria = (d, i, nodes) => (confGraphModal.model.getDisplayGroup(confGraphModal.model.ALL_NODES, d.group)) ? "visible" : "hidden";
                text.attr(
                    "visibility",
                    (d, i, nodes) => nodesDisplayCriteria(d, i, nodes)
                );
            }
            updateSet("nodes", textNodes);
            updateSet("edges", textEdges);
                // textEdges.attr("visibility", confGraphModal.edgesCheckbox.property("checked") ? "visible" : "hidden");
            };

        var maxLen = [];
        results.nodes.forEach(
            (node, index, array) => {
                maxLen[node.id] = 0;
            }
        );
        results.links.forEach(
            (link, index, array) => {
                maxLen[link.source] = Math.max(maxLen[link.source], link.label.length);
                maxLen[link.target] = Math.max(maxLen[link.target], link.label.length);
            }
        );


        var color = d3.scaleOrdinal( d3.schemeCategory20 );

        visualizer.simulation = d3.forceSimulation(results.nodes)
            .force("link", d3.forceLink().id(function (d) {
                return d.id;
            }))
            // .force("charge", d3.forceManyBody())
            .force("charge",
                n => n.weight
            )
            .force("center", d3.forceCenter(800,500))
            .on("tick", visualizer.graph.ticked);
        var width = +visualizer.graph.node().getBoundingClientRect().width;
        var height = +visualizer.graph.node().getBoundingClientRect().height;
        visualizer.simulation
            .force("link")
            .links(results.links);
        visualizer.simulation
            .force("center", d3.forceCenter(width / 2, height / 2));

        var g = visualizer.graph.append("g")
            .attr("class", "everything");
        var defs = visualizer.graph.append("defs");
        defs.append('marker')
            .attr('id', 'arrowhead')
            .attr('viewBox', '-0 -50 100 100')
            .attr('refX', 130)
            .attr('refY', 0)
            .attr('orient', 'auto')
            .attr('markerWidth', 10)
            .attr('markerHeight', 10)
            .attr('xoverflow', 'visible')
            .attr('markerUnits', 'userSpaceOnUse')
            .append('svg:path')
            .attr('d', 'M 0,-20 L 100 ,0 L 0,20')
            // .style('stroke','grey')
            .style('markerUnits', 'userSpaceOnUse')
            .style('fill', 'grey')
        ;

        let textNodes = g.append("g").attr("class", "textNodes").selectAll("text")
            .data(visualizer.simulation.nodes())
            .enter().append("text")
            .attr("class", (edge, i, edges) => {
                return (edge.class !== undefined) ? edge.class : "default";
            })
            .text(function (d) {
                return d.label;
            })
            .each(
                (d, i, nodes) => {
                    d.textNode = nodes[i];
                }
            );
        var textEdges = g.append("g").attr("class", "textPaths").selectAll("text")
            .data(results.links)
            .enter().append("text")
            .append("textPath")
            .attr("xlink:href", d => {return `#${d.id}_edge`;})
            .attr("startOffset", "25%")
            .text(function (d) {
                return d.label;
            })
            .attr("class", (edge, i, edges) => {
                return (edge.class !== undefined) ? edge.class : "default";
            })
            .attr("xlink:href", (edge, i, edges) => {
                return "#" + edge.id + "_edge";
            })
            .each(
                (d, i, nodes) =>
                    d.textEdges = nodes[i]
            );

        var links = g.append("g")
            .attr("class", "links")
            .selectAll("path")
            .data(results.links)
            .enter().append("path")
            .attr(
                "class",
                d => {
                    if (d.class !== undefined) {
                        return d.class;
                    } else {
                        return "default";
                    }
                }
            )
            .attr("id", d => `${d.id}_edge` )
            .each(
                (d, i, nodes) =>
                    d.link = nodes[i]
            );

        links.append("title")
            .text(function (d) {
                return d.label;
            });
        let nodes = g.append("g")
            .attr("class", "nodes")
            .selectAll("circle")
            .data(results.nodes)
            .enter()
            .append("g")
            .attr("class", "nodes")
            .append("circle")
            .attr(
                "class",
                d => {
                    if (d.class !== undefined) {
                        return d.class;
                    } else {
                        return "default";
                    }
                }
            )
            .attr("r", visualizer.model.nodeRadius)
            .each(
                    (d, i, nodes) => {
                        var current = d3.select(nodes[i]);
                        var father = d3.select(current.node().parentNode);
                        var color = d3.scaleOrdinal(d3.schemeCategory20).domain(Array.from(confGraphModal.model.getGroups("nodes")));
                        d.colorMap = color;
                        if (d.bg_image === undefined) {
                            current.attr("fill", color(d.group));
                            current.attr("r", 5);
                        } else {
                            current.attr("r", visualizer.model.nodeRadius);
                            var width = Math.sqrt(2) * current.attr("r");
                            father.append("image")
                                .attr("xlink:href", d.bg_image)
                                .attr("height", width)
                                .attr("width", width)
                                .attr("pointer-events", "none");
                        }
                    }
            )
            .each(
                (d, i, nodes) =>
                    d.node = nodes[i]
            )
            .each(
                (_links => {
                 return (d, i, nodes) => {
                    var neighbors = new Set();

                    _links.forEach( link => {
                        if (link.source.id === d.id || link.target.id == d.id) {
                            neighbors.add( link.source.id );
                            neighbors.add( link.target.id );
                        }
                    });
                    d.neighbors = neighbors;
                 }
                })(results.links)
            )
            .call(d3.drag()
                .on("start", visualizer.dragstarted(visualizer.simulation))
                .on("drag", visualizer.dragged)
                .on("end", visualizer.dragended(visualizer.simulation)))
            .on("click", (d) => {
                if (d.url  !== undefined) window.open(d.url);
                if (d.link !== undefined) trans(d.link);
            })
            .on("mouseover", (d, i , nodes) => {
                var center = d3.select(nodes[i]);
                var datum = center.datum();
                var counter = 0;
                nodes.forEach( node => {
                    if (datum.neighbors.has(d3.select(node).datum().id)) {
                        d3.select(node).attr("fill", "red");
                        d3.select(node).attr("background-color", "yellow");
                        d3.select(textNodes.nodes()[counter]).attr("visibility", "visible");
                    }
                    counter++;
                })

            })
            .on("mouseout", (d, i, nodes) => {
                var center = d3.select(nodes[i]);
                var datum = center.datum();
                var counter = 0;
                nodes.forEach( node => {
                    var datumNode = d3.select(node).datum();
                    if (datum.neighbors.has(datumNode.id)) {
                        d3.select(node).attr("fill", datumNode.colorMap(datumNode.group));
                        d3.select(node).attr("background-color", "white");
                        d3.select(textNodes.nodes()[counter]).attr("visibility", "hidden");
                    }
                    counter++;
                })
                visualizer.graph.updateConfiguration();
                visualizer.graph.ticked();

            });
        nodes.append("title")
            .text(function (d) {
                return d.label;
            });

        visualizer.graph.updateConfiguration();
        var pathLabels = visualizer.graph.append("defs").attr("class", "paths").selectAll("path")
            .data(results.links)
            .enter().append("path")
            .attr("id", (edge, i, edges) => {
                return edge.id;
            })
            .attr("d", visualizer.buildPathFromEdge(1)(results.links));
        //add zoom capabilities
        var zoom_handler = d3.zoom()
            .on("zoom", visualizer.graph.zoomed);
        zoom_handler(visualizer.graph);


    }
}

