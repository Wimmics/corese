"use strict";
export class OntologyDrawer {
    constructor() {
        this.setProperties({"rdfs:subClassOf": true});
    }

    setData(data) {
        this.rawData = data;
        this.dataMap = {};
        for (let d of this.rawData.nodes) {
            this.dataMap[d.id] = {
                id: d.id,
                label: d.label,
                group: d.group,
                class: d.class,
                url: d.url
            };
            this.dataMap[d.id].children = {};
        }
        for (let e of data.edges) {
            let s = e.source.id;
            let t = e.target.id;
            if (e.label  in this.properties) {
                this.dataMap[t].children[s] = true;
                this.dataMap[s].parent = t;
            }
        }

        // compute numbers of tree roots.
        this.nbRoots = 0;
        this.roots = [];
        for (let d of data.nodes) {
            this.dataMap[d.id].value = Object.keys(this.dataMap[d.id].children).length;
            this.dataMap[d.id].r = 10;
            if (this.dataMap[d.id].parent === undefined && Object.keys(this.dataMap[d.id].children).length !== 0) {
                console.log(`new tree root : ${d.id}`);
                this.nbRoots++;
                this.roots.push(d.id);
            }
        }
        if (this.nbRoots > 1) {
            this.dataMap["Root"] = {
                id: "Root",
                label: "Root",
                r: 10,
                value:1,
                children: {}
            }
            for (let child of this.roots) {
                this.dataMap["Root"].children[child] = true;
                this.dataMap[child].parent = "Root";
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
    setParameters( params ) {
        if (params !== undefined) {
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
    /*
     *  Set the properties to be used when extracting the tree.
     */
    setProperties(properties) {
        this.properties = properties;
    }
    computeHierarchy() {
        if (this.displayRoot === undefined) {
            this.displayRoot = this.root;
        }
        // Fill the children map with { id: dataMap[id] }, in order to make the dataMap structure compatible
        // with the layout algorithm.
        this.hierarchy = this.dataMap[this.displayRoot];
        let stack = [];
        stack.push(this.hierarchy);
        while (stack.length !== 0) {
            let summit = stack.pop();
            for (let childId of Object.keys(summit.children)) {
                summit.children[childId] = this.dataMap[childId];
                stack.push(summit.children[childId]);
            }
        }

        // compute width and height of the tree
        let recurNode = function(tree, nodeId) {
            let data = tree.dataMap[nodeId];
            let height = 0;
            let width = 0;
            if ( Object.keys(data.children).length == 0) {
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
            return { "height": height, "width": width};
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
        d3.select(svgId).node().oncontextmenu = function() { return false; }
        this.computeHierarchy();
        // set the dimensions and margins of the diagram
        let margin = {top: 40, right: 20, bottom: 20, left: 20};
        let width = Math.max(this.getWidth(), 5 ) * 45 - margin.left - margin.right;
        let height = Math.max( this.getHeight(),5 ) * 125 - margin.top - margin.bottom;

// declares a tree layout and assigns the size
        var treemap = d3.tree()
            .size([width, height]);

//  assigns the data to a hierarchy using parent-child relationships
        var nodes = d3.hierarchy(this.hierarchy
            , function children(d) {
                return Object.values(d.children);
            }
        );

// maps the node data to the tree layout
        nodes = treemap(nodes);

// append the svg object to the body of the page
// appends a 'group' element to 'svg'
// moves the 'group' element to the top left margin
        let svg = d3.select(svgId);
        svg.selectAll("g").remove();
        let g = svg.append("g")
            .attr("transform",
                "translate(" + margin.left + "," + margin.top + ")");
        // adds the links between the nodes
        var link = g.selectAll(".link")
            .data(nodes.descendants().slice(1))
            .enter().append("path")
            .attr("class", "link")
            .attr("d", function (d) {
                return "M" + d.y + "," + d.x
                    + "C" + (d.y + d.parent.y) / 2 + "," + d.x
                    + " " + (d.y + d.parent.y) / 2 + "," + d.parent.x
                    + " " + d.parent.y + "," + d.parent.x;
            });

// adds each node as a group
        var node = g.selectAll(".node")
            .data(nodes.descendants())
            .enter().append("g")
            .attr("class", function (d) {
                return "node" +
                    (d.children ? " node--internal" : " node--leaf");
            })
            .attr("transform", function (d) {
                return "translate(" + d.y + "," + d.x + ")";
            });
        node.on("contextmenu", (currentNode) => {
            d3.event.preventDefault();
            d3.event.stopImmediatePropagation();
            this.menuNode.setParameters( currentNode );
            this.menuNode.displayOn();
        });

// adds the circle to the node
        node.append("circle")
            .attr("r", (d) => 10 );

// adds the text to the node
        node.append("text")
            .attr("dy", ".35em")
            .attr("y", function (d) {
                return d.children ? -20 : 20;
            })
            .style("text-anchor", "middle")
            .text(function (d) {
                return d.data.label;
            });



        this.addOptionButton();

        let zoomed = function () {
            g.attr("transform", d3.event.transform);
        };
        var zoom_handler = d3.zoom().on("zoom", zoomed);
        zoom_handler(svg);
        return this;
    }

    up() {
        if (this.dataMap[this.displayRoot].parent !== undefined) {
            this.displayRoot = this.dataMap[this.displayRoot].parent;
        }
    }

    addOptionButton() {
        "use strict";
        let selectButtonId = "selectButton";
        if (d3.select(`#${selectButtonId}`).empty()) {
            var fo = d3.select(this.svgId).append('foreignObject').attr("width", "100%").attr("height", "34px");
            this.selectButton = fo.append("xhtml:select").
            attr("id", selectButtonId).
            attr("class","select");
            this.selectButton.selectAll("option").data(Object.keys(this.dataMap)).enter().
            append("option").attr("value", (d) => d).text((d)=> this.dataMap[d].label);
            this.selectButton.on("change", (d) => {
                const selectValue = d3.select(`#${selectButtonId}`).property('value')
                this.setDisplayRoot(selectValue);
                this.draw(this.svgId);
            });
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
            .sum(function(d) { return d.value+1; } )
            .sort(function(a, b) { return b.value - a.value; })
        ;
        pack(root);

        var node = svg.select("g")
            .selectAll("g")
            .data(root.descendants())
            .enter().append("g")
            .attr("transform", function(d) {
                return "translate(" + d.x + "," + d.y + ")";
            })
            .attr("class", function(d) {
                return "node" + (!d.children ? " node--leaf" : d.depth ? "" : " node--root");
            })
            .each(function(d) {
                d.node = this;
            });

        node.append("circle")
            .attr("id", function(d) { return "node-" + d.id; })
            .attr("r", function(d) { return d.r; })
            .style("fill", function(d) { return color(d.depth); });
        // node.append("text")
        //     .text(function(d) { return d.children ? "" : d.data.label; });

        var leaf = node.filter(function(d) { return !d.children; });

        leaf.append("clipPath")
            .attr("id", function(d) { return "clip-" + d.id; })
            .append("use")
            .attr("xlink:href", function(d) { return "#node-" + d.id + ""; });

        leaf.append("text")
            .attr("clip-path", function(d) { return "url(#clip-" + d.id + ")"; })
            .selectAll("tspan")
            .data(function(d) { console.log(d);return [ d.data.label ]; })
            .enter().append("tspan")
            .attr("x", function(d, i, nodes) { return -d.length*2-13} )
            .attr("y", function(d, i, nodes) { return 3; })
            .text(function(d) { return d; });

        node.append("title")
            .text(function(d) { return d.data.label; });
    }
}