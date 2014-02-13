package fr.inria.edelweiss.rif.xml;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import fr.inria.edelweiss.rif.ast.Group;
import fr.inria.edelweiss.rif.ast.RIFDocument;
import fr.inria.edelweiss.rif.xml.schema.Document;


public class RIFXMLParser {

	private static final String JAXB_CONTEXT_PACKAGE = "fr.inria.edelweiss.rif.xml.schema" ;
	
	/** Source XML file to be parsed */
	private File xmlFile ;
	
	/** JAXB entity to unwrap XML tree to a concrete syntax tree */
	private Unmarshaller unmarshaller ;
	
	/** The entity class in which the AST will be stored. It also includes several informations
	 * related to the whole RIF document, like imported namespaces, etc. */
	private RIFDocument rifdoc ;
	
	public void setRIFDocument(RIFDocument rifdoc) {
		this.rifdoc = rifdoc ;
	}
	
	public RIFXMLParser(File f) {
		this.xmlFile = f ;
		try {
			JAXBContext jbc = JAXBContext.newInstance(JAXB_CONTEXT_PACKAGE) ;
			this.unmarshaller = jbc.createUnmarshaller() ;
		} catch (JAXBException e) {
			// TODO Should do some environment-specific handling
			e.printStackTrace();
		}
	}
	
	public Group xml2AST() {
		try {
			Document doc = (Document) unmarshaller.unmarshal(xmlFile) ;
			Group g = doc.XML2AST(rifdoc) ;
			return g ;
		} catch (JAXBException e) {
			// TODO Should do some environment-specific handling
			e.printStackTrace();
		}
		return null ;
	}
	
	
}
