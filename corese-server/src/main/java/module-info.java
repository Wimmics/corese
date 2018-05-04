module fr.inria.corese.corese_server {
    requires fr.inria.corese.corese_core;
    requires jersey.media.multipart;
    requires jersey.common;
    requires java.ws.rs;
    requires org.apache.logging.log4j;
    requires javax.servlet.api;
    requires commons.cli;
    requires commons.io;
    requires commons.vfs;
    requires jetty.http;
    requires jetty.server;
    requires jetty.servlet;
    requires jetty.util;
    requires jersey.container.jetty.http;
    requires jersey.server;
    requires jersey.container.servlet.core;
    requires java.logging;
}