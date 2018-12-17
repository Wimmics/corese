export class OntologyDrawer {
    constructor() {
    }

    setData(data) {
        this.rawData = data;
        this.dataMap = {};
        for (let d of data.nodes) {
            this.dataMap[d.id] = d;
            this.dataMap[d.id].children = {};
        }
        for (let e of data.edges) {
            let s = e.source;
            let t = e.target;
            if (e.label === "rdfs:subClassOf") {
                this.dataMap[t].children[s] = true;
            }
        }



        return this;
    }

    setRoot(root) {
        this.root = root;
        return this;
    }
    computeHierarchy() {
        this.hierarchy = this.dataMap[this.root];
        let stack = [];
        stack.push(this.hierarchy);
        while (stack.length !== 0) {
            let summit = stack.pop();
            for (let childId of Object.keys(summit.children)) {
                summit.children[childId] = this.dataMap[childId];
                stack.push(summit.children[childId]);
            }
        }
    }

    draw(svgId) {
        this.svgId = svgId;
        this.computeHierarchy();
        // set the dimensions and margins of the diagram
        var margin = {top: 40, right: 20, bottom: 20, left: 20},
            width = 660 - margin.left - margin.right,
            height = 500 - margin.top - margin.bottom;

// declares a tree layout and assigns the size
        var treemap = d3.tree()
            .size([width, height]);

//  assigns the data to a hierarchy using parent-child relationships
//         var nodes = d3.hierarchy( this.hierarchy,
//             function(d) {
//                 return Object.values(d);
//             }
//         );
        var nodes = d3.hierarchy(this.hierarchy
            , function children(d) {
                return Object.values(d.children);
            }
        );

// maps the node data to the tree layout
        nodes = treemap(nodes);

// append the svg obgect to the body of the page
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

// adds the circle to the node
        node.append("circle")
            .attr("r", 10);

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
    }

    addOptionButton() {
        "use strict";
        let selectButtonId = "selectButton";
        if (d3.select(`#${selectButtonId}`).empty()) {
            this.selectButton = d3.select(`body`).append("select").
            attr("id", selectButtonId).
            attr("class","select");
            this.selectButton.selectAll("option").data(this.rawData.nodes).enter().
            append("option").attr("value", (d) => d.id).text((d)=>d.label);
            this.selectButton.on("change", (d) => {
                const selectValue = d3.select(`#${selectButtonId}`).property('value')
                this.setRoot(selectValue);
                this.draw(this.svgId);
            });
        }
    }
}