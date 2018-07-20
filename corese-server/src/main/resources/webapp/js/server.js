var content = "#contentOfSite";
var changeURL = true;
var srv = "/srv";

//Get template by sending ajax request 'GET or 'POST' according to parameter type
function trans(obj) {
    if (typeof obj === 'string') {
        transGET(obj);
    } else if (typeof obj === 'object') {
        transPOST(obj);
    } else {
        return false;
    }
}

//get generated html from server by sending ajax 'GET' request
function transGET(url) {
    url = url.replace('#', '%23');//encode '#'
    $.ajax({
        type: 'GET',
        url: url,
        crossDomain: true,
        success: function (response) {
            success(response, url);
        },
        error: function (response, status, error) {
            error(response, error);
        }
    });
}

//get generated html from server by sending ajax 'POST' request
function transPOST(form) {
    var fd = new FormData(form);
    if (form.id === '') {
        alert('Please give a unique name to the form.');
        return false;
    }
    var url = $('#' + form.id).attr('action');//get relative path
    $.ajax({
        type: 'POST',
        url: url,
        data: fd,
        enctype: "multipart/form-data",
        processData: false,
        contentType: false,
        crossDomain: true,
        success: function (response) {
            success(response, url);
        },
        error: function (response, status, err) {
            error(response, err, url);
        }
    });
}

//return 500 error
function error(response, err, url) {
    var text = '<div class="container"><h2>' + err + ' (error code: ' + response.status + ')</h2><br>' + response.responseText + '</div>';
    $(content).html(text);
    updateUrl(url);
}

//when ajax returns '200 ok', display the response text on the page
function success(response, url) {
    const text = '<div class="container">' + response + '</div>';
    const range = document.createRange();
    const contentNode = document.getElementById("contentOfSite");
    range.selectNode(contentNode);
    var documentFragment = range.createContextualFragment(text);
    contentNode.innerHTML = "";
    contentNode.appendChild(documentFragment);
    updateUrl(url); //2 change the url displayed in broswer url bar
}

// store the browsering history and change the url in the browser url bar
function updateUrl(url) {
    if (changeURL && url.trim() !== '') {
        url = location.protocol + "//" + location.host + "/srv" + url;
        window.history.pushState('', '', url);
    }
    changeURL = true;
}

//initialize the page content
window.onload = function () {
    loadContent();
};

//go back/forward, reload page
window.onpopstate = function (event) {
    changeURL = false;//when go back/forward, don't change history manutually
    loadContent();
};

//loading content of page whe refreshing or reloading page, load all contents
function loadContent() {
    //0. load header and footer of page
    $('#navgator').load("/html/navigator.html");
    $('#footerOfSite').load("/html/footer.html");

    //1 load home page content
    if (location.pathname === '/demo_new.html' || location.pathname.trim() === '/') {
        $(content).load("/html/content.html");
        changeURL = true;
        return;
    }

    //2 load template from server
    var url = location.pathname + location.search;
    var index = url.indexOf(srv);
    if (index === -1) {
        alert("The url does not contain '/srv' service.");
        return;
    }
    url = url.substr(index + srv.length);
    trans(url);
}

//call function 'callback' when ajax request is completed
//function 'callback' is a javascript function that is defined in template,
//which need to be executed after ajax request completes
$(document).ajaxComplete(function (event, request, settings) {
    //if the funciton 'callback' is defined, otherwise do nothing
    if (typeof callback === 'function') {
        callback();
    }
});
