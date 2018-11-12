class GraphModel {
    constructor() {
        this.nodeRadius = 10;
    }
}

GraphModel.BNODE_ID = "bnode";
GraphModel.URI_ID = "uri";
GraphModel.LITERAL_ID = "literal";

class ConfGraphModal {
    /**
     *
     * @param id    id given to the DOM node containing the window.
     * @param root  node parent of the configuration window.
     * @param graph Reference to the object responsible of the graph management.
     */
    constructor(id, root, graph, data) {
        this.id = id;
        this.nodeGroups = this.computeGroups(data.nodes);
        this.edgeGroups = this.computeGroups(data.edges);
        this.domNode = root.append("div")
            .attr("id", this.id)
            .attr("class", "modal modal-sm")
            .html(
                this.createLabelsLi(this.nodeGroups, this.edgeGroups)
            );
        d3.select("body")
            .on("keydown", function (that) {
                    return function () {
                        const key = d3.event.key;
                        let numGroup = -1.0;
                        if (isFinite(key)) {
                            numGroup = parseInt(key) - 1;
                            // Changing for a more natural mapping: 1 -> first element,
                            // 2 -> second, etc. 0 -> 10th element.
                            if (numGroup === -1) {
                                numGroup = 10;
                            }
                            if (d3.event.ctrlKey) {
                                if (numGroup < that.edgeGroups.size) {
                                    const groupToSwitch = Array.from(that.edgeGroups)[numGroup];
                                    that.getGroupCheckbox("edges", groupToSwitch).property("checked", !that.getGroupCheckbox("edges", groupToSwitch).property("checked"));
                                }
                            } else {
                                if (numGroup < that.nodeGroups.size) {
                                    const groupToSwitch = Array.from(that.nodeGroups)[numGroup];
                                    that.getGroupCheckbox("nodes", groupToSwitch).property("checked", !that.getGroupCheckbox("nodes", groupToSwitch).property("checked"));
                                }
                            }
                            graph.updateConfiguration();
                            graph.ticked();
                        } else if (key === "n") {
                            if (d3.event.ctrlKey) {
                                that.edgesCheckbox.check();
                            } else {
                                that.nodesCheckbox.check();
                            }
                            graph.updateConfiguration();
                            graph.ticked();
                        } else {
                            console.log("key" + key);
                        }
                    }
                }(this)
            );
        this.nodesCheckbox = d3.select("#nodesCheckbox");
        this.edgesCheckbox = d3.select("#edgesCheckbox");
        this.closeButton = d3.select("#configurationGraphCloseButton");

        this.setHierarchicalCheckboxesHandler("nodes", this.nodesCheckbox, this.nodeGroups, graph);
        this.setHierarchicalCheckboxesHandler("edges", this.edgesCheckbox, this.edgeGroups, graph);

        this.closeButton
            .on("click", e => {
                this.displayOff();
            });
    }

    setHierarchicalCheckboxesHandler( groupName, fatherCheckbox, groups, graph ) {
        // 1. compute sub-checkboxes.
        var groupCheckboxes = new Set();
        groups.forEach(
            group => groupCheckboxes.add( this.getGroupCheckbox(groupName, group))
        );

        // when father checkbox is changed all sub-checkboxes are changed to the same value.
        fatherCheckbox.on("change",
            function (container) {
                return function (evt) {
                    groupCheckboxes.forEach(
                        checkbox => checkbox.property("checked", container.property("checked"))
                    )
                    graph.updateConfiguration();
                    graph.ticked();
                }
            }(fatherCheckbox)
        );
        fatherCheckbox.check = function() {
            fatherCheckbox.property("checked", !fatherCheckbox.property("checked"));
            groupCheckboxes.forEach(
                checkbox => checkbox.property("checked", fatherCheckbox.property("checked"))
            )
            graph.updateConfiguration();
            graph.ticked();
        }
        // when one of subcheckboxes is checked, update if necessary the father checkbox :
        //   - if all subcheckboxes are unset, unset the father checkbox ;
        //   - if all subcheckboxes are set, set the father checkbox.
        groupCheckboxes.forEach(button =>
            function (father) {
                return button.on("change", function () {
                    var allChecked = true;
                    groupCheckboxes.forEach(
                        button => allChecked = allChecked && button.property("checked")
                    );
                    father.property("checked", allChecked);
                    graph.updateConfiguration();
                    graph.ticked();
                })
            }(fatherCheckbox)
        );
    }

    getGroups(groupName) {
        if (groupName === "nodes") {
            return this.nodeGroups;
        } else if (groupName === "edges") {
            return this.edgeGroups;
        } else {
            throw `incorrect groupName value = ${groupName}`;
        }
    }

