var simulation;
var sheet = document.createElement('style');
sheet.innerHTML = ".links line { stroke: #999; stroke-opacity: 0.6; } .nodes circle { stroke: #fff; stroke-width: 1.5px; }";
document.head.appendChild(sheet);

function dragstarted(d) {
  if (!d3.event.active) simulation.alphaTarget(0.3).restart();
  d.fx = d.x;
  d.fy = d.y;
}

function dragged(d) {
  d.fx = d3.event.x;
  d.fy = d3.event.y;
}

function dragended(d) {
  if (!d3.event.active) simulation.alphaTarget(0);
  d.fx = null;
  d.fy = null;
}

// svgName : id of the svg element to draw the graph (do not forget the #).
function drawRdf(results, svgId) {
	results.links = results.edges;

	if (d3.select("#configurationGraph").size() === 0) {
        var body = d3.select("body");
        var confGraphModal = body
            .append("div")
            .attr("id", "configurationGraph")
            .attr("class", "modal");
        var divModal = confGraphModal
            .append("div")
            .attr("class", "modal-content");
        divModal.append("div").html("<label class=\"switch\">\n" +
            "  <input id=\"nodesCheckbox\" type=\"checkbox\" > Nodes\n" +
            "</label>\n" +
            "<label class=\"switch\">\n" +
            "  <input id=\"edgesCheckbox\" type=\"checkbox\" > Labels\n" +
            "  <span class=\"slider\"></span>" +
            "</label>");
        d3.select("#nodesCheckbox").on("change", e => {displayNodeLabels = d3.select("#nodesCheckbox").property("checked"); updateConfiguration(); ticked()});
        d3.select("#edgesCheckbox").on("change", e => {displayEdgeLabels = d3.select("#edgesCheckbox").property("checked"); updateConfiguration(); ticked()});
        var confGraphModalClose = divModal.append("button").attr("class", "btn btn-default").text("Close");
    }

	var graph = d3.select(svgId),
		width =  +graph.attr("width"),
		height = +graph.attr("height");
	var color = d3.scaleOrdinal(d3.schemeCategory20);

    simulation = d3.forceSimulation(results.nodes)
        .force("link", d3.forceLink().id(function(d) { return d.id; }))
        .force("charge", d3.forceManyBody())
        .force("center", d3.forceCenter(width / 2, height / 2))
        .on("tick", ticked);



	var g = graph.append("g")
	  .attr("class", "everything");
	var link = g.append("g")
		.attr("class", "links")
		.selectAll("line")
		.data(results.links)
		.enter().append("line")
		.attr("stroke-width", function(d) { return Math.sqrt(d.value); });
	link.append("title")
		.text(function(d) { return d.label; });

	var node = g.append("g")
		.attr("class", "nodes")
		.selectAll("circle")
		.data(results.nodes)
		.enter().append("circle")
		.attr("r", 5)
		.attr("fill", function(d) { return color(d.group); })
		.call(d3.drag()
			.on("start", dragstarted)
			.on("drag", dragged)
			.on("end", dragended));
	node.append("title")
		.text(function(d) { return d.label; });
    textNodes = g.append("g").selectAll("text")
        .data(simulation.nodes())
        .enter().append("text")
        .attr("x", 8)
        .attr("y", ".31em")
        .text(function(d) { return d.label; });
    var textEdges = g.append("g").selectAll("text")
        .data(results.links)
        .enter().append("text")
        .attr("x", 8)
        .attr("y", ".31em")
        .text(function(d) { return d.label; });

    var fo = graph.append('foreignObject').attr("width", "100%").attr("height", "100%");
    var button = fo.append("xhtml:button")
		.attr("class", "btn btn-info")
		.attr("id", "configurationButton")
		.on("click", e => {
            d3.select("#nodesCheckbox").property("checked", displayNodeLabels);
            d3.select("#edgesCheckbox").property("checked", displayEdgeLabels);
            confGraphModal.attr("style", "display:block");
		});
    button.append("xhtml:span")
        .attr("class", "glyphicon glyphicon-cog");
    confGraphModalClose
		.on("click", e => {
            confGraphModal.attr("style", "display:none");
		});

    var displayEdgeLabels = false;
    var displayNodeLabels = false;
    d3.select("body")
        .on("keydown", function() {
            switch (d3.event.keyCode) {
				case 49: {
                    displayNodeLabels = !displayNodeLabels;
                    ticked();
                    break;
                }
				case 50: {
					displayEdgeLabels = !displayEdgeLabels;
					ticked();
					break;
				}
			}
        }
    );

	updateConfiguration();
	simulation.force("link")
		.links(results.links); 
	//add zoom capabilities
	var zoom_handler = d3.zoom()
		.on("zoom", zoomed);
	zoom_handler( graph );

	function updateConfiguration() {
        if (!displayNodeLabels) {
            textNodes.attr("visibility", "hidden");
        } else {
            textNodes.attr("visibility", "visible");
        }
        if (!displayEdgeLabels) {
            textEdges.attr("visibility", "hidden");
        } else {
            textEdges.attr("visibility", "visible");
        }
	}
	function ticked() {
		link
			.attr("x1", function(d) { return d.source.x; })
			.attr("y1", function(d) { return d.source.y; })
			.attr("x2", function(d) { return d.target.x; })
			.attr("y2", function(d) { return d.target.y; });

		node
			.attr("cx", function(d) { return d.x; })
			.attr("cy", function(d) { return d.y; });
		if (displayNodeLabels) {
            textNodes
                .attr("x", d => d.x)
                .attr("y", d => d.y);
        }
        if (displayEdgeLabels) {
            textEdges.attr("transform", (d,i,nodes) => {
                // return "translate(" + ((d.source.x + d.target.x) / 2) + "," + ((d.source.y + d.target.y - nodes[i].getBBox().width) / 2) + ")"
                return "translate(" + ((d.source.x + d.target.x - nodes[i].getBBox().width) / 2) + "," + ((d.source.y + d.target.y) / 2) + ")"
            });
        }
	}
	function zoomed() {
		g.attr("transform", d3.event.transform)
	}
}


