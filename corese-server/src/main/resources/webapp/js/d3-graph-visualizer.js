var simulation;
var sheet = document.createElement('style');
sheet.innerHTML = ".links line { stroke: black; stroke-width: 0.1; stroke-opacity: 1; marker-end: url(#arrowhead) } .nodes circle { stroke: #fff; stroke-width: 1.5px; }";
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
function buildPathFromEdge(scale)
{
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
function createConfigurationPanel(rootConfPanel, graph) {
	var result = d3.select("#configurationGraph");
	if (result.size() === 0) {

        confGraphModal = rootConfPanel.append("div");
        confGraphModal
            .attr("id", "configurationGraph")
            .attr("class", "modal")
            .html(
            "<div class='modal-content' style='list-style:none;'>" +
            "<ul>" +
            "<li><label><input id='nodesCheckbox' type='checkbox'/>All Nodes Labels</label>" +
            "<ul>" +
            "<li><label><input id='bnodesCheckbox' type='checkbox'/>Blank Nodes</label>" +
            "<li><label><input id='uriCheckbox' type='checkbox'/>URI</label>" +
            "<li><label><input id='literalCheckbox' type='checkbox'/>Literal</label>" +
            "</ul>" +
            "<li><label><input id='edgesCheckbox' type='checkbox'/>Edges</label>" +
            "</ul>" +
            "<br>" +
            "<button id='configurationGraphCloseButton' class='btn btn-default'>Close</button>" +
            "</div>" );

        confGraphModal.nodesCheckbox= d3.select("#nodesCheckbox");
        confGraphModal.bnodesCheckbox = d3.select("#bnodesCheckbox");
        confGraphModal.uriCheckbox = d3.select("#uriCheckbox");
        confGraphModal.literalCheckbox = d3.select("#literalCheckbox");
        confGraphModal.edgesCheckbox = d3.select("#edgesCheckbox");
        confGraphModal.closeButton = d3.select("#configurationGraphCloseButton");

        confGraphModal.nodesCheckbox.on("change", function() {
            [ confGraphModal.bnodesCheckbox, confGraphModal.uriCheckbox, confGraphModal.literalCheckbox ].forEach( button => {
                button.property( "checked", this.checked ); // set all the sub-checkboxes to the same value as nodesCheckbox
            });
            graph.updateConfiguration(); graph.ticked();
        });
        [ confGraphModal.bnodesCheckbox, confGraphModal.uriCheckbox, confGraphModal.literalCheckbox ].forEach( button => {
            button.on("change", function () {
                var allChecked = true;
                [ confGraphModal.bnodesCheckbox, confGraphModal.uriCheckbox, confGraphModal.literalCheckbox ].forEach(
                    button => allChecked = allChecked && button.property("checked")
                );
                confGraphModal.nodesCheckbox.property("checked", allChecked);
                graph.updateConfiguration();
                graph.ticked();
            })
        });
        confGraphModal.edgesCheckbox.on("change", function() {
        	d3.select("#edgesCheckbox").property("checked"); graph.updateConfiguration(); graph.ticked()
        });
        confGraphModal.closeButton
            .on("click", e => {
                confGraphModal.attr("style", "display:none");
            });


        return confGraphModal;
    } else {
		return result;
	}
}
var counter = 1;
// svgName : id of the svg element to draw the graph (do not forget the #).
function drawRdf(results, svgId) {
    var confGraphModal;
    var graph = d3.select(svgId);


    graph.displayNodeLabels = function() {
        // return confGraphModal.nodesCheckbox.property("checked") || ;
        return true;
    }
    graph.displayEdgeLabels = function() {
        return confGraphModal.edgesCheckbox.property("checked");
    }
    graph.ticked = function(s) {
        scale = (s === undefined) ? scale : s;
        link
            .attr("x1", function(d) { return d.source.x * scale; })
            .attr("y1", function(d) { return d.source.y * scale; })
            .attr("x2", function(d) { return d.target.x * scale; })
            .attr("y2", function(d) { return d.target.y * scale; });

        node
            .attr("cx", function(d) { return d.x * scale; })
            .attr("cy", function(d) { return d.y * scale; });
        if (graph.displayNodeLabels()) {
            textNodes
                .attr("x", d => d.x * scale)
                .attr("y", d => d.y * scale);
        }
        if (graph.displayEdgeLabels()) {
            pathLabels.attr( "d", buildPathFromEdge(scale)(results.links) );
        }
    };
    graph.zoomed = function() {
        var copieTransform = new d3.event.transform.constructor(d3.event.transform.k, d3.event.transform.x, d3.event.transform.y);
        copieTransform.k  = 1;
        g.attr("transform", copieTransform);
        graph.ticked( d3.event.transform.k);
    };

    var scale = 1;
	results.links = results.edges;

    var rootConfPanel = d3.select( d3.select(svgId).node().parentNode, graph );
    confGraphModal = createConfigurationPanel(rootConfPanel, graph);
    graph.updateConfiguration = function(modal) {
        return function () {
            var visibleNodes = new Set();
            if (modal.bnodesCheckbox.property("checked")) visibleNodes.add("2");
            if (modal.uriCheckbox.property("checked")) visibleNodes.add("1");
            if (modal.literalCheckbox.property("checked")) visibleNodes.add("3");
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
			maxLen[ link.source ] = Math.max( maxLen[ link.source ], link.label.length );
            maxLen[ link.target ] = Math.max( maxLen[ link.target ], link.label.length );
		}
	);



	var color = d3.scaleOrdinal(d3.schemeCategory20);

    simulation = d3.forceSimulation(results.nodes)
        .force("link", d3.forceLink().id(function(d) { return d.id; }))
        .force("charge", d3.forceManyBody())
        // .force("center", d3.forceCenter(width, height))
        .on("tick", graph.ticked);



	var g = graph.append("g")
	  .attr("class", "everything");
	var defs = graph.append("defs");
    defs.append('marker')
        .attr('id','arrowhead')
        .attr('viewBox','-0 -50 100 100')
        .attr('refX',130)
        .attr('refY',0)
        .attr('orient','auto')
        .attr('markerWidth',130)
        .attr('markerHeight',130)
        .attr('xoverflow','visible')
        .append('svg:path')
        .attr('d', 'M 0,-20 L 100 ,0 L 0,20')
        .style('stroke','grey')
        .style('fill','grey')
    ;


	var link = g.append("g")
		.attr("class", "links")
		.selectAll("line")
		.data(results.links)
		.enter().append("line")
	;

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
    var textNodes = g.append("g").selectAll("text")
        .data(simulation.nodes())
        .enter().append("text")
        .attr("x", 8)
        .attr("y", ".31em")
        .text(function(d) { return d.label; });
    var textEdges = g.append("g").selectAll("text")
        .data(results.links)
        .enter().append("text")
        .append("textPath")
        .attr("startOffset", "25%")
        .text(function(d) { return d.label; })
        .attr("xlink:href", (edge, i, edges) => {
            return "#"+edge.id;
        });

    var fo = graph.append('foreignObject').attr("width", "100%").attr("height", "100%");
    var button = fo.append("xhtml:button")
		.attr("class", "btn btn-info")
		.attr("id", "configurationButton")
		.on("click", e => {
            d3.select("#configurationGraph")
				.style("display","block")
				.style("top", d3.event.y+"px")
				.style("left", d3.event.x+"px");
		});
    button.append("xhtml:span")
        .attr("class", "glyphicon glyphicon-cog");


    var displayEdgeLabels = false;
    var displayNodeLabels = false;

	graph.updateConfiguration();
    var width =  +graph.node().getBBox().width;
    var height =  +graph.node().getBBox().height;
	simulation
		.force("link")
		.links(results.links);
	simulation
        .force("center", d3.forceCenter(width / 2, height / 2));
    var pathLabels = defs.selectAll("path")
        .data(results.links)
        .enter().append("path")
        .attr("id", (edge, i, edges) => {
            return edge.id;
        })
        .attr("d", buildPathFromEdge(1)(results.links) );
	//add zoom capabilities
	var zoom_handler = d3.zoom()
		.on("zoom", graph.zoomed);
	zoom_handler( graph );




}