    computeGroups(data) {
        var result = new Set();
        data.forEach(
           elem => {
               if (elem.group === undefined) {
                   elem.group = "default";
               }
               if (!result.has(elem.group)) {
                   result.add(elem.group);
               }
           }
        )
        console.log(`found groups: ${result}`)
        return result;
    }

    createLabelsLi(nodeGroups, edgeGroups) {
        var result =
            "<div class='modal-content' style='list-style:none;'>" +
            "<label><input id='nodesCheckbox' type='checkbox'/>All Nodes Labels</label>" +
            "<ul>" +
            this.addGroups( "nodes", nodeGroups ) +
            "</ul>" +
            "<p><label><input id='edgesCheckbox' type='checkbox'/>Edges</label>" +
            "<ul>" +
            this.addGroups( "edges", edgeGroups ) +
            "</ul>" +
            "<br>" +
            "<button id='configurationGraphCloseButton' class='btn btn-default'>Close</button>" +
            "</div>";
        return result;
    }

    getCheckboxName( groupName, group) {
       return `${groupName}-${group}Checkbox`;
    }

    addGroups( groupName, groups ) {
        var result = "";
        if (groups !== undefined) {
            var numGroup = 1;
            groups.forEach( group => {
            result += `<ul><label>${numGroup} <input id='${this.getCheckboxName(groupName, group)}' type='checkbox'/>${group}</label></ul>`;
            numGroup++;
            } );
        }
        return result;
    }

    getGroupCheckbox(groupName, group) {
        return d3.select( '#'+this.getCheckboxName(groupName, group) );
    }

    static createConfigurationPanel(rootConfPanel, graph, data) {
        var result = d3.select("#configurationGraph");
        if (result.size() === 0) {
            var confGraphModal = new ConfGraphModal("configurationGraph", rootConfPanel, graph, data);
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
}


class D3GraphVisualizer {
    constructor() {
        this.model = new GraphModel();
        // this.simulation = undefined;
        var sheet = document.createElement('style');

        document.head.appendChild(sheet); // Bug : does not support multiple graphs in the same page.
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
                var dr = 100/edge.linknum * scale;  //linknum is defined above
                var lefterpoint, righterpoint;
                var sourceLeft = edge.source.x <= edge.target.x;
                [lefterpoint, righterpoint] = (sourceLeft) ? [edge.source, edge.target] : [edge.target, edge.source];
                var leftx = lefterpoint.x * scale;
                var lefty = lefterpoint.y * scale;
                var rightx = righterpoint.x * scale;
                var righty = righterpoint.y * scale;
                var sweep = (sourceLeft) ? "1" : "0";
                return `M ${leftx},${lefty} A ${dr},${dr} 0 0,${sweep} ${rightx},${righty}`
            };
        }
    }


