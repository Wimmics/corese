package fr.inria.corese.gui.query;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Iterator;

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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.gui.core.MainFrame;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.compiler.parser.Pragma;
import fr.inria.corese.kgram.api.core.ExpType;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.print.ResultFormat;
import fr.inria.corese.core.print.XMLFormat;
import fr.inria.corese.core.transform.Transformer;
import fr.inria.corese.core.util.CompareMappings;
import fr.inria.corese.core.util.Property;
import static fr.inria.corese.core.util.Property.Value.GUI_CONSTRUCT_FORMAT;
import static fr.inria.corese.core.util.Property.Value.GUI_SELECT_FORMAT;
import static fr.inria.corese.core.util.Property.Value.GUI_XML_MAX;
import fr.inria.corese.core.util.SPINProcess;
import fr.inria.corese.kgram.core.SparqlException;
import fr.inria.corese.sparql.datatype.RDF;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.Metadata;
import java.util.Date;
import java.util.List;
import org.apache.logging.log4j.Level;


import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.graphicGraph.stylesheet.StyleSheet;
import org.graphstream.ui.layout.springbox.implementations.LinLog;
import org.graphstream.ui.swingViewer.View;
import org.graphstream.ui.swingViewer.Viewer;

/**
 * Onglet Query avec tout ce qu'il contient.
 *
 * @author saguilel
 * @author Maraninchi jerôme
 */
public final class MyJPanelQuery extends JPanel {

    private static final long serialVersionUID = 1L;
    static final String SERVICE = ""; //"@federate <http://fr.dbpedia.org/sparql>";
    static final String NL = System.getProperty("line.separator");
    static int FontSize = SparqlQueryEditor.FontSize;
    
    static final int GRAPH_PANEL    = 0;
    static final int XML_PANEL      = 1;
    static final int TABLE_PANEL    = 2;

    // display max table result
    int maxres = 1000000;
    // display max xml result format
    int maxresxml = 1000;
    
    //Boutton du panneau Query
    private JButton buttonRun, buttonShacl, buttonPush, buttonCopy, 
            buttonSort, buttonCompare,
            buttonShex, 
            buttonKill, buttonStop, buttonValidate, buttonToSPIN, buttonToSPARQL, 
            buttonTKgram, buttonProve;
    private JButton buttonSearch;
    private JButton buttonRefreshStyle, buttonDefaultStyle;
    //panneau de la newQuery
    private JPanel paneQuery;
    //Ajoute le scroll pour les différents panneaux
    private JScrollPane scrollPaneTreeResult;
    private JScrollPane scrollPaneXMLResult;
    private JScrollPane scrollPaneValidation;
    private JScrollPane scrollPaneTable;
    private JTable tableResults;
    //Conteneur d'onglets de résultats et les onglets
    private JTabbedPane tabbedPaneResults;
    private JTextArea textAreaXMLResult;
    private JTextPane textPaneValidation;
    private JTextPane textPaneStyleGraph;
    private JTree treeResult;
    //Pour le graphe
    private MultiGraph graph;
    private boolean excepCatch = false;
    private JTextArea textAreaLinesGraph;
    private String stylesheet = "";
    //private CharSequence resultXML = "";
    //private String resultXML = "";
    private SparqlQueryEditor sparqlQueryEditor;
    private JTextPane serviceEditor;
    private MainFrame mainFrame;
    private Exec current;
    private static final String KGSTYLE = ExpType.KGRAM + "style";
    private static final String KGGRAPH = Pragma.GRAPH;
    private static final Logger logger = LogManager.getLogger(MyJPanelQuery.class.getName());
    private boolean displayLink = true;
    private QueryExec queryExec;
    private Mappings mappings;

    public MyJPanelQuery() {
        super();
        initComponents();
        setQuery("empty request");
    }

    public MyJPanelQuery(final MainFrame coreseFrame, String query, String name) {
        super();
        initComponents();
        mainFrame = coreseFrame;
        installListenersOnMainFrame(coreseFrame);
        //setQuery(coreseFrame.getTextQuery());
        setQuery(query);
        setFileName(name);
        stylesheet = coreseFrame.getDefaultStylesheet();
    }
    
    public void setFileName(String name) {
         serviceEditor.setText(name);
    }
    
