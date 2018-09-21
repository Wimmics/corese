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
var counter = 1;
// svgName : id of the svg element to draw the graph (do not forget the #).
function drawRdf(results, svgId) {
    var scale = 1;
	results.links = results.edges;
    var confGraphModal;
    if (d3.select("#configurationGraph").size() === 0) {
        var body = d3.select( d3.select(svgId).node().parentNode );
        confGraphModal = body
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
            "  <input id=\"edgesCheckbox\" type=\"checkbox\" > Edges\n" +
            "  <span class=\"slider\"></span>" +
            "</label>");
        d3.select("#nodesCheckbox").on("change", e => {displayNodeLabels = d3.select("#nodesCheckbox").property("checked"); updateConfiguration(); ticked()});
        d3.select("#edgesCheckbox").on("change", e => {displayEdgeLabels = d3.select("#edgesCheckbox").property("checked"); updateConfiguration(); ticked()});
        var confGraphModalClose = divModal.append("button").attr("class", "btn btn-default").text("Close");
        confGraphModalClose
            .on("click", e => {
                confGraphModal.attr("style", "display:none");
            });
    }
    var maxLen = [];
    results.nodes.forEach(
		(node, index, array) => {
			maxLen[node.id] = 0;
		}
	);
    results.links.forEach(
    	(link, index, array) => {
			maxLen[ link.source ] = Math.max( maxLen[ link.source ], link.label.length );
            maxLen[ link.target ] = Math.max( maxLen[ link.target ], link.label.length );
		}
	);

	var graph = d3.select(svgId);

	var color = d3.scaleOrdinal(d3.schemeCategory20);

    simulation = d3.forceSimulation(results.nodes)
        .force("link", d3.forceLink().id(function(d) { return d.id; }))
        .force("charge", d3.forceManyBody())
        // .force("center", d3.forceCenter(width, height))
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
            d3.select("#configurationGraph")
				.style("display","block")
				.style("top", d3.event.y+"px")
				.style("left", d3.event.x+"px");
		});
    button.append("xhtml:span")
        .attr("class", "glyphicon glyphicon-cog");


    var displayEdgeLabels = false;
    var displayNodeLabels = false;
    d3.select("body")
        .on("keydown", function() {
            switch (d3.event.keyCode) {
				case 49: {
                    displayNodeLabels = !displayNodeLabels;
                    updateConfiguration();
                    ticked();
                    break;
                }
				case 50: {
					displayEdgeLabels = !displayEdgeLabels;
                    updateConfiguration();
					ticked();
					break;
				}
			}
        }
    );

	updateConfiguration();
    var width =  +graph.node().getBBox().width;
    var height =  +graph.node().getBBox().height;
	simulation
		.force("link")
		.links(results.links);
	simulation
        .force("center", d3.forceCenter(width / 2, height / 2));
	//add zoom capabilities
	var zoom_handler = d3.zoom()
		.on("zoom", zoomed);
	zoom_handler( graph );

	function updateConfiguration() {
            textNodes.attr("visibility", displayNodeLabels ? "visible" : "hidden");
            textEdges.attr("visibility", displayEdgeLabels ? "visible" : "hidden");
	}

	function ticked(s) {
		scale = (s === undefined) ? scale : s;
		link
			.attr("x1", function(d) { return d.source.x * scale; })
			.attr("y1", function(d) { return d.source.y * scale; })
			.attr("x2", function(d) { return d.target.x * scale; })
			.attr("y2", function(d) { return d.target.y * scale; });

		node
			.attr("cx", function(d) { return d.x * scale; })
			.attr("cy", function(d) { return d.y * scale; });
		if (displayNodeLabels) {
            textNodes
                .attr("x", d => d.x * scale)
                .attr("y", d => d.y * scale);
        }
        if (displayEdgeLabels) {
            textEdges
				.attr("x", (d,i,nodes) => ((d.source.x + d.target.x) / 2) * scale )
                .attr("y", (d,i,nodes) => ((d.source.y + d.target.y) / 2) * scale );
        }
	}
	function zoomed() {
	    var copieTransform = new d3.event.transform.constructor(d3.event.transform.k, d3.event.transform.x, d3.event.transform.y);
	    copieTransform.k  = 1;
		g.attr("transform", copieTransform);
        ticked( d3.event.transform.k);
	}
}


