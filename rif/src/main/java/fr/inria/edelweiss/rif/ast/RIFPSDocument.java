package fr.inria.edelweiss.rif.ast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import fr.inria.edelweiss.rif.javacc.ParseException;
import fr.inria.edelweiss.rif.javacc.RIFPSParser;

public class RIFPSDocument extends RIFDocument {

	private RIFPSDocument(String doc) {
		super(doc) ;
	}

	private RIFPSDocument(File doc) {
		super(doc) ;
	}

	public static RIFPSDocument create(String doc) {
		return new RIFPSDocument(doc) ;
	}

	public static RIFPSDocument create(File doc) {
		return new RIFPSDocument(doc) ;
	}

	public String getDocumentText() {
		return this.rifDocStr ;
	}

	public void setDocumentText(String rifDocText) {
		this.rifDocStr = rifDocText ;
	}

	/** Process the lexical + syntactic analysis of a RIF-BLD document expressed in Presentation Syntax 
	 * The result (AST) is stored in {@link #payload payload} field.  */
	@Override
	public void compile() {
		if(this.rifDocStr == null || this.rifDocStr.equals("")) {
			if(this.rifDocFile == null) {
				return ; // TODO throw something
			} else {
				this.rifDocStr = this.readRIFPS(rifDocFile) ;
			}
		} 
		RIFPSParser parser = new RIFPSParser(new StringReader(this.rifDocStr)) ;
		try {
			this.payload = parser.Document() ;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private String readRIFPS(File aFile) {
		StringBuilder contents = new StringBuilder() ;		    
		try {
			BufferedReader input =  new BufferedReader(new FileReader(aFile)) ;
			try {
				String line = null ;
				while (( line = input.readLine()) != null) {
					contents.append(line) ;
					contents.append(System.getProperty("line.separator")) ;
				}
			}
			finally { input.close() ; }
		}
		catch (IOException ex) {
			ex.printStackTrace() ; // malformed file
		}		    
		return contents.toString() ;
	}

}
