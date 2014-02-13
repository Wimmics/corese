/** 
* Controler code for the Corese/KGRAM web application
*
* author : alban.gaignard@cnrs.fr
*/


// Useful functions for array handling
Array.prototype.contains = function(a) { return this.indexOf(a) != -1 };
Array.prototype.remove = function(a) {if (this.contains(a)){ return this.splice(this.indexOf(a),1)}};

function alertTimeout(wait){
    setTimeout(function(){
        $('#footer').children('.alert:last-child').remove();
    }, wait);
}

// var optsSpinner = {
//   lines: 9, // The number of lines to draw
//   length: 10, // The length of each line
//   width: 14, // The line thickness
//   radius: 6, // The radius of the inner circle
//   corners: 1, // Corner roundness (0..1)
//   rotate: 29, // The rotation offset
//   direction: 1, // 1: clockwise, -1: counterclockwise
//   color: '#000', // #rgb or #rrggbb
//   speed: 1, // Rounds per second
//   trail: 40, // Afterglow percentage
//   shadow: false, // Whether to render a shadow
//   hwaccel: false, // Whether to use hardware acceleration
//   className: 'spinner', // The CSS class to assign to the spinner
//   zIndex: 2e9, // The z-index (defaults to 2000000000)
//   top: 'auto', // Top position relative to parent in px
//   left: 'auto' // Left position relative to parent in px
// };
// var target = document.getElementById('btnLoad');
// var spinner = new Spinner(optsSpinner).spin(target);

// The root URL for the RESTful services
var rootURL = "http://"+window.location.host+"/kgram";
console.log("Connected to the Corese/KGRAM endpoint "+rootURL);

// reset the connected data sources so that when teh page is reloaded, the gui is synchronized with the server. 
resetDQP();

var statVOID = ["SELECT (COUNT(*) AS ?no) { ?s ?p ?o  }", 
"SELECT (COUNT(distinct ?s) AS ?no) { ?s a []  }",
"SELECT (COUNT(DISTINCT ?s ) AS ?no) { { ?s ?p ?o  } UNION { ?o ?p ?s } FILTER(!isBlank(?s) && !isLiteral(?s)) }",
"SELECT (COUNT(distinct ?o) AS ?no) { ?s rdf:type ?o }",
"SELECT (count(distinct ?p) AS ?no) { ?s ?p ?o }",
"SELECT (COUNT(DISTINCT ?s ) AS ?no) {  ?s ?p ?o }",
"SELECT (COUNT(DISTINCT ?o ) AS ?no) {  ?s ?p ?o  filter(!isLiteral(?o)) }",
"SELECT DISTINCT ?type { ?s a ?type }",
"SELECT DISTINCT ?p { ?s ?p ?o }",
"SELECT  ?class (COUNT(?s) AS ?count ) { ?s a ?class } GROUP BY ?class ORDER BY ?count",
"SELECT  ?p (COUNT(?s) AS ?count ) { ?s ?p ?o } GROUP BY ?p ORDER BY ?count",
"SELECT  ?p (COUNT(DISTINCT ?s ) AS ?count ) { ?s ?p ?o } GROUP BY ?p ORDER BY ?count",
"SELECT  ?p (COUNT(DISTINCT ?o ) AS ?count ) { ?s ?p ?o } GROUP BY ?p ORDER BY ?count" ];

var remoteFilePaths = ["http://nyx.unice.fr/~gaignard/data/cog-2012.ttl", "http://nyx.unice.fr/~gaignard/data/popleg-2010.ttl"];

var validDataSources = [];


// -------------------------
// GUI Controls -> functions

$('#btnStartUploads').click(function() {
	$('#btnStartUploads').button('loading');
});

$('#btnReset').click(function() {
	reset();
});

$('#btnLoad').click(function() {
	load($('#txtLoad').val());
});

$('#btnQuery').click(function() {
	sparql($('#sparqlTextArea').val());
});

$('#btnQueryFed').click(function() {
	sparqlFed($('#sparqlFedTextArea').val());
});

