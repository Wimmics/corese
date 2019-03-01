"use strict";

export class OntologyDrawer {
    constructor() {
        this.horizontalLayout = true;
        this.setProperties(new Set(["rdfs:subClassOf"])); // - minus means that the representation of the link must be inverted.
    }

    setData(data) {
        this.rawData = data;
        this.dataMap = {};
        for (let d of this.rawData.nodes) {
            this.dataMap[d.id] = {};
            for (let p in d) {
                this.dataMap[d.id][p] = d[p];
            }
            this.dataMap[d.id].isFolded = false;
            this.dataMap[d.id].getActualChildren = function () {
                return this.valChildren;
            };
            this.dataMap[d.id].getVisibleChildren = function () {
                if (!this.isFolded) {
                    return this.valChildren;
                } else {
                    return {};
                }
            };
            this.dataMap[d.id].isLeaf = function () {
                return Object.keys(this.valChildren).length === 0;
            };
            Object.defineProperty(this.dataMap[d.id], "children", {
                get: function () {
                    return this.getVisibleChildren();
                }
            });
            this.dataMap[d.id].valChildren = function () {
            }
        }
        this.edgesMapId = {};
        for (let e of data.edges) {
            this.edgesMapId[e.id] = e;
            let s = e.source.id;
            let t = e.target.id;
            if (this.properties.has(e.label)) {
                if (this.invertProperties[e.label]) {
                    this.dataMap[t].valChildren[s] = true;
                    this.dataMap[s].parent = t;
                    this.dataMap[s].parentEdge = e;
                } else {
                    this.dataMap[s].valChildren[t] = true;
                    this.dataMap[t].parent = s;
                    this.dataMap[t].parentEdge = e;
                }
            }
        }

        // compute numbers of tree roots.
        this.nbRoots = 0;
        this.roots = [];
        for (let d of data.nodes) {
            this.dataMap[d.id].value = Object.keys(this.dataMap[d.id].valChildren).length;
            this.dataMap[d.id].r = 10;
            if (this.dataMap[d.id].parent === undefined && Object.keys(this.dataMap[d.id].valChildren).length !== 0) {
                console.log(`new tree root : ${d.id}`);
                this.nbRoots++;
                this.roots.push(d.id);
            }
        }
        if (this.nbRoots === 0 && data.nodes.length !== 0) { // cyclic graph choosing an arbitrary root.
            this.nbRoots = 1;
            let idPseudoRoot = data.nodes[0].id;
            this.roots.push(idPseudoRoot);
        }
        if (this.nbRoots > 1) {
            this.dataMap["Root"] = {
                id: "Root",
                label: "Root",
                r: 10,
                value: 1,
                isFolded: false,
                getActualChildren() {
                    return this.valChildren;
                },
                getVisibleChildren() {
                    if (!this.isFolded) {
                        return this.valChildren;
                    } else {
                        return {};
                    }
                },
                isLeaf: function () {
                    return Object.keys(this.valChildren).length === 0;
                },
                get children() {
                    return this.getVisibleChildren();
                },
                valChildren: {}
            }
            for (let child of this.roots) {
                this.dataMap["Root"].valChildren[child] = true;
                this.dataMap[child].parent = "Root";
                this.dataMap[child].parentEdge = {class: "root"};
            }
            this.root = "Root";
        } else {
            this.root = this.roots.pop();
        }

        return this;
    }

    /**
     *
     * @param params Expect { rootId : "id", properties: {"prop1", "prop2"}, ["menuNode": menu]}
     */
    setParameters(params) {
        if (params !== undefined) {
            this.parameters = params;
            if ("rootId" in params) {
                this.setDisplayRoot(params.rootId);
            }
            if ("properties" in params) {
                this.setProperties(params.properties);
            }
            if ("menuNode" in params) {
                this.menuNode = params.menuNode;
            }
        }
        return this;
    }

    /*
     *  Set the root used by the tree layout algorithm. I.e. draw the subtree below root (included).
     */
    setDisplayRoot(root) {
        this.displayRoot = root;
        return this;
    }

