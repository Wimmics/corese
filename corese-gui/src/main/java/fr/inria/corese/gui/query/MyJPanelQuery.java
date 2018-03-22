package fr.inria.corese.gui.query;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
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
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.gui.core.MainFrame;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.compiler.parser.Pragma;
import fr.inria.corese.kgram.api.core.Entity;
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
import fr.inria.corese.core.util.SPINProcess;
import java.util.List;
import org.apache.logging.log4j.Level;


import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
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
    static final String SERVICE = ""; //"@service <http://fr.dbpedia.org/sparql>";
    static final String NL = System.getProperty("line.separator");
    static int FontSize = SparqlQueryEditor.FontSize;
    
    static final int GRAPH_PANEL    = 0;
    static final int XML_PANEL      = 1;
    static final int TABLE_PANEL    = 2;

    int maxres = 1000000;
    
    //Boutton du panneau Query
    private JButton buttonRun, buttonValidate, buttonToSPIN, buttonToSPARQL, buttonTKgram, buttonProve;
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
    private CharSequence resultXML = "";
    private SparqlQueryEditor sparqlQueryEditor;
    private JTextPane serviceEditor;
    private MainFrame mainFrame;
    private static final String KGSTYLE = ExpType.KGRAM + "style";
    private static final String KGGRAPH = Pragma.GRAPH;
    private static final Logger logger = LogManager.getLogger(MyJPanelQuery.class.getName());

    public MyJPanelQuery() {
        super();
        initComponents();
        setQuery("empty request");
    }

    public MyJPanelQuery(final MainFrame coreseFrame) {
        super();
        initComponents();
        mainFrame = coreseFrame;
        installListenersOnMainFrame(coreseFrame);
        setQuery(coreseFrame.getTextQuery());
        stylesheet = coreseFrame.getDefaultStylesheet();
    }

    private void initComponents() {
      
        paneQuery = new JPanel(new BorderLayout());
        paneQuery.setName("paneQuery");
        setLayout(new BorderLayout(5, 5));
        add(paneQuery);

        buttonRun = new JButton();
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
        buttonRun.setText("Query");
        buttonValidate.setText("Validate");
        buttonToSPIN.setText("to SPIN");
        buttonToSPARQL.setText("to SPARQL");
        buttonProve.setText("Prove");
        buttonTKgram.setText("Trace");
        //OC: buttonTKgram.addActionListener(sparqlQueryEditor);

        //Pour chercher un string dans la fen�tre de r�sultat XML
        buttonSearch.setText("Search");

        ActionListener searchListener = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                textAreaXMLResult.setText(resultXML.toString());
                String toSearch = "";
                String message = "";

                CharSequence temps = textAreaXMLResult.getText();
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
        textAreaXMLResult.setText(resultXML.toString());
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
        hSeq2.addComponent(buttonValidate);
        hSeq2.addComponent(buttonToSPIN);
        hSeq2.addComponent(buttonToSPARQL);
//        hSeq2.addComponent(buttonProve);
//        hSeq2.addComponent(buttonTKgram);
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
        vParallel2.addComponent(buttonValidate);
        vParallel2.addComponent(buttonToSPIN);
        vParallel2.addComponent(buttonToSPARQL);

//        vParallel2.addComponent(buttonProve);
//        vParallel2.addComponent(buttonTKgram);

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
    
     public String getSparqlService() {
        return serviceEditor.getText();
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

        coreseFrame.getUndo().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    sparqlQueryEditor.getUndoManager().undo();
                    sparqlQueryEditor.requestFocus();
                } catch (CannotUndoException ex) {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        });

        coreseFrame.getRedo().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    sparqlQueryEditor.getUndoManager().redo();
                    sparqlQueryEditor.requestFocus();
                } catch (CannotRedoException ex) {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        });

        ActionListener l_RunListener = createListener(coreseFrame, false);
        buttonRun.addActionListener(l_RunListener);
        buttonValidate.addActionListener(l_RunListener);
        buttonToSPIN.addActionListener(l_RunListener);
        buttonToSPARQL.addActionListener(l_RunListener);

//        buttonProve.addActionListener(l_RunListener);

