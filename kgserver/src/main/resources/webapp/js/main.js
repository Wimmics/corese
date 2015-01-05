/** 
 * Controler code for the Corese/KGRAM web application
 *
 * author : alban.gaignard@cnrs.fr
 */

// Useful functions for array handling
Array.prototype.contains = function(a) {
    return this.indexOf(a) != -1
};
Array.prototype.remove = function(a) {
    if (this.contains(a)) {
        return this.splice(this.indexOf(a), 1)
    }
};

$.support.cors = true;

function alertTimeout(wait) {
    setTimeout(function() {
        $('#footer').children('.alert:last-child').remove();
    }, wait);
}

// The root URL for the RESTful services
//var rootURL = "http://"+window.location.host+"/kgram";
var rootURL = "http://"+window.location.host;
console.log("Connected to the Corese/KGRAM endpoint "+rootURL);

// reset the connected data sources so that when the page is reloaded, the gui is synchronized with the server. 
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
    "SELECT  ?p (COUNT(DISTINCT ?o ) AS ?count ) { ?s ?p ?o } GROUP BY ?p ORDER BY ?count"];

var fedQueries = [
    'PREFIX idemo:<http://rdf.insee.fr/def/demo#> \n\
PREFIX igeo:<http://rdf.insee.fr/def/geo#> \n\
SELECT ?nom ?popTotale WHERE { \n\
    ?region igeo:codeRegion "24" .\n\
    ?region igeo:subdivisionDirecte ?departement .\n\
    ?departement igeo:nom ?nom .\n\
    ?departement idemo:population ?popLeg .\n\
    ?popLeg idemo:populationTotale ?popTotale .\n\
} ORDER BY ?popTotale',
    'PREFIX semehr: <http://www.mnemotix.com/ontology/semEHR#> \n\
SELECT ?codePostal (count(*) as ?total) WHERE { \n\
    ?cv semehr:value "BHGSA5B0"^^xsd:string . \n\
    ?patient semehr:hasMedicalBag ?bag .\n\
    ?bag semehr:hasMedicalEvent ?evt .\n\
    ?evt semehr:hasClinicalVariable ?cv . \n\
    ?patient semehr:address ?addr .\n\
    ?addr semehr:postalCode ?codePostal . \n\
} GROUP BY ?codePostal ORDER BY desc(?total)',
    'PREFIX semehr: <http://www.mnemotix.com/ontology/semEHR#> \n\
SELECT ?codePostal (count(*) as ?total) WHERE { \n\
    ?cv semehr:value "BHGSA5B0"^^xsd:string . \n\
    ?patient semehr:hasMedicalBag/semehr:hasMedicalEvent/semehr:hasClinicalVariable ?cv . \n\
    ?patient semehr:address/semehr:postalCode ?codePostal . \n\
} GROUP BY ?codePostal ORDER BY desc(?total)',
    'PREFIX semehr: <http://www.mnemotix.com/ontology/semEHR#> \n\
PREFIX dbpedia-owl: <http://dbpedia.org/ontology/> \n\
SELECT (count(distinct ?patient) as ?nbPatients)  (sum(?pop) as ?totalPop) ?postalCode (count(distinct ?patient)*10000/sum(?pop) as ?occurencePer10k) WHERE { \n\
    ?cv semehr:value "BHGSA5B0"^^xsd:string . \n\
    ?patient semehr:hasMedicalBag/semehr:hasMedicalEvent/semehr:hasClinicalVariable ?cv . \n\
    ?patient semehr:address/semehr:postalCode ?postalCode . \n\
\n\
    SERVICE <http://fr.dbpedia.org/sparql> { \n\
        SELECT DISTINCT (str(?cp) as ?postalCode) ?pop WHERE { \n\
            ?s dbpedia-owl:region <http://fr.dbpedia.org/resource/Auvergne> .\n\
            ?s dbpedia-owl:postalCode ?cp . \n\
            ?s dbpedia-owl:populationTotal ?pop \n\
        } \n\
    } \n\
} GROUP BY  ?postalCode ORDER BY desc(?occurencePer10k)'
];