$('#btnDataSource').click(function() {
	addDataSource($('#txtDataSource').val());
});

$('#sparqlTextArea').val(statVOID[0]);

$('#VOIDSparql_Select').on('change', function (e) {
    var query = statVOID[$(this).val()];
    //console.log(query);
    $('#sparqlTextArea').val(query);
});

$('#DataSource_Select').on('change', function (e) {
    var endpoint = $('#DataSource_Select option:selected').html();
    $('#txtDataSource').val(endpoint);
});

$('#Data_Select').on('change', function (e) {
    // var path = $('#Data_Select option:selected').html();
    var path = remoteFilePaths[$(this).val()];
    $('#txtLoad').val(path);
});

$('#tbDataSources').on("click", "#testBtn", function(e) {
	var row = $(this).closest("tr");
	var endpoint = row.children(":first").html(); // table row ID 
	testEndpoint(endpoint, row.index());
});

$('#tbDataSources').on("click", "#delBtn", function(e) {
	var endpointUrl = $(this).closest("tr").children(":first").html(); // table row ID 
	validDataSources.remove(endpointUrl);
	console.log(validDataSources);
	$(this).parent().parent().remove();
	resetDQP();
});

// -------------------------
// functions

// http call to reset the remote knowledge graph
function reset() {
	console.log('Reset KGRAM graph');
	$.ajax({
		type: 'POST',
		url: rootURL + '/sparql/reset',
		data: {'entailments': $('#checkEntailments').prop('checked')},
		dataType: "text",
		success: function(data, textStatus, jqXHR){
			infoSuccess('Corese/KGRAM graph reset done (entailments: '+$('#checkEntailments').prop('checked')+').');
			console.log(data);
			$('#checkEntailments').prop('checked', false);
		},
		error: function(jqXHR, textStatus, errorThrown){
			infoError('Corese/KGRAM error: ' + textStatus);
			console.log(errorThrown);
		}
	});
}

// http call to reset the configuration of the federation engine
function resetDQP() {
	console.log('Reset KGRAM-DQP');
	$.ajax({
		type: 'POST',
		url: rootURL + '/dqp/reset',
		dataType: "text",
		success: function(data, textStatus, jqXHR){
			//infoSuccess('KGRAM-DQP data sources reset.');
			console.log('KGRAM-DQP data sources reset. '+data);

			configureDQP();
		},
		error: function(jqXHR, textStatus, errorThrown){
			//infoError('Can\'t reset KGRAM-DQP: ' + textStatus);
			console.log(errorThrown);
		}
	});
}

function configureDQP() {
	console.log("Configuring DQP with "+validDataSources);
	$.each(validDataSources, function(index, item){
		//jsonDataSources+='{\"endpointUrl\" : \"'+item+'\"},';
		$.ajax({
			type: 'POST',
			url: rootURL + '/dqp/configureDatasources',
			//headers: { 
        		//'Accept': 'application/json',
        	//	'Content-Type': 'application/json' 
        	//},
        	data: {'endpointUrl':item},
			//data: jsonDataSources,
			dataType: "text",
			success: function(data, textStatus, jqXHR){
				console.log(data);
			},
			error: function(jqXHR, textStatus, errorThrown){
				infoError('Corese/KGRAM error: ' + textStatus);
				console.log(errorThrown);
			}
		});
	});
	infoSuccess('Configured KGRAM-DQP with '+validDataSources+' endpoints.');
}

function load() {
	$('#btnLoad').attr("disabled", true);
	$("#btnLoad").html("Loading ...");
	console.log('Loading '+$('#txtLoad').val()+' to '+rootURL+ ' / '+$('#graphLoad').val());
	$.ajax({
		type: 'POST',
		url: rootURL + '/sparql/load',
		data: {'remote_path': $('#txtLoad').val(), 'source':$('#graphLoad').val()},
		dataType: "text",
		success: function(data, textStatus, jqXHR){
			console.log(data);
			infoSuccess("Loading done.");
			$('#btnLoad').attr("disabled", false);
			$("#btnLoad").html("Load");
		},
		error: function(jqXHR, textStatus, errorThrown){
			infoError('Corese/KGRAM error: ' + textStatus);
			console.log(errorThrown);
			console.log(jqXHR);
			$('#btnLoad').attr("disabled", false);
			$("#btnLoad").html("Load");
		}
	});
}