    setVisibility(node, value, recursive) {
        if (!this.dataMap[node].isLeaf()) {
            this.dataMap[node].isFolded = value;
            if (recursive) {
                for (let child in this.dataMap[node].children) {
                    this.setVisibility(child, value, true);
                }
            }
        }
    }

    switchVisibility(node, recursive) {
        if (!this.dataMap[node].isLeaf()) {
            this.dataMap[node].isFolded = !this.dataMap[node].isFolded;
            this.setVisibility(node, this.dataMap[node].isFolded, recursive);
        }
    }

    /*
     *  Set the properties to be used when extracting the tree.
     */
    setProperties(properties) {
        this.properties = properties;
        this.invertProperties = {};
        let newProperties = new Set();
        for (let currentProp of this.properties.values()) {
            //if (currentProp[0] === '-') {
            //    this.properties.delete(currentProp);
            //    currentProp = currentProp.substring(1, currentProp.length);
            //    this.invertProperties[currentProp] = true;
            //} else {
            //    if (currentProp[0] === '+') {
            //        currentProp = currentProp.substring(1, currentProp.length);
            //    }
            this.invertProperties[currentProp] = true;
            //}
            newProperties.add(currentProp);
        }
        this.properties = newProperties;
    }

    computeHierarchy() {
        if (this.displayRoot === undefined) {
            this.displayRoot = this.root;
        }
        this.slices = {}; // in order to know which nodes are at the same depth.

        // Fill the children map with { id: dataMap[id] }, in order to make the dataMap structure compatible
        // with the layout algorithm.
        this.hierarchy = this.dataMap[this.displayRoot];
        this.hierarchy.depth = 0;
        let stack = [];
        stack.push(this.hierarchy);
        let alreadySeen = new Set();
        alreadySeen.add(this.hierarchy);
        while (stack.length !== 0) {
            let summit = stack.pop();
            if (summit.parent !== undefined) {
                summit.depth = this.dataMap[summit.parent].depth + 1;
            }
            if (this.slices[summit.depth] === undefined) {
                this.slices[summit.depth] = [];
            }
            this.slices[summit.depth].push(summit);
            for (let childId of Object.keys(summit.children)) {
                summit.children[childId] = this.dataMap[childId];
                if (alreadySeen.has(summit.children[childId])) {
                    console.log("cycle detected including node:" + childId);
                    delete summit.children[childId];
                } else {
                    stack.push(summit.children[childId]);
                    alreadySeen.add(summit.children[childId]);
                }
            }
        }
        for (let slice in this.slices) {
            let i = 0;
            for (let n of this.slices[slice]) {
                this.dataMap[n.id].evenNode = (i % 2 === 0);
                i++;
            }
        }

        // compute width and height of the tree
        let recurNode = function (tree, nodeId) {
            let data = tree.dataMap[nodeId];
            let height = 0;
            let width = 0;
            if (Object.keys(data.children).length === 0) {
                height = 1;
                width = 1;
            } else {
                for (let childId of Object.keys(data.children)) {
                    let result = recurNode(tree, childId);
                    height = Math.max(height, result.height);
                    width += result.width;
                }
                height += 1; // count "node" itself.
            }
            return {"height": height, "width": width};
        }
        let geomTree = recurNode(this, this.displayRoot);
        this.width = geomTree.width;
        this.height = geomTree.height;
        return this;
    }

    getWidth() {
        return this.width;
    }

    getHeight() {
        return this.height;
    }