//        ActionListener kt_RunListener = createListener(coreseFrame, true);
//        buttonTKgram.addActionListener(kt_RunListener);
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
        IDatatype dt = (IDatatype) n.getValue();
        if (dt.isURI()){
            return nsm.toPrefix(n.getLabel());
        }
        else {
            return n.getLabel();
        }
    }

    String toString(Mappings map) {
        Query q = map.getQuery();
        ASTQuery ast = (ASTQuery) map.getQuery().getAST();
        if (ast.isSPARQLQuery()) {
            if (map.getGraph() != null) {
                return graphToString(map);
            } else {
                // RDF or XML
                ResultFormat rf = ResultFormat.create(map);
                rf.setNbResult(maxres);
                String str = rf.toString();
                if (str == "" && ast.getErrors() != null) {
                    return ast.getErrorString();
                }
                return str;
            }
        } else {
            // XML bindings only (do not display the whole graph)
            return XMLFormat.create(map).toString();
        }
    }
    
    String graphToString(Mappings map) {
        Graph g = (Graph) map.getGraph();
        Transformer t = Transformer.create(g, Transformer.TURTLE);
        return t.transform();
    }

    void fillTable(Mappings map) {
        Query q = map.getQuery();
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
            String columnName = var.getLabel();
            //System.out.println(sv);
            String[] colmunData = new String[size];

            for (int j = 0; j < map.size(); j++) {
                if (j >= maxres){
                    logger.warn("Stop display after " + maxres + " results out of " + map.size());
                    break;
                }
                Mapping m = map.get(j);
                fr.inria.corese.kgram.api.core.Node value = m.getNode(columnName);
                
                if (value != null) {
                    IDatatype dt = (IDatatype) value.getValue();
                    colmunData[j] = pretty(dt);
                }
            }
            model.addColumn(columnName, colmunData);
        }

        this.tableResults.setModel(model);
    }
    
    String pretty(IDatatype dt) {
        if (dt.isList()) {
            return dt.getValues().toString();
        } else if (dt.isPointer()) {
            return dt.getPointerObject().toString();
        } else if (dt.isLiteral()) { 
            if (dt.getCode() == IDatatype.STRING || (dt.getCode() == IDatatype.LITERAL && ! dt.hasLang())){
                return dt.stringValue();
            }
            return dt.toString(); 
        } 
        else if (dt.isURI()){
            return dt.toString();
        }
        else {
            return dt.getLabel();
        }
    }

    void display(Mappings map, MainFrame coreseFrame) {
        if (map == null) {
                    // go to XML for error message
                    tabbedPaneResults.setSelectedIndex(XML_PANEL);
                    return;
        }
        Query q = map.getQuery();
        ASTQuery ast = (ASTQuery) q.getAST();
        boolean oneValue = !map.getQuery().isListGroup();
        resultXML = toString(map);
        textAreaXMLResult.setText(resultXML.toString());

        // On affiche la version en arbre du résultat dans l'onglet Tree
        // crée un arbre de racine "root"
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        DefaultTreeModel treeModel = new DefaultTreeModel(root);
        treeResult = new JTree(treeModel);
        treeResult.setShowsRootHandles(true);

        //display(root, map);

        TreePath myPath = treeResult.getPathForRow(0);
        treeResult.expandPath(myPath);
        scrollPaneTreeResult.setViewportView(treeResult);

        //afficher les resultats dans une tableau sauf pour les templates
        if (q.isTemplate() || ast.isAsk() || ast.getErrors() != null){
            tabbedPaneResults.setSelectedIndex(XML_PANEL);
        } else{
            this.fillTable(map);
            tabbedPaneResults.setSelectedIndex(TABLE_PANEL);
        }

        if (q.isConstruct()) {
            displayGraph((Graph) map.getGraph(), ast.getNSM());
        } // draft
        else if (map.getQuery().isTemplate() && map.getQuery().isPragma(KGGRAPH)) {
            display(map, ast.getNSM());
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

        for (Entity ent : g.getEdges()) {
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
                if (n1.isBlank()) {
                    gsub.setAttribute("ui.class", "Blank");
                }
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
                    if (n2.isBlank()) {
                        gobj.setAttribute("ui.class", "Blank");
                    }
                    IDatatype dt = (IDatatype) n2.getValue();
                    if (dt.isLiteral()) {
                        gobj.setAttribute("ui.class", "Literal");
                    }
                }
                num++;

                Edge ee = graph.addEdge("edge" + num, sujetUri, objetUri, true);
                ee.addAttribute("label", predicat);
            }
        }

        return graph;
    }


    private boolean isStyle(fr.inria.corese.kgram.api.core.Edge edge) {
        return edge.getLabel().equals(KGSTYLE);
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

    private ActionListener createListener(final MainFrame coreseFrame, final boolean isTrace) {

        return new ActionListener() {            
            @Override
            public void actionPerformed(ActionEvent ev) {
                textAreaXMLResult.setText("");
                Mappings l_Results = null;
                scrollPaneTreeResult.setViewportView(new JPanel());
                scrollPaneTreeResult.setRowHeaderView(new JPanel());

                String l_message = new String("Parsing:\n");
                GraphEngine engine = coreseFrame.getMyCorese();

                try {
                    String service = getSparqlService();
                    String query = sparqlQueryEditor.getTextPaneQuery().getText();
                    if (! service.isEmpty()){
                        query = service + NL + query;
                    }
                    if (ev.getSource() == buttonToSPARQL) {
                        SPINProcess spin = SPINProcess.create();
                        String str = spin.toSparql(query);
                        coreseFrame.getPanel().getTextArea().setText(str);
                        tabbedPaneResults.setSelectedIndex(XML_PANEL);
                    } else if (ev.getSource() == buttonToSPIN) {
                        SPINProcess spin = SPINProcess.create();
                        String str = spin.toSpin(query);
                        coreseFrame.getPanel().getTextArea().setText(str);
                        tabbedPaneResults.setSelectedIndex(XML_PANEL);
//                    } else if (ev.getSource() == buttonProve) {
//                        l_Results = engine.SPARQLProve(query);
//                        if (l_Results != null) {
//                            display(l_Results, coreseFrame);
//                        }
                    } else if (ev.getSource() == buttonRun || ev.getSource() == buttonValidate) {
                        // buttonRun
                        Exec exec = new Exec(coreseFrame, query, isTrace);
                        exec.setValidate(ev.getSource() == buttonValidate);
                        exec.process();

                        //Permet de passer a true toutes les options du trace KGram
                        for (int i = 0; i < coreseFrame.getListCheckbox().size(); i++) {
                            coreseFrame.getListCheckbox().get(i).setEnabled(true);
                        }
                        for (int i = 0; i < coreseFrame.getListJMenuItems().size(); i++) {
                            coreseFrame.getListJMenuItems().get(i).setEnabled(true);
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
    
    public void exec(MainFrame frame, String query) {
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
}
