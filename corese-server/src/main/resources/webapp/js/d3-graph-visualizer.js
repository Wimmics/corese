var simulation;
var sheet = document.createElement('style')
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
    var textNodes = graph.append("g").selectAll("text")
        .data(simulation.nodes())
        .enter().append("text")
        .attr("x", 8)
        .attr("y", ".31em")
        .text(function(d) { return d.label; });
    var textEdges = graph.append("g").selectAll("text")
        .data(results.links)
        .enter().append("text")
        .attr("x", 8)
        .attr("y", ".31em")
        .text(function(d) { return d.label; });
    var displayEdgeLabels = true;
    var displayNodeLabels = true;
    d3.select("body")
        .on("keydown", function() {
        	console.log("coucou "+d3.event.keyCode)
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


	simulation.force("link")
		.links(results.links); 
	//add zoom capabilities
	var zoom_handler = d3.zoom()
		.on("zoom", zoomed);
	zoom_handler( graph );


	function ticked() {
		link
			.attr("x1", function(d) { return d.source.x; })
			.attr("y1", function(d) { return d.source.y; })
			.attr("x2", function(d) { return d.target.x; })
			.attr("y2", function(d) { return d.target.y; });

		node
			.attr("cx", function(d) { return d.x; })
			.attr("cy", function(d) { return d.y; });
        textNodes.attr("transform", d => "translate(" + d.x + "," + d.y + ")");
        textEdges.attr("transform", d => "translate(" + (( d.source.x + d.target.x ) / 2) + "," + (( d.source.y + d.target.y ) / 2 ) + ")");
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
	function zoomed() {
		g.attr("transform", d3.event.transform)
	}
}


