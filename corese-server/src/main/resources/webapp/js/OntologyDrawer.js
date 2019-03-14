"use strict";

export class OntologyDrawer {
    constructor() {
        this.horizontalLayout = false;
        this.setProperties(new Set(["rdfs:subClassOf"])); // - minus means that the representation of the link must be inverted.
        /** !Object */ this.rawData = undefined;
        /** !Object */ this.dataMap = undefined;
    }

    setData(data) {
        this.rawData = data;
        this.dataMap = {};
        for (let d of this.rawData.nodes) {
            /** @TODO dataMap objects should be a class instead of an adhoc structure. */
            this.dataMap[d.id] = {};
            for (let p in d) {
                this.dataMap[d.id][p] = d[p];
            }
            // this.dataMap[d.id].isFolded = false;
            // this.dataMap[d.id].getActualChildren = function () {
            //     return this.valChildren;
            // };
            // this.dataMap[d.id].getVisibleChildren = function () {
            //     if (!this.isFolded) {
            //         return this.valChildren;
            //     } else {
            //         return [];
            //     }
            // };
            // this.dataMap[d.id].isLeaf = function () {
            //     return Object.keys(this.valChildren).length === 0;
            // };
            // Object.defineProperty(this.dataMap[d.id], "children", {
            //     get: function () {
            //         return this.getVisibleChildren();
            //     }
            // });
            this.dataMap[d.id].children = [];
            this.dataMap[d.id].edgeChildren = [];
            /** !Set<string> */ this.dataMap[d.id].parents = new Set();
        }
        this.edgesMapId = {};
        /** @TODO this should be moved to computeHierarchy(). The dataMap structure should be a read-only data
         * structure keeping the graph returned by Corese. */
        for (let e of data.edges) {
            this.edgesMapId[e.id] = e;
            if (this.properties.has(e.label)) {
                let s = e.source.id;
                let t = e.target.id;
                if (this.invertProperties[e.label]) {
                    let temp = s;
                    s = t;
                    t = temp;
                }
                this.dataMap[s].children.push(t);
                this.dataMap[s].edgeChildren.push(e);
                this.dataMap[t].parents.add(s);
            }
        }

        // compute numbers of tree roots.
        this.nbRoots = 0;
        this.roots = [];
        for (let d of data.nodes) {
            this.dataMap[d.id].value = this.dataMap[d.id].children.length;
            this.dataMap[d.id].r = 10;
            if (this.dataMap[d.id].parents.size == 0 || (this.dataMap[d.id].parents.size == 1 && this.dataMap[d.id].parents.has(d.id))) {
                if (this.dataMap[d.id].children.length == 0) {
                    continue;
                }
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
                children: [],
                edgeChildren: []
            };
            for (let child of this.roots) {
                this.dataMap["Root"].children.push(child);
                this.dataMap["Root"].edgeChildren.push({source: "Root", target: child, label: "root", class: "root"});
                this.dataMap[child].parents.add("Root");
            }
            this.root = "Root";
        } else {
            this.root = this.roots.pop();
        }
        this.computeHierarchy();
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

    /**
     *  Set the root used by the tree layout algorithm. I.e. draw the subtree below root (included).
     *  @param {!string} root
     */
    setDisplayRoot(root) {
        this.displayRoot = root.id;
        this.displayRootNode = root;
        return this;
    }

    setVisibility(node, value, recursive) {
        if (!node.isLeaf()) {
            node.isFolded = value;
            if (recursive) {
                for (let child of node.children) {
                    this.setVisibility(child, value, true);
                }
            }
        }
    }

    switchVisibility(node, recursive) {
        if (!node.isLeaf()) {
            node.isFolded = !node.isFolded;
            this.setVisibility(node, node.isFolded, recursive);
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
            if (currentProp[0] === "-") {
                // this.properties.delete(currentProp);
                currentProp = currentProp.substring(1, currentProp.length);
                this.invertProperties[currentProp] = false;
            } else {
                if (currentProp[0] === "+") {
                    currentProp = currentProp.substring(1, currentProp.length);
                }
                this.invertProperties[currentProp] = true;
            }
            newProperties.add(currentProp);
        }
        this.properties = newProperties;
    }

    /**
     * The method builds a *tree* from the graph stored in this.dataRaw and this.dataMap.
     * @returns {OntologyDrawer}
     */
    computeHierarchy() {
        if (this.displayRoot === undefined) {
            this.displayRoot = this.root;
        }

        /**
         *  Represents a tree node.
         *  Note that children store all the children, and getVisibleChildren() returns only the visible ones.
         */
        class Node {

            constructor(id, depth, parent, uplink) {
                /** string */ this.id = id;
                /** !number */ this.depth = depth;
                /** !Node */   this.parent = parent;
                /** Edge  */   this.uplink = uplink;
                /** !Array<Node> */ this.children = [];
                /** !boolean */ this.evenNode = undefined;
                /** string */  this.label = Node.dataMap[this.id].label;
                /** string */ this.url = Node.dataMap[this.id].url;
                /** string */ this.link = Node.dataMap[this.id].link;
                /** boolean */ this.isFolded = false;
            }

            addChild(newChild) {
                this.children.push(newChild);
            }

            /** @return {string} */ toString() {
                let /** !string */ s = "";
                for (let k of ["id", "depth", "evenNode"]) {
                    s += `${k}: ${this[k]} `;
                }
                if (this.parent !== undefined) {
                    s += `parent ${this.parent.id} `;
                } else {
                    s += `no parent `;
                }
                s += "children [";
                for (let k of this.children) {
                    s += `${k.id} `;
                }
                s += "]";
                return s;
            }


            getVisibleChildren() {
                if (this.isFolded) {
                    return [];
                } else {
                    return this.children;
                }
            };

            isLeaf() {
                return (this.children.length === 0);
            };
        }

        Node.dataMap = this.dataMap;

        // Fill the children map with { id: dataMap[id] }, in order to make the dataMap structure compatible
        // with the layout algorithm.
        /** !Object<number, Array<string>> */ this.slices = {}; // in order to know which nodes are at the same depth.
        /** !Node */ this.hierarchy = new Node(this.displayRoot, 0, undefined, undefined);
        let /** !Array<Node> */ stack = [];
        stack.push(this.hierarchy);
        let /** !Set<number> */ alreadySeen = new Set();
        while (stack.length !== 0) {
            let /** !Node */ summit = stack.pop();
            console.log(`summit ${summit}`);
            if (this.slices[summit.depth] === undefined) {
                this.slices[summit.depth] = [];
            }
            this.slices[summit.depth].push(summit);
            if (alreadySeen.has(summit.id)) {
                console.log("cycle detected including node:" + summit.id);
            } else {
                alreadySeen.add(summit.id);
                let /** !Object */ data = this.dataMap[summit.id];
                for (let i = 0; i < data.children.length; i++) {
                    let /** number */ childId = data.children[i];
                    let /** !Edge */ childEdge = data.edgeChildren[i];
                    let /** !Node */ childNode = new Node(childId, summit.depth + 1, summit, childEdge);
                    summit.addChild(childNode);
                    stack.push(childNode);
                }
            }
        }
        for (let /** number */ slice in this.slices) {
            console.log(`slice: ${slice}`);
            let /** number */ i = 0;
            for (let /** !number */ node of this.slices[slice]) {
                node.evenNode = (i % 2 === 0);
                i++;
                console.log(`${node} `);
            }
        }

        /** compute width and height of the tree.
         *  @param {!Node} tree
         *  @return {Object}
         * */
        let recurNode = function (tree) {
            let height = 0;
            let width = 0;
            if (tree !== undefined) {
                for (let /** !Node */ child of tree.children) {
                    let result = recurNode(child);
                    height = Math.max(height, result.height);
                    width += result.width;
                }
                height += 1; // count "node" itself.
                width = Math.max(width, 1);
            }
            return {"height": height, "width": width};
        }.bind(this);
        let geomTree = recurNode(this.hierarchy);
        this.width = geomTree.width;
        this.height = geomTree.height;
        return this;
    }

    /** return {number} */
    getWidth() {
        return this.width;
    }

    /** return {number} */
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
                let nodeA = this.dataMap[a.data.id];
                let nodeB = this.dataMap[b.data.id];
                return (nodeA.label.length + nodeB.label.length) / 20;
            }.bind(this))
            .nodeSize([50, 150])
        ;

