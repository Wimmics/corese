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

import org.miv.graphstream.algorithm.layout2.elasticbox.ElasticBox;
import org.miv.graphstream.graph.Node;
import org.miv.graphstream.graph.implementations.MultiGraph;
import org.miv.graphstream.ui.swing.SwingGraphRenderer;
import org.miv.graphstream.ui.swing.SwingGraphViewer;


import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.api.IEngine;
import fr.inria.acacia.corese.api.IResult;
import fr.inria.acacia.corese.api.IResultValue;
import fr.inria.acacia.corese.api.IResults;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.exceptions.QueryLexicalException;
import fr.inria.acacia.corese.exceptions.QuerySemanticException;
import fr.inria.acacia.corese.exceptions.QuerySyntaxException;
import fr.inria.acacia.corese.gui.core.MainFrame;
import fr.inria.edelweiss.kgengine.QueryResults;
import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import javax.swing.JFrame;

/**
 * Onglet Query avec tout ce qu'il contient.
 * @author saguilel
 * @author Maraninchi jerôme
 */
public final class MyJPanelQuery extends JPanel {

    private static final long serialVersionUID = 1L;
    //Boutton du panneau Query
    private JButton buttonRun, buttonTKgram, buttonProve;
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
                    excepCatch = false;
                } catch (Exception e1) {
                    areaException.setText(e1.getMessage());
                    areaException.setEditable(false);
                    areaException.setForeground(Color.red);
                    JOptionPane.showMessageDialog(null, areaException, "Error Syntax", JOptionPane.WARNING_MESSAGE);
                    excepCatch = true;
                } catch (org.miv.graphstream.ui.graphicGraph.stylesheet.parser.TokenMgrError e1) {
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
         * DocumentListener sur le textPaneStyleGraph
         * Listener sur le textPaneStyleGraph afin d'actualiser le nombre de lignes
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
         * FocusListener sur le textPaneStyleGraph
         * Permet d'actualiser le compteur de ligne lors d'un FocusGained et FocusLost
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
        /** Bouttons et leurs actions **/
        //Lancer une requête
        buttonRun.setText("Query ");
        buttonProve.setText("Prove");
        buttonTKgram.setText("Trace");
        buttonTKgram.addActionListener(sparqlQueryEditor);

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
         * ActionListener sur le bouton DefaultStylesheet
         * Permet d'attribuer un style par défaut défini au niveau de la MainFrame au graphe
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

    public void display(IResults l_Results, MainFrame coreseFrame) {
        // On affiche la version XML du résultat dans l'onglet XML
        resultXML = l_Results.toString();
        textAreaXMLResult.setText(resultXML.toString());

        int num = 0;
        graph = new MultiGraph(false, true);
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

        for (IResult res : l_Results) {
            DefaultMutableTreeNode x = new DefaultMutableTreeNode("result " + i);
            // Pour chaque variable du résultat on ajoute une feuille contenant le nom de la variable et sa valeur
            for (String var : l_Results.getVariables()) {
                if (res.getResultValues(var) != null) {
                    for (IResultValue val : res.getResultValues(var)) {
                        x.add(new DefaultMutableTreeNode(var));
                        x.add(new DefaultMutableTreeNode(val.getDatatypeValue().toString()));
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


        if (l_Results.isConstruct() || l_Results.isDescribe()) {

            graph.addNode(temp).addAttribute("ui.style", "color:white;");

            String sujet = null;
            String objet = null;

            Iterable<Entity> edges = getEdges(l_Results);

            for (Entity ent : edges) {
                Edge edge = ent.getEdge();
                sujetUri = edge.getNode(0).getLabel();
                objetUri = edge.getNode(1).getLabel();

//				if (edge instanceof CoreseRelation)
//					predicat = ((CoreseRelation)edge).getCType().toString();
//				else
                predicat = getLabel(edge.getEdgeNode().getLabel());

                sujet = getLabel(sujetUri);
                objet = getLabel(objetUri);

                if (find(sujetUri, graph.getNodeIterator()) == null) {
                    graph.addNode(sujetUri).addAttribute("label", sujet);
                    graph.getNode(sujetUri).setAttribute("ui.class", sujet);
                    if (edge.getNode(0).isBlank()) {
                        graph.getNode(sujetUri).setAttribute("ui.class", "BlankNode");
                    }
                    graph.addEdge("temp" + num, sujetUri, temp);
                    graph.getEdge("temp" + num).addAttribute("ui.style", "width:0;edge-style:dashes;color:white;");

                }
                num++;

                //Lors de l'ajout d'un Noeud (ou pas) Objet
                if (find(objetUri, graph.getNodeIterator()) == null) {
                    graph.addNode(objetUri).addAttribute("label", objet);
                    graph.getNode(objetUri).setAttribute("ui.class", objet);
                    if (edge.getNode(1).isBlank()) {
                        graph.getNode(objetUri).setAttribute("ui.class", "BlankNode");
                    }
                    IDatatype dt = (IDatatype) edge.getNode(1).getValue();
                    if (dt.isLiteral()) {
                        graph.getNode(objetUri).setAttribute("ui.class", "Literal");
                    }

                    graph.addEdge("temp" + num, objetUri, temp);
                    //graph.getEdge("temp"+num).addAttribute("label", "http://www.inria.fr/acacia/corese#result");
                    graph.getEdge("temp" + num).addAttribute("ui.style", "width:0;edge-style:dashes;color:white;");
                }
                num++;

                graph.addEdge("edge" + num, sujetUri, objetUri, true);
                graph.getEdge("edge" + num).addAttribute("label", predicat);
                graph.getEdge("edge" + num).addAttribute("ui.class", predicat);

            }

            textPaneStyleGraph.setText(stylesheet);
            graph.addAttribute("ui.stylesheet", stylesheet);

            //permet de visualiser correctement le graphe dans l'onglet de Corese
            ElasticBox eb = new ElasticBox();
            eb.setForce((float) 0.1);
            SwingGraphViewer sgv = new SwingGraphViewer(graph, eb, true, true);
            SwingGraphRenderer sgr = sgv.getRenderer();

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
                    // Lance d'abbord la validation
                    if (!coreseFrame.isKgram()) {
                        engine.SPARQLValidate(sparqlQueryEditor.getTextPaneQuery().getText());
                    }
                    try {
                        if (ev.getSource() == buttonProve) {
                            l_Results = engine.SPARQLProve(sparqlQueryEditor.getTextPaneQuery().getText());
                        } // Lance la requête
                        else if (!coreseFrame.isKgram()) {
                            l_Results = engine.SPARQLQuery(sparqlQueryEditor.getTextPaneQuery().getText());
                        } else {
                            Exec exec = new Exec(coreseFrame, sparqlQueryEditor.getTextPaneQuery().getText(), isTrace);
                            //l_Results = exec.query();
                            exec.process();

                            //Permet de passer a true toutes les options du trace KGram
                            for (int i = 0; i < coreseFrame.getListCheckbox().size(); i++) {
                                coreseFrame.getListCheckbox().get(i).setEnabled(true);
                            }
                            for (int i = 0; i < coreseFrame.getListJMenuItems().size(); i++) {
                                coreseFrame.getListJMenuItems().get(i).setEnabled(true);
                            }


                        }

                        // Si le résultat existe
                        if (l_Results != null) {
                            display(l_Results, coreseFrame);
                        } else if (!coreseFrame.isKgram()) {
                            textAreaXMLResult.setText(coreseFrame.getMyCapturer().getContent()); // display errors
                            //coreseFrame.getCapturer()
                        }
                    } catch (EngineException e) {
                        e.printStackTrace();
                        textAreaXMLResult.setText(coreseFrame.getMyCapturer().getContent() + e.getMessage()); // display errors
                    }
                } catch (QuerySyntaxException e) {
                    tabbedPaneResults.setSelectedIndex(2);
                    l_message += "____ Syntax error\n" + e.getMessage() + "________\n";
                } catch (QueryLexicalException e) {
                    tabbedPaneResults.setSelectedIndex(2);
                    l_message += "____ Lexical error\n" + e.getMessage() + "________\n";
                } catch (QuerySemanticException e) {
                    tabbedPaneResults.setSelectedIndex(2);
                    l_message += "____ Semantic error\n" + e.getMessage() + "________\n";
                } catch (EngineException e) {
                    e.printStackTrace();
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
