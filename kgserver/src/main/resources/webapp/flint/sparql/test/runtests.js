function runParserTests(mode,tests) {
    var node= document.getElementById("output");
    var passes=0;
    var fails=0;
    var state;
    var trace='';
    var callback= function (string,st) {state=st;};

    var html='<table border="1">';
    for (var i=0; i<tests.length; ++i) {
	var pass= runTest(tests[i].query,
			  tests[i].expected);
	var report="<td>"+tests[i].name+"</td>";


	if (tests[i].expected)
	    report+="<td>+</td>";
	else
	    report+="<td>-</td>";
    
	if (pass) {
	    report+='<td bgcolor="#30FF30">Passed</td>';
	    ++passes;
	} else {
	    report+='<td bgcolor="#FF3030">Failed</td>';
	    ++fails;
	}
	report+="<td>"+tests[i].comment+"</td>";
	html+="<tr>"+report+"</tr>";
    }
    html+="</table>";
    html=passes+" passed<br/>"+fails+" failed"+html;
    
    node.innerHTML=html;

    function runTest(query,expected) {
	CodeMirror.runMode( query,
			    mode,
			    callback
			  );
	var stack=state.stack, len=state.stack.length;
	var queryValid=true;

	trace+=state.complete;
	for (var i=0; i<stack.length; i++) {
	  trace+= i+": "+stack[i]+"\n";
	}
	if (state.OK==false)
	  queryValid=false;
	else 
	  queryValid= state.complete;

       	return (queryValid==expected);
	//return (state.OK==expected);
    }
}

