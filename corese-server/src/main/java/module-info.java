module fr.inria.corese.corese_server {
    requires fr.inria.corese.sparql;
    requires fr.inria.corese.corese_core;
    requires fr.inria.corese.compiler;
    requires fr.inria.corese.shex;

    requires org.eclipse.jetty.server;
    requires org.eclipse.jetty.servlets;
    requires org.eclipse.jetty.util;
    requires org.eclipse.jetty.http;
    requires org.eclipse.jetty.websocket.servlet;

    requires jersey.server;
    requires jersey.common;
    requires jersey.media.multipart;
    requires jersey.container.jetty.http;
    requires jersey.container.servlet.core;

    requires jakarta.ws.rs;

    requires jena.arq;

    requires org.apache.logging.log4j;

    requires commons.cli;
    requires commons.vfs;
    requires org.apache.commons.io;

    requires java.logging;

    requires org.jsoup;

    requires org.slf4j;

}
