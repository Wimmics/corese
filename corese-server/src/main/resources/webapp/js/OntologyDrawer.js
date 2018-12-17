export class OntologyDrawer {
    constructor() {
    }

    setData(data) {
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
        return this;
    }

    setRoot(root) {
        this.root = root;
        return this;
    }

    draw(svgId) {
        // set the dimensions and margins of the diagram
        var margin = {top: 40, right: 20, bottom: 20, left: 20},
            width = 660 - margin.left - margin.right,
            height = 500 - margin.top - margin.bottom;
        width *= 1.5;

// declares a tree layout and assigns the size
        var treemap = d3.tree()
            .size([width, height]);

//  assigns the data to a hierarchy using parent-child relationships
//         var nodes = d3.hierarchy( this.hierarchy,
//             function(d) {
//                 return Object.values(d);
//             }
//         );
        var nodes = d3.hierarchy( this.hierarchy
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
        let g = svg.append("g")
                .attr("transform",
                    "translate(" + margin.left + "," + margin.top + ")");
        // adds the links between the nodes
        var link = g.selectAll(".link")
            .data( nodes.descendants().slice(1))
            .enter().append("path")
            .attr("class", "link")
            .attr("d", function(d) {
                return "M" + d.x + "," + d.y
                    + "C" + d.x + "," + (d.y + d.parent.y) / 2
                    + " " + d.parent.x + "," +  (d.y + d.parent.y) / 2
                    + " " + d.parent.x + "," + d.parent.y;
            });

// adds each node as a group
        var node = g.selectAll(".node")
            .data(nodes.descendants())
            .enter().append("g")
            .attr("class", function(d) {
                return "node" +
                    (d.children ? " node--internal" : " node--leaf"); })
            .attr("transform", function(d) {
                return "translate(" + d.x + "," + d.y + ")"; });

// adds the circle to the node
        node.append("circle")
            .attr("r", 10);

// adds the text to the node
        node.append("text")
            .attr("dy", ".35em")
            .attr("y", function(d) { return d.children ? -20 : 20; })
            .style("text-anchor", "middle")
            .text(function(d) { return d.data.label; });
    }
}