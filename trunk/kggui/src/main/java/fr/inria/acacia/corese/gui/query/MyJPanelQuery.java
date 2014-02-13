package fr.inria.acacia.corese.gui.query;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
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

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.api.IEngine;
import fr.inria.acacia.corese.api.IResult;
import fr.inria.acacia.corese.api.IResultValue;
import fr.inria.acacia.corese.api.IResults;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.gui.core.MainFrame;
import fr.inria.acacia.corese.triple.parser.ASTQuery;
import fr.inria.edelweiss.kgengine.QueryResults;
import fr.inria.edelweiss.kgenv.parser.Pragma;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.ExpType;
import fr.inria.edelweiss.kgram.core.Mapping;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgram.core.Query;
import fr.inria.edelweiss.kgtool.load.Load;
import fr.inria.edelweiss.kgtool.load.LoadException;
import fr.inria.edelweiss.kgtool.print.ResultFormat;
import fr.inria.edelweiss.kgtool.print.XMLFormat;
import fr.inria.edelweiss.kgtool.util.SPINProcess;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

import javax.swing.JFrame;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.graphicGraph.stylesheet.StyleSheet;
import org.graphstream.ui.layout.springbox.implementations.LinLog;
import org.graphstream.ui.swingViewer.View;
import org.graphstream.ui.swingViewer.Viewer;
import org.graphstream.util.parser.TokenMgrError;

/**
 * Onglet Query avec tout ce qu'il contient.
 *
 * @author saguilel
 * @author Maraninchi jerôme
 */
public final class MyJPanelQuery extends JPanel {

    private static final long serialVersionUID = 1L;
    //Boutton du panneau Query
    private JButton buttonRun, buttonToSPIN, buttonToSPARQL, buttonTKgram, buttonProve;
    private JButton buttonSearch;
    private JButton buttonRefreshStyle, buttonDefaultStyle;
    //panneau de la newQuery
    private JPanel paneQuery;
    //Ajoute le scroll pour les différents panneaux
    private JScrollPane scrollPaneTreeResult;
    private JScrollPane scrollPaneXMLResult;
    private JScrollPane scrollPaneValidation;
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
    private MainFrame mainFrame;
    private static final String KGSTYLE = ExpType.KGRAM + "style";
    private static final String KGGRAPH = Pragma.GRAPH;

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
        textAreaXMLResult = new JTextArea();
        scrollPaneValidation = new JScrollPane();
        textPaneValidation = new JTextPane();
        textPaneStyleGraph = new JTextPane();