var remoteFilePaths = [
    "http://nyx.unice.fr/~gaignard/data/cog-2012.ttl",
    "http://nyx.unice.fr/~gaignard/data/popleg-2010.ttl",
    "http://nyx.unice.fr/~gaignard/data/creatis-ginseng-all.ttl",
    "http://nyx.unice.fr/~gaignard/data/i3s-ginseng-all.ttl",
    "http://nyx.unice.fr/~gaignard/data/in2p3-ginseng-all.ttl"
];

var validDataSources = [];


// -------------------------
// GUI Controls -> functions

// init GUI components
$('#sparqlTextArea').val(statVOID[0]);
$('#sparqlFedTextArea').val(fedQueries[0]);
$('#txtSlice').attr("disabled", true);


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

$('#VOIDSparql_Select').on('change', function(e) {
    var query = statVOID[$(this).val()];
    //console.log(query);
    $('#sparqlTextArea').val(query);
});

$('#checkTPGrouping').on('change', function(e) {
    resetDQP();
    if ($('#checkTPGrouping').prop('checked')) {
        $('#txtSlice').attr("disabled", false);
    } else {
        $('#txtSlice').attr("disabled", true);
    }
});

$('#txtSlice').on('change', function(e) {
    resetDQP();
});

$('#FedSparql_Select').on('change', function(e) {
    var query = fedQueries[$(this).val()];
    //console.log(query);
    $('#sparqlFedTextArea').val(query);
});

$('#DataSource_Select').on('change', function(e) {
    var endpoint = $('#DataSource_Select option:selected').html();
    $('#txtDataSource').val(endpoint);
});

$('#Data_Select').on('change', function(e) {
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

//$(document)
//    .on('change', '.btn-file :file', function() {
//        var input = $(this),
//            numFiles = input.get(0).files ? input.get(0).files.length : 1,
//            label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
//        input.trigger('fileselect', [numFiles, label]);
//});
//
//$(document).ready( function() {
//    $('.btn-file :file').on('fileselect', function(event, numFiles, label) {
//        console.log(numFiles);
//        console.log(label);
//    });
//});
//
//$('#btnUpload').click(function(){
//    var formData = new FormData($('form')[0]);
//    console.log(formData);
//    $.ajax({
//        url: rootURL + '/sparql/upload',  //Server script to process data
//        type: 'POST',
//        xhr: function() {  // Custom XMLHttpRequest
//            var myXhr = $.ajaxSettings.xhr();
//            if(myXhr.upload){ // Check if upload property exists
//                myXhr.upload.addEventListener('progress',progressHandlingFunction, false); // For handling the progress of the upload
//            }
//            return myXhr;
//        },
//        //Ajax events
//        beforeSend: beforeSendHandler,
//        success: completeHandler,
//        error: errorHandler,
//        // Form data
//        data: formData,
//        //Options to tell jQuery not to process data or worry about content-type.
//        cache: false,
//        contentType: false,
//        processData: false
//    });
//});


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
        success: function(data, textStatus, jqXHR) {
            infoSuccess('Corese/KGRAM graph reset done (entailments: ' + $('#checkEntailments').prop('checked') + ').');
            console.log(data);
            $('#checkEntailments').prop('checked', false);
        },
        error: function(jqXHR, textStatus, errorThrown) {
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
        success: function(data, textStatus, jqXHR) {
            //infoSuccess('KGRAM-DQP data sources reset.');
            console.log('KGRAM-DQP data sources reset. ' + data);

            configureDQP();
        },
        error: function(jqXHR, textStatus, errorThrown) {
            //infoError('Can\'t reset KGRAM-DQP: ' + textStatus);
            console.log(errorThrown);
        }
    });
}