    /**
     * @param svgId
     */
    draw(svgId) {
        this.svgId = svgId;
        d3.select(svgId).node().oncontextmenu = function () {
            return false;
        }
        this.computeHierarchy();
        // set the dimensions and margins of the diagram
        let margin = {top: 20, right: 20, bottom: 20, left: 20};
        let width = Math.max(this.getWidth(), 5) * 45 - margin.left - margin.right;
        let height = Math.max(this.getHeight(), 5) * 125 - margin.top - margin.bottom;
        if (!this.horizontalLayout) {
            let temp = width;
            width = height;
            height = temp;
        }

// declares a tree layout and assigns the size
        var treemap = d3.tree()
            .separation(function (a, b) {
                return (a.data.label.length + b.data.label.length) / 20;
            })
            .nodeSize([50, 150])
        ;

//  assigns the data to a hierarchy using parent-child relationships
        var nodes = d3.hierarchy(this.hierarchy
            , function children(d) {
                return Object.values(d.getVisibleChildren());
            }
        );

// maps the node data to the tree layout
        nodes = treemap(nodes);

// append the svg object to the body of the page
// appends a 'group' element to 'svg'
// moves the 'group' element to the top left margin
        this.svg = d3.select(svgId);
        if (this.g === undefined) {
            this.g = this.svg
                .append("g")
        }
        // this.g.attr("transform",
        //     function () {
        //         // let t = d3.zoom();
        //         // let result = "";
        //         // if (!this.horizontalLayout) {
        //         //     t = t.translate(-margin.left, margin.top + width);
        //         //     // result += `translate( ${margin.left} , ${margin.top + width})`;
        //         // } else {
        //         //     t = t.translate(-margin.left + width, margin.top );
        //         //     // result += `translate( ${margin.left + width} , ${margin.top})`;
        //         // }
        //         this.svg.call(this.zoomListener.transform, t);
        //         // result += "scale(1)";
        //         return result;
        //     }.bind(this));
        // this.centerDisplay();
        // adds the links between the nodes
        this.g.selectAll(".link").remove();
        var link = this.g.selectAll(".link")
            .data(nodes.descendants().slice(1))
            .enter().append("path")
            .attr("class", function(d) {
                let result = "link";
                if (d.data.parentEdge.class !== undefined) {
                    result = `${result} ${d.data.parentEdge.class}`;
                }
                return result;
            })
            .attr("d", function (d) {
                if (this.horizontalLayout) {
                    return "M" + d.y + "," + d.x
                        + "C" + (d.y + d.parent.y) / 2 + "," + d.x
                        + " " + (d.y + d.parent.y) / 2 + "," + d.parent.x
                        + " " + d.parent.y + "," + d.parent.x;
                } else {
                    return "M" + d.x + "," + d.y
                        + "C" + (d.x + d.parent.x) / 2 + "," + d.y
                        + " " + (d.x + d.parent.x) / 2 + "," + d.parent.y
                        + " " + d.parent.x + "," + d.parent.y;
                }
            }.bind(this));
        link.append("title")
            .text((d) => {
                    // return `link between ${d.parent.data.id} -- [${this.dataMap[d.data.id].parentEdge.label}] --> ${d.data.id}` ;
                    let result = "";
                    let first = true;
                    if ("edgePropertiesToDisplay" in this.parameters) {
                        for (let prop of this.parameters.edgePropertiesToDisplay) {
                            if (first) {
                                first = false;
                            } else {
                                result += `\n`;
                            }
                            if (this.dataMap[d.data.id].parentEdge !== undefined) {
                                result += `${prop}: ${this.dataMap[d.data.id].parentEdge[prop]}`;
                            }
                        }
                    }
                    return result;
                }
            );

// begin: draw each node.
        this.g.selectAll(".node").remove();
        var node = this.g.selectAll(".node")
            .data(nodes.descendants())
            .enter().append("g")
            .attr("class", function (_dataMap) {
                    return function (d) {
                        let result = "node" +
                            (_dataMap[d.data.id].isFolded ?
                                " node--folded" :
                                (_dataMap[d.data.id].isLeaf() ? " node--leaf" : " node--internal"));
                        if (_dataMap[d.data.id].class !== undefined) {
                            result = `${result} ${_dataMap[d.data.id].class} `;
                        }
                        return result;
                    }
                }(this.dataMap)
            )
            .attr("transform", function (d) {
                if (this.horizontalLayout) {
                    return "translate(" + d.y + "," + d.x + ")";
                } else {
                    return "translate(" + d.x + "," + d.y + ")";
                }

            }.bind(this));
        node.on("click", (d) => {
            if (d.data.url !== undefined) window.open(d.data.url);
            if (d.data.link !== undefined) trans(d.data.link);
        });
        node
            .append("title")
            .text((d) => {
                let result = "";
                let first = true;
                if ("nodePropertiesToDisplay" in this.parameters) {
                    for (let prop of this.parameters.nodePropertiesToDisplay) {
                        if (first) {
                            first = false;
                        } else {
                            result += `\n`;
                        }
                        result += `${prop}: ${this.dataMap[d.data.id][prop]}`;
                    }
                    return result;
                }
            });
        node.on("contextmenu", (currentNode) => {
            d3.event.preventDefault();
            d3.event.stopImmediatePropagation();
            this.menuNode.setParameters(currentNode);
            this.menuNode.displayOn();
        });

// adds the circle to the node
        node.append("circle")
            .attr("r", (d) => 10);

// adds the text to the node
        let textNode = node.append("text");
        textNode.attr("dy", ".35em")
            .attr("y", function (d) {
                    if (!this.horizontalLayout) {
                        return d.data.evenNode ? "20" : "-20";
                    } else {
                        return "20";
                    }
                }.bind(this)
            )
            .style("text-anchor", "middle")
            .text(function (d) {
                return d.data.label;
            });
// end: draw each node.

        this.zoomed = function () {
            this.g.attr("transform", d3.event.transform);
        };
        this.zoomListener = d3.zoom().on("zoom", this.zoomed.bind(this));

        this.svg.call(this.zoomListener);
        return this;
    }

