package fr.inria.acacia.corese.gui.core;


import java.io.StringWriter;

public class CaptureOutput extends StringWriter {

     CaptureOutput() {
         super();
         
     }
       public String getContent() {
        String content = this.toString();
        this.getBuffer().setLength(0);
        return content; //requested by Virginie ;-)
      }

  }