        //compteur de ligne pour la feuille de style de graphe
        textAreaLinesGraph = new JTextArea();
        textAreaLinesGraph.setFont(new Font("Sanserif", Font.PLAIN, 12));
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
                } catch (TokenMgrError e1) {
                    areaException.setText(e1.getMessage());
                    areaException.setEditable(false);
                    areaException.setForeground(Color.red);
                    JOptionPane.showMessageDialog(null, areaException, "Error Syntax", JOptionPane.WARNING_MESSAGE);
                    excepCatch = true;
                }
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
        tabbedPaneResults.addTab("XML", scrollPaneXMLResult);

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
        hSeq2.addComponent(buttonToSPIN);
        hSeq2.addComponent(buttonToSPARQL);
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

        vParallel2.addComponent(buttonRun);
        vParallel2.addComponent(buttonToSPIN);
        vParallel2.addComponent(buttonToSPARQL);

        vParallel2.addComponent(buttonProve);
        vParallel2.addComponent(buttonTKgram);

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
        buttonToSPIN.addActionListener(l_RunListener);
        buttonToSPARQL.addActionListener(l_RunListener);

        buttonProve.addActionListener(l_RunListener);

        ActionListener kt_RunListener = createListener(coreseFrame, true);
        buttonTKgram.addActionListener(kt_RunListener);
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

    private Iterable<Entity> getEdges(IResults l) {
        if (l instanceof QueryResults) {
            // kgraph
            QueryResults qr = (QueryResults) l;
            return qr.getEdges();
        }
        return new ArrayList<Entity>();
    }

    fr.inria.edelweiss.kgraph.core.Graph getGraph(IResults l) {
        if (l instanceof QueryResults) {
            QueryResults qr = (QueryResults) l;
            return qr.getGraph();
        }
        return fr.inria.edelweiss.kgraph.core.Graph.create();
    }

    private String getLabel(String name) {
        int ind = name.lastIndexOf("#");
        if (ind == -1) {
            ind = name.lastIndexOf("/");
        }
        if (ind == -1 || ind == name.length() - 1) {
            return name;
        }
        return name.substring(ind + 1);
    }

    String toString(Mappings map) {
        Query q = map.getQuery();
        ASTQuery ast = (ASTQuery) map.getQuery().getAST();
        if (ast.isSPARQLQuery()) {
            // RDF or XML
            return ResultFormat.create(map).toString();
        } else {
            // XML bindings only (do not display the whole graph)
            return XMLFormat.create(map).toString();
        }
    }

    public void display(IResults l_Results, MainFrame coreseFrame) {
        if (l_Results == null) {
            // go to XML for error message
            tabbedPaneResults.setSelectedIndex(1);
            return;
        }

        boolean oneValue = true;
        Mappings map = null;

        if (l_Results instanceof QueryResults) {
            // in old Corese, there may be several values for one variable
            // use case: group by ?x and pragma {kg:kgram kg:list true}
            // this is deprecated
            QueryResults qr = (QueryResults) l_Results;
            map = qr.getMappings();
            oneValue = !map.getQuery().isListGroup();
            resultXML = toString(map);
        } else {
            // On affiche la version XML du résultat dans l'onglet XML
            resultXML = l_Results.toString();
        }

        textAreaXMLResult.setText(resultXML.toString());

        // On affiche la version en arbre du résultat dans l'onglet Tree
        // crée un arbre de racine "root"
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        DefaultTreeModel treeModel = new DefaultTreeModel(root);
        treeResult = new JTree(treeModel);
        treeResult.setShowsRootHandles(true);
        // Pour chaque resultat de l_Results on crée un noeud "result"
        //if (l_Results.size()<1000)      

        int i = 1;
        if (oneValue) {
            display(root, map);
        } else {
            display(root, l_Results);
        }

        TreePath myPath = treeResult.getPathForRow(0);
        treeResult.expandPath(myPath);
        scrollPaneTreeResult.setViewportView(treeResult);

        //pointe sur le résultat XML
        tabbedPaneResults.setSelectedIndex(1);

        if (l_Results.isConstruct() || l_Results.isDescribe()) {
            displayGraph(getGraph(l_Results));
        } // draft
        else if (map.getQuery().isTemplate() && map.getQuery().isPragma(KGGRAPH)) {
            display(map);
        }

    }

    /**
     * template return turtle graph description display as graph
     */
    void display(Mappings map) {
        fr.inria.edelweiss.kgram.api.core.Node res = map.getTemplateResult();
        if (res != null) {
            fr.inria.edelweiss.kgraph.core.Graph g = fr.inria.edelweiss.kgraph.core.Graph.create();
            Load ld = Load.create(g);
            String str = res.getLabel();
            try {
                ld.load(new ByteArrayInputStream(str.getBytes("UTF-8")), "turtle.ttl");
                displayGraph(g);
            } catch (LoadException ex) {
                java.util.logging.Logger.getLogger(MyJPanelQuery.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedEncodingException ex) {
                java.util.logging.Logger.getLogger(MyJPanelQuery.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    void displayGraph(fr.inria.edelweiss.kgraph.core.Graph g) {

        int num = 0;
        String sujetUri, predicat, objetUri, temp = "http://www.inria.fr/acacia/corese#Results";

        graph = new MultiGraph(g.getName(), false, true);
//            graph.addNode(temp).addAttribute("ui.style", "fill-color:white;");

        String sujet = null;
        String objet = null;

        Iterable<Entity> edges = g.getEdges();

        for (Entity ent : edges) {
            fr.inria.edelweiss.kgram.api.core.Edge edge = ent.getEdge();
            sujetUri = edge.getNode(0).getLabel();
            objetUri = edge.getNode(1).getLabel();

            predicat = getLabel(edge.getEdgeNode().getLabel());

            sujet = getLabel(sujetUri);
            objet = getLabel(objetUri);

            Node gsub = graph.getNode(sujetUri);
            // if (find(sujetUri, graph.getNodeIterator()) == null) {
            if (gsub == null) {
                gsub = graph.addNode(sujetUri);
                gsub.addAttribute("label", sujet);
//                gsub.addAttribute("ui.style", "fill-color:lightblue;size-mode:dyn-size;shape:rounded-box;");
                //graph.getNode(sujetUri)
//                    gsub.setAttribute("ui.class", sujet);
                if (edge.getNode(0).isBlank()) {
                    //graph.getNode(sujetUri)
                    gsub.setAttribute("ui.class", "Blank");
                }
//                    Edge ee = graph.addEdge("temp" + num, sujetUri, temp);
//                    ee.addAttribute("ui.style", "size:0;edge-style:dashes;fill-color:white;");

            }
            num++;

            if (isStyle(edge)) {
                // draft style
                // xxx kg:style ex:Wimmics
                // it is a fake edge, do not create it
//                gsub.setAttribute("ui.class", objet);
            } else {
                Node gobj = graph.getNode(objetUri);
                //if (find(objetUri, graph.getNodeIterator()) == null) {
                if (gobj == null) {
                    gobj = graph.addNode(objetUri);
                    gobj.addAttribute("label", objet);
//                    gobj.setAttribute("ui.class", objet);
                    if (edge.getNode(1).isBlank()) {
                        gobj.setAttribute("ui.class", "Blank");
                    }
                    IDatatype dt = (IDatatype) edge.getNode(1).getValue();
                    if (dt.isLiteral()) {
                        gobj.setAttribute("ui.class", "Literal");
                    }

//                        Edge ee = graph.addEdge("temp" + num, objetUri, temp);
//                        ee.addAttribute("ui.style", "size:0;edge-style:dashes;fill-color:white;");
                }
                num++;

                Edge ee = graph.addEdge("edge" + num, sujetUri, objetUri, true);
                ee.addAttribute("label", predicat);
//                ee.addAttribute("ui.class", predicat);
            }
        }

        textPaneStyleGraph.setText(stylesheet);
        graph.addAttribute("ui.stylesheet", stylesheet);
        graph.addAttribute("ui.antialias");

        //permet de visualiser correctement le graphe dans l'onglet de Corese
        LinLog lLayout = new LinLog();
        lLayout.setQuality(0.9);
        lLayout.setGravityFactor(0.9);

        Viewer sgv = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_SWING_THREAD);
        sgv.enableAutoLayout(lLayout);
        View sgr = sgv.addDefaultView(false);
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
        tabbedPaneResults.setSelectedIndex(0);

    }

    private boolean isStyle(fr.inria.edelweiss.kgram.api.core.Edge edge) {
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

            for (fr.inria.edelweiss.kgram.api.core.Node var : map.getSelect()) {
                fr.inria.edelweiss.kgram.api.core.Node node = res.getNode(var);
                if (node != null) {
                    x.add(new DefaultMutableTreeNode(var.getLabel()));
                    x.add(new DefaultMutableTreeNode(node.getValue().toString()));
                    root.add(x);
                }
            }
            i++;
        }
    }

    /**
     * Display result using IResults
     *
     * @deprecated
     */
    void display(DefaultMutableTreeNode root, IResults l_Results) {
        int i = 1;
        for (IResult res : l_Results) {
            DefaultMutableTreeNode x = new DefaultMutableTreeNode("result " + i);
            // Pour chaque variable du résultat on ajoute une feuille contenant le nom de la variable et sa valeur

            for (String var : l_Results.getVariables()) {
                IResultValue[] lres = res.getResultValues(var);
                if (lres != null) {

                    for (IResultValue val : lres) {
                        x.add(new DefaultMutableTreeNode(var));
                        x.add(new DefaultMutableTreeNode(val.getDatatypeValue().toString()));
                        root.add(x);
                    }
                }
            }
            i++;
        }
    }

    private ActionListener createListener(final MainFrame coreseFrame, final boolean isTrace) {

        return new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                textAreaXMLResult.setText("");
                IResults l_Results = null;
                scrollPaneTreeResult.setViewportView(new JPanel());
                scrollPaneTreeResult.setRowHeaderView(new JPanel());

                String l_message = new String("Parsing:\n");
                IEngine engine = coreseFrame.getMyCorese();

                try {
                    String query = sparqlQueryEditor.getTextPaneQuery().getText();

                    if (ev.getSource() == buttonToSPARQL) {
                        SPINProcess spin = SPINProcess.create();
                        String str = spin.toSparql(query);
                        coreseFrame.getPanel().getTextArea().setText(str);
                        tabbedPaneResults.setSelectedIndex(1);
                    } else if (ev.getSource() == buttonToSPIN) {
                        SPINProcess spin = SPINProcess.create();
                        String str = spin.toSpin(query);
                        coreseFrame.getPanel().getTextArea().setText(str);
                        tabbedPaneResults.setSelectedIndex(1);
                    } else if (ev.getSource() == buttonProve) {
                        l_Results = engine.SPARQLProve(query);
                        if (l_Results != null) {
                            display(l_Results, coreseFrame);
                        }
                    } else {
                        Exec exec = new Exec(coreseFrame, query, isTrace);
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
