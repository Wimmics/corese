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

	simulation = d3.forceSimulation()
		.force("link", d3.forceLink().id(function(d) { return d.id; }))
		.force("charge", d3.forceManyBody())
		.force("center", d3.forceCenter(width / 2, height / 2));



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

	simulation
		.nodes(results.nodes)
		.on("tick", ticked);

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
	}
	function zoomed() {
		g.attr("transform", d3.event.transform)
	}
}