function configureDQP() {
    console.log("Configuring DQP with " + validDataSources);
    $.each(validDataSources, function(index, item) {
        //jsonDataSources+='{\"endpointUrl\" : \"'+item+'\"},';
        $.ajax({
            type: 'POST',
            url: rootURL + '/dqp/configureDatasources',
            //headers: { 
            //'Accept': 'application/json',
            //	'Content-Type': 'application/json' 
            //},
            data: {'endpointUrl': item},
            //data: jsonDataSources,
            dataType: "text",
            success: function(data, textStatus, jqXHR) {
                console.log(data);
            },
            error: function(jqXHR, textStatus, errorThrown) {
                infoError('Corese/KGRAM error: ' + textStatus);
                console.log(errorThrown);
            }
        });
    });
//	infoSuccess('Configured KGRAM-DQP with '+validDataSources+' endpoints.');
}

function load() {
    $('#btnLoad').attr("disabled", true);
    $("#btnLoad").html("Loading ...");
    console.log('Loading ' + $('#txtLoad').val() + ' to ' + rootURL + ' / ' + $('#graphLoad').val());
    $.ajax({
        type: 'POST',
        url: rootURL + '/sparql/load',
        data: {'remote_path': $('#txtLoad').val(), 'source': $('#graphLoad').val()},
        dataType: "text",
        success: function(data, textStatus, jqXHR) {
            console.log(data);
            infoSuccess("Loading done.");
            $('#btnLoad').attr("disabled", false);
            $("#btnLoad").html("Load");
        },
        error: function(jqXHR, textStatus, errorThrown) {
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
    isConstruct = (sparqlQuery.toLowerCase().indexOf("construct") >= 0) || (sparqlQuery.toLowerCase().indexOf("describe") >= 0);

    if (isConstruct) {
        endpointURL = rootURL + '/sparql/d3';
    } else {
        endpointURL = rootURL + '/sparql';
    }
    console.log('sparql ' + sparqlQuery + ' to ' + endpointURL);

    $.ajax({
        type: 'GET',
        headers: {
            Accept: "application/sparql-results+json"
        },
        url: endpointURL,
        data: {'query': sparqlQuery},
        //dataType: "application/sparql-results+json",
        dataType: "json",
        crossDomain: true,
        success: function(data, textStatus, jqXHR) {
//                    console.log(data);  
            $('#parRDFGraph svg').remove();
            if (!isConstruct) {
                renderList(data);
            } else {
//                        renderList(data.mappings);
                $('#tbRes thead tr').remove();
                $('#tbRes tbody tr').remove();
                renderD3(data, "#parRDFGraph");
            }

            $('#btnQuery').attr("disabled", false);
            $("#btnQuery").html("Query");
        },
        error: function(jqXHR, textStatus, errorThrown) {
            infoError("SPARQL querying failure: " + textStatus);
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
    $('#tbAdvanced tbody').html("");
    $('#tbResFed thead').html("");
    $('#tbResFed tbody').html("");
    $('#parProvGraph svg').remove();

    console.log('Federated sparql querying ' + sparqlQuery);
    fedURL = '';
    if ($('#checkProv').prop('checked')) {
        fedURL = rootURL + '/dqp/sparqlprov';
    } else {
        fedURL = rootURL + '/dqp/sparql';
    }

    if ($('#checkAdvanced').prop('checked')) {
        pollCost();
    } else {
        $('#parAdvanced').html("");
    }

    var boolTPgrouping = $('#checkTPGrouping').prop('checked');
    if (boolTPgrouping) {
        console.log("Triple pattern grouping enabled");
    } else {
        console.log("Triple pattern grouping disabled");
    }


    $.ajax({
        type: 'GET',
        headers: {
            Accept: "application/sparql-results+json"
        },
        // url: rootURL + '/dqp/sparql',
        url: fedURL,
        data: {'query': sparqlQuery, 'tpgrouping': boolTPgrouping, 'slicing': $('#txtSlice').val()},
        //dataType: "application/sparql-results+json",
        dataType: "json",
        crossDomain: true,
        success: function(data, textStatus, jqXHR) {
//                        console.log(data)
            if ($('#checkProv').prop('checked')) {
                renderListFed(data.mappings);
                renderD3(data, "#parProvGraph");
            } else {
                renderListFed(data);
            }

            $('#btnQueryFed').attr("disabled", false);
            $("#btnQueryFed").html("Query");
        },
        error: function(jqXHR, textStatus, errorThrown) {
            infoError("SPARQL querying failure: " + textStatus);
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

function pollCost() {
    $.ajax({
        url: rootURL + '/dqp/getCost',
        type: 'GET',
        dataType: 'json',
        success: function(data) {
            // depending on the data, either call setTimeout or simply don't
//            renderCostOneTab(data);
            renderCostMultiTab(data);
            if ($('#btnQueryFed').is(":disabled")) {
                setTimeout(pollCost, 500);
            }
        },
        error: function(jqXHR, textStatus, errorThrown) {
            console.log(textStatus);
            console.log(jqXHR.responseText);
            console.log(errorThrown);
            infoError(rootURL + '/dqp/getCost' + " does not monitor DQP cost");
        }
    });
}


function addDataSource(endpointURL) {
    if (!validDataSources.contains(endpointURL)) {
        $('#tbDataSources tbody').append("<tr> \n\
                    <td>" + endpointURL + "</td>\n\
                    <td align=right >\n\
                        <button id=\"testBtn\" class=\"btn btn-xs btn-success\" type=button>Test</button> \n\
                        <button id=\"delBtn\" class=\"btn btn-xs btn-danger\" type=button>Delete</button></td> \n\
                    </tr>");
        testEndpoint(endpointURL, $('#tbDataSources tbody tr:last').index());
    }
}

function testEndpoint(endpointURL, rowIndex) {
    console.log("Testing " + endpointURL + " endpoint !");
    $.ajax({
        type: 'POST',
        url: rootURL + '/dqp/testDatasources',
        data: {'endpointUrl': endpointURL},
        dataType: 'json',
        success: function(data, textStatus, jqXHR) {
            console.log(data);
            if (data.test === true) {
                console.log(endpointURL + " responds to SPARQL queries");

                //update the icon of the data source
                $('#tbDataSources tbody tr:eq(' + rowIndex + ') td:eq(1)').html('<button id=\"testBtn\" class=\"btn btn-xs btn-success\" type=button>Test</button> \n\
                            <button id=\"delBtn\" class=\"btn btn-xs btn-danger\" type=button>Delete</button>\n\
                            <i class=\"glyphicon glyphicon-ok\"></i>');
                //update the internal list of data sources
                if (!validDataSources.contains(endpointURL)) {
                    validDataSources.push(endpointURL);
                }
                resetDQP();
            } else {
                console.log(endpointURL + " does NOT respond to SPARQL queries");
            }
        },
        error: function(jqXHR, textStatus, errorThrown) {
            console.log(jqXHR);
            console.log(errorThrown);
            infoError(endpointURL + " does not responds to SPARQL queries");
            //update the icon of the data source
            //$('#tbDataSources tbody tr:eq('+rowIndex+')').append('<td><i class=\"icon-warning-sign\"></i></td>');
            $('#tbDataSources tbody tr:eq(' + rowIndex + ') td:eq(1)').html('<button id=\"testBtn\" class=\"btn btn-xs btn-success\" type=button>Test</button> \n\
                            <button id=\"delBtn\" class=\"btn btn-xs btn-danger\" type=button>Delete</button>\n\
                            <i class=\"glyphicon glyphicon-warning-sign\"></i></td>');
        }
    });
}

function renderList(data) {

    // JAX-RS serializes an empty list as null, and a 'collection of one' as an object (not an 'array of one')
    var listVal = data.results.bindings == null ? [] : (data.results.bindings instanceof Array ? data.results.bindings : [data.results.bindings]);
    var listVar = data.head.vars == null ? [] : (data.head.vars instanceof Array ? data.head.vars : [data.head.vars]);

    $('#tbRes thead tr').remove();
    $('#tbRes tbody tr').remove();

    if (data.results.bindings.length > 0) {
        //Rendering the headers
        var tableHeader = '<tr>';
        $.each(listVar, function(index, item) {
            tableHeader = tableHeader + '<th>?' + item + '</th>';
        });
        tableHeader = tableHeader + '</tr>';
        $('#tbRes thead').html(tableHeader);

        //Rendering the values
        $.each(listVal, function(index, item) {
            var row = "<tr>";

            for (var i = 0; i < listVar.length; i++) {
                var v = listVar[i];
                if (item.hasOwnProperty(v)) {
                    row = row + "<td>" + htmlEncode(item[v].value) + "</td>";
                } else {
                    row = row + "<td></td>";
                }
            }

            row = row + "</tr>";
//                $('#tbRes tbody').prepend(row);
            $('#tbRes tbody').append(row);
        });
    }
}

function renderCostMultiTab(data) {
    $('#parAdvanced').html("");

    var table = "<table id=\"tbAdvanced\" class=\"table table-striped\"> \n <tbody>  ";

    var totalQReq = data.totalQueryReq;
    var totalQRes = data.totalQueryRes;
    var totalSrcReq = data.totalSourceReq;
    var totalSrcRes = data.totalSourceRes;

    var listQCost = data.queryCost;
    // number of requests per subqueries
    
    
    table = table + "<caption><strong>Requests per subquery</strong></caption> \n";
    $.each(listQCost, function(index, item) {
        console.log(listQCost[index].query);
        console.log(listQCost[index].nbReq);
        console.log(listQCost[index].nbRes);
        var query = listQCost[index].query;
        var v = Math.round(100 * (listQCost[index].nbReq) / totalQReq);
        var p = '<div class="progress"> \n\
                <div class="progress-bar mypopover" role="progressbar" aria-valuenow="' + v + '" aria-valuemin="0" aria-valuemax="' + 100 + '" style="width: ' + v + '%;"> \n\
               <span>' + v + '% of total requests</span> \n\
               </div> \n\
                </div>';
        table = table + "<tr> \n\
                    <td>" + htmlEncode(query) + "</td>\n\
                    <td align=left >\n\
                    " + p + "\n\
                </td></tr> \n";
        //$('#parAdvanced').append(p);
        //console.log(query+' : '+v+'% of total requests');
    });
    table = table + "</tbody></table>";
    $('#parAdvanced').append(table);
    $('#parAdvanced').append("<br>");

    table = "<table id=\"tbAdvanced\" class=\"table table-striped\"> \n <tbody>  ";
    // number of results per subqueries
    table = table + "<caption><strong>Results per subquery</strong></caption> \n";
    $.each(listQCost, function(index, item) {
        //console.log(listQCost[index].query);
        //console.log(listQCost[index].nbReq);
        //console.log(listQCost[index].nbRes);
        var query = listQCost[index].query;
        var v = Math.round(100 * (listQCost[index].nbRes) / totalQRes);
        var p = '<div class="progress"> \n\
                <div class="progress-bar progress-bar-success" role="progressbar" aria-valuenow="' + v + '" aria-valuemin="0" aria-valuemax="' + 100 + '" style="width: ' + v + '%;"> \n\
                <span>' + v + '% of total results</span> \n\
                </div> \n\
            </div>';
        table = table + "<tr> \n\
                    <td>" + htmlEncode(query) + "</td>\n\
                    <td align=left >\n\
                    " + p + "\n\
                </td></tr> \n";
        //$('#parAdvanced').append(p);
        //console.log(query+' : '+v+'% of total results');
    });
     table = table + "</tbody></table>";
    $('#parAdvanced').append(table);
    $('#parAdvanced').append("<br>");
    // $('#parAdvanced').append("<br>");



    var listSrcCost = data.sourceCost;
    table = "<table id=\"tbAdvanced\" class=\"table table-striped\"> \n <tbody>  ";
    // number of requests per source
    table = table + "<caption><strong>Requests per source</strong></caption> \n";
    $.each(listSrcCost, function(index, item) {
        //console.log(listQCost[index].query);
        //console.log(listQCost[index].nbReq);
        //console.log(listQCost[index].nbRes);
        var source = listSrcCost[index].source;
        var v = Math.round(100 * (listSrcCost[index].nbReq) / totalSrcReq);
        var p = '<div class="progress"> \n\
                <div class="progress-bar" role="progressbar" aria-valuenow="' + v + '" aria-valuemin="0" aria-valuemax="' + 100 + '" style="width: ' + v + '%;"> \n\
               <span>' + v + '% of total requests</span> \n\
               </div> \n\
            </div>';
        table = table + "<tr> \n\
                    <td>" + htmlEncode(source) + "</td>\n\
                    <td align=left >\n\
                    " + p + "\n\
                </td></tr> \n";
        //$('#parAdvanced').append(p);
        //console.log(source+' : '+v+'% of total requests');
    });
    table = table + "</tbody></table>";
    $('#parAdvanced').append(table);
    $('#parAdvanced').append("<br>");

    // number of results per source
    table = "<table id=\"tbAdvanced\" class=\"table table-striped\"> \n <tbody>  ";
    table = table + "<caption><strong>Results per source</strong></caption> \n";
    $.each(listSrcCost, function(index, item) {
        //console.log(listQCost[index].query);
        //console.log(listQCost[index].nbReq);
        //console.log(listQCost[index].nbRes);
        var source = listSrcCost[index].source;
        var v = Math.round(100 * (listSrcCost[index].nbRes) / totalSrcRes);
        var p = '<div class="progress"> \n\
                <div class="progress-bar progress-bar-success" role="progressbar" aria-valuenow="' + v + '" aria-valuemin="0" aria-valuemax="' + 100 + '" style="width: ' + v + '%;"> \n\
                <span>' + v + '% of total results</span> \n\
                </div> \n\
            </div>';
        table = table + "<tr> \n\
                    <td>" + htmlEncode(source) + "</td>\n\
                    <td align=left >\n\
                    " + p + "\n\
                </td></tr> \n";
        //$('#parAdvanced').append(p);
        //console.log(source+' : '+v+'% of total results');
    });
    table = table + "</tbody></table>";
    $('#parAdvanced').append(table);
    $('#parAdvanced').append("<br>");
//    $('#tbAdvanced').append(table);
}

function renderD3(data, htmlCompId) {
    var d3Data = data.d3;
    var mappings = data.mappings;
    var sMaps = JSON.stringify(mappings);

    var width = $(htmlCompId).parent().width();
//        var height = $("svg").parent().height();
    var height = 400;
    var color = d3.scale.category20();

    var force = d3.layout.force()
            .charge(-200)
            .linkDistance(50)
//        .friction(.8)
            .size([width, height]);

    var svg = d3.select(htmlCompId).append("svg")
//    	.attr("width", width)
//    	.attr("height", height)
            .attr("viewBox", "0 0 600 400")
            .attr("width", "100%")
            .attr("height", 400)
            .attr("preserveAspectRatio", "xMidYMid")
            .style("background-color", "#F4F2F5");

    force.nodes(d3Data.nodes).links(d3Data.edges).start();

    var link = svg.selectAll(".link")
            .data(d3Data.edges)
            .enter().append("path")
            .attr("d", "M0,-5L10,0L0,5")
            // .enter().append("line")
            .attr("class", "link")
            .style("stroke-width", function(d) {
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
            .text(function(d) {
                return d.label;
            });


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
            .data(d3Data.nodes)
            .enter().append("g")
            .attr("class", "node")
            // .call(force.drag);
            .call(node_drag);

    node.append("title")
            .text(function(d) {
                return d.name;
            });

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
            .on("mouseover", fade(.1)).on("mouseout", fade(1))
            .style("stroke", function(d) {
                return color(d.group);
            })
            .style("stroke-width", 5)
            .style("stroke-width", function(d) {
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
            .style("fill", function(d) {
                return color(d.group);
            });
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
            .attr("font-weight", "200")
            .text(function(d) {
                if ((sMaps.indexOf(d.name) !== -1) && (d.group !== 0)) {
                    return d.name;
                }
            });


    var linkedByIndex = {};
    d3Data.edges.forEach(function(d) {
        linkedByIndex[d.source.index + "," + d.target.index] = 1;
    });

    function isConnected(a, b) {
        return linkedByIndex[a.index + "," + b.index] || linkedByIndex[b.index + "," + a.index] || a.index === b.index;
    }

    force.on("tick", tick);

    function tick() {
        link.attr("x1", function(d) {
            return d.source.x;
        })
                .attr("y1", function(d) {
                    return d.source.y;
                })
                .attr("x2", function(d) {
                    return d.target.x;
                })
                .attr("y2", function(d) {
                    return d.target.y;
                });

        node.attr("transform", function(d) {
            return "translate(" + d.x + "," + d.y + ")";
        });

        link.attr("d", function(d) {
            var dx = d.target.x - d.source.x,
                    dy = d.target.y - d.source.y,
                    dr = Math.sqrt(dx * dx + dy * dy);

            return "M" + d.source.x + "," + d.source.y + "A" + dr + "," + dr + " 0 0,1 " + d.target.x + "," + d.target.y;
        });
    }
    ;

    function fade(opacity) {
        return function(d) {
            node.style("stroke-opacity", function(o) {
                thisOpacity = isConnected(d, o) ? 1 : opacity;
                this.setAttribute('fill-opacity', thisOpacity);
                return thisOpacity;
            });

            link.style("stroke-opacity", function(o) {
                return o.source === d || o.target === d ? 1 : opacity;
            });
        };
    }
}

function renderListFed(data) {
    // JAX-RS serializes an empty list as null, and a 'collection of one' as an object (not an 'array of one')
    var listVal = data.results.bindings == null ? [] : (data.results.bindings instanceof Array ? data.results.bindings : [data.results.bindings]);
    var listVar = data.head.vars == null ? [] : (data.head.vars instanceof Array ? data.head.vars : [data.head.vars]);

//	$('#tbResFed thead tr').remove();
//	$('#tbResFed tbody tr').remove();

    //Rendering the headers
    var tableHeader = '<tr>';
    $.each(listVar, function(index, item) {
        tableHeader = tableHeader + '<th>?' + item + '</th>';
    });
    tableHeader = tableHeader + '</tr>';
    $('#tbResFed thead').html(tableHeader);

    //Rendering the values
    $.each(listVal, function(index, item) {
        var row = "<tr>";

        for (var i = 0; i < listVar.length; i++) {
            var v = listVar[i];
            if (item.hasOwnProperty(v)) {
                row = row + "<td>" + htmlEncode(item[v].value) + "</td>";
            } else {
                row = row + "<td></td>";
            }
        }

        row = row + "</tr>";
        $('#tbResFed tbody').append(row);
    });
}

function infoWarning(message) {
    var html = "<div class=\"alert alert\"><button type=\"button\" class=\"close\" data-dismiss=\"alert\">&times;</button><strong>Warning!</strong> " + message + "</div>";
    $('#footer').prepend(html);
    alertTimeout(5000);
}
function infoSuccess(message) {
    var html = "<div class=\"alert alert-success\"><button type=\"button\" class=\"close\" data-dismiss=\"alert\">&times;</button><strong>Success!</strong> " + message + "</div>";
    $('#footer').prepend(html);
    alertTimeout(5000);
}
function infoError(message) {
    var html = "<div class=\"alert alert-error \"><button type=\"button\" class=\"close\" data-dismiss=\"alert\">&times;</button><strong>Error!</strong> " + message + "</div>";
    $('#footer').prepend(html);
    alertTimeout(5000);
}

// Utility functions
function htmlEncode(value) {
    //create a in-memory div, set it's inner text(which jQuery automatically encodes)
    //then grab the encoded contents back out.  The div never exists on the page.
    return $('<div/>').text(value).html();
}

function htmlDecode(value) {
    return $('<div/>').html(value).text();
}
