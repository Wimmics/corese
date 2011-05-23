package fr.inria.acacia.corese.gui.core;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.LayoutStyle;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.miv.graphstream.algorithm.layout2.elasticbox.ElasticBox;
import org.miv.graphstream.graph.Node;
import org.miv.graphstream.graph.implementations.MultiGraph;
import org.miv.graphstream.ui.swing.SwingGraphRenderer;
import org.miv.graphstream.ui.swing.SwingGraphViewer;

import com.ibm.icu.util.StringTokenizer;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.api.IEngine;
import fr.inria.acacia.corese.api.IResult;
import fr.inria.acacia.corese.api.IResultValue;
import fr.inria.acacia.corese.api.IResults;
//import fr.inria.acacia.corese.cg.CoreseGraph;
//import fr.inria.acacia.corese.cg.CoreseRelation;
//import fr.inria.acacia.corese.cg.Result;
//import fr.inria.acacia.corese.cst.RDF;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.exceptions.QueryLexicalException;
import fr.inria.acacia.corese.exceptions.QuerySemanticException;
import fr.inria.acacia.corese.exceptions.QuerySyntaxException;
import fr.inria.acacia.corese.gui.query.Exec;
import fr.inria.edelweiss.kgengine.QueryResults;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgraph.core.Graph;
/**
 * Onglet Query avec tout ce qu'il contient
 * @author saguilel
 * @author Maraninchi jerôme
 */

public class MyJPanelQuery extends JPanel implements Runnable, ActionListener, DocumentListener, FocusListener, CaretListener{
	
	private static final long serialVersionUID = 1L;

	//Boutton du panneau Query
	private JButton buttonRun,buttonTKgram,buttonProve;
	private JButton buttonSearch;
	private JButton buttonRefreshStyle,buttonDefaultStyle;
	
	//panneau de la newQuery	
	private JPanel paneQuery;			
	
	//Ajoute le scroll pour les différents panneaux
	private JScrollPane scrollPaneQuery;
	private JScrollPane scrollPaneTreeResult;		
	private JScrollPane scrollPaneXMLResult;	
	private JScrollPane scrollPaneValidation;
	
	//Conteneur d'onglets de résultats et les onglets
	private JTabbedPane tabbedPaneResults;
	private JTextArea textAreaXMLResult;
	private JTextPane textPaneValidation;
	private JTextPane textPaneStyleGraph;
	private JTree treeResult;
	
	//Le panel d'édition de query
	private JTextPane textPaneQuery;
	private JTextArea textAreaLines;
	
	//Pour le graphe
	private MultiGraph graph;
	private boolean excepCatch = false; 
	private JTextArea textAreaLinesGraph;

	
	//pour la fonction Search coloration syntaxique
	private int temp;
	private int temp3;
	private boolean isColoring = false;       
	
	//pour la fonction highlight
	private Object start,end;
	private Highlighter highlighter;

	private String stylesheet;

	private CharSequence resultXML = "";	
		
