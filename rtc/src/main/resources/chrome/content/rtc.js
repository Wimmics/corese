/**
 * Javascript file for page processing for firefox plugin
 * 
 * @author Fuqi Song, wimmics INRIA - I3S
 * @date March 2014
 */

//** global variables **

var graph;
var triplesNo = 0, queryResultsNo = 0;
var graphName;
var json;

var c = {
    'TABLE_TRIPLES': 'rtc-extracted-triples',
    'TABLE_RESULTS': 'rtc-query-results',
    'TREE_CELL': 'treecell',
    'TREE_ROW': 'treerow',
    'TREE_ITEM': 'treeitem',
    'TREE_COLS': 'treecols',
    'TREE_COL': 'treecol',
    'LABEL': 'label',
    'FLEX': 'flex'
}
;

// when loading the document, extract the triples
window.addEventListener("load", function load(event) {
    window.removeEventListener("load", load, false); //remove listener, no longer needed
    myExtension.init();
}, false);

// when switching the tabs, reload the document and re-extract triples
gBrowser.tabContainer.addEventListener("TabSelect", function reload(event) {
    loadTriples(gBrowser.contentDocument);
}, false);

var myExtension = {
    init: function() {
        // The event can be DOMContentLoaded, pageshow, pagehide, load or unload.
        if (gBrowser)
            gBrowser.addEventListener("DOMContentLoaded", this.onPageLoad, false);
    },
    onPageLoad: function(aEvent) {
        var doc = aEvent.originalTarget; // doc is document that triggered the event
        loadTriples(doc);
    }
};

//show a message on the top bar
function showMsg(msg, alt) {
    $(("#rtc-info")).val('* message: ' + msg);
    if (alt)
        alert(msg);
}

//Load triples from page and display in tables
function loadTriples(doc) {
    graphName = doc.location.href;

    //** 2. RDFa parser
    GreenTurtle.attach(doc);
    graph = doc.data.graph;
    var subjs = graph.subjects;
    triplesNo = graph.tripleCount;

    // if triples deteced, then show icon in url bar
    $('#url-bar-start').attr('hidden', triplesNo === 0);

    //** 1. initialize
    clear();
    showMsg(triplesNo + ' triples found on "' + doc.title + '": [' + doc.location.href + ']', false);

    //** 3. variables
    var table = document.getElementById('rtc-extracted-triples-rows');
    //var table = $('#'+c.TABLE_TRIPLES+' treechildren');
    var i = 0;

    //** 4 read s, p, o and add to table
    for (var s in subjs) {
        //***2 subject
        var snode = subjs[s];
        for (var p in snode.predicates) {
            //****3 predicate
            var pnode = snode.predicates[p];
            for (var o in pnode.objects) {
                //*** object
                var onode = pnode.objects[o];
                i++;

                //create row/cell
                var item = document.createElement(c.TREE_ITEM);
                var row = document.createElement(c.TREE_ROW);

                var rid = c.TABLE_TRIPLES + '-' + i;
                row.setAttribute('id', rid);

                addCell(row, c.TREE_CELL, c.LABEL, i);//id
                addCell(row, c.TREE_CELL, c.LABEL, snode.id);//subject
                addCell(row, c.TREE_CELL, c.LABEL, pnode.id);//predicate

                var lang = onode.language === null || typeof onode.language === "undefined" ? '' : '@' + onode.language;
                addCell(row, c.TREE_CELL, c.LABEL, onode.value + lang);//object

                item.appendChild(row);
                table.appendChild(item);
            }
        }
    }
}
;

//add one cell to a row in a table
function addCell(parent, name, attr, value, flex) {
    var cell = document.createElement(name);
    if (attr !== null) {
        cell.setAttribute(attr, value);
    }

    if (flex) {
        cell.setAttribute('flex', flex);
    }
    parent.appendChild(cell);
}
;

