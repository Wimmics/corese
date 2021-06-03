module fr.inria.corese.corese_server {
    requires fr.inria.corese.sparql;
    requires fr.inria.corese.corese_core;
    requires fr.inria.corese.kgram;
    requires fr.inria.corese.compiler;
    requires fr.inria.corese.shex;

    requires jetty.server;
    requires jetty.servlet;
    requires jetty.util;
    requires jetty.http;

    requires jersey.server;
    requires jersey.common;
    requires jersey.media.multipart;
    requires jersey.container.servlet.core;
    requires jersey.container.jetty.http;

    requires javax.servlet.api;

    requires jena.arq;

    requires org.apache.logging.log4j;

    requires java.ws.rs;

    requires commons.cli;
    requires commons.io;
    requires commons.vfs;

    requires java.logging;

    requires org.jsoup;

    requires org.slf4j;


}