function sparql(sparqlQuery) {
	$('#btnQuery').attr("disabled", true);
	$("#btnQuery").html("Querying ...");
	console.log('sparql '+sparqlQuery+' to '+rootURL);
	$.ajax({
		type: 'GET',
		headers: { 
        	Accept : "application/sparql-results+json"
    	},
		url: rootURL + '/sparql',
		data: {'query':sparqlQuery},
		//dataType: "application/sparql-results+json",
		dataType: "json",
		crossDomain: true,
		success: function(data, textStatus, jqXHR){
			console.log(data);
			renderList(data);
			$('#btnQuery').attr("disabled", false);
			$("#btnQuery").html("Query");
		},
		error: function(jqXHR, textStatus, errorThrown){
			infoError("SPARQL querying failure: "+textStatus);
			console.log(errorThrown);
			console.log(jqXHR);
			$('#btnQuery').attr("disabled", false);
			$("#btnQuery").html("Query");
		}
	});
}

function sparqlFed(sparqlQuery) {
	$('#btnQueryFed').attr("disabled", true);
	$("#btnQueryFed").html("Querying ...");
	console.log('Federated sparql querying '+sparqlQuery);
	$.ajax({
		type: 'GET',
		headers: { 
        	Accept : "application/sparql-results+json"
    	},
		// url: rootURL + '/dqp/sparql',
		url: rootURL + '/dqp/sparqlprov',
		data: {'query':sparqlQuery},
		//dataType: "application/sparql-results+json",
		dataType: "json",
		crossDomain: true,
		success: function(data, textStatus, jqXHR){
			// console.log(data);
			renderListFed(data.mappings);
			renderProv(data);
			$('#btnQueryFed').attr("disabled", false);
			$("#btnQueryFed").html("Query");
		},
		error: function(jqXHR, textStatus, errorThrown){
			infoError("SPARQL querying failure: "+textStatus);
			$('#tbResFed thead tr').remove();
			$('#tbResFed tbody tr').remove();
			$('#parProvGraph svg').remove();

			console.log(errorThrown);
			console.log(jqXHR);
			$('#btnQueryFed').attr("disabled", false);
			$("#btnQueryFed").html("Query");
		}
	});
}

function addDataSource(endpointURL) {
	if (! validDataSources.contains(endpointURL)) {
		$('#tbDataSources tbody').append("<tr> <td>"+endpointURL+"</td><td><button id=\"testBtn\" class=\"btn btn-mini btn-success\" type=button>Test</button></td> <td><button id=\"delBtn\" class=\"btn btn-mini btn-danger\" type=button>Delete</button></td> <td></td> </tr>");
		testEndpoint(endpointURL,$('#tbDataSources tbody tr:last').index());
	}
}

function testEndpoint(endpointURL, rowIndex){
	console.log("Testing "+endpointURL+" endpoint !");
	var testQuery = "SELECT * where {?x ?p ?y} LIMIT 10"
	$.ajax({
		type: 'GET',
		// headers: { 
  //       	Accept : "application/sparql-results+json"
  //   	},
		url: endpointURL,
		data: {'query':testQuery},
		dataType: "json",
		// crossDomain: true,
		success: function(data, textStatus, jqXHR){
			console.log(endpointURL+" responds to SPARQL queries");
			//update the icon of the data source
			$('#tbDataSources tbody tr:eq('+rowIndex+') td:eq(3)').html('<i class=\"icon-ok\"></i>');
			//update the internal list of data sources
			if(!validDataSources.contains(endpointURL)) {
				validDataSources.push(endpointURL);
			}
			resetDQP();
		},
		error: function(jqXHR, textStatus, errorThrown){
			infoError(endpointURL+" does not responds to SPARQL queries");
			//update the icon of the data source
			//$('#tbDataSources tbody tr:eq('+rowIndex+')').append('<td><i class=\"icon-warning-sign\"></i></td>');
			$('#tbDataSources tbody tr:eq('+rowIndex+') td:eq(3)').html('<i class=\"icon-warning-sign\"></i>');
		}
	});
}

