package fr.inria.corese.rif.ast;

import java.io.File;

import fr.inria.corese.rif.xml.RIFXMLParser;

public class RIFXMLDocument extends RIFDocument {

	private RIFXMLDocument(File doc) {
		super(doc) ;
	}

	public static RIFXMLDocument create(File doc) {
		return new RIFXMLDocument(doc) ;
	}

	@Override
	/** Run JAXB unmarshaller to import the RIF-XML document into a syntactic tree, from which we produce an AST, 
	 * stored in the {@link #payload payload} field. */
	public void compile() {
		if(this.rifDocFile == null || !this.rifDocFile.exists())
			return ; // throw something
		else {
			RIFXMLParser parser = new RIFXMLParser(this.rifDocFile) ;
			parser.setRIFDocument(this) ;
			this.payload = parser.xml2AST() ;
		}
	}

}
