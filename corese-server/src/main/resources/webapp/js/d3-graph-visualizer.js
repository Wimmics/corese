class GraphModel {
    constructor() {
        this.nodeRadius = 5;
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
        this.nodeGroups = this.computeGroups(data);
        this.domNode = root.append("div")
            .attr("id", this.id)
            .attr("class", "modal")
            .html(
                this.createLabelsLi(this.nodeGroups)
            );

        this.nodesCheckbox = d3.select("#nodesCheckbox");
        this.edgesCheckbox = d3.select("#edgesCheckbox");
        this.closeButton = d3.select("#configurationGraphCloseButton");
        var nodeGroupCheckboxes = new Set();
        this.nodeGroups.forEach(
            group => nodeGroupCheckboxes.add( this.getGroupCheckbox(group))
        );

        // when father checkbox is changed all sub-checkboxes are changed to the same value.
        this.nodesCheckbox.on("change",
            function (container) {
                return function (evt) {
                    nodeGroupCheckboxes.forEach(
                       checkbox => checkbox.property("checked", container.property("checked"))
                    )
                    graph.updateConfiguration();
                    graph.ticked();
                }
            }(this.nodesCheckbox)
        );
        // when one of subcheckboxes is checked, update if necessary the father checkbox :
        //   - if all subcheckboxes are unset, unset the father checkbox ;
        //   - if all subcheckboxes are set, set the father checkbox.
        nodeGroupCheckboxes.forEach(button =>
            function (container) {
                return button.on("change", function () {
                    var allChecked = true;
                    nodeGroupCheckboxes.forEach(
                        button => allChecked = allChecked && button.property("checked")
                    );
                    container.nodesCheckbox.property("checked", allChecked);
                    graph.updateConfiguration();
                    graph.ticked();
                })
            }(this)
        );
        this.edgesCheckbox.on("change", function () {
            d3.select("#edgesCheckbox").property("checked");
            graph.updateConfiguration();
            graph.ticked()
        });
        this.closeButton
            .on("click", e => {
                this.displayOff();
            });
    }

    getNodeGroups() {
        return this.nodeGroups;
    }

    computeGroups(data) {
        var result = new Set();
        data.nodes.forEach(
           node => {
               if (!result.has(node.group)) {
                   result.add(node.group);
               }
           }
        )
        console.log(`found groups: ${result}`)
        return result;
    }

    createLabelsLi(groups) {
        var result =
        "<div class='modal-content' style='list-style:none;'>" +
        "<ul>" +
        "<li><label><input id='nodesCheckbox' type='checkbox'/>All Nodes Labels</label>" +
        "<ul>";
        groups.forEach( group => result += `<li><label><input id='${group}Checkbox' type='checkbox'/>${group}</label>` );
        result += "</ul>" +
        "<li><label><input id='edgesCheckbox' type='checkbox'/>Edges</label>" +
        "</ul>" +
        "<br>" +
        "<button id='configurationGraphCloseButton' class='btn btn-default'>Close</button>" +
        "</div>";
        return result;
    }

    getGroupCheckbox(group) {
        return d3.select(`#${group}Checkbox`);
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
        sheet.innerHTML = ".links line { stroke: black; stroke-width: 0.1; stroke-opacity: 1; marker-end: url(#arrowhead) } "
            + ".nodes circle { stroke: #fff; stroke-width: 1.5px; }"
            // + ".nodes circle.special1 { stroke: red; fill: green; stroke-width: 4px; }"
            + ".links line.bigredline { stroke: red; stroke-width: 5; markerWidth: 20; markerHeight: 20;}";
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

    buildPathFromEdge(scale) {
        return links => {
            return (edge, i, edges) => {
                var lefterPoint, righterPoint;
                [lefterPoint, righterPoint] = (links[i].source.x <= links[i].target.x) ? [links[i].source, links[i].target] : [links[i].target, links[i].source];
                var leftx = lefterPoint.x * scale;
                var lefty = lefterPoint.y * scale;
                var rightx = righterPoint.x * scale;
                var righty = righterPoint.y * scale;

                return `M ${leftx},${lefty} L ${rightx},${righty}`;
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


        // TODO : à corriger, il faut renvoyer true ssi au moins un group de noeuds est à afficher
        graph.displayNodeLabels = function () {
            // return confGraphModal.nodesCheckbox.property("checked") || ;
            return true;
        }
        graph.displayEdgeLabels = function () {
            return confGraphModal.edgesCheckbox.property("checked");
        }
        graph.ticked = function (s) {
            scale = (s === undefined) ? scale : s;
            link
                .attr("x1", function (d) {
                    return d.source.x * scale;
                })
                .attr("y1", function (d) {
                    return d.source.y * scale;
                })
                .attr("x2", function (d) {
                    return d.target.x * scale;
                })
                .attr("y2", function (d) {
                    return d.target.y * scale;
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
                confGraphModal.displayOn()
            });
        button.append("xhtml:span")
            .attr("class", "glyphicon glyphicon-cog");
        results.links = results.edges;

        var rootConfPanel = d3.select(d3.select(svgId).node().parentNode, graph);
        confGraphModal = ConfGraphModal.createConfigurationPanel(rootConfPanel, graph, results);
        graph.updateConfiguration = function (modal) {
            return function () {
                var visibleNodes = new Set();
                confGraphModal.getNodeGroups().forEach(
                    group => {
                        var checkbox = confGraphModal.getGroupCheckbox(group);
                        (checkbox.property("checked")) ? visibleNodes.add(group) : undefined;
                    }
                )
                var nodesDisplayCriteria = (d, i, nodes) => (visibleNodes.has(d.group)) ? "visible" : "hidden"
                textNodes.attr(
                    "visibility",
                    (d, i, nodes) => nodesDisplayCriteria(d, i, nodes)
                );
                textEdges.attr("visibility", confGraphModal.edgesCheckbox.property("checked") ? "visible" : "hidden");
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
            .on("tick", graph.ticked);


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
            .selectAll("line")
            .data(results.links)
            .enter().append("line")
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
        ;

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
                var color = d3.scaleOrdinal(d3.schemeCategory20).domain(Array.from(confGraphModal.getNodeGroups()));
                if (d.bg_image === undefined) {
                    current.attr("fill", color(d.group));
                } else {
                    var width = Math.sqrt(2) * current.attr("r");
                    father.append("image").attr("xlink:href", d.bg_image).attr("height", width).attr("width", width);
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
        var textNodes = g.append("g").selectAll("text")
            .data(visualizer.simulation.nodes())
            .enter().append("text")
            .attr("class", (edge, i, edges) => {
                return (edge.class !== undefined) ? edge.class : "default";
            })
            .text(function (d) {
                return d.label;
            });
        var textEdges = g.append("g").selectAll("text")
            .data(results.links)
            .enter().append("text")
            .append("textPath")
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
        var width = +graph.node().getBBox().width;
        var height = +graph.node().getBBox().height;
        visualizer.simulation
            .force("link")
            .links(results.links);
        visualizer.simulation
            .force("center", d3.forceCenter(width / 2, height / 2));
        var pathLabels = defs.selectAll("path")
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