//Display the details of one triples
function showDetails(table) {
    var view = table.view;
    var row = view.getItemAtIndex(view.selection.currentIndex);
    var rs = row.getElementsByTagName(c.TREE_CELL);
    var detail = '';
    $("#" + table.getAttribute('id') + ' ' + c.TREE_COL).each(function(index, item) {
        detail += item.getAttribute(c.LABEL) + ":\t" + rs[index].getAttribute(c.LABEL) + "\n";
    });

    $("#rtc-triple-detail").val(detail);
}
;

//Clear tables and some text fields when loading pages
function clear() {
    $('treeitem').remove();
    $("#rtc-triple-detail").val('');
    $("#rtc-info").val('');
}
;

//download triples into a file on disk in format .ttl
//action = 1, download extracted triples in turtle
//action = 2, download query results in json
function download(action) {
    var filter, textToDownload, defaultName;
    //*0 check
    if (action === 1) {
        if (triplesNo === 0) {
            showMsg('No triples to download!', true);
            return;
        } else {
            filter = "*.ttl";
            defaultName = "triples" + filter;
            textToDownload = graph.toString();
        }
    }

    if (action === 2) {
        if (queryResultsNo === 0)
        {
            showMsg('No query results to download!', true);
            return;
        } else {
            filter = ".json";
            defaultName = "query" + filter;
            textToDownload = json;
        }
    }
    //*1 open file choose dialog
    var nsIFilePicker = Components.interfaces.nsIFilePicker;
    var fp = Components.classes["@mozilla.org/filepicker;1"].createInstance(nsIFilePicker);
    fp.init(window, "Select a File", nsIFilePicker.modeSave);
    fp.appendFilter(filter, filter);
    fp.defaultString = defaultName;

    var res = fp.show();
    if (res !== nsIFilePicker.returnCancel) {
        //*2 initialize the file
        var filePath = fp.file.path;
        var file = Components.classes["@mozilla.org/file/local;1"].createInstance(Components.interfaces.nsILocalFile);

        file.initWithPath(filePath);
        if (file.exists() === false) {
            file.create(Components.interfaces.nsIFile.NORMAL_FILE_TYPE, 420);
        }

        //*3 initialize the stream
        var foStream = Components.classes["@mozilla.org/network/file-output-stream;1"]
                .createInstance(Components.interfaces.nsIFileOutputStream);

        foStream.init(file, 0x02 | 0x08 | 0x20, 0666, 0);
        var converter = Components.classes["@mozilla.org/intl/converter-output-stream;1"].createInstance(Components.interfaces.nsIConverterOutputStream);
        converter.init(foStream, "UTF-8", 0, 0);
        converter.writeString(textToDownload);
        converter.close();

        //*4 alert
        showMsg('File saved to ' + filePath, true);
    }
}
;
var insert;
function upload() {
    if (triplesNo === 0) {
        showMsg('no triples to upload', true);
        return;
    }

    //** process url
    var url = $("#rtc-server-address").val();

    if ($.trim(url).length < 1) {
        showMsg('Please input the server address!', true);
    }
    //graph.toString return the triples in turtle format
    var sGraph = graph.toString();

    if ($('#radio-ldp').is(':selected')) {
        url += "/ldp/upload";
        sGraph = graph.toString({}, true);
    } else {
        url += "/sparql/update";
    }
    insert = 'INSERT DATA { GRAPH <' + graphName + '> {' + sGraph + ' } }';

    $.ajax({
        type: 'POST',
        headers: {
            Accept: "application/x-www-form-urlencoded"
        },
        url: url,
        data: {'update': insert},
        // dataType: "json",
        crossDomain: true,
        success: function() {
            var msg = '* ' + triplesNo + ' triples uploaded successfully! [' + url + ']';
            showMsg(msg, true);
        },
        error: function(jqXHR, textStatus, errorThrown) {
            showMsg("Upload triples failure: " + textStatus, true);
            console.log(errorThrown);
            console.log(jqXHR);
        }
    });
}
;