function renderList(data) {
	// JAX-RS serializes an empty list as null, and a 'collection of one' as an object (not an 'array of one')
	var listVal = data.results.bindings == null ? [] : (data.results.bindings instanceof Array ? data.results.bindings : [data.results.bindings]);
	var listVar = data.head.vars == null ? [] : (data.head.vars instanceof Array ? data.head.vars : [data.head.vars]);


	$('#tbRes thead tr').remove();
	$('#tbRes tbody tr').remove();

	//Rendering the headers
	var tableHeader = '<tr>';
	$.each(listVar, function(index, item) {
		tableHeader = tableHeader + '<th>?'+item+'</th>';
	});
	tableHeader = tableHeader +'</tr>';
	$('#tbRes thead').html(tableHeader);

	//Rendering the values
	$.each(listVal, function(index, item) {
		var row = "<tr>";
		$.each(item, function(name, v) {
    		/// do stuff
    		row = row + "<td>"+htmlEncode(v.value)+"</td>";
    		console.log(name + '=' +  htmlEncode(v.value));
  		});
		row = row + "</tr>";
		$('#tbRes tbody').prepend(row); 
	});
}

function renderProv(data) {

	var provenance = data.provenance;
	var mappings = data.mappings;

	var sMaps = JSON.stringify(mappings);

	$('#parProvGraph svg').remove();

	var width = 960, height = 500;
	var color = d3.scale.category20();

	var force = d3.layout.force()
    	.charge(-150)
    	.linkDistance(40)
    	.size([width, height]);

	var svg = d3.select('#parProvGraph').append("svg")
    	.attr("width", width)
    	.attr("height", height);

	force.nodes(provenance.nodes).links(provenance.edges).start();

  	var link = svg.selectAll(".link")
    	.data(provenance.edges)
      	.enter().append("path")
     	.attr("d", "M0,-5L10,0L0,5")
     	// .enter().append("line")
     	.attr("class", "link")
     	.style("stroke-width",function(d) {
    		if (d.label.indexOf("prov#") !== -1) {
        		return 4;
    		}
      		return 4;
      	})
       	.on("mouseout", function(d, i) {
        	d3.select(this).style("stroke", " #a0a0a0");
       	})
       	.on("mouseover", function(d, i) {
          	d3.select(this).style("stroke", " #000000");
       	});

  	link.append("title")
      	.text(function(d) { return d.label; });


	var node_drag = d3.behavior.drag()
        .on("dragstart", dragstart)
        .on("drag", dragmove)
        .on("dragend", dragend);

    function dragstart(d, i) {
            force.stop() // stops the force auto positioning before you start dragging
    }

	function dragmove(d, i) {
            d.px += d3.event.dx;
            d.py += d3.event.dy;
            d.x += d3.event.dx;
            d.y += d3.event.dy; 
            tick(); // this is the key to make it work together with updating both px,py,x,y on d !
	}

	function dragend(d, i) {
            d.fixed = true; // of course set the node to fixed so the force doesn't include the node in its auto positioning stuff
            tick();
            force.resume();
	}

  	var node = svg.selectAll("g.node")
      	.data(provenance.nodes)
      	.enter().append("g")
      	.attr("class", "node")
     	// .call(force.drag);
        .call(node_drag);

  	node.append("title")
    	.text(function(d) { return d.name; });
	
	node.append("circle")
      	.attr("class", "node")
      	.attr("r", function(d) {
    		if (d.group === 0) {
        		return 6;
    		}
      		return 12;
      	})
      	.on("dblclick", function(d) {
      		d.fixed = false;
      	})
      	.style("stroke", function(d) { return color(d.group); })
      	.style("stroke-width", 5)
      	.style("stroke-width",function(d) {
    		if (sMaps.indexOf(d.name) !== -1) {
        		return 8;
    		}
      		return 3;
      	})
      // 	.style("stroke-dasharray",function(d) {
    		// if (sMaps.indexOf(d.name) !== -1) {
      //   		return "5,5";
    		// }
      // 		return "none";
      // 	})
      	// .style("fill", "white")
      	.style("fill", function(d) { return color(d.group); })
      	// .on("mouseout", function(d, i) {
       //  	d3.select(this).style("fill", "white");
      	// })
      	// .on("mouseover", function(d, i) {
       //  	d3.select(this).style("fill", function(d) { return color(d.group); });
      	// }) ;
	
	node.append("svg:text")
				.attr("text-anchor", "middle") 
				// .attr("fill","white")
				.style("pointer-events", "none")
				.attr("font-size", "18px")
				.attr("font-weight", "200" )
				.text( function(d) { if ((sMaps.indexOf(d.name) !== -1) && (d.group !== 0)) { return d.name ;}  } ) ;
                        
    
	force.on("tick", tick); 

  	function tick() {
    	link.attr("x1", function(d) { return d.source.x  ; })
        	.attr("y1", function(d) { return d.source.y  ; })
        	.attr("x2", function(d) { return d.target.x  ; })
        	.attr("y2", function(d) { return d.target.y  ; });

    	node.attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });

    	link.attr("d", function(d) {
    		var dx = d.target.x - d.source.x,
        		dy = d.target.y - d.source.y,
        		dr = Math.sqrt(dx * dx + dy * dy);
    	
    	return "M" + d.source.x + "," + d.source.y + "A" + dr + "," + dr + " 0 0,1 " + d.target.x + "," + d.target.y;
  		});
	};
}