    switchLayout() {
        this.horizontalLayout = !this.horizontalLayout;
    }

    centerDisplay() {
        this.svg.call(this.zoomListener.transform, d3.zoomIdentity);
    }

    goTop() {
        this.displayRoot = this.root;
    }

    up() {
        if (this.dataMap[this.displayRoot].parent !== undefined) {
            this.displayRoot = this.dataMap[this.displayRoot].parent;
        }
    }

    drawCircle(svgId) {
        this.svgId = svgId;
        this.root = "Root";
        this.computeHierarchy();
        var svg = d3.select(svgId),
            width = +svg.attr("width"),
            height = +svg.attr("height");
        var format = d3.format(",d");

        var color = d3.scaleSequential(d3.interpolateMagma)
            .domain([-4, 4]);

        var stratify = d3.stratify()
            .id((d) => d.id)
            .parentId((d) => d.parent);

        var pack = d3.pack()
            .size([width - 2, height - 2])
            .padding(3);
        var root = stratify(Object.values(this.dataMap))
            .sum(function (d) {
                return d.value + 1;
            })
            .sort(function (a, b) {
                return b.value - a.value;
            })
        ;
        pack(root);

        var node = svg.select("g")
            .selectAll("g")
            .data(root.descendants())
            .enter().append("g")
            .attr("transform", function (d) {
                return "translate(" + d.x + "," + d.y + ") scale(1)";
            })
            .attr("class", function (d) {
                return "node" + (!d.children ? " node--leaf" : d.depth ? "" : " node--root");
            })
            .each(function (d) {
                d.node = this;
            });

        node.append("circle")
            .attr("id", function (d) {
                return "node-" + d.id;
            })
            .attr("r", function (d) {
                return d.r;
            })
            .style("fill", function (d) {
                return color(d.depth);
            });

        var leaf = node.filter(function (d) {
            return !d.children;
        });

        leaf.append("clipPath")
            .attr("id", function (d) {
                return "clip-" + d.id;
            })
            .append("use")
            .attr("xlink:href", function (d) {
                return "#node-" + d.id + "";
            });

        leaf.append("text")
            .attr("clip-path", function (d) {
                return "url(#clip-" + d.id + ")";
            })
            .selectAll("tspan")
            .data(function (d) {
                console.log(d);
                return [d.data.label];
            })
            .enter().append("tspan")
            .attr("x", function (d, i, nodes) {
                return -d.length * 2 - 13
            })
            .attr("y", function (d, i, nodes) {
                return 3;
            })
            .text(function (d) {
                return d;
            });

        node.append("title")
            .text(function (d) {
                return d.data.label;
            });
    }
}