	public MyJPanelQuery(final MainFrame coreseFrame){
		super();
		//On définit les couleurs pour le highlight des "{"
		
		paneQuery = new JPanel(new BorderLayout());
		setLayout(new BorderLayout(5,5));
		add(paneQuery);
		
	    scrollPaneQuery = new JScrollPane();
	    textPaneQuery = new JTextPane();
	    buttonRun = new JButton();
	    buttonProve = new JButton();
	    buttonTKgram = new JButton();
	    buttonSearch = new JButton();
	    buttonRefreshStyle = new JButton();
	    buttonDefaultStyle = new JButton();
	    tabbedPaneResults = new JTabbedPane();
	    scrollPaneTreeResult = new JScrollPane();
	    scrollPaneXMLResult = new JScrollPane();
	    textAreaXMLResult = new JTextArea();
	    scrollPaneValidation = new JScrollPane();
	    textPaneValidation = new JTextPane();	
	    textPaneStyleGraph = new JTextPane();   
	    
	    
	    // Saisie des requêtes	    
	    textPaneQuery.setFont(new Font("Sanserif", Font.PLAIN, 13));
	    textPaneQuery.setPreferredSize(new Dimension(400,250));
	    //si on crée une nouvelle requête on met le texte de base, si on charge une requête on met le texte qui est dans le fichier .txt
	    textPaneQuery.setText(coreseFrame.getTextQuery());
	    StyledDocument doc = textPaneQuery.getStyledDocument();
	    MutableAttributeSet attr= new SimpleAttributeSet();
	    StyleConstants.setLineSpacing(attr, 0);
	    doc.setParagraphAttributes(0, doc.getLength(), attr, false);
	    textPaneQuery.setMargin(new Insets(1,1,1,1));
	 
		//compteur de ligne pour la feuille de style de graphe
		textAreaLinesGraph = new JTextArea();
		textAreaLinesGraph.setFont(new Font("Sanserif", Font.PLAIN,12));
		textAreaLinesGraph.setEditable(false);
		textAreaLinesGraph.setFocusable(false);
		textAreaLinesGraph.setBackground(new Color (230,230,230));
		textAreaLinesGraph.setForeground(Color.black);
		textAreaLinesGraph.setAutoscrolls(true);
		
		//compteur de ligne pour la query
	    textAreaLines = new JTextArea(10,2);
	    textAreaLines.setFont(new Font("Sanserif", Font.PLAIN, 13));
	    textAreaLines.setEditable(false);
	    textAreaLines.setFocusable(false);
	    textAreaLines.setBackground(new Color (230,230,230));
	    textAreaLines.setForeground(Color.black);
	    textAreaLines.setAutoscrolls(true);
	    textAreaLines.add(textPaneQuery);
	    textAreaLines.setMargin(new Insets(3,1,1,1));
	    
	    scrollPaneQuery.setRowHeaderView(textAreaLines);
	    textPaneQuery.addFocusListener(this);
	    textPaneQuery.getDocument().addDocumentListener(this);	    
	    textPaneQuery.addCaretListener(this);
	    
	    //Bouton refreshStyleGraph + listener a l'écoute afin de recharger la feuille de style ou de renvoyer une exception
	    buttonRefreshStyle.setText("Refresh graph stylesheet");
	    buttonDefaultStyle.setText("Default stylesheet");
	    buttonRefreshStyle.setEnabled(false);
	    buttonDefaultStyle.setEnabled(false);

	    stylesheet = coreseFrame.getDefaultStylesheet();
	    
	   /**
	    * ActionListener sur le Bouton refresh Stylesheet
	    * Refresh stylesheet du Graphe 
	    * Permet de mettre a jour le style du Graphe ou d'afficher une exception sous forme de PopUp lors d'un problème syntaxique
	    */
	    ActionListener refreshListener = new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	        	String style = textPaneStyleGraph.getText();
	        	JTextArea areaException = new JTextArea();
	        	org.miv.graphstream.ui.graphicGraph.stylesheet.StyleSheet sh = new org.miv.graphstream.ui.graphicGraph.stylesheet.StyleSheet();
	        	try {
					sh.parseFromString(style);
					areaException.setText("");
					excepCatch=false;
				} catch (Exception e1) {
					 areaException.setText(e1.getMessage());
					 areaException.setEditable(false);
					 areaException.setForeground(Color.red);
					 JOptionPane.showMessageDialog(null, areaException,"Error Syntax",JOptionPane.WARNING_MESSAGE);
					 excepCatch=true;
				}
				catch(org.miv.graphstream.ui.graphicGraph.stylesheet.parser.TokenMgrError e1){
					 areaException.setText(e1.getMessage());
					 areaException.setEditable(false);
					 areaException.setForeground(Color.red);
					 JOptionPane.showMessageDialog(null, areaException,"Error Syntax",JOptionPane.WARNING_MESSAGE);
					 excepCatch=true;
				}
				if(!excepCatch){
					graph.addAttribute( "ui.stylesheet", style );	
					textPaneStyleGraph.setText(style); 
					stylesheet =style;
				}	

	        }
	    };        
		buttonRefreshStyle.addActionListener(refreshListener);
		
		/**
		 * ActionListener sur le bouton DefaultStylesheet
		 * Permet d'attribuer un style par défaut défini au niveau de la MainFrame au graphe		 
		 */
		ActionListener defaultListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stylesheet = coreseFrame.getDefaultStylesheet();
				textPaneStyleGraph.setText(stylesheet);
				graph.addAttribute( "ui.stylesheet", stylesheet);
			}
		};
		buttonDefaultStyle.addActionListener(defaultListener);
		
	    /**
	     * appel de la fonction de coloration syntaxique et de comptage de ligne pour l'initialisation
	     */
		doColoringLater();
		checkLines(textPaneQuery,textAreaLines);
		checkLines(textPaneStyleGraph,textAreaLinesGraph);

	    /**
	     *  Ajoute les fonctions UNDO, REDO 
	     **/
	
		
		final CompoundUndoManager undoManager = new CompoundUndoManager( textPaneQuery );		 
		
		
		coreseFrame.getUndo().addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					undoManager.undo();
					textPaneQuery.requestFocus();
				}
				catch (CannotUndoException ex)
				{
			        Toolkit.getDefaultToolkit().beep();
				}
			}
		});
		
	
		coreseFrame.getRedo().addActionListener( new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					undoManager.redo();
					textPaneQuery.requestFocus();
				}
				catch (CannotRedoException ex)
				{
			        Toolkit.getDefaultToolkit().beep();
				}
			}
		});
 
		
 		
	    scrollPaneQuery.setViewportView(textPaneQuery);
	    
	    
	    /**
	     * DocumentListener sur le textPaneStyleGraph
	     * Listener sur le textPaneStyleGraph afin d'actualiser le nombre de lignes
	     */
	    DocumentListener l_paneGraphListener = new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				checkLines(textPaneStyleGraph,textAreaLinesGraph);				
			}		
			@Override
			public void insertUpdate(DocumentEvent e) {
				checkLines(textPaneStyleGraph,textAreaLinesGraph);				
			}	
			@Override
			public void changedUpdate(DocumentEvent e) {
				checkLines(textPaneStyleGraph,textAreaLinesGraph);			
			}
		};
		Document doc3 = textPaneStyleGraph.getDocument();
		doc3.addDocumentListener(l_paneGraphListener);
		
		/**
		 * FocusListener sur le textPaneStyleGraph
		 * Permet d'actualiser le compteur de ligne lors d'un FocusGained et FocusLost
		 */
		FocusListener l_paneGraphFocus = new FocusListener() {		
			@Override
			public void focusLost(FocusEvent e) {
				checkLines(textPaneStyleGraph,textAreaLinesGraph);				
			}			
			@Override
			public void focusGained(FocusEvent e) {
				checkLines(textPaneStyleGraph,textAreaLinesGraph);			
			}
		};    
	    textPaneStyleGraph.addFocusListener(l_paneGraphFocus);
	   
	    
	    /** Bouttons et leurs actions **/	
	    //Lancer une requête
	    buttonRun.setText("Query");
	    ActionListener l_RunListener = createListener(coreseFrame, false);
	    buttonRun.addActionListener(l_RunListener);
	    
	    buttonProve.setText("Prove");
	    buttonProve.addActionListener(l_RunListener);

	
	    buttonTKgram.setText("Trace");
	    ActionListener kt_RunListener = createListener(coreseFrame, true);
	    buttonTKgram.addActionListener(kt_RunListener);
	    buttonTKgram.addActionListener(this);

	
	    //Pour chercher un string dans la fen�tre de r�sultat XML
	    buttonSearch.setText("Search");
	    
	
	      ActionListener searchListener = new ActionListener() {
		        public void actionPerformed(ActionEvent e) {
		        	textAreaXMLResult.setText(resultXML.toString());
		        	String toSearch = "";
		      	  	String message = "";
		      	  	int start=0,stop=0;
		      	  	CharSequence temps =textAreaXMLResult.getText();	
		      		ArrayList<Integer>  tabStart =  new ArrayList<Integer>();
		      		ArrayList<Integer>  tabStop =  new ArrayList<Integer>();
				
		      	  	toSearch = JOptionPane.showInputDialog("Search", message);
		      	  	boolean b = false;
		      	  	if(toSearch != null){
	      	  			b = temps.toString().contains(toSearch);	  				
		      	  		for(int i=temp;i<temps.length();i++){	
			      	  		//fichier temporaire qui tronque le fichier result a partir du String=toSearch afin de trouver les prochains String
			      	  		b = temps.toString().contains(toSearch);
			      	  		if(b ==true){
			      	  			start=temps.toString().indexOf(toSearch);		//stocke dans le tableau afin de le surligner plus tard
			      	  			tabStart.add(start+temp);
			      	  			assert(start > -1);
			      	  			stop = start + toSearch.length();
			      	  			tabStop.add(stop+temp);
			      	  			//permet de faire la relation entre le fichier temporaire et le fichier de base (JtextArea)
			      	  			//maintien à jour les chiffres stop et start faussé par le subSequence 
			      	  			temp+=stop;
			      	  			temps = (temps.subSequence(stop, temps.length()));
			      	  		}		
			      	  	else break;

		      	  		}
			      	  	if(tabStart.size()>0){
		      	  			textAreaXMLResult.setCaretPosition(tabStart.get(0));
			      	  	}
			      	  	//remise a zèro
			      	  	temps =textAreaXMLResult.getText();
			      	  	stop=0;start=0;temp=0;
			      	  				
				      	try {
				      	  	for(int i=0;i<tabStart.size();i++){			      	  					
				      	  		textAreaXMLResult.getHighlighter().addHighlight(tabStart.get(i), tabStop.get(i), DefaultHighlighter.DefaultPainter);
				      	  	} 
						} catch (BadLocationException e1) {
								e1.printStackTrace();
						}    						      	  
						if(tabStart.size()==0 && !toSearch.equals("")) JOptionPane.showMessageDialog( getPaneQuery(),toSearch + " n'apparait pas dans le résultat","info", JOptionPane.INFORMATION_MESSAGE);
						if(toSearch.equals("")) {
							JOptionPane.showMessageDialog( getPaneQuery(),"Veuillez entrer une chaine de caractere","info", JOptionPane.INFORMATION_MESSAGE);
							JOptionPane.showInputDialog("Search", message);
						}
						
		      	  	}
		      	  	else {		      	  
						JOptionPane.showMessageDialog( getPaneQuery(),"Veuillez entrer une chaine de caracteres","info", JOptionPane.INFORMATION_MESSAGE);
		      	  	}
		        }
		        
		      };
	    buttonSearch.addActionListener(searchListener);
	
	
	    // Résultat sous forme d'arbre     
	    tabbedPaneResults.addTab("Graph", scrollPaneTreeResult);
	    
	    // Résultat sous forme XML 
	    textAreaXMLResult.setEditable(false);	    
	    textAreaXMLResult.setText(resultXML.toString());
	    scrollPaneXMLResult.setViewportView(textAreaXMLResult);
	    tabbedPaneResults.addTab("XML", scrollPaneXMLResult);
	    
	    // Messages de la validation 
	    textPaneValidation.setEditable(false);
	    textPaneValidation.setText("");
	    scrollPaneValidation.setViewportView(textPaneValidation);
	
	    tabbedPaneResults.addTab("Validate", scrollPaneValidation);
	
	    // Mise en forme 

		final JSplitPane jp = new JSplitPane(JSplitPane.VERTICAL_SPLIT	, scrollPaneQuery, tabbedPaneResults);
		jp.setContinuousLayout(true);

	    GroupLayout pane_listenerLayout = new GroupLayout(paneQuery);
	    paneQuery.setLayout(pane_listenerLayout);
         
        GroupLayout.ParallelGroup hParallel1 = pane_listenerLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.SequentialGroup hSeq1 = pane_listenerLayout.createSequentialGroup();
        GroupLayout.ParallelGroup hParallel2 = pane_listenerLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.SequentialGroup hSeq2 = pane_listenerLayout.createSequentialGroup();

        hSeq2.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 257, Short.MAX_VALUE);
        hSeq2.addComponent(buttonRun);
        hSeq2.addComponent(buttonProve);
        hSeq2.addComponent(buttonTKgram);
        hSeq2.addComponent(buttonSearch);
        hSeq2.addGap(30, 30, 30);
        hSeq2.addComponent(buttonRefreshStyle);
        hSeq2.addComponent(buttonDefaultStyle);
        hParallel2.addGroup(hSeq2);
        hParallel2.addComponent(jp, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        hSeq1.addContainerGap();
        hSeq1.addGroup(hParallel2);
        hParallel1.addGroup(hSeq1);
        
        pane_listenerLayout.setHorizontalGroup(hParallel1);
       
        
        GroupLayout.ParallelGroup vParallel1 = pane_listenerLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.ParallelGroup vParallel2 = pane_listenerLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.SequentialGroup vSeq1 = pane_listenerLayout.createSequentialGroup();

        vParallel2.addComponent(buttonTKgram);
        vParallel2.addComponent(buttonRun);
        vParallel2.addComponent(buttonProve);

        vParallel2.addComponent(buttonSearch);
        vParallel2.addComponent(buttonRefreshStyle);
        vParallel2.addComponent(buttonDefaultStyle);
        vSeq1.addContainerGap();
        vSeq1.addGroup(vParallel2);
        vSeq1.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        vSeq1.addComponent(jp);
        vSeq1.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        vSeq1.addContainerGap();
        vParallel1.addGroup(vSeq1);
        
        pane_listenerLayout.setVerticalGroup(vParallel1);

	}
	

	

	public void search(JTextPane comp){
	  	  			
			/**
			 * Initialisation des variables
			 */
			int startQuote=0,startComm=0,start=0,stop=0;
	  	  	CharSequence content=null;
	  	  	Document d = textPaneQuery.getDocument();
	  	  	boolean b_word=false, b_wordUpper=false;
	  	  	boolean b_quote=false;
	  	  	boolean b_function=false, b_functionUpper=false;

	  	  	try {
	  	  		content = d.getText(0, d.getLength());
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	  	  
			/**
			 * Les différents ArrayList nous permettant de stocker les indexs des mots à colorier et également les mots qu'il faut rechercher
			 */
					
	  	  	ArrayList<Integer> tabStartWord =  new ArrayList<Integer>();
	  	  	ArrayList<Integer> tabStopWord =  new ArrayList<Integer>();
	  	  	
	  		ArrayList<Integer> tabStartComm =  new ArrayList<Integer>();
	  	  	ArrayList<Integer> tabStopComm =  new ArrayList<Integer>();
	  	  	
	  	  	ArrayList<Integer> tabStartQuote=  new ArrayList<Integer>();
	  	  	ArrayList<Integer> tabStopQuote =  new ArrayList<Integer>();
	  	  	
	  		ArrayList<Integer> tabStartFunction=  new ArrayList<Integer>();
	  	  	ArrayList<Integer> tabStopFunction =  new ArrayList<Integer>();
	  	  	
	  	  	ArrayList<String> listWords =  new ArrayList<String>();
	  	  	ArrayList<String> listFunctions =  new ArrayList<String>();

	  	  	/**
	  	  	 * Remplit les deux ArraList
	  	  	 */
	  	
		   	listWords.add("select");	
		  	listWords.add("where");	 	
		  	listWords.add("optional");	
		  	listWords.add("filter");	
		  	listWords.add("prefix");	
		  	listWords.add("construct");
		  	listWords.add("describe");	
		  	listWords.add("union");		
		  	  	
		  	listWords.add("base");		  	  	
		  	listWords.add("ask");		  	
		  	listWords.add("order by");	
		  	listWords.add("group by");
		  	listWords.add("limit");			  	
		  	listWords.add("offset");	 	
		  	listWords.add("distinct");	 	  	
		  	listWords.add("reduced");	 	  
		  	listWords.add("from");		  	  	
		  	listWords.add("from named");	  
		  	listWords.add("graph");		
		  	listWords.add("not");	
		  	listWords.add("exists");	
	  	  	
		  	listFunctions.add("regex");	
		  	listFunctions.add("bound");	
		  	listFunctions.add("isIRI");	
		  	listFunctions.add("isBlank");	
		  	listFunctions.add("isLiteral");
		  	listFunctions.add("str");	
		  	listFunctions.add("lang");	
		  	listFunctions.add("logical-or");	
		  	listFunctions.add("logical-and");	
		  	listFunctions.add("RDFterm-equal");	
		  	listFunctions.add("sameTerm");
		  	listFunctions.add("langMatches");	

		  	

	  	  /**
	  	   * On définit les différentes polices à utilisées 	
	  	   */
		  	StyleContext sc = new StyleContext();
		  	Style style = sc.addStyle("style", null);
		  	style.addAttribute(StyleConstants.Foreground, new Color(255,102,102));

		  	Style normal = sc.addStyle("normal", null);
		  	normal.addAttribute(StyleConstants.Foreground, new Color(0,0,0));

		 	Style comm = sc.addStyle("comm", null);
		  	comm.addAttribute(StyleConstants.Foreground, new Color(0,153,0));
	  	  	comm.addAttribute(StyleConstants.CharacterConstants.Italic, Boolean.TRUE);
	  	  
	  	  	Style quote = sc.addStyle("quote", null);
	  	  	quote.addAttribute(StyleConstants.Foreground, new Color(51,51,255));
	  	  
	  	  	Style function = sc.addStyle("function", null);
	  	  	function.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.TRUE);

	  	  	

	      
	  	  	/**
	  	  	 * Pour la recherche des words
	  	  	 */
	  	  	for(int j=0;j<listWords.size();j++){
	  	  		for(int i=temp;i<d.getLength();i++){	
		  			//fichier temporaire qui tronque le fichier result a partir du String=toSearch afin de trouver les prochains String
	  	  			b_word = content.toString().contains(listWords.get(j));
	  	  			b_wordUpper = content.toString().contains(listWords.get(j).toUpperCase());
		  			if(b_word ==true || b_wordUpper ==true){
		  				if(b_word==true){
			  				start=content.toString().indexOf(listWords.get(j));		//stocke dans le tableau afin de le surligner plus tard
			  				assert(start > -1);
			  				stop = start + listWords.get(j).length();
		  				}
		  				else {
		  					start=content.toString().indexOf(listWords.get(j).toUpperCase());	//	Cherche le mot en majuscule
			  				assert(start > -1);
			  				stop = start + listWords.get(j).toUpperCase().length();
		  				}
		  				char temp2;
		  				char temp3;
		  				if(start==0 && stop==content.length()){
		  					temp2=' ';
		  					temp3=' ';
		  				}
		  				else if(start==0){							//le cas du début pas besoin d'espace avant le mot
			   				temp2=' ';
			  	  			temp3 = content.charAt(stop);				  	  				
		  	  			}
			  	  		else if(stop==content.length()){		//le cas de la fin pas besoin d'espace après le mot		
			  	  			temp2 = content.charAt(start-1);
		  	  				temp3= ' ';
		  	  			}
		  	  			else{											//le cas général
			  	  			temp2 = content.charAt(start-1);		
			  	  			temp3 = content.charAt(stop);
		  	  			}		
			  	  							
				  		if((temp2 == ' ' || temp2 == '\n'|| temp2=='}') && (temp3== ' ' || temp3== '\n' || temp3=='{')){		//on vérifie alors qu'on est bien entre deux blancs ou entre un saut à la ligne
				  			tabStartWord.add(start+temp);
				  			tabStopWord.add(stop+temp);
			  			}
				      	   						      								      		  
		  	  			//permet de faire la relation entre le fichier temporaire et le fichier de base (JtextArea)
		  	  			//maintien à jour les chiffres stop et start faussé par le subSequence 
		  	  			temp+=stop;
		  	  			content = (content.subSequence(stop, content.length()));
		  	  			
		  	  		}		  	  				  	  			

	  	  		}
	  	  		try {
					content =d.getText(0, d.getLength());
					stop=0;start=0;temp=0;	  	
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
					
		  	}
	 	  	/**
	 	  	 * Pour la recherche des # (les commentaires)
	 	  	 */	 		
		 	  	for(int j=0;j<getTextAreaLines().getLineCount()-1;j++){
		 	  		int lineStartOffset = 0;
					try {
						lineStartOffset = MainFrame.getLineStartOffset(getTextPaneQuery(), j);
		  				if(lineStartOffset == d.getLength()){
		  					break;
		  				}
		  				if (content.toString().charAt(lineStartOffset) == '#'){
		  					startComm = lineStartOffset;
			  	  			tabStartComm.add(startComm);							//on ajoute dans notre arraylist l'index de début
		  					while(content.charAt(startComm)!='\n' && startComm<(d.getLength()-1)){				//on recherche le saut de ligne afin de déterminer l'index du caractère final 		  	  				
		  						startComm++;	  	  				
		  					}
		  					if(j==getTextAreaLines().getLineCount()-2){			//utilisation du if pour colorier différement selon la fin du commentaire (saut de ligne ou fin de requête)
			  					tabStopComm.add(startComm+1);
		  					}
		  					else {
			  					tabStopComm.add(startComm);
		  					}
		  						  				
			  				
		  				}
		  				
					} catch (BadLocationException e) {
						// TODO Auto-gnerated catch block
						e.printStackTrace();
					}
		 	  	}
	 	  	
			try {
				content =d.getText(0, d.getLength());

			} catch (BadLocationException e) {
				e.printStackTrace();
			}			
				
			/**
			 * Pour la recherche des " (chaines de caractères)
			 */
			boolean isComm=false;
  	  		int pair=1;  													//variable pour déterminer si "ouvrant ou "fermant
			for(int i=temp3;i<d.getLength();i++){							//permet de mettre d'un certaine couleur les charactères entre "" 
				startQuote=0;
				isComm=false;
				b_quote = content.toString().contains("\"");
  	  			if(b_quote==true){
  	  				startQuote = content.toString().indexOf("\""); 
  	  				for(int j=0;j<tabStartComm.size();j++){			//si le guillemet se situe dans un commentaire => met a jour le temp a la fin du comm et on ne rempli pas le tableau
  	  					if(startQuote<=(tabStopComm.get(j)-temp3) && startQuote>=(tabStartComm.get(j)-temp3)){
  	  						isComm=true;
		  	  				temp3+=startQuote+1;
  	  					}
  	  				}	
  	  				if(!isComm){
	  	  				if(pair>=0 && pair%2==1){											//si guillement ouvrant on range l'index du caractère courant dans l'arraylist d'index de début
	  	  					tabStartQuote.add(startQuote+temp3);
		  	  			}
		  	  			else {													//sinon on range l'index du caractère  dans l'arraylist d'index de fin 
		  	  				temp3+=2;											//on incrémente de 2 pour colorier les guillemets également
		  	  				tabStopQuote.add(startQuote+temp3);	
			  	  			}	 	  				
	  	  				pair++;	
	  	  				temp3+=startQuote;
  	  				}	
  	  			content = (content.subSequence(startQuote+1, content.length()));	//decoupe la chaine pour trouver le prochain guillemet
  	  			}

			}	
				try {
					content =d.getText(0, d.getLength());
					temp3=0;
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			
			/**
			 * Pour la recherche des fonctions
			 */
				for(int j=0;j<listFunctions.size();j++){
		  	  		for(int i=temp;i<d.getLength();i++){	
			  			//fichier temporaire qui tronque le fichier result a partir du String=toSearch afin de trouver les prochains String
		  	  			b_function = content.toString().contains(listFunctions.get(j));
		  	  			b_functionUpper = content.toString().contains(listFunctions.get(j).toUpperCase());
			  			if(b_function ==true || b_functionUpper ==true){
			  				if(b_function){
				  				start=content.toString().indexOf(listFunctions.get(j));		//stocke dans le tableau afin de le surligner plus tard
				  				assert(start > -1);
				  				stop = start + listFunctions.get(j).length();
			  				}
			  				else{
			  					start=content.toString().indexOf(listFunctions.get(j).toUpperCase());		//stocke dans le tableau afin de le surligner plus tard
				  				assert(start > -1);
				  				stop = start + listFunctions.get(j).toUpperCase().length();
			  				}
			  				char temp2;
			  				char temp3 = 0;
			  				if(start==0 && stop==content.length()){
			  					temp2=' ';
			  					temp3=' ';
			  				}			  				
				   			else if(start==0){							//le cas du début pas besoin d'espace avant le mot
				   				temp2=' ';
				  	  			temp3 = content.charAt(stop);				  	  				
			  	  			}
				  	  		else if(stop==content.length()){		//le cas de la fin pas besoin d'espace après le mot
				  	  			temp2 = content.charAt(start-1);
			  	  			}
			  	  			else{									//le cas général
				  	  			temp2 = content.charAt(start-1);
				  	  			temp3 = content.charAt(stop);
			  	  			}		
					  		if((temp2 == ' ' || temp2 == '\n') && (temp3== '(' )){		//on vérifie alors qu'on est bien entre deux blancs ou entre un saut à la ligne
					  			tabStartFunction.add(start+temp);
					  			tabStopFunction.add(stop+temp);			//pr pas prendre la parenthèse
				  			}
					      	 
			  	  			//permet de faire la relation entre le fichier temporaire et le fichier de base (JtextArea)
			  	  			//maintien à jour les chiffres stop et start faussé par le subSequence 
			  	  			temp+=stop;
			  	  			content = (content.subSequence(stop, content.length()));
			  	  			
			  	  		}		  	  				  	  			

		  	  		}
		  	  		try {
						content =d.getText(0, d.getLength());
						stop=0;start=0;temp=0;	  	
					} catch (BadLocationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
						
			  	}	
				
				
				textPaneQuery.getStyledDocument().setCharacterAttributes(0, content.length(), normal, true);

	 	  		//ici on colorie le texte en fonction des données recueillis auparavant
	 	 		for(int i =0;i<tabStartWord.size();i++)	{
	 	 			textPaneQuery.getStyledDocument().setCharacterAttributes(tabStartWord.get(i), tabStopWord.get(i)-tabStartWord.get(i), style, false);
 	  			}  	 
	 	  		for(int i =0;i<tabStartFunction.size();i++)	{		 	  			
	 	  			textPaneQuery.getStyledDocument().setCharacterAttributes(tabStartFunction.get(i), tabStopFunction.get(i)-tabStartFunction.get(i), function, false);
	 	  		}  	 
	 	  		for(int i=0;i<tabStopQuote.size();i++){
	 	  			textPaneQuery.getStyledDocument().setCharacterAttributes(tabStartQuote.get(i), tabStopQuote.get(i) - tabStartQuote.get(i), quote, false);
	 	  		}
	 	  		for(int i=0; i<tabStartComm.size();i++){
	 	  			textPaneQuery.getStyledDocument().setCharacterAttributes(tabStartComm.get(i), tabStopComm.get(i)-tabStartComm.get(i), comm, false);
	  	  		}
 
	 } 	  	

	
	public String find(String mySearch, Iterator<Node> iterator){
		while (iterator.hasNext()){
			String temp = iterator.next().getId();
        	if(mySearch.equals(temp)){
        		return temp;
        	}
    	}
		return null;
	}	
	
	/**
	 * fonction temporaire permettant de savoir si la requête est sélective ou constructive
	 * A améliorer car analyse le texte de la requête
	 * @param l_Results
	 * @return
	 */
	
	public boolean isSelect(IResults l_Results){
		if(l_Results.toString().contains("SELECT") || l_Results.toString().contains("select")){
			return true;
		}
		else return false;
	}

	Iterable<Entity> getEdges(IResults l){
		if (l instanceof QueryResults){
			// kgraph
			QueryResults qr = (QueryResults) l;
			return qr.getEdges();
		}
		return null;
//		else {
//			// corese/kgram
//			CoreseGraph cg = null;
//
//			for (IResult res : l){
//				cg = (CoreseGraph) res;	
//			}
//			if (cg == null) return new ArrayList<Entity>();
//			Iterable<Entity> edges = cg.getEdges();
//			return edges;
//		} 
	}
	
	String getLabel(String name){
		int ind = name.lastIndexOf("#");
		if (ind == -1) ind = name.lastIndexOf("/");
		if (ind == -1 || ind == name.length()-1) return name;
		return name.substring(ind+1);
	}
	
	
	
	public void display(IResults l_Results, MainFrame coreseFrame){
		// On affiche la version XML du résultat dans l'onglet XML
		resultXML = l_Results.toString();
		textAreaXMLResult.setText(resultXML.toString());
		
		int num=0;
		graph = new MultiGraph( false, true );	
		String sujetUri, predicat, objetUri, temp = "http://www.inria.fr/acacia/corese#Results";		
		
		// On affiche la version en arbre du résultat dans l'onglet Tree
		// crée un arbre de racine "root"
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");	    	        	
		DefaultTreeModel treeModel = new DefaultTreeModel(root);
		treeResult = new JTree(treeModel); 
		treeResult.setShowsRootHandles(true);
		int i = 1;		    	        	
		// Pour chaque resultat de l_Results on crée un noeud "result"
		//if (l_Results.size()<1000)
		
		for(IResult res : l_Results){		    	        		
			DefaultMutableTreeNode x = new DefaultMutableTreeNode("result " + i);		    	        	
			// Pour chaque variable du résultat on ajoute une feuille contenant le nom de la variable et sa valeur
			for (String var : l_Results.getVariables()){
				if (res.getResultValues(var) != null){
					for (IResultValue val : res.getResultValues(var)){	
						x.add( new DefaultMutableTreeNode(var));
						x.add( new DefaultMutableTreeNode(val.getDatatypeValue().toString()));
						root.add(x);
					}
				}
			}				    			
			i++;
		}
		TreePath myPath = treeResult.getPathForRow(0);
		treeResult.expandPath(myPath);
		scrollPaneTreeResult.setViewportView(treeResult);

		//pointe sur le résultat XML
		tabbedPaneResults.setSelectedIndex(1);

		
		if (l_Results.isConstruct() || l_Results.isDescribe()){
						
			graph.addNode(temp).addAttribute( "ui.style", "color:white;");

			String sujet = null;
			String objet = null;
			
			Iterable<Entity> edges = getEdges(l_Results);
			
			for (Entity ent : edges){
				Edge edge = ent.getEdge();
				sujetUri = edge.getNode(0).getLabel();
				objetUri = edge.getNode(1).getLabel();
				
//				if (edge instanceof CoreseRelation)
//					predicat = ((CoreseRelation)edge).getCType().toString();
//				else 
					predicat = getLabel(edge.getEdgeNode().getLabel());
				
				sujet = getLabel(sujetUri);
				objet = getLabel(objetUri);
				
				if (find(sujetUri,graph.getNodeIterator())==null){
					graph.addNode(sujetUri).addAttribute("label", sujet);
					graph.getNode(sujetUri).setAttribute("ui.class", sujet);
					if (edge.getNode(0).isBlank())
					{
						graph.getNode(sujetUri).setAttribute("ui.class", "BlankNode");
					}
					graph.addEdge("temp"+num, sujetUri, temp);
					graph.getEdge("temp"+num).addAttribute("ui.style", "width:0;edge-style:dashes;color:white;");

				}			
				num++;
				
				//Lors de l'ajout d'un Noeud (ou pas) Objet
				if (find(objetUri,graph.getNodeIterator())==null){
					graph.addNode(objetUri).addAttribute("label", objet);
					graph.getNode(objetUri).setAttribute("ui.class", objet);
					if (edge.getNode(1).isBlank())
					{
						graph.getNode(objetUri).setAttribute("ui.class", "BlankNode");
					}
					IDatatype dt = (IDatatype) edge.getNode(1).getValue();
					if (dt.isLiteral())
					{
						graph.getNode(objetUri).setAttribute("ui.class", "Literal");
					}
					
					graph.addEdge("temp"+num, objetUri, temp);
					//graph.getEdge("temp"+num).addAttribute("label", "http://www.inria.fr/acacia/corese#result");
					graph.getEdge("temp"+num).addAttribute("ui.style", "width:0;edge-style:dashes;color:white;");
				}			
				num++;
				
				graph.addEdge("edge"+num, sujetUri, objetUri , true); 
				graph.getEdge("edge"+num).addAttribute("label", predicat);
				graph.getEdge("edge"+num).addAttribute("ui.class", predicat);

			}

			textPaneStyleGraph.setText(stylesheet);
			graph.addAttribute( "ui.stylesheet", stylesheet);
	
			//permet de visualiser correctement le graphe dans l'onglet de Corese
			ElasticBox eb = new ElasticBox();
			eb.setForce((float) 0.1);
			SwingGraphViewer sgv = new SwingGraphViewer(graph, eb,true, true);	
			SwingGraphRenderer sgr = sgv.getRenderer();

			//Dégrise le bouton et ajoute le texte dans le textPane
			buttonRefreshStyle.setEnabled(true);
			buttonDefaultStyle.setEnabled(true);
			 
			JPanel panelStyleGraph = new JPanel();
			panelStyleGraph.setLayout(new BorderLayout());
			 	 	 

			panelStyleGraph.add(textPaneStyleGraph,BorderLayout.CENTER);
			panelStyleGraph.add(textAreaLinesGraph, BorderLayout.WEST);
			
			JScrollPane jsStyleGraph = new JScrollPane();
			jsStyleGraph.setViewportView(panelStyleGraph);

			final JSplitPane jpGraph = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT	, jsStyleGraph, sgr);
			jpGraph.setContinuousLayout(true);
			scrollPaneTreeResult.setViewportView(jpGraph);

			//pointe sur l'onglet Graph
			tabbedPaneResults.setSelectedIndex(0);

		}	 
	
		 
	}	
		
		



	ActionListener  createListener(final MainFrame coreseFrame, final boolean isTrace){
		
		return new ActionListener() {
			
			public void actionPerformed(ActionEvent ev) {
				textAreaXMLResult.setText("");
				IResults l_Results = null;
				scrollPaneTreeResult.setViewportView(new JPanel());
				scrollPaneTreeResult.setRowHeaderView(new JPanel());
				
				String l_message =new String("Parsing:\n");
				IEngine engine = coreseFrame.getMyCorese();
				try {
					// Lance d'abbord la validation
					if (!coreseFrame.isKgram()) {
						engine.SPARQLValidate(textPaneQuery.getText());
					}
					try {
						if (ev.getSource() == buttonProve){
							l_Results = engine.SPARQLProve(textPaneQuery.getText());
						}
						// Lance la requête
						else if (!coreseFrame.isKgram()){
							l_Results = engine.SPARQLQuery(textPaneQuery.getText());
						}
						else {
							Exec exec = new Exec(coreseFrame, textPaneQuery.getText(), isTrace);
							//l_Results = exec.query();							
							exec.process();
							
							//Permet de passer a true toutes les options du trace KGram
							for(int i=0;i<coreseFrame.getListCheckbox().size();i++){
								coreseFrame.getListCheckbox().get(i).setEnabled(true);
							}
							for(int i=0;i<coreseFrame.getListJMenuItems().size();i++){
								coreseFrame.getListJMenuItems().get(i).setEnabled(true);
							}
							

						}
						
						// Si le résultat existe
						if (l_Results != null){
							display(l_Results, coreseFrame);
						}
						else if (!coreseFrame.isKgram()){
							textAreaXMLResult.setText( coreseFrame.getMyCapturer().getContent()); // display errors
							//coreseFrame.getCapturer()
						}
					} catch (EngineException e) {
						e.printStackTrace();
						textAreaXMLResult.setText(  coreseFrame.getMyCapturer().getContent()  + e.getMessage() ); // display errors
					}
				}
				catch (QuerySyntaxException e) {
					tabbedPaneResults.setSelectedIndex(2);
					l_message += "____ Syntax error\n" + e.getMessage() + "________\n";
				}
				catch (QueryLexicalException e) {
					tabbedPaneResults.setSelectedIndex(2);
					l_message += "____ Lexical error\n" + e.getMessage() + "________\n";
				} catch (QuerySemanticException e) {
					tabbedPaneResults.setSelectedIndex(2);
					l_message += "____ Semantic error\n" + e.getMessage() + "________\n";
				} catch (EngineException e) {
					e.printStackTrace();
				}
				textPaneValidation.setText(l_message + "Done.");
			};
		};
	}
    
	private void checkLines(JTextComponent textComponentInput, JTextComponent textComponentOutput){
		String text="";
    	Document doc2 = textComponentInput.getDocument();
		int lineCount = doc2.getDefaultRootElement().getElementCount();	
		for(int i=1;i<lineCount+1;i++){
			text +=String.valueOf(i)+"\n";
		}
    	textComponentOutput.setText(text);	
	}


	public JTextArea getTextAreaLines() {
		return textAreaLines;
	}


	@Override
	public void changedUpdate(DocumentEvent e) {
		this.checkLines(textPaneQuery,textAreaLines);
		doColoringLater();

	}



	@Override
	public void insertUpdate(DocumentEvent e) {
		this.checkLines(textPaneQuery,textAreaLines);
		doColoringLater();

	}



	@Override
	public void removeUpdate(DocumentEvent e) {
		this.checkLines(textPaneQuery,textAreaLines);
		doColoringLater();


	}



	@Override
	public void focusGained(FocusEvent e) {
		doColoringLater();
	}



	@Override
	public void focusLost(FocusEvent e) {
		this.checkLines(textPaneQuery,textAreaLines);
		doColoringLater();
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		MainFrame.conteneurOnglets.setSelectedIndex(0);
	}

	
	
	  public void clearHighlights()
	    {
		if(highlighter != null) {
		    if(start != null)
			highlighter.removeHighlight(start);
		    if(end != null)
			highlighter.removeHighlight(end);
		    start = end = null;
		    highlighter = null;
		}
	    }

	    /** Returns the character at position p in the document*/
	    public static char getCharAt(Document doc, int p) 
		throws BadLocationException
	    {
		return doc.getText(p, 1).charAt(0);
	    }


	 public static int findMatchingParen(Document d, int paren) 
	 
		throws BadLocationException
	    {
		int parenCount = 1;
		int i = paren-1;
		for(; i >= 0; i--) {
		    char c = getCharAt(d, i);
		    switch(c) {
		    case ')':
		    case '}':
		    case ']':
			parenCount++;
			break;
		    case '(':
		    case '{':
		    case '[':
			parenCount--;
			break;
		    }
		    if(parenCount == 0)
			break;
		}
		return i;
	    }



	@Override
	public void caretUpdate(CaretEvent e) {
		Highlighter.HighlightPainter goodPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.cyan);
		Highlighter.HighlightPainter badPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.magenta);

		
		clearHighlights();
		JTextComponent source = (JTextComponent) e.getSource();
		highlighter = source.getHighlighter();
		Document doc = source.getDocument();
		
		if(e.getDot() == 0){
			return;
		}
		
		int closeParen = e.getDot()-1;
		try {
			char c = getCharAt(doc, closeParen);
			if(c == ')' || c == '}' || c == ']'){
				int openParen = findMatchingParen(doc,closeParen);
				if(openParen >= 0){
					char c2 = getCharAt(doc, openParen);
					if(c2 == '(' && c == ')' ||
					   c2 == '{' && c =='}' ||
					   c2 == '[' && c ==']')
					{
						start = highlighter.addHighlight(openParen, openParen+1,goodPainter);
						end =   highlighter.addHighlight(closeParen, closeParen+1,goodPainter);
					}
					else{
						start = highlighter.addHighlight(openParen, openParen+1,badPainter);
						end =   highlighter.addHighlight(closeParen, closeParen+1,badPainter);
					}
				}
				else 	{
					end =   highlighter.addHighlight(closeParen, closeParen+1,badPainter);
				}
				
			}

			
		} catch (BadLocationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}

	 private void  doColoringLater() {
		    if (!isColoring) {
		      SwingUtilities.invokeLater(this);
		    }
		  }


	@Override
	public void run() {
		 isColoring = true;
		    try {
		    	this.search(textPaneQuery);

		    }
		    finally { 
		      isColoring = false;
		    }
	}
	
	

	//getteurs et setteurs utiles
	public void setJPanel1(JPanel pane_query) {
		this.paneQuery = pane_query;
	}
	
	public JPanel getPaneQuery() {
		return paneQuery;
	}
	
	public  JTextPane getTextPaneQuery() {
		return textPaneQuery;
	}

	public  JTextArea getTextAreaXMLResult() {
		return textAreaXMLResult;
	}

	public JButton getButtonTKgram() {
		return buttonTKgram;
	}




}