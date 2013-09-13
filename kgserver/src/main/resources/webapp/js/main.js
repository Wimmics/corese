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
	console.log('Loading '+$('#txtLoad').val()+' to '+rootURL);
	$.ajax({
		type: 'POST',
		url: rootURL + '/sparql/load',
		//data: JSON.stringify({'remote_path': $('#txtLoad').val()}),
		data: {'remote_path': $('#txtLoad').val()},
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
		success: function(data, textStatus, jqXHR){
			//console.log(data);
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
		url: rootURL + '/dqp/sparql',
		data: {'query':sparqlQuery},
		//dataType: "application/sparql-results+json",
		dataType: "json",
		success: function(data, textStatus, jqXHR){
			//console.log(data);
			renderListFed(data);
			$('#btnQueryFed').attr("disabled", false);
			$("#btnQueryFed").html("Query");
		},
		error: function(jqXHR, textStatus, errorThrown){
			infoError("SPARQL querying failure: "+textStatus);
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
		headers: { 
        	Accept : "application/sparql-results+json"
    	},
		url: endpointURL,
		data: {'query':testQuery},
		dataType: "json",
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
    		row = row + "<td>"+v.value+"</td>";
    		//console.log(name + '=' + v.value);
  		});
		row = row + "</tr>";
		$('#tbRes tbody').prepend(row); 
	});
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