//  assigns the data to a hierarchy using parents-child relationships
        var nodes = d3.hierarchy(this.hierarchy, function (d) {
            return d.getVisibleChildren();
        });

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
        this.g.selectAll(".link").remove();
        const link = this.g.selectAll(".link")
            .data(nodes.descendants().slice(1))
            .enter().append("path")
            .attr("class", function (d) {
                let result = "link";
                if (d.data.uplink.class !== undefined) {
                    result = `${result} ${d.data.uplink.class}`;
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
                    // return `link between ${d.parents.data.id} -- [${this.dataMap[d.data.id].parentEdge.label}] --> ${d.data.id}` ;
                    let result = "";
                    let first = true;
                    if ("edgePropertiesToDisplay" in this.parameters) {
                        for (let prop of this.parameters.edgePropertiesToDisplay) {
                            if (first) {
                                first = false;
                            } else {
                                result += "\n";
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
        const node = this.g.selectAll(".node")
            .data(nodes.descendants())
            .enter().append("g")
            .attr("class", function (d) {
                    let result = "node" +
                        (d.data.isFolded ?
                            " node--folded" :
                            (d.data.isLeaf() ? " node--leaf" : " node--internal"));
                    if (d.data.class !== undefined) {
                        result = `${result} ${d.data.class}`;
                    }
                    return result;
                }.bind(this)
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
                            result += "\n";
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
        let divHeight = this.svg.node().parentNode.clientHeight;
        let divWidth = this.svg.node().parentNode.clientWidth;
        let bbox = this.g.node().getBBox();
        let tx = -bbox.x;
        let ty = -bbox.y;
        let scale = Math.min(divHeight / bbox.height, divWidth / bbox.width);
        let zoom = d3.zoomTransform(this.svg.node());
        zoom.k = scale;
        zoom.x = tx * scale;
        zoom.y = ty * scale;
        this.svg.call(this.zoomListener.transform, zoom);
    }

    goTop() {
        this.displayRoot = this.root;
    }

    up() {
        if (this.displayRootNode.parent !== undefined) {
            this.displayRoot = this.displayRootNode.parent.id;
            this.displayRootNode = this.displayRootNode.parent;
        } else if (this.dataMap["Root"] !== undefined) {
            this.displayRoot = "Root";
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