    /**
     * \param svgId : id of the svg element to draw the graph (do not forget the #).
     */
    static drawRdf(results, svgId) {
        var visualizer = new D3GraphVisualizer();
        var confGraphModal;
        var graph = d3.select(svgId);


        results.edges.sort(function(a,b) {
            if (a.source > b.source) {return 1;}
            else if (a.source < b.source) {return -1;}
            else {
                if (a.target > b.target) {return 1;}
                if (a.target < b.target) {return -1;}
                else {return 0;}
            }

        });
        for (var i=0; i<results.edges.length; i++) {
            if (i != 0 &&
                results.edges[i].source == results.edges[i-1].source &&
                results.edges[i].target == results.edges[i-1].target) {
                results.edges[i].linknum = results.edges[i-1].linknum + 1;
            }
            else {results.edges[i].linknum = 1;};
        };

        // TODO : à corriger, il faut renvoyer true ssi au moins un groupe de noeuds est à afficher
        graph.displayNodeLabels = function () {
            // return confGraphModal.nodesCheckbox.property("checked") || ;
            return true;
        }
        graph.displayEdgeLabels = function () {
            return true;
            // return confGraphModal.edgesCheckbox.property("checked");
        }
        graph.ticked = function (s) {
            scale = (s === undefined) ? scale : s;
            link.attr("d", function(edge, i, edges) {
                var r = 100;
                var dr = r / edge.linknum ;
                d3.select(this).attr("marker-end", "url(#arrowhead)");
                return `M ${edge.source.x * scale},${edge.source.y * scale} A ${dr * scale},${dr * scale} 0 0,1 ${edge.target.x * scale},${edge.target.y * scale}`
            });

            node
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

            if (graph.displayNodeLabels()) {
                textNodes
                    .attr("x", d => d.x * scale)
                    .attr("y", d => d.y * scale);
            }
            if (graph.displayEdgeLabels()) {
                pathLabels.attr("d", visualizer.buildPathFromEdge(scale)(results.links));
            }
        };
        graph.zoomed = function () {
            var copieTransform = new d3.event.transform.constructor(d3.event.transform.k, d3.event.transform.x, d3.event.transform.y);
            copieTransform.k = 1;
            g.attr("transform", copieTransform);
            graph.ticked(d3.event.transform.k);
        };

        var scale = 1;
        var fo = graph.append('foreignObject').attr("width", "40px").attr("height", "34px");
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

        var rootConfPanel = d3.select(d3.select(svgId).node().parentNode, graph);
        confGraphModal = ConfGraphModal.createConfigurationPanel(rootConfPanel, graph, results);
        graph.updateConfiguration = function () {
            return function () {
                var updateSet = function(groupName, text) {
                    var visibleNodes = new Set();
                    confGraphModal.getGroups(groupName).forEach(
                        group => {
                            var checkbox = confGraphModal.getGroupCheckbox(groupName, group);
                            (checkbox.property("checked")) ? visibleNodes.add(group) : undefined;
                        }
                    );
                    var nodesDisplayCriteria = (d, i, nodes) => (visibleNodes.has(d.group)) ? "visible" : "hidden"
                    text.attr(
                        "visibility",
                        (d, i, nodes) => nodesDisplayCriteria(d, i, nodes)
                    );
                }
                updateSet("nodes", textNodes);
                updateSet("edges", textEdges);
                // textEdges.attr("visibility", confGraphModal.edgesCheckbox.property("checked") ? "visible" : "hidden");
            };
        }(confGraphModal);

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


        var color = d3.scaleOrdinal(d3.schemeCategory20);

        visualizer.simulation = d3.forceSimulation(results.nodes)
            .force("link", d3.forceLink().id(function (d) {
                return d.id;
            }))
            .force("charge", d3.forceManyBody())
            // .force("center", d3.forceCenter(width, height))
            .force("center", d3.forceCenter(800, 500))
            .on("tick", graph.ticked);
        var width = +graph.node().getBoundingClientRect().width;
        var height = +graph.node().getBoundingClientRect().height;
        visualizer.simulation
            .force("link")
            .links(results.links);
        visualizer.simulation
            .force("center", d3.forceCenter(width / 2, height / 2));

        var g = graph.append("g")
            .attr("class", "everything");
        var defs = graph.append("defs");
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


        var link = g.append("g")
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
            .attr("id", d => `${d.id}_edge` );

        link.append("title")
            .text(function (d) {
                return d.label;
            });

        var node = g.append("g")
            .attr("class", "nodes")
            .selectAll("circle")
            .data(results.nodes)
            .enter()
            .append("g")
            .attr("class", "node")
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
            .each((d, i, nodes) => {
                var current = d3.select(nodes[i]);
                var father = d3.select(current.node().parentNode);
                var color = d3.scaleOrdinal(d3.schemeCategory20).domain(Array.from(confGraphModal.getGroups("nodes")));
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
            })
            .call(d3.drag()
                .on("start", visualizer.dragstarted(visualizer.simulation))
                .on("drag", visualizer.dragged)
                .on("end", visualizer.dragended(visualizer.simulation)))
            .on("click", (d) => {
                if (d.url  !== undefined) window.open(d.url);
                if (d.link !== undefined) trans(d.link);
            });
        node.append("title")
            .text(function (d) {
                return d.label;
            });
        var textNodes = g.append("g").attr("class", "texts").selectAll("text")
            .data(visualizer.simulation.nodes())
            .enter().append("text")
            .attr("class", (edge, i, edges) => {
                return (edge.class !== undefined) ? edge.class : "default";
            })
            .text(function (d) {
                return d.label;
            });
        var textEdges = g.append("g").attr("class", "textPaths").selectAll("text")
            .data(results.links)
            .enter().append("text")
            .append("textPath")
            .attr("xlink:href", d => {return `#${d.id}`;})
            .attr("startOffset", "25%")
            .text(function (d) {
                return d.label;
            })
            .attr("class", (edge, i, edges) => {
                return (edge.class !== undefined) ? edge.class : "default";
            })
            .attr("xlink:href", (edge, i, edges) => {
                return "#" + edge.id;
            });


        var displayEdgeLabels = false;
        var displayNodeLabels = false;

        graph.updateConfiguration();
        // var width = +graph.node().getBBox().width;
        // var height = +graph.node().getBBox().height;
        // visualizer.simulation
        //     .force("link")
        //     .links(results.links);
        // visualizer.simulation
        //     .force("center", d3.forceCenter(width / 2, height / 2));
        var pathLabels = graph.append("defs").attr("class", "paths").selectAll("path")
            .data(results.links)
            .enter().append("path")
            .attr("id", (edge, i, edges) => {
                return edge.id;
            })
            .attr("d", visualizer.buildPathFromEdge(1)(results.links));
        //add zoom capabilities
        var zoom_handler = d3.zoom()
            .on("zoom", graph.zoomed);
        zoom_handler(graph);


    }
}