//Execute sparql statement and return results
function query() {
    //** 1 url process
    var url = $("#rtc-server-address").val() + "/sparql";

    if ($.trim(url).length < 1) {
        showMsg('Please input the server address!', true);
    }

    //** 2 process spqral
    var sparql = $.trim($("#rtc-sparql").val());
    sparql = filter(sparql);
    if (sparql === 0) {
        return;
    }
    $('#' + c.TABLE_RESULTS + ' treeitem').remove();

    //** 3 send request and receive response
    $.ajax({
        type: 'GET',
        headers: {
            Accept: "application/sparql-results+json"
        },
        url: url,
        data: {'query': sparql},
        dataType: "json",
        crossDomain: true,
        success: function(data) {
            fillList(data);
            json = JSON.stringify(data);
        },
        error: function(jqXHR, textStatus, errorThrown) {
            showMsg("SPARQL querying failure: " + textStatus, true);
            console.log(errorThrown);
            console.log(jqXHR);
        }
    });
}
;

//fill the table using return data
function fillList(data) {
    // JAX-RS serializes an empty list as null, and a 'collection of one' as an object (not an 'array of one')
    var listVal = data.results.bindings == null ? [] : (data.results.bindings instanceof Array ? data.results.bindings : [data.results.bindings]);
    var listVar = data.head.vars == null ? [] : (data.head.vars instanceof Array ? data.head.vars : [data.head.vars]);
    queryResultsNo = data.results.bindings.length;

//** tree sturcture**
// tree
//   -treecols
//     -treecol
//   -treechildern
//     -treeitem
//       -treerow
//         -treecell
    if (queryResultsNo > 0) {
        $('#' + c.TABLE_RESULTS).empty();
        //**** 1 process header of table *****
        //Rendering the headers
        var head = document.createElement(c.TREE_COLS);
        //first column
        var first = document.createElement(c.TREE_COL);
        first.setAttribute(c.LABEL, '#');
        first.setAttribute('width', '30px');
        head.appendChild(first);

        $.each(listVar, function(index, item) {
            addCell(head, c.TREE_COL, c.LABEL, item, 1);
            addCell(head, 'splitter', 'class', 'tree-splitter');
        });

        //add header and cols to table
        $('#' + c.TABLE_RESULTS).append(head);
        var treechildren = document.createElement('treechildren');
        //Rendering the values
        //**** 2 process data of table *****
        $.each(listVal, function(index, item) {
            var treeItem = document.createElement(c.TREE_ITEM);
            var row = document.createElement(c.TREE_ROW);
            row.setAttribute('id', c.TABLE_RESULTS + '-' + (index + 1));

            addCell(row, c.TREE_CELL, c.LABEL, index + 1);

            for (var i = 0; i < listVar.length; i++) {
                var v = listVar[i];
                if (item.hasOwnProperty(v)) {
                    addCell(row, c.TREE_CELL, c.LABEL, item[v].value);
                } else {
                    addCell(row, c.TREE_CELL, null, null);
                }
            }
            treeItem.appendChild(row);
            treechildren.appendChild(treeItem);
        });

        $('#' + c.TABLE_RESULTS).append(treechildren);
    }

    showMsg(queryResultsNo + ' result(s) queried from server.', false);
}
;

// chgeck and re-write sparql statement
function filter(sparql) {
    if (sparql.length < 1) {
        showMsg('Please input sparql statement!', true);
        return 0;
    }

    var index = sparql.toLowerCase().indexOf('from');
    if (index !== -1) {
        showMsg("Please don't specify the <from> clause!", true);
        return 0;
    } else {
        if ($('#radio-onthispage').is(':selected')) {

            var from = '\nFROM <' + graphName + '> \n';
            var wi = sparql.indexOf('where');
            if (wi !== -1) {
                return sparql.slice(0, wi) + from + sparql.slice(wi, sparql.length);
            } else {
                showMsg('Please check the sparql statement, there is no <where> clause', true);
                return 0;
            }
        } else {
            return sparql;
        }
    }
}
;

//Hide panel of extension
//state:true, hide; state:false, show
function hideExt(state) {
    $('#toolbar-id').attr('hidden', state);
    $('#main-content-id').attr('hidden', state);
}
;