    private void initComponents() {
      
        paneQuery = new JPanel(new BorderLayout());
        paneQuery.setName("paneQuery");
        setLayout(new BorderLayout(5, 5));
        add(paneQuery);

        buttonRun = new JButton();
        buttonShacl = new JButton();
        buttonShex = new JButton();
        buttonPush = new JButton();
        buttonCopy = new JButton();
        buttonSort = new JButton();
        buttonCompare = new JButton();
        buttonStop = new JButton();
        buttonKill = new JButton();
        buttonValidate = new JButton();
        buttonToSPIN = new JButton();
        buttonToSPARQL = new JButton();
        buttonProve = new JButton();
        buttonTKgram = new JButton();
        buttonSearch = new JButton();
        buttonRefreshStyle = new JButton();
        buttonDefaultStyle = new JButton();
        tabbedPaneResults = new JTabbedPane();
        scrollPaneTreeResult = new JScrollPane();
        scrollPaneXMLResult = new JScrollPane();
        scrollPaneTable = new JScrollPane();
        tableResults = new JTable(new DefaultTableModel());
        tableResults.setFont(new Font("Sanserif", Font.PLAIN, 18));
        textAreaXMLResult = new JTextArea();
        textAreaXMLResult.setFont(new Font("Sanserif", Font.BOLD, FontSize));

        scrollPaneValidation = new JScrollPane();
        textPaneValidation = new JTextPane();
        textPaneStyleGraph = new JTextPane();

        //compteur de ligne pour la feuille de style de graphe
        textAreaLinesGraph = new JTextArea();
        textAreaLinesGraph.setFont(new Font("Sanserif", Font.BOLD, 12));
        textAreaLinesGraph.setEditable(false);
        textAreaLinesGraph.setFocusable(false);
        textAreaLinesGraph.setBackground(new Color(230, 230, 230));
        textAreaLinesGraph.setForeground(Color.black);
        textAreaLinesGraph.setAutoscrolls(true);

        //Bouton refreshStyleGraph + listener a l'écoute afin de recharger la feuille de style ou de renvoyer une exception
        buttonRefreshStyle.setText("Refresh stylesheet");
        buttonDefaultStyle.setText("Default stylesheet");
        buttonRefreshStyle.setEnabled(false);
        buttonDefaultStyle.setEnabled(false);

        /**
         * ActionListener sur le Bouton refresh Stylesheet Refresh stylesheet du
         * Graphe Permet de mettre a jour le style du Graphe ou d'afficher une
         * exception sous forme de PopUp lors d'un problème syntaxique
         */
        ActionListener refreshListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String style = textPaneStyleGraph.getText();
                JTextArea areaException = new JTextArea();
                StyleSheet sh = new StyleSheet();
                try {
                    sh.parseFromString(style);
                    areaException.setText("");
                    excepCatch = false;
                } catch (Exception e1) {
                    areaException.setText(e1.getMessage());
                    areaException.setEditable(false);
                    areaException.setForeground(Color.red);
                    JOptionPane.showMessageDialog(null, areaException, "Error Syntax", JOptionPane.WARNING_MESSAGE);
                    excepCatch = true;
                } /*catch (TokenMgrError e1) {
                    areaException.setText(e1.getMessage());
                    areaException.setEditable(false);
                    areaException.setForeground(Color.red);
                    JOptionPane.showMessageDialog(null, areaException, "Error Syntax", JOptionPane.WARNING_MESSAGE);
                    excepCatch = true;
                }*/
                if (!excepCatch) {
                    graph.addAttribute("ui.stylesheet", style);
                    textPaneStyleGraph.setText(style);
                    stylesheet = style;
                }

            }
        };
        buttonRefreshStyle.addActionListener(refreshListener);

        checkLines(textPaneStyleGraph, textAreaLinesGraph);
        /**
         * DocumentListener sur le textPaneStyleGraph Listener sur le
         * textPaneStyleGraph afin d'actualiser le nombre de lignes
         */
        DocumentListener l_paneGraphListener = new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                checkLines(textPaneStyleGraph, textAreaLinesGraph);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                checkLines(textPaneStyleGraph, textAreaLinesGraph);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkLines(textPaneStyleGraph, textAreaLinesGraph);
            }
        };
        Document doc3 = textPaneStyleGraph.getDocument();
        doc3.addDocumentListener(l_paneGraphListener);

        /**
         * FocusListener sur le textPaneStyleGraph Permet d'actualiser le
         * compteur de ligne lors d'un FocusGained et FocusLost
         */
        FocusListener paneGraphFocusListener = new FocusListener() {
            @Override
            public void focusLost(final FocusEvent e) {
                checkLines(textPaneStyleGraph, textAreaLinesGraph);
            }

            @Override
            public void focusGained(final FocusEvent e) {
                checkLines(textPaneStyleGraph, textAreaLinesGraph);
            }
        };
        textPaneStyleGraph.addFocusListener(paneGraphFocusListener);

        sparqlQueryEditor = new SparqlQueryEditor(mainFrame);
//        sparqlQueryEditor.setPreferredSize(new Dimension(200,200));
        sparqlQueryEditor.refreshColoring();
        /**
         * Bouttons et leurs actions *
         */
        //Lancer une requête
        // copy current result into new query panel (template)
        buttonPush.setText("Push");
        // copy current result into last result panel
        buttonCopy.setText("Copy");
        buttonSort.setText("Modifier");
        buttonCompare.setText("Compare");
        buttonRun.setText("Query");
        buttonShacl.setText("Shacl");
        buttonShex.setText("Shex");
        buttonStop.setText("Stop");
        buttonKill.setText("Kill");
        buttonValidate.setText("Validate");
        buttonToSPIN.setText("to SPIN");
        buttonToSPARQL.setText("to SPARQL");
        buttonProve.setText("Prove");
        buttonTKgram.setText("Trace");

        //Pour chercher un string dans la fenêtre de résultat XML
        buttonSearch.setText("Search");

        ActionListener searchListener = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                //textAreaXMLResult.setText(resultXML.toString());
                String toSearch = "";
                String message = "";

                String temps = textAreaXMLResult.getText();
                toSearch = JOptionPane.showInputDialog("Search", message);

                if (toSearch != null) {
                    sparqlQueryEditor.search(temps, toSearch, textAreaXMLResult, message);
                } else {
                    JOptionPane.showMessageDialog(getPaneQuery(), "Veuillez entrer une chaine de caracteres", "info", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        };
        buttonSearch.addActionListener(searchListener);

        // Résultat sous forme d'arbre
        tabbedPaneResults.addTab("Graph", scrollPaneTreeResult);

        // Résultat sous forme XML
        textAreaXMLResult.setEditable(false);
        //textAreaXMLResult.setText(resultXML.toString());
        textAreaXMLResult.setText("");
        scrollPaneXMLResult.setViewportView(textAreaXMLResult);
        tabbedPaneResults.addTab("XML/RDF", scrollPaneXMLResult);

        //results in table
        tableResults.setPreferredScrollableViewportSize(tableResults.getPreferredSize());
        tableResults.setFillsViewportHeight(true);
        scrollPaneTable.setViewportView(tableResults);
        tabbedPaneResults.addTab("Table", scrollPaneTable);

        // Messages de la validation
        textPaneValidation.setEditable(false);
        textPaneValidation.setText("");
        scrollPaneValidation.setViewportView(textPaneValidation);

        tabbedPaneResults.addTab("Validate", scrollPaneValidation);

        // Mise en forme
        final JSplitPane jp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, sparqlQueryEditor, tabbedPaneResults);
        jp.setContinuousLayout(true);

        GroupLayout pane_listenerLayout = new GroupLayout(paneQuery);
        paneQuery.setLayout(pane_listenerLayout);

        GroupLayout.ParallelGroup hParallel1 = pane_listenerLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.SequentialGroup hSeq1 = pane_listenerLayout.createSequentialGroup();
        GroupLayout.ParallelGroup hParallel2 = pane_listenerLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.SequentialGroup hSeq2 = pane_listenerLayout.createSequentialGroup();

        hSeq2.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 257, Short.MAX_VALUE);
        hSeq2.addComponent(buttonRun);
        hSeq2.addComponent(buttonShacl);
        hSeq2.addComponent(buttonShex);
        hSeq2.addComponent(buttonPush);
        hSeq2.addComponent(buttonCopy);
        hSeq2.addComponent(buttonSort);
        hSeq2.addComponent(buttonCompare);
        hSeq2.addComponent(buttonStop);
        hSeq2.addComponent(buttonKill);
        hSeq2.addComponent(buttonValidate);
        hSeq2.addComponent(buttonToSPIN);
        hSeq2.addComponent(buttonToSPARQL);

        hSeq2.addComponent(buttonSearch);
        hSeq2.addGap(30, 30, 30);
        hSeq2.addComponent(buttonRefreshStyle);
        hSeq2.addComponent(buttonDefaultStyle);
        hParallel2.addGroup(hSeq2); 
        
        serviceEditor = new JTextPane();
        serviceEditor.setText(SERVICE);
        JPanel service = new JPanel(new BorderLayout());
        service.setMaximumSize(new Dimension(400, 10));
        service.add(serviceEditor);
        service.setName("service");
        hParallel2.addComponent(service, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        
        hParallel2.addComponent(jp, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        hSeq1.addContainerGap();
        hSeq1.addGroup(hParallel2);
        hParallel1.addGroup(hSeq1);

        pane_listenerLayout.setHorizontalGroup(hParallel1);

        GroupLayout.ParallelGroup vParallel1 = pane_listenerLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.ParallelGroup vParallel2 = pane_listenerLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.SequentialGroup vSeq1 = pane_listenerLayout.createSequentialGroup();

        vParallel2.addComponent(buttonRun);
        vParallel2.addComponent(buttonShacl);
        vParallel2.addComponent(buttonShex);
        vParallel2.addComponent(buttonPush);
        vParallel2.addComponent(buttonCopy);
        vParallel2.addComponent(buttonSort);
        vParallel2.addComponent(buttonCompare);
        vParallel2.addComponent(buttonStop);
        vParallel2.addComponent(buttonKill);
        vParallel2.addComponent(buttonValidate);
        vParallel2.addComponent(buttonToSPIN);
        vParallel2.addComponent(buttonToSPARQL);

        vParallel2.addComponent(buttonSearch);
        vParallel2.addComponent(buttonRefreshStyle);
        vParallel2.addComponent(buttonDefaultStyle);
        vSeq1.addContainerGap();
        vSeq1.addGroup(vParallel2);
        vSeq1.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        
        vSeq1.addComponent(service);
        vSeq1.addComponent(jp);
        
        vSeq1.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        vSeq1.addContainerGap();
        vParallel1.addGroup(vSeq1);

        pane_listenerLayout.setVerticalGroup(vParallel1);

    }
    
    public String getSparqlRequest() {
        return sparqlQueryEditor.getTextPaneQuery().getText();
    }
    
    public JTextPane getTextPaneQuery() {
        return sparqlQueryEditor.getTextPaneQuery();
    }

    public int getLineNumber() {
        return sparqlQueryEditor.getTextAreaLines().getLineCount();
    }

    private void installListenersOnMainFrame(final MainFrame coreseFrame) {
        /**
         * ActionListener sur le bouton DefaultStylesheet Permet d'attribuer un
         * style par défaut défini au niveau de la MainFrame au graphe
         */
        ActionListener defaultListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stylesheet = coreseFrame.getDefaultStylesheet();
                textPaneStyleGraph.setText(stylesheet);
                graph.addAttribute("ui.stylesheet", stylesheet);
            }
        };
        buttonDefaultStyle.addActionListener(defaultListener);

        ActionListener l_RunListener = createListener(coreseFrame, false);
        
        buttonRun.addActionListener(l_RunListener);
        buttonShacl.addActionListener(l_RunListener);
        buttonShex.addActionListener(l_RunListener);
        buttonPush.addActionListener(l_RunListener);
        buttonCopy.addActionListener(l_RunListener);
        buttonSort.addActionListener(l_RunListener);
        buttonCompare.addActionListener(l_RunListener);
        buttonStop.addActionListener(l_RunListener);
        buttonKill.addActionListener(l_RunListener);
        buttonValidate.addActionListener(l_RunListener);
        buttonToSPIN.addActionListener(l_RunListener);
        buttonToSPARQL.addActionListener(l_RunListener);
    }

    public void setQuery(final String newRequest) {
        //si on crée une nouvelle requête on met le texte de base, si on charge une requête on met le texte qui est dans le fichier .txt
        sparqlQueryEditor.setQueryText(newRequest);
    }

    private String find(String mySearch, Iterator<Node> iterator) {
        while (iterator.hasNext()) {
            String temp = iterator.next().getId();
            if (mySearch.equals(temp)) {
                return temp;
            }
        }
        return null;
    }


    private String getLabel(NSManager nsm, fr.inria.corese.kgram.api.core.Node n) {
        IDatatype dt =  n.getValue();
        if (dt.isURI()){
            return nsm.toPrefix(n.getLabel());
        }
        else {
            return n.getLabel();
        }
    }
    
    /**
     * Max number of xml results to display can be set by Property
     */
    int maxResXML() {
        if (Property.intValue(GUI_XML_MAX) != null) {
            return Property.intValue(GUI_XML_MAX);
        }
        return maxresxml;
    }

    String toString(Mappings map) {
        Query q = map.getQuery();
        ASTQuery ast =  map.getQuery().getAST();
        if (ast.isSPARQLQuery()) {
            if (map.getGraph() != null) {
                return graphToString(map);
            } else {
                // RDF or XML
                String str = mapToString(map);
                if (str == null) {
                    // use case: template fail
                    str = "";
                }
                if (map.size() > maxResXML()) {
                    System.out.println(String.format("GUI display %s XML results out of %s", maxResXML(), map.size()));
                }
                if (str.isEmpty() && ast.getErrors() != null) {
                    return ast.getErrorString();
                }
                return str;
            }
        } else {
            // XML bindings only (do not display the whole graph)
            return XMLFormat.create(map).toString();
        }
    }
    
    String mapToString(Mappings map) {
        if (Property.stringValue(GUI_SELECT_FORMAT) != null) {
            switch (Property.stringValue(GUI_SELECT_FORMAT)) {
                case Property.JSON:
                    return clean(ResultFormat.create(map, ResultFormat.JSON_FORMAT)
                            .setNbResult(maxResXML()).toString());
                case Property.XML:
                    return ResultFormat.create(map, ResultFormat.XML_FORMAT)
                            .setNbResult(maxResXML()).toString();
            }
        }
        ResultFormat rf = ResultFormat.create(map).setNbResult(maxResXML());
        return rf.toString();
    }

    String graphToString(Mappings map) {
        Graph g = (Graph) map.getGraph();
        
        if (Property.stringValue(GUI_CONSTRUCT_FORMAT) != null) {
            switch (Property.stringValue(GUI_CONSTRUCT_FORMAT)) {
                case Property.XML:
                case Property.RDF_XML:
                    return ResultFormat.create(g, ResultFormat.RDF_XML_FORMAT).toString();
                case Property.TURTLE:
                    return ResultFormat.create(g, ResultFormat.TURTLE_FORMAT).toString();
                case Property.TRIG:
                    return ResultFormat.create(g, ResultFormat.TRIG_FORMAT).toString();
                case Property.JSON:
                    return clean(ResultFormat.create(g, ResultFormat.JSON_LD_FORMAT).toString());   }
        }
        // default
        return ResultFormat.create(g, ResultFormat.TRIG_FORMAT).toString();
        //return turtle(g);
    }
    
    String clean(String str) {
        return str.replace("\t", " ");
    }
    
    String turtle(Graph g) {
        Transformer t = Transformer.create(g, Transformer.TURTLE);
        try {
            return t.transform();
        } catch (EngineException ex) {
            return ex.getMessage();
        }
    }

    void fillTable(Mappings map) {
        fillTable(map, -1);
    }
    
    void fillTable(Mappings map, int sort) {
        Query q = map.getQuery();
        ASTQuery ast = q.getAST();
        List<fr.inria.corese.kgram.api.core.Node> vars = q.getSelect();
        if (q.isUpdate() && map.size() > 0){            
           vars = map.get(0).getQueryNodeList();           
        }
        DefaultTableModel model = new DefaultTableModel();
        
        int size = Math.min(maxres, map.size());
        
        String[] col = new String[size];
        for (int i = 0; i<size; i++){
            col[i] = Integer.toString(i+1);
        }
        model.addColumn("num", col);
        
        for (fr.inria.corese.kgram.api.core.Node var : vars) {
            if (accept(ast, var.getLabel())) {
                String columnName = var.getLabel();
                //System.out.println(sv);
                String[] colmunData = new String[size];
                for (int j = 0; j < map.size(); j++) {
                    if (j >= maxres) {
                        logger.warn("Stop display after " + maxres + " results out of " + map.size());
                        break;
                    }
                    Mapping m = map.get(j);
                    fr.inria.corese.kgram.api.core.Node value = m.getNode(columnName);

                    if (value != null) {
                        IDatatype dt = value.getValue();
                        colmunData[j] = pretty(dt);
                    }
                }
                model.addColumn(columnName, colmunData);
            }
        }
        
        if (sort>=0) {
            sort(model, sort);
        }
        
        this.tableResults.setModel(model);
    }
    
    // @hide ?_service_report
    boolean accept(ASTQuery ast, String var) {
        if (ast.hasMetadata(Metadata.HIDE)) {
            return ! var.startsWith(Binding.SERVICE_REPORT);
        }
        return true;
    }
    
    TableModel getTable() {
        return tableResults.getModel();
    }
    
    // table = model
    void sort(DefaultTableModel table, int col) {
        for (int i = 0; i<table.getRowCount(); i++) {
            String v1 = (String) table.getValueAt(i, col);
            
            for (int j=i+1; j<table.getRowCount(); j++) {
                String v2 = (String) table.getValueAt(j, col);
                if (v1.compareTo(v2) > 0) {
                    swap(table, i, j);
                    v1 = v2;
                }
            }
        }
    }
       
    // swap row i and j
    void swap(DefaultTableModel table, int i, int j){
        for (int k=0; k<table.getColumnCount(); k++) {
            Object temp = table.getValueAt(i, k);
            table.setValueAt(table.getValueAt(j, k), i, k);
            table.setValueAt(temp, j, k);
        }
    }
    
    String pretty(IDatatype dt) {
        if (dt.isList()) {
            return dt.getValues().toString();
        } else if (dt.isPointer()) {
            return dt.getPointerObject().toString();
        } else if (dt.isLiteral()) { 
            return prettyLiteral(dt);
        } 
        else if (dt.isTriple()) {
            return dt.toString();
        }
        else if (dt.isURI()){
            return dt.toString();
        }
        else {
            return dt.getLabel();
        }
    }
    
    String prettyLiteral(IDatatype dt) {
        if (dt.getCode() == IDatatype.STRING || 
           (dt.getCode() == IDatatype.LITERAL && !dt.hasLang())) {
            return dt.stringValue();
        }
        if (dt.isDecimalInteger()) {
            if (dt.getDatatypeURI().equals(RDF.xsddecimal)) {
                if (dt.doubleValue() == 0.0) {
                    return "0";
                }
                return String.format("%g", dt.decimalValue());
            }
        }
        return dt.toString();
    }
    
    String getResultText() {
        return getTextAreaXMLResult().getText();
    }

    void basicDisplay(Mappings map) {
        display(map, null, -1, false);
    }
    
    public void display(Mappings map) {
        display(map, null, -1, true);
    }
    
    public void display(Mappings map, Binding bind) {
        display(map, bind, -1, true);
    }
    
    void display(Mappings map, Binding bind, int sort, boolean link) {
        if (map == null) {
            // go to XML for error message
            tabbedPaneResults.setSelectedIndex(XML_PANEL);
            return;
        }
        setMappings(map);
        Query q = map.getQuery();
        ASTQuery ast =  q.getAST();
        boolean oneValue = !map.getQuery().isListGroup();
        getTextAreaXMLResult().setText(toString(map));

        // On affiche la version en arbre du résultat dans l'onglet Tree
        // crée un arbre de racine "root"
//        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
//        DefaultTreeModel treeModel = new DefaultTreeModel(root);
//        treeResult = new JTree(treeModel);
//        treeResult.setShowsRootHandles(true);
//
//        //display(root, map);
//
//        TreePath myPath = treeResult.getPathForRow(0);
//        treeResult.expandPath(myPath);
//        scrollPaneTreeResult.setViewportView(treeResult);

        //afficher les resultats dans une tableau sauf pour les templates
        if (q.isTemplate() || ast.isAsk() || ast.getErrors() != null){
            tabbedPaneResults.setSelectedIndex(XML_PANEL);
        } else{
            this.fillTable(map, sort);
            tabbedPaneResults.setSelectedIndex(TABLE_PANEL);
        }

        if (q.isConstruct()) {
            displayGraph((Graph) map.getGraph(), ast.getNSM());
        } // draft
        else if (map.getQuery().isTemplate() && map.getQuery().isPragma(KGGRAPH)) {
            display(map, ast.getNSM());
        }
        
        if (link) {
            linkedResult(ast, map, bind);
        }
    }
    
    void linkedResult(ASTQuery ast, Mappings map, Binding bind) {
        if (isDisplayLink() && !map.getLinkList().isEmpty()) {
            new LinkedResult(mainFrame).process(map);
        }
        if (ast.hasMetadata(Metadata.EXPLAIN)) {
            new DocumentResult(mainFrame).process(map);
        }
        if (ast.hasMetadata(Metadata.WHY) && bind.getLog()!=null) {
            new LocalResult(mainFrame).process(bind.getLog());
            new LocalResult(mainFrame).message(map, bind);
        }
        if (ast.hasMetadata(Metadata.MESSAGE)) {
            new LocalResult(mainFrame).message(map, bind);
        }
    }
    
  

    /**
     * template return turtle graph description display as graph
     */
    void display(Mappings map, NSManager nsm) {
        fr.inria.corese.kgram.api.core.Node res = map.getTemplateResult();
        if (res != null) {
            		fr.inria.corese.core.Graph g = fr.inria.corese.core.Graph.create();
            Load ld = Load.create(g);
            String str = res.getLabel();
            try {
                ld.loadString(str, Load.TURTLE_FORMAT);
                displayGraph(g, nsm);
            } catch (LoadException ex) {
                logger.log(Level.ERROR, "", ex);
            }
        }
    }

    void displayGraph(fr.inria.corese.core.Graph g, NSManager nsm) {
        graph = create(g, nsm);
        graph.addAttribute("ui.stylesheet", stylesheet);
        graph.addAttribute("ui.antialias");
        textPaneStyleGraph.setText(stylesheet);

        //permet de visualiser correctement le graphe dans l'onglet de Corese
        LinLog lLayout = new LinLog();
        lLayout.setQuality(0.9);
        lLayout.setGravityFactor(0.9);

        Viewer sgv = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_SWING_THREAD);
        sgv.enableAutoLayout(lLayout);
        View sgr = sgv.addDefaultView(false);
        
        //View myView = graph.display().getDefaultView();
        
        sgr.getCamera().setAutoFitView(true);

        //Dégrise le bouton et ajoute le texte dans le textPane
        buttonRefreshStyle.setEnabled(true);
        buttonDefaultStyle.setEnabled(true);

        JPanel panelStyleGraph = new JPanel();
        panelStyleGraph.setLayout(new BorderLayout());

        panelStyleGraph.add(textPaneStyleGraph, BorderLayout.CENTER);
        panelStyleGraph.add(textAreaLinesGraph, BorderLayout.WEST);

        JScrollPane jsStyleGraph = new JScrollPane();
        jsStyleGraph.setViewportView(panelStyleGraph);

        final JSplitPane jpGraph = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, jsStyleGraph, sgr);
        jpGraph.setContinuousLayout(true);
        scrollPaneTreeResult.setViewportView(jpGraph);

        //pointe sur l'onglet Graph
        tabbedPaneResults.setSelectedIndex(GRAPH_PANEL);

    }

    
    
    MultiGraph create(fr.inria.corese.core.Graph g, NSManager nsm){
        //            graph.addNode(temp).addAttribute("ui.style", "fill-color:white;");
        //                gsub.addAttribute("ui.style", "fill-color:lightblue;size-mode:dyn-size;shape:rounded-box;");
        //                    ee.addAttribute("ui.style", "size:0;edge-style:dashes;fill-color:white;");
        int num = 0;
        String sujetUri, predicat, objetUri;

        String sujet;
        String objet;

        MultiGraph graph = new MultiGraph(g.getName(), false, true);

        for (fr.inria.corese.kgram.api.core.Edge ent : g.getEdges()) {
            fr.inria.corese.kgram.api.core.Edge edge = ent.getEdge();
            fr.inria.corese.kgram.api.core.Node n1 = edge.getNode(0);
            fr.inria.corese.kgram.api.core.Node n2 = edge.getNode(1);

            sujetUri = n1.getLabel();
            objetUri = n2.getLabel();

            predicat = getLabel(nsm, edge.getEdgeNode());

            sujet = getLabel(nsm, n1);
            objet = getLabel(nsm, n2);

            Node gsub = graph.getNode(sujetUri);
            if (gsub == null) {
                gsub = graph.addNode(sujetUri);
                gsub.addAttribute("label", sujet);
                style(n1, gsub);
            }
            num++;

            if (isStyle(edge)) {
                // xxx kg:style ex:Wimmics
                // it is a fake edge, do not create it
                gsub.setAttribute("ui.class", objet);
            } else {
                Node gobj = graph.getNode(objetUri);
                if (gobj == null) {
                    gobj = graph.addNode(objetUri);
                    gobj.addAttribute("label", objet);
                    style(n2, gobj);
                }
                num++;

                Edge ee = graph.addEdge("edge" + num, sujetUri, objetUri, true);
                ee.addAttribute("label", predicat);
            }
        }

        return graph;
    }
    
    void style(fr.inria.corese.kgram.api.core.Node n, Node gn) {
        if (n.isBlank()) {
            gn.setAttribute("ui.class", "Blank");
        } else if (n.getDatatypeValue().isLiteral()) {
            gn.setAttribute("ui.class", "Literal");
        }
    }


    private boolean isStyle(fr.inria.corese.kgram.api.core.Edge edge) {
        return edge.getEdgeLabel().equals(KGSTYLE);
    }

    /**
     * Display result using Mappings
     */
    void display(DefaultMutableTreeNode root, Mappings map) {
        int i = 1;
        for (Mapping res : map) {
            DefaultMutableTreeNode x = new DefaultMutableTreeNode("result " + i);
            // Pour chaque variable du résultat on ajoute une feuille contenant le nom de la variable et sa valeur

            for (fr.inria.corese.kgram.api.core.Node var : map.getSelect()) {
                fr.inria.corese.kgram.api.core.Node node = res.getNode(var);
                if (node != null) {
                    x.add(new DefaultMutableTreeNode(var.getLabel()));
                    x.add(new DefaultMutableTreeNode(node.getValue().toString()));
                    root.add(x);
                }
            }
            i++;
        }
    }
    
    public void display(String text) {
        textAreaXMLResult.append(text);
    }

    private ActionListener createListener(final MainFrame coreseFrame, final boolean isTrace) {

        return new ActionListener() {            
            @Override
            public void actionPerformed(ActionEvent ev) {
                String txt = getTextArea().getText();
                getTextArea().setText("");
                Mappings l_Results = null;
                scrollPaneTreeResult.setViewportView(new JPanel());
                scrollPaneTreeResult.setRowHeaderView(new JPanel());

                String l_message = new String("Parsing:\n");
                GraphEngine engine = coreseFrame.getMyCorese();

                try {
                    String query = sparqlQueryEditor.getTextPaneQuery().getText();
                    
                    if (ev.getSource() == buttonToSPARQL) {
                        SPINProcess spin = SPINProcess.create();
                        String str = spin.toSparql(query);
                        coreseFrame.getPanel().getTextArea().setText(str);
                        tabbedPaneResults.setSelectedIndex(XML_PANEL);
                    } else if (ev.getSource() == buttonToSPIN) {
                        SPINProcess spin = SPINProcess.create();
                        String str = spin.toSpin(query);
                        //coreseFrame.getPanel().
                        getTextArea().setText(str);
                        tabbedPaneResults.setSelectedIndex(XML_PANEL);
                    }
                    else if (ev.getSource() == buttonPush) { 
                        // copy current result into new query editor (as if load query were done)
                        coreseFrame.newQuery(txt, "generated");
                    } 
                    else if (ev.getSource() == buttonCopy) { 
                        // copy current result into last result panel 
                        // (as if load result), for xt:mappings()
                        coreseFrame.getLastQueryPanel().basicDisplay(getMappings());
                    } 
                    else if (ev.getSource() == buttonSort) { 
                        modifier(query);
                    } 
                    else if (ev.getSource() == buttonCompare) { 
                        compare();
                    } 
                    else if (ev.getSource() == buttonRun || ev.getSource() == buttonValidate 
                            || ev.getSource() == buttonShacl || ev.getSource() == buttonShex) {
                        // buttonRun
                        Exec exec = new Exec(coreseFrame, query, isTrace);
                        setCurrent(exec);
                        exec.setValidate(ev.getSource() == buttonValidate);
                        exec.setShacl(ev.getSource()    == buttonShacl || ev.getSource()    == buttonShex);
                        exec.setShex(ev.getSource()     == buttonShex); //coreseFrame.isShexSemantics());
                        // draft test: Mappings available using xt:mappings()
                        // Mappings from previous query may have been copied here
                        // using Copy button
                        exec.setMappings(getQueryMappings());
                                                
                        exec.process();
                        //Permet de passer a true toutes les options du trace KGram
                        for (int i = 0; i < coreseFrame.getListCheckbox().size(); i++) {
                            coreseFrame.getListCheckbox().get(i).setEnabled(true);
                        }
                        for (int i = 0; i < coreseFrame.getListJMenuItems().size(); i++) {
                            coreseFrame.getListJMenuItems().get(i).setEnabled(true);
                        }
                    }
                    else if (ev.getSource() == buttonStop || ev.getSource() == buttonKill) {
                        if (getCurrent() != null) {
                            getCurrent().finish(ev.getSource() == buttonKill);
                        }
                    }

                } catch (EngineException e) {
                    e.printStackTrace();
                    textAreaXMLResult.setText(coreseFrame.getMyCapturer().getContent() + e.getMessage()); // display errors
                }
                textPaneValidation.setText(l_message + "Done.");
            }
        };
    }
    
    /**
     * User edit order by clause and click button Sort
     * Execute order by on current Mappings, assuming query is "the same"
     */
    void modifier(String query) {
        if (getCurrent() == null) {
            logger.info("Undefined Query Exec");
            setCurrent(new Exec(MainFrame.getSingleton(), query, false));
        } 
        
        //else {
            try {
                Date d1 = new Date();
                Mappings res = getQueryExec().modifier(query, getMappings());
                Date d2 = new Date();
                fillTable(res);
                //setMappings(res);
            } catch (EngineException ex) {
                logger.error(ex);
            } catch (SparqlException ex) {
                logger.error(ex);

            }
        //}
    }
    
        // compare last and previous result
    void compare() {
        show("Compare");
        MyJPanelQuery previous = mainFrame.getPreviousQueryPanel();
        if (getMappings() == null || previous == null || previous.getMappings() == null) {
            show("Query Results not found");
            return;
        }
        compare(getMappings(), previous.getMappings());
    }
    
    

    void compare(Mappings m1, Mappings m2) {
        CompareMappings cp = new CompareMappings(m1, m2);
        cp.process();
    }
    
        // compare last and previous result
    void compare2() {
        show("Compare");
        MyJPanelQuery previous = mainFrame.getPreviousQueryPanel();
        if (previous == null) {
            show("Previous Query Result not found");
            return;
        }
        compare(getTable(), previous.getTable());
    }
    
    
    void compare(TableModel fst, TableModel snd) {
        boolean found = false;
        for (int i = 0; i < fst.getRowCount() && i < snd.getRowCount(); i++) {
            for (int j = 0; j < fst.getColumnCount() && j < snd.getColumnCount(); j++) {
                String v1 = (String) fst.getValueAt(i, j);
                String v2 = (String) snd.getValueAt(i, j);
                
                if (! v1.equals(v2)) {
                    found = true;
                    show("Different value at: (row=%s, var=%s): %s != %s", i, fst.getColumnName(j), v1, v2);
                }
            }
        }
        
        if (fst.getRowCount() != snd.getRowCount()) {
            show("Different size: %s vs %s", fst.getRowCount(), snd.getRowCount());
        }
        else if (!found) {
            show("No difference found");
        }
    }
    
    void show(String mes, Object... obj) {
        System.out.println(String.format(mes, obj));
    }

    void setCurrent(Exec e) {
        current = e;
    }
    
    Exec getCurrent() {
        return current;
    }
    
    public void exec(MainFrame frame, String query) {
        logger.info("Simple Exec");
        Exec exec = new Exec(frame, query, false); 
        exec.process();
    }

    //getteurs et setteurs utiles
    public void setJPanel1(JPanel pane_query) {
        this.paneQuery = pane_query;
    }

    private JPanel getPaneQuery() {
        return paneQuery;
    }

    public JTextArea getTextAreaXMLResult() {
        return textAreaXMLResult;
    }

    public JTextArea getTextArea() {
        return textAreaXMLResult;
    }

    public JButton getButtonTKgram() {
        return buttonTKgram;
    }

    private void checkLines(JTextComponent textComponentInput, JTextComponent textComponentOutput) {
        String text = "";
        Document doc2 = textComponentInput.getDocument();
        int lineCount = doc2.getDefaultRootElement().getElementCount();
        for (int i = 1; i < lineCount + 1; i++) {
            text += String.valueOf(i) + "\n";
        }
        textComponentOutput.setText(text);
    }

    static public void main(String[] args) throws InterruptedException {
        JFrame frame = new JFrame("MyJPanelQuery");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        MyJPanelQuery panelQuery = new MyJPanelQuery();
        panelQuery.setPreferredSize(new Dimension(175, 100));
        frame.getContentPane().add(panelQuery, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }

    public JTextArea getTextAreaLines() {
        return sparqlQueryEditor.getTextAreaLines();
    }

    public boolean isDisplayLink() {
        return displayLink;
    }

    public void setDisplayLink(boolean displayLink) {
        this.displayLink = displayLink;
    }
    
    /**
     * Query result accessible by xt:mappings()
     * Use case: Previous Query Result has been Copied (Copy button)
     * here to be processed by xt:mappings()
     */
    Mappings getQueryMappings() {
        //return mainFrame.getPreviousMappings();
        return getMappings();
    }

    public Mappings getMappings() {
        return mappings;
    }

    public void setMappings(Mappings mappings) {
        this.mappings = mappings;
    }

    public QueryExec getQueryExec() {
        if (getCurrent() == null) {
            return null;
        }
        return getCurrent().getQueryExec();
    }
   
}