function renderListFed(data) {
	// JAX-RS serializes an empty list as null, and a 'collection of one' as an object (not an 'array of one')
	var listVal = data.results.bindings == null ? [] : (data.results.bindings instanceof Array ? data.results.bindings : [data.results.bindings]);
	var listVar = data.head.vars == null ? [] : (data.head.vars instanceof Array ? data.head.vars : [data.head.vars]);


	$('#tbResFed thead tr').remove();
	$('#tbResFed tbody tr').remove();

	//Rendering the headers
	var tableHeader = '<tr>';
	$.each(listVar, function(index, item) {
		tableHeader = tableHeader + '<th>?'+item+'</th>';
	});
	tableHeader = tableHeader +'</tr>';
	$('#tbResFed thead').html(tableHeader);

	//Rendering the values
	$.each(listVal, function(index, item) {
		var row = "<tr>";
		$.each(item, function(name, v) {
    		/// do stuff
    		row = row + "<td>"+v.value+"</td>";
    		//console.log(name + '=' + v.value);
  		});
		row = row + "</tr>";
		$('#tbResFed tbody').prepend(row); 
	});
}

function infoWarning(message){
	var html = "<div class=\"alert alert\"><button type=\"button\" class=\"close\" data-dismiss=\"alert\">&times;</button><strong>Warning!</strong> "+message+"</div>";
	$('#footer').prepend(html);
	alertTimeout(5000);
}
function infoSuccess(message){
	var html = "<div class=\"alert alert-success\"><button type=\"button\" class=\"close\" data-dismiss=\"alert\">&times;</button><strong>Success!</strong> "+message+"</div>";
	$('#footer').prepend(html);
	alertTimeout(5000);
}
function infoError(message){
	var html = "<div class=\"alert alert-error \"><button type=\"button\" class=\"close\" data-dismiss=\"alert\">&times;</button><strong>Error!</strong> "+message+"</div>";
	$('#footer').prepend(html);
	alertTimeout(5000);
}

// Utility functions
function htmlEncode(value){
  //create a in-memory div, set it's inner text(which jQuery automatically encodes)
  //then grab the encoded contents back out.  The div never exists on the page.
  return $('<div/>').text(value).html();
}

function htmlDecode(value){
  return $('<div/>').html(value).text();
}