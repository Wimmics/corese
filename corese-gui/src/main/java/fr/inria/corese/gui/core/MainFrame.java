package fr.inria.corese.gui.core;

import fr.inria.corese.gui.query.MyJPanelQuery;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.gui.event.MyEvalListener;
import fr.inria.corese.gui.query.Buffer;
import fr.inria.corese.gui.query.GraphEngine;
import fr.inria.corese.core.workflow.Data;
import fr.inria.corese.core.workflow.WorkflowParser;
import fr.inria.corese.core.workflow.SemanticWorkflow;
import fr.inria.corese.core.workflow.WorkflowProcess;
import fr.inria.corese.kgram.event.Event;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.transform.TemplatePrinter;
import fr.inria.corese.sparql.triple.parser.Access;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.PatternLayout;

/**
 * Fenêtre principale, avec le conteneur d'onglets et le menu
 */
public class MainFrame extends JFrame implements ActionListener {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final int LOAD = 1;
    private static final String TITLE = "Corese 4.1 - Wimmics INRIA I3S - 2019-04-01";
    // On déclare notre conteneur d'onglets
    protected static JTabbedPane conteneurOnglets;
    // Compteur pour le nombre d'onglets query créés
    private ArrayList<Integer> nbreTab = new ArrayList<Integer>();
    private String lCurrentPath = "user/home";
    private String lPath;
    private String fileName = "";
    //Variable true ou false pour déterminer le mode Kgram ou Corese
    private boolean isKgram = true;
    boolean trace = false;
    // Pour le menu
    private JMenuItem loadRDF;
    private JMenuItem loadRDFs;
    private JMenuItem loadQuery;
    private JMenuItem execWorkflow, loadWorkflow, loadRunWorkflow;
    private JMenuItem loadRule;
    private JMenuItem loadStyle;
    private JMenuItem cpTransform;
    private JMenuItem saveQuery;
    private JMenuItem saveResult;
    private JMenuItem loadAndRunRule;
    private JMenuItem refresh;
    private JMenuItem copy;
    private JMenuItem cut;
    private JMenuItem paste;
    private JMenuItem undo;
    private JMenuItem redo;
    private JMenuItem duplicate;
    private JMenuItem duplicateFrom;
    private JMenuItem newQuery;
    private JMenuItem runRules, runRulesOpt;
    private JMenuItem reset;
    private ButtonGroup myRadio;
    private JRadioButton kgramBox;
    private JMenuItem apropos;
    private JMenuItem tuto;
    private JMenuItem doc;
    private JMenuItem comment;
    private JMenuItem help;
    private JMenuItem next;
    private JMenuItem complete;
    private JMenuItem forward;
    private JMenuItem map;
    private JMenuItem success;
    private JMenuItem quit;
    private JMenuItem iselect, igraph, iconstruct, iask, idescribe,
            iserviceCorese, iserviceDBpedia, ifederate,
            iinsert, iinsertdata, idelete, ideleteinsert,
            iturtle, irdfxml, ijson, itrig, ispin, iowl, itypecheck,
            ientailment, irule, isystem, iprovenance, iindex, ifunction, ical;
    HashMap<Object, String> itable;
    private JCheckBox checkBoxQuery;
    private JCheckBox checkBoxRule;
    private JCheckBox checkBoxVerbose;
    private JCheckBox checkBoxLoad;
    private JCheckBox cbrdfs, cbowlrl, cbowlrllite, cbtrace, cbnamed;
    private JMenuItem validate;
    //style correspondant au graphe
    private String defaultStylesheet, saveStylesheet;
    private ArrayList<JCheckBox> listCheckbox;	//list qui stocke les JCheckBoxs présentes sur le JPanelListener
    private ArrayList<JMenuItem> listJMenuItems;	//list qui stocke les Boutons présents sur le JPanelListener
    // Les 3 types d'onglets
    private ArrayList<MyJPanelQuery> monTabOnglet;
    private JPanel plus;
    private MyJPanelQuery current;
    private MyJPanelListener ongletListener;
    // Pour connaître l'onglet selectionné
    protected int selected;
    // Texte dans l'onglet requête
    private String textQuery;
    // Texte par défaut dans l'onglet requête
    private static final String DEFAULT_SELECT_QUERY = "select.rq";
    private static final String DEFAULT_GRAPH_QUERY = "graph.rq";
    private static final String DEFAULT_CONSTRUCT_QUERY = "construct.rq";
    private static final String DEFAULT_ASK_QUERY = "ask.rq";
    private static final String DEFAULT_DESCRIBE_QUERY = "describe.rq";
    private static final String DEFAULT_SERVICE_CORESE_QUERY = "servicecorese.rq";
    private static final String DEFAULT_SERVICE_DBPEDIA_QUERY = "servicedbpedia.rq";
    private static final String DEFAULT_INSERT_QUERY = "insert.rq";
    private static final String DEFAULT_INSERT_DATA_QUERY = "insertdata.rq";
    private static final String DEFAULT_DELETE_QUERY = "delete.rq";
    private static final String DEFAULT_DELETE_INSERT_QUERY = "deleteinsert.rq";
    private static final String DEFAULT_ENTAILMENT_QUERY = "entailment.rq";
    private static final String DEFAULT_RULE_QUERY = "rule.rq";
    private static final String DEFAULT_FUN_QUERY = "function.rq";
    private static final String DEFAULT_TEMPLATE_QUERY = "turtle.rq";
    private static final String DEFAULT_RDF_XML_QUERY = "rdfxml.rq";
    private static final String DEFAULT_TRIG_QUERY = "trig.rq";
    private static final String DEFAULT_OWL_QUERY = "owl.rq";
    private static final String DEFAULT_SPIN_QUERY = "spin.rq";
    private static final String DEFAULT_TYPECHECK_QUERY = "typecheck.rq";
    private static final String DEFAULT_SYSTEM_QUERY = "introspect.rq";
    private static final String DEFAULT_PROVENANCE_QUERY = "provenance.rq";
    private String defaultQuery = DEFAULT_SELECT_QUERY;
    private GraphEngine myCorese = null;
    private CaptureOutput myCapturer = null;
    private static final Logger LOGGER = LogManager.getLogger(MainFrame.class.getName());
    private MyEvalListener el;
    Buffer buffer;
    private static final String STYLE = "/style/";
    private static final String QUERY = "/query/";
    private static final String STYLESHEET = "style.txt";
    private static final String TXT = ".txt";
    private static final String RQ = ".rq";
    private static final String URI_CORESE = "http://wimmics.inria.fr/corese";
    private static final String URI_GRAPHSTREAM = "http://graphstream-project.org/";
    int nbTabs = 0;
    
    Command cmd;

    static {
        // false: load files into named graphs
        // true:  load files into kg:default graph
        Load.setDefaultGraphValue(false);
    }

    /**
     * Crée la fenêtre principale, initialise Corese
     *
     * @param aCapturer
     * @param pPropertyPath
     */
    public MainFrame(CaptureOutput aCapturer, String[] args) {
        super();  
        cmd = new Command(args);
        this.setTitle(TITLE);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(900, 700);
        this.setMinimumSize(this.getSize());
        this.setLocationRelativeTo(null);
        this.setResizable(true);
        try {
            defaultQuery = read(QUERY + DEFAULT_SELECT_QUERY);
        } catch (LoadException ex) {
            LogManager.getLogger(MainFrame.class.getName()).log(Level.ERROR, "", ex);
        } catch (IOException ex) {
            LogManager.getLogger(MainFrame.class.getName()).log(Level.ERROR, "", ex);
        }

        //Initialise Corese
        myCapturer = aCapturer;
        setMyCoreseNewInstance(true);

        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

        //Initialise le menu
        initMenu();

        listCheckbox = new ArrayList<JCheckBox>();
        listJMenuItems = new ArrayList<JMenuItem>();

        //Création et ajout de notre conteneur d'onglets à la fenêtre
        conteneurOnglets = new JTabbedPane();
        this.getContentPane().add(conteneurOnglets, BorderLayout.CENTER);

        //Création et ajout des deux onglets "Listener" et "+"
        monTabOnglet = new ArrayList<MyJPanelQuery>();
        ongletListener = new MyJPanelListener(this);
        plus = new JPanel();
        conteneurOnglets.addTab("System", ongletListener);
        conteneurOnglets.addTab("+", plus);

        //Par défaut, l'onglet sélectionné est "listener"
        conteneurOnglets.setSelectedIndex(0);

        //S'applique lors d'un changement de selection d'onglet
        conteneurOnglets.addChangeListener(
                new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                // c est le composant sélectionné
                Component c = conteneurOnglets.getSelectedComponent();

                // selected est l'indice du composant sélectionné dans le conteneur d'onglets
                selected = conteneurOnglets.getSelectedIndex();

                //Si l'onglet sélectionné est un onglet Query il devient l'onglet "courant"
                if (c instanceof MyJPanelQuery) {
                    current = (MyJPanelQuery) c;

                    // Certaines options du menu deviennent utilisables
                    undo.setEnabled(true);
                    redo.setEnabled(true);
                    cut.setEnabled(true);
                    copy.setEnabled(true);
                    paste.setEnabled(true);
                    duplicate.setEnabled(true);
                    duplicateFrom.setEnabled(true);
                    comment.setEnabled(true);
                    saveQuery.setEnabled(true);
                    saveResult.setEnabled(true);

                    MyJPanelQuery temp = (MyJPanelQuery) getConteneurOnglets().getComponentAt(selected);

                    if (isKgram) {
                        temp.getButtonTKgram().setEnabled(true);
                    } else {
                        temp.getButtonTKgram().setEnabled(false);
                    }

                } // Sinon elles restent grisées et inutilisables
                else {
                    undo.setEnabled(false);
                    redo.setEnabled(false);
                    cut.setEnabled(false);
                    copy.setEnabled(false);
                    paste.setEnabled(false);
                    duplicate.setEnabled(false);
                    duplicateFrom.setEnabled(false);
                    comment.setEnabled(false);
                    saveQuery.setEnabled(false);
                    saveResult.setEnabled(false);
                }
                // Si l'onglet sélectionné est le "+" on crée un nouvel onglet Query
                if (c == plus) {
                    execPlus();
                }
            }
        });
        this.setVisible(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        appendMsg("Initialization:\n\n" + myCapturer.getContent() + "\n\n");

        //On remplit notre liste de JCheckBox
        listCheckbox.add(checkBoxLoad);
        listCheckbox.add(checkBoxQuery);
        listCheckbox.add(checkBoxRule);
        listCheckbox.add(checkBoxVerbose);
        for (int i = 0; i < listCheckbox.size(); i++) {
            listCheckbox.get(i).setEnabled(false);
        }

        //on remplit notre liste de Bouton
        listJMenuItems.add(help);
        listJMenuItems.add(map);
        listJMenuItems.add(next);
        listJMenuItems.add(forward);
        listJMenuItems.add(quit);
        listJMenuItems.add(complete);
        listJMenuItems.add(success);
        for (int i = 0; i < listJMenuItems.size(); i++) {
            listJMenuItems.get(i).setEnabled(false);
        }
        process(cmd);
    }

    void execPlus() {
        execPlus(defaultQuery);
    }

    void execPlus(String str) {
        // s : texte par défaut dans la requête
        textQuery = str;
        //Crée un nouvel onglet Query
        newQuery(str);
    }

    void setStyleSheet() {
        try {
            saveStylesheet = read(STYLE + STYLESHEET);
        } catch (LoadException ex) {
            LogManager.getLogger(MainFrame.class.getName()).log(Level.ERROR, "", ex);
        } catch (IOException ex) {
            LogManager.getLogger(MainFrame.class.getName()).log(Level.ERROR, "", ex);
        }
        defaultStylesheet = saveStylesheet;
    }

    /**
     * Affiche du texte dans le panel logs *
     */
    public void appendMsg(String msg) {
        final Document currentDoc = ongletListener.getTextPaneLogs().getDocument();
        try {
            currentDoc.insertString(ongletListener.getTextPaneLogs().getDocument().getLength(), msg, null);

            //Place l'ascenceur en bas à chaque ajout de texte
            ongletListener.getScrollPaneLog().revalidate();
            int length = ongletListener.getTextPaneLogs().getDocument().getLength();
            ongletListener.getTextPaneLogs().setCaretPosition(length);
        } catch (Exception innerException) {
            LOGGER.fatal("Output capture problem:", innerException);
        }
    }

    /**
     * Crée un onglet Query *
     */
    MyJPanelQuery newQuery(String query) {
        return newQuery(query, "");
    }
    
    public MyJPanelQuery newQuery(String query, String name) {
        nbTabs++;
        //supprime l'onglet "+", ajoute un onglet Query, puis recrée l'onglet "+" à la suite
        conteneurOnglets.remove(plus);
        MyJPanelQuery temp = new MyJPanelQuery(this, query, name);

        monTabOnglet.add(temp);
        nbreTab.add(nbTabs);
        for (int n = 1; n <= monTabOnglet.size(); n++) {
            conteneurOnglets.add("Query" + (n), temp);
        }
        conteneurOnglets.add("+", plus);

        /**
         * ajoute le bouton de fermeture Juste après la création de l'onglet
         * Query il y a donc 3 composants au conteneur d'onglet (Listener,
         * Query, +) On différencie si c'est le 1er onglet créé ou non car le
         * fait d'ajouter le croix fermante à l'onglet ajoute un composant au
         * conteneur d'onglet (1 onglets = 1 composants onglet + 1 composant
         * "croix fermante" = 2 composants) mais ceci une seule fois (2 onglets
         * = 2 composants onglet + 1 composant "croix fermante" = 3 composants)
         * initTabComponent(0); appliquerait la croix fermante au 1er onglet du
         * conteneur cad à "Listener"
         * initTabComponent(conteneurOnglets.getComponentCount()-1);
         * l'appliquerait au dernier composant du conteneur cad à "+"
         * initTabComponent(conteneurOnglets.getComponentCount()-3); car il faut
         * retirer la croix et l'onglet "+" dans le compte
         */
        //Si c'est le 1er onglet Query créé
        if (conteneurOnglets.getComponentCount() == 3) {
            //On applique la croix fermante sur le 2eme composant (l'onglet tout juste créé)
            initTabComponent(1);
        } //S'il y en avait déjà
        else {
            initTabComponent(conteneurOnglets.getComponentCount() - 3);
        }

        //sélectionne l'onglet fraichement créé
        conteneurOnglets.setSelectedIndex(conteneurOnglets.getComponentCount() - 3);
        return temp;
    }

    //Barre du menu
    private void initMenu() {
        JMenuBar menuBar = new JMenuBar();
        //crée les options du menu et leurs listeners
        loadRDFs = new JMenuItem("1 - Load RDFS/OWL");
        loadRDFs.addActionListener(this);
        loadRDFs.setToolTipText("Step 1 : load RDFs file and ontology rules");

        loadRule = new JMenuItem("2 - Load Rule");
        loadRule.addActionListener(this);
        loadRule.setToolTipText("Step 2 : Load file with inferencing rules");

        loadRDF = new JMenuItem("3 - Load RDF");
        loadRDF.addActionListener(this);
        loadRDF.setToolTipText("Step 3 : Load RDF file");

        execWorkflow = new JMenuItem("Process Workflow");
        execWorkflow.addActionListener(this);
        
        loadWorkflow = new JMenuItem("Load Workflow");
        loadWorkflow.addActionListener(this);
        
        loadRunWorkflow = new JMenuItem("Load & Run Workflow");
        loadRunWorkflow.addActionListener(this);

        loadQuery = new JMenuItem("Load Query");
        loadQuery.addActionListener(this);

        cpTransform = new JMenuItem("Compile Transformation");
        cpTransform.addActionListener(this);

        loadStyle = new JMenuItem("Load Style");
        loadStyle.addActionListener(this);

        saveQuery = new JMenuItem("Save Query");
        saveQuery.addActionListener(this);

        saveResult = new JMenuItem("Save Result");
        saveResult.addActionListener(this);

        itable = new HashMap<Object, String>();

        iselect = defItem("Select", DEFAULT_SELECT_QUERY);
        igraph = defItem("Graph", DEFAULT_GRAPH_QUERY);
        iconstruct = defItem("Construct", DEFAULT_CONSTRUCT_QUERY);
        iask = defItem("Ask", DEFAULT_ASK_QUERY);
        idescribe = defItem("Describe", DEFAULT_DESCRIBE_QUERY);
        iserviceCorese = defItem("Service Corese", DEFAULT_SERVICE_CORESE_QUERY);
        iserviceDBpedia = defItem("Service DBpedia", DEFAULT_SERVICE_DBPEDIA_QUERY);
        ifederate = defItem("Federate", "federate.rq");
        ifunction = defItem("Function", DEFAULT_FUN_QUERY);
        ical = defItem("Calendar", "cal.rq");

        iinsert = defItem("Insert", DEFAULT_INSERT_QUERY);
        iinsertdata = defItem("Insert Data", DEFAULT_INSERT_DATA_QUERY);
        idelete = defItem("Delete", DEFAULT_DELETE_QUERY);
        ideleteinsert = defItem("Delete Insert", DEFAULT_DELETE_INSERT_QUERY);

        ientailment = defItem("Entailment", DEFAULT_ENTAILMENT_QUERY);
        irule = defItem("Rule", DEFAULT_RULE_QUERY);
        isystem = defItem("System", DEFAULT_SYSTEM_QUERY);
        iindex = defItem("Index", "index.rq");
        iprovenance = defItem("Provenance", DEFAULT_PROVENANCE_QUERY);

        iturtle = defItem("Turtle", DEFAULT_TEMPLATE_QUERY);
        irdfxml = defItem("RDF/XML", DEFAULT_RDF_XML_QUERY);
        ijson = defItem("JSON", "json.rq");
        itrig = defItem("Trig", DEFAULT_TRIG_QUERY);
        ispin = defItem("SPIN", DEFAULT_SPIN_QUERY);
        iowl = defItem("OWL", DEFAULT_OWL_QUERY);
        itypecheck = defItem("TypeCheck", DEFAULT_TYPECHECK_QUERY);

        loadAndRunRule = new JMenuItem("Load&Run Rule");
        loadAndRunRule.addActionListener(this);
        cut = new JMenuItem("Cut");
        cut.addActionListener(this);
        copy = new JMenuItem("Copy");
        copy.addActionListener(this);
        paste = new JMenuItem("Paste ");
        paste.addActionListener(this);
        undo = new JMenuItem("Undo");
        redo = new JMenuItem("Redo");
        duplicate = new JMenuItem("Duplicate Query");
        duplicate.addActionListener(this);
        duplicateFrom = new JMenuItem("Duplicate from selection");
        duplicateFrom.addActionListener(this);
        newQuery = new JMenuItem("New Query");
        newQuery.addActionListener(this);
        runRules = new JMenuItem("Run Rules");
        runRules.addActionListener(this);
        runRulesOpt = new JMenuItem("Run Rules Optimize");
        runRulesOpt.addActionListener(this);
        reset = new JMenuItem("Reset");
        reset.addActionListener(this);
        apropos = new JMenuItem("About Corese");
        apropos.addActionListener(this);
        tuto = new JMenuItem("Online tutorial");
        tuto.addActionListener(this);
        doc = new JMenuItem("Online doc GraphStream");
        doc.addActionListener(this);
        refresh = new JMenuItem("Reload");
        refresh.addActionListener(this);
        myRadio = new ButtonGroup();
//        coreseBox = new JRadioButton("Corese - SPARQL 1.1");
//        coreseBox.setSelected(true);
//        coreseBox.addActionListener(this);system
        kgramBox = new JRadioButton("Corese/Kgram SPARQL 1.1");
        kgramBox.setSelected(true);
        kgramBox.addActionListener(this);
        comment = new JMenuItem("Comment");
        comment.addActionListener(this);
        help = new JMenuItem("About debug");

        next = new JMenuItem("Next");
        complete = new JMenuItem("Complete");
        forward = new JMenuItem("Forward");
        map = new JMenuItem("Map");
        success = new JMenuItem("Success");
        quit = new JMenuItem("Quit");
        cbtrace = new JCheckBox("Trace");
        cbrdfs = new JCheckBox("RDFS");
        cbowlrllite = new JCheckBox("OWL RL Lite");
        cbowlrl = new JCheckBox("OWL RL");
        cbnamed = new JCheckBox("Load Named");
        checkBoxLoad = new JCheckBox("Load");
        checkBoxQuery = new JCheckBox("Query");
        checkBoxRule = new JCheckBox("Rule");
        checkBoxVerbose = new JCheckBox("Verbose");
        validate = new JMenuItem("Validate");

        undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
        redo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
        cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
        newQuery.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        duplicate.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
        saveQuery.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        help.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.ALT_MASK));
        next.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK));
        complete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.ALT_MASK));
        forward.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.ALT_MASK));
        map.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.ALT_MASK));
        next.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK));
        success.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
        quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.ALT_MASK));

        JMenu fileMenu = new JMenu("File");
        JMenu editMenu = new JMenu("Edit");
        JMenu engineMenu = new JMenu("Engine");
        JMenu debugMenu = new JMenu("Debug");
        JMenu queryMenu = new JMenu("Query");
        JMenu templateMenu = new JMenu("Template");
        JMenu explainMenu = new JMenu("Explain");
        JMenu aboutMenu = new JMenu("?");

        //On ajoute tout au menu
        fileMenu.add(loadRDFs);
        fileMenu.add(loadRule);
        fileMenu.add(loadRDF);
        fileMenu.add(loadQuery);
        fileMenu.add(loadWorkflow);
        fileMenu.add(loadRunWorkflow);
        fileMenu.add(execWorkflow);
        fileMenu.add(cpTransform);
        fileMenu.add(saveQuery);
        fileMenu.add(saveResult);
        fileMenu.add(loadAndRunRule);
        fileMenu.add(loadStyle);

        queryMenu.add(iselect);
        queryMenu.add(igraph);
        queryMenu.add(iconstruct);
        queryMenu.add(iask);
        queryMenu.add(idescribe);
        queryMenu.add(iserviceCorese);
        queryMenu.add(iserviceDBpedia);
        queryMenu.add(ifederate);
        queryMenu.add(ifunction);
        queryMenu.add(ical);

        queryMenu.add(idelete);
        queryMenu.add(iinsert);
        queryMenu.add(iinsertdata);
        queryMenu.add(ideleteinsert);

        explainMenu.add(ientailment);
        explainMenu.add(irule);
        explainMenu.add(iprovenance);
        explainMenu.add(isystem);
        explainMenu.add(iindex);

        templateMenu.add(iturtle);
        templateMenu.add(irdfxml);
        templateMenu.add(ijson);
        templateMenu.add(itrig);
        templateMenu.add(iowl);
        templateMenu.add(ispin);

        templateMenu.add(itypecheck);

        editMenu.add(undo);
        editMenu.add(redo);
        editMenu.add(cut);
        editMenu.add(copy);
        editMenu.add(paste);
        editMenu.add(duplicate);
        editMenu.add(duplicateFrom);
        editMenu.add(comment);
        editMenu.add(newQuery);

        engineMenu.add(runRules);
        engineMenu.add(runRulesOpt);
        engineMenu.add(reset);
        engineMenu.add(refresh);
//        engineMenu.add(coreseBox);
//        myRadio.add(coreseBox);
        // engineMenu.add(kgramBox);
        engineMenu.add(cbrdfs);
        engineMenu.add(cbowlrl);
        engineMenu.add(cbowlrllite);
        engineMenu.add(cbtrace);
        engineMenu.add(cbnamed);
        myRadio.add(kgramBox);
        aboutMenu.add(apropos);
        aboutMenu.add(tuto);
        aboutMenu.add(doc);

        aboutMenu.add(help);
        ActionListener lHelpListener = new ActionListener() {
            public void actionPerformed(ActionEvent l_Event) {
                set(Event.HELP);
            }
        };
        help.addActionListener(lHelpListener);

        debugMenu.add(next);
        ActionListener lNextListener = new ActionListener() {
            public void actionPerformed(ActionEvent l_Event) {
                set(Event.STEP);
            }
        };
        next.addActionListener(lNextListener);

        debugMenu.add(complete);
        ActionListener lSkipListener = new ActionListener() {
            public void actionPerformed(ActionEvent l_Event) {
                set(Event.COMPLETE);

            }
        };
        complete.addActionListener(lSkipListener);

        debugMenu.add(forward);
        ActionListener lPlusListener = new ActionListener() {
            public void actionPerformed(ActionEvent l_Event) {
                set(Event.FORWARD);

            }
        };
        forward.addActionListener(lPlusListener);

        debugMenu.add(map);
        ActionListener lMapListener = new ActionListener() {
            public void actionPerformed(ActionEvent l_Event) {
                set(Event.MAP);

            }
        };
        map.addActionListener(lMapListener);

        debugMenu.add(success);
        ActionListener lSuccessListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                set(Event.SUCCESS);
            }
        };
        success.addActionListener(lSuccessListener);

        debugMenu.add(quit);
        ActionListener lQuitListener = new ActionListener() {
            public void actionPerformed(ActionEvent l_Event) {
                set(Event.QUIT);
            }
        };
        quit.addActionListener(lQuitListener);

        debugMenu.add(checkBoxLoad);

        cbtrace.setEnabled(true);
        cbtrace.addItemListener(
                new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                trace = cbtrace.isSelected();
            }
        });
        cbtrace.setSelected(false);

        cbrdfs.setEnabled(true);
        cbrdfs.addItemListener(
                new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                setRDFSEntailment(cbrdfs.isSelected());
            }
        });
        cbrdfs.setSelected(true);

        cbnamed.setSelected(true);
        cbnamed.setEnabled(true);
        cbnamed.addItemListener(
                new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                Load.setDefaultGraphValue(!cbnamed.isSelected());
            }
        });

        cbowlrl.setEnabled(true);
        cbowlrl.setSelected(false);
        cbowlrl.addItemListener(
                new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                setOWLRL(cbowlrl.isSelected(), false);
            }
        });

        cbowlrllite.setEnabled(true);
        cbowlrllite.setSelected(false);
        cbowlrllite.addItemListener(
                new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                setOWLRL(cbowlrllite.isSelected(), true);
            }
        });

        checkBoxLoad.addItemListener(
                new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
            }
        });

        debugMenu.add(checkBoxQuery);
        checkBoxQuery.addItemListener(
                new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
            }
        });

        debugMenu.add(checkBoxRule);
        checkBoxRule.addItemListener(
                new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
            }
        });

        debugMenu.add(checkBoxVerbose);
        checkBoxVerbose.addItemListener(
                new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (checkBoxVerbose.isSelected()) {
                    set(Event.VERBOSE);
                } else {
                    set(Event.NONVERBOSE);
                }
            }
        });

        debugMenu.add(validate);
        ActionListener l_validateListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent l_Event) {
                lPath = null;
                JFileChooser fileChooser = new JFileChooser(lCurrentPath);
                fileChooser.setMultiSelectionEnabled(true);
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File l_Files[] = fileChooser.getSelectedFiles();
                    for (File f : l_Files) {
                        lPath = f.getAbsolutePath();
                        lCurrentPath = f.getParent();   //recupere le dossier parent du fichier que l'on charge
                    }
                }
            }
        };
        validate.addActionListener(l_validateListener);
        validate.setToolTipText("to validate loading of file");

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(engineMenu);
        menuBar.add(debugMenu);
        menuBar.add(queryMenu);
        menuBar.add(templateMenu);
        menuBar.add(explainMenu);
        menuBar.add(aboutMenu);

        setJMenuBar(menuBar);

        //S'il n'y a pas encore d'onglet Query ces options sont inutilisables
        if (nbreTab.isEmpty()) {
            undo.setEnabled(false);
            redo.setEnabled(false);
            cut.setEnabled(false);
            copy.setEnabled(false);
            paste.setEnabled(false);
            duplicate.setEnabled(false);
            duplicateFrom.setEnabled(false);
            comment.setEnabled(false);
            saveQuery.setEnabled(false);
            saveResult.setEnabled(false);
        }
    }

    JMenuItem defItem2(String name, String query) {
        JMenuItem it = new JMenuItem(name);
        it.addActionListener(this);
        itable.put(it, query);
        return it;
    }

    JMenuItem defItem(String name, String q) {
        JMenuItem it = new JMenuItem(name);
        it.addActionListener(this);
        try {
            String str = read(QUERY + q);
            itable.put(it, str);
        } catch (LoadException ex) {
            LOGGER.error(ex);
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
        return it;
    }

    private void setOWLRL(boolean selected, boolean lite) {
        Entailment e = new Entailment(myCorese);
        e.setOWLRL(selected, lite);
        e.setTrace(trace);
        e.process();
    }

    //Actions du menu
    @Override
    public void actionPerformed(ActionEvent e) {
        //Appelle la fonction pour le chargement d'un fichier RDFS/OWL
        if (e.getSource() == loadRDFs) {
            loadRDFs();

        } //Appelle la fonction pour le chargement d'un fichier query
        else if (e.getSource() == loadQuery) {
            loadQuery();
        } else if (e.getSource() == loadRule) {
            loadRule();
        } //Appelle la fonction pour le chargement d'un fichier RDF
        else if (e.getSource() == loadRDF) {
            loadRDF();
        } else if (e.getSource() == execWorkflow) {
            execWorkflow();
        } else if (e.getSource() == loadWorkflow) {
            loadWorkflow(false);
        } else if (e.getSource() == loadRunWorkflow) {
            loadWorkflow(true);
        } 
        else if (e.getSource() == cpTransform) {
            compile();
        } //sauvegarde la requête dans un fichier texte (.txt)
        else if (e.getSource() == saveQuery) {
          saveQuery();
        } else if (e.getSource() == loadStyle) {
            String style = loadText();
            defaultStylesheet = style;
        } //Sauvegarde le résultat sous forme XML dans un fichier texte
        else if (e.getSource() == saveResult) {
            save(current.getTextAreaXMLResult().getText());
        } // Charge et exécute une règle directement
        else if (e.getSource() == loadAndRunRule) {
            loadRunRule();
        } // Couper, copier, coller
        else if (e.getSource() == cut) {
            if (!nbreTab.isEmpty()) {
                current.getTextPaneQuery().cut();
            }
        } //utilisation de la presse papier pour le copier coller
        else if (e.getSource() == copy) {
            if (!nbreTab.isEmpty()) {
                current.getTextPaneQuery().copy();
            }
        } else if (e.getSource() == paste) {
            if (!nbreTab.isEmpty()) {
                current.getTextPaneQuery().paste();
            }
        } //Dupliquer une requête
        else if (e.getSource() == duplicate) {
            if (!nbreTab.isEmpty()) {
                String toDuplicate;
                toDuplicate = current.getTextPaneQuery().getText();
                textQuery = toDuplicate;
                newQuery(textQuery);
            }
        } //Dupliquer une requête à partir du texte sélectionné
        else if (e.getSource() == duplicateFrom) {
            if (!nbreTab.isEmpty()) {
                String toDuplicate;
                toDuplicate = current.getTextPaneQuery().getSelectedText();
                textQuery = toDuplicate;
                newQuery(textQuery);
            }
        } //Commente une sélection dans la requête
        else if (e.getSource() == comment) {
            if (!nbreTab.isEmpty()) {
                String line;
                String result = "";
                int selectedTextSartPosition = current.getTextPaneQuery().getSelectionStart();
                int selectedTextEndPosition = current.getTextPaneQuery().getSelectionEnd();
                for (int i = 0; i < current.getTextAreaLines().getLineCount() - 1; i++) {
                    try {
                        int lineStartOffset = getLineStartOffset(current.getTextPaneQuery(), i);
                        line = current.getTextPaneQuery().getText(lineStartOffset, getLineOfOffset(current.getTextPaneQuery(), i) - lineStartOffset);

                        if (lineStartOffset >= selectedTextSartPosition && lineStartOffset <= selectedTextEndPosition && !line.startsWith("#")) {
                            //on regarde si la ligne est deja commentée ou non
                            //on commente
                            line = "#" + line;
                        }
                        result += line;
                    } catch (BadLocationException e1) {
                        LOGGER.error(e1);
                    }
                }
                current.getTextPaneQuery().setText(result);
            }
        } //crée un nouvel onglet requête
        else if (e.getSource() == newQuery) {
            textQuery = defaultQuery();
            newQuery(textQuery);
        } //Applique les règles chargées
        else if (e.getSource() == runRules) {
            runRules(false);
        } else if (e.getSource() == runRulesOpt) {
            runRules(true);
        } //Remet tout à zéro
        else if (e.getSource() == reset) {
            reset();
        } //Recharge tous les fichiers déjà chargés
        else if (e.getSource() == refresh) {
            ongletListener.refresh(this);
        } else if (e.getSource() == apropos || e.getSource() == tuto || e.getSource() == doc) {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                try {
                    String uri = URI_CORESE;
                    if (e.getSource() == doc) {
                        uri = URI_GRAPHSTREAM;
                    }
                    Desktop.getDesktop().browse(new URI(uri));
                } catch (IOException e1) {
                    LOGGER.error(e1);
                } catch (URISyntaxException e1) {
                    LOGGER.error(e1);
                }
            }
        } else if (e.getSource() == kgramBox) {
            isKgram = true;
            //DatatypeMap.setLiteralAsString(true);
            for (int i = 0; i < monTabOnglet.size(); i++) {
                MyJPanelQuery temp = monTabOnglet.get(i);
                temp.getButtonTKgram().setEnabled(true);
            }
        } else if (itable.get(e.getSource()) != null) {
            String query = itable.get(e.getSource());
            execPlus(query);
        }
    }
    
    void loadRunRule() {
        String lPath = null;
        JFileChooser fileChooser = new JFileChooser(lCurrentPath);
        fileChooser.setMultiSelectionEnabled(true);
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File[] lFiles = fileChooser.getSelectedFiles();
            for (File f : lFiles) {
                lPath = f.getAbsolutePath();
                if (lPath != null) {
                    try {
                        myCorese.load(lPath);
                        appendMsg("Loading file from path : " + f.getAbsolutePath() + "\n");
                        appendMsg(myCapturer.getContent() + "\ndone.\n\n");
                        ongletListener.getModel().addElement(lPath);
                        boolean b = myCorese.runRuleEngine();
                        if (b) {
                            appendMsg("\n rules applied... \n" + myCapturer.getContent() + "\ndone.\n");
                        }
                    } catch (EngineException | LoadException e1) {
                        LOGGER.error(e1);
                        appendMsg(e1.toString());
                    }
                }
            }
        }
    }
    
    void saveQuery() {
        // Créer un JFileChooser
        JFileChooser filechoose = new JFileChooser(lCurrentPath);
        // Le bouton pour valider l’enregistrement portera la mention enregistrer
        String approve = "Save";
        int resultatEnregistrer = filechoose.showDialog(filechoose, approve); // Pour afficher le JFileChooser…
        // Si l’utilisateur clique sur le bouton enregistrer
        if (resultatEnregistrer == JFileChooser.APPROVE_OPTION) {
            File file = filechoose.getSelectedFile();
            lCurrentPath = file.getParent();
            // Récupérer le nom du fichier qu’il a spécifié
            String myFile = file.toString();

            if (!myFile.endsWith(RQ) && !myFile.endsWith(TXT)) {
                myFile = myFile + RQ;
            }

            try {
                // Créer un objet java.io.FileWriter avec comme argument le mon du fichier dans lequel enregsitrer
                FileWriter lu = new FileWriter(myFile);
                // Mettre le flux en tampon (en cache)
                BufferedWriter out = new BufferedWriter(lu);
                // Mettre dans le flux le contenu de la zone de texte
                out.write(current.getTextPaneQuery().getText());
                current.setFileName(file.getName());
                // Fermer le flux
                out.close();

            } catch (IOException er) {
                LOGGER.error(er);
            }

        }
    }

    void reset() {
        ongletListener.getTextPaneLogs().setText("");
        ongletListener.getListLoadedFiles().removeAll();
        ongletListener.getModel().removeAllElements();
        setMyCoreseNewInstance();
        appendMsg("reset... \n" + myCapturer.getContent() + "\ndone.\n");
    }

    void save(String str) {
        JFileChooser filechoose = new JFileChooser(lCurrentPath);
        // Le bouton pour valider l’enregistrement portera la mention enregistrer
        String approve = "Save";
        int resultatEnregistrer = filechoose.showDialog(filechoose, approve); // Pour afficher le JFileChooser…
        // Si l’utilisateur clique sur le bouton enregistrer
        if (resultatEnregistrer == JFileChooser.APPROVE_OPTION) {
            // Récupérer le nom du fichier qu’il a spécifié
            String myFile = filechoose.getSelectedFile().toString();
            try {
                // Créer un objet java.io.FileWriter avec comme argument le mon du fichier dans lequel enregsitrer
                FileWriter lu = new FileWriter(myFile);
                // Mettre le flux en tampon (en cache)
                BufferedWriter out = new BufferedWriter(lu);
                // Mettre dans le flux le contenu de la zone de texte
                out.write(str);
                // Fermer le flux
                out.close();

            } catch (IOException er) {
                er.printStackTrace();
            }
        }
    }

    void runRules(boolean opt) {
        if (opt) {
            cbrdfs.setSelected(false);
            setRDFSEntailment(false);
        }
        Date d1 = new Date();
        boolean b = myCorese.runRuleEngine(opt, trace);
        Date d2 = new Date();
        if (b) {
            appendMsg("\nrules applied... \n" + myCapturer.getContent() + "\ndone.\n");
            appendMsg("time: " + (d2.getTime() - d1.getTime()) / (1000.0));
        }
    }

    String defaultQuery() {
        return defaultQuery;
    }

    public static int getLineStartOffset(final JTextComponent textComponent, final int line) throws BadLocationException {
        final Document doc = textComponent.getDocument();
        final int lineCount = doc.getDefaultRootElement().getElementCount();
        if (line < 0) {
            throw new BadLocationException("Negative line", -1);
        } else if (line > lineCount) {
            throw new BadLocationException("No such line", doc.getLength() + 1);
        } else {
            Element map = doc.getDefaultRootElement();
            Element lineElem = map.getElement(line);
            return lineElem.getStartOffset();
        }
    }

    public int getLineOfOffset(final JTextComponent textComponent, final int line) throws BadLocationException {
        final Document doc = textComponent.getDocument();
        final int lineCount = doc.getDefaultRootElement().getElementCount();
        if (line < 0) {
            throw new BadLocationException("Negative line", -1);
        } else if (line > lineCount) {
            throw new BadLocationException("No such line", doc.getLength() + 1);
        } else {
            Element map = doc.getDefaultRootElement();
            Element lineElem = map.getElement(line);
            return lineElem.getEndOffset();
        }
    }

    //Pour la croix fermante sur les onglets
    private void initTabComponent(int i) {
        conteneurOnglets.setTabComponentAt(i, new ButtonTabComponent(conteneurOnglets, this));
    }

    /**
     * Permet de récupérer sous forme de String l'extension du fichier
     *
     * @param o
     * @return
     */
    public String extension(Object o) {
        String extension = null;
        String s = String.valueOf(o);
        int i = s.lastIndexOf('.');	//récupére l'index a partir duquel il faut couper

        if (i > 0 && i < s.length() - 1) {
            extension = s.substring(i + 1).toLowerCase();		//on récupére l'extension
        }
        return extension;	//on retourne le résultat
    }

    /**
     * permet de ranger un tableau selon l'ordre suivant 1)rdfs,owl 2)rul 3)rdf
     *
     * @param strArray
     */
    public void sortArray(String[] strArray) {
        String tmp;
        if (strArray.length == 1) {
            return;
        }
        for (int i = 0; i < strArray.length; i++) {
            for (int j = i + 1; j < strArray.length; j++) {
                Object tempi = strArray[i];
                String exti = MyCellRenderer.extension(tempi);
                Object tempj = strArray[j];
                String extj = MyCellRenderer.extension(tempj);
                if (exti.equals("rdf") && (extj.equals("rul") || extj.equals("rdfs") || extj.equals("owl"))) {
                    tmp = strArray[i];
                    strArray[i] = strArray[j];
                    strArray[j] = tmp;
                }
                if (exti.equals("rul") && (extj.equals("rdfs") || extj.equals("owl"))) {
                    tmp = strArray[i];
                    strArray[i] = strArray[j];
                    strArray[j] = tmp;
                }
                if (exti.equals("owl") && (extj.equals("rdfs"))) {
                    tmp = strArray[i];
                    strArray[i] = strArray[j];
                    strArray[j] = tmp;
                }

            }
        }
    }

    /**
     * Permet d'ordonnée une liste en utilisant la méthode SortArray Récupère le
     * model de la liste afin de réorganiser celle-ci
     */
    public void sort() {
        DefaultListModel dlm;
        dlm = ongletListener.getModel();

        int numItems = dlm.getSize();
        String[] a = new String[numItems];
        for (int i = 0; i < numItems; i++) {
            a[i] = (String) dlm.getElementAt(i);
        }
        sortArray(a);
        for (int i = 0; i < numItems; i++) {
            dlm.setElementAt(a[i], i);
        }

    }

    void display() {
        for (int i = 0; i < getOngletListener().getModel().getSize(); i++) {
            System.out.println("GUI: " + ongletListener.getModel().get(i).toString());
        }
    }

    public void loadRDF() {
        Filter FilterRDF = new Filter(new String[]{"rdf", "ttl", "html"}, "RDF files (*.rdf,*.ttl)");
        load(FilterRDF);
    }

    public void loadRDFs() {
        Filter FilterRDFS = new Filter(new String[]{"rdfs", "owl", "ttl"}, "RDFS/OWL files (*.rdfs,*.owl,*.ttl)");
        load(FilterRDFS);
    }

    public void execWorkflow() {
        Filter FilterRDF = new Filter(new String[]{"ttl", "sw"}, "Workflow files (*.ttl, *.sw)");
        load(FilterRDF, true, true, false);
    }
    
    public void loadWorkflow(boolean run) {
        Filter FilterRDF = new Filter(new String[]{"ttl", "sw"}, "Workflow files (*.ttl, *.sw)");
        load(FilterRDF, true, false, run);
    }
    /**
     * Charge un fichier dans CORESE
     */
    public void load(Filter filter) {
        load(filter, false, false, false);
    }

    /**
     * wf: load a Workflow
     * exec: run Workflow using std Worklow engine
     * run: set the queries in query panels an run the queries in the GUI
     */
    public void load(Filter filter, boolean wf, boolean exec, boolean run) {
        controler(LOAD);
        lPath = null;
        JFileChooser fileChooser = new JFileChooser(lCurrentPath);
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.addChoosableFileFilter(filter);
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File[] lFiles = fileChooser.getSelectedFiles();

            DefaultListModel model = getOngletListener().getModel();
            for (File f : lFiles) {
                lPath = f.getAbsolutePath();
                if (lPath == null) {
                    continue;
                }

                lCurrentPath = f.getParent();

                if (!model.contains(lPath) && !wf) {
                    model.addElement(lPath);
                }
                appendMsg("Loading " + extension(lPath) + " File from path : " + lPath + "\n");
                if (wf){ 
                   if (exec) {
                       execWF(lPath);
                   }
                   else {
                       loadWF(lPath, run);
                   }
                } 
                else {
                    load(lPath);
                }
            }
        }
    }

    void execWF(String path) {
        execWF(path, true);
    }
    
    void execWF(String path, boolean reset) {
        if (reset) {
            reset();
        }
        WorkflowParser parser = new WorkflowParser();
        // parser.setDebug(true);
        try {
            Date d1 = new Date();
            parser.parse(path);
            SemanticWorkflow wp = parser.getWorkflowProcess();
            // wp.setDebug(true);
            Data res = wp.process(new Data(myCorese.getGraph()));
            Date d2 = new Date();
            System.out.println("time: " + (d2.getTime() - d1.getTime()) / (1000.0));
            appendMsg(res.toString() + "\n");
            appendMsg("time: " + (d2.getTime() - d1.getTime()) / (1000.0) + "\n");
        } catch (LoadException ex) {
            LOGGER.error(ex);
            appendMsg(ex.toString());
        } catch (EngineException ex) {
            LOGGER.error(ex);
            appendMsg(ex.toString());
        }
    }

    void loadWF(String path, boolean run) {      
        WorkflowParser parser = new WorkflowParser();
        try {
            parser.parse(path);
            SemanticWorkflow wp = parser.getWorkflowProcess();
            defQuery(wp, run);
        } catch (LoadException ex) {
            LOGGER.error(ex);
            appendMsg(ex.toString());
        }
    }
    
    void defQuery(WorkflowProcess wp, boolean run) {
        if (wp.getProcessList() != null) {
            for (WorkflowProcess wf : wp.getProcessList()) {
                if (wf.isQuery()) {
                    defQuery(wf.getQueryProcess().getQuery(), run);
                } else {
                    defQuery(wf, run);
                }
            }
        }
    }

    /**
     * Compile a transformation
     *
     * @param filter
     */
    public void compile() {
        lPath = null;
        JFileChooser fileChooser = new JFileChooser(lCurrentPath);
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File l_Files[] = fileChooser.getSelectedFiles();

            DefaultListModel model = getOngletListener().getModel();
            for (File f : l_Files) {
                lPath = f.getAbsolutePath();
                lCurrentPath = f.getParent();
                if (lPath != null) {
                    appendMsg("Compile " + lPath + "\n");
                    compile(lPath);
                }
            }
        }
    }

    void compile(String path) {
        TemplatePrinter p = TemplatePrinter.create(path);
        try {
            StringBuilder sb = p.process();
            if (current != null) {
                current.getTextAreaXMLResult().setText(sb.toString());
            }
            save(sb.toString());

        } catch (IOException ex) {
            LogManager.getLogger(MainFrame.class.getName()).log(Level.ERROR, "", ex);
        } catch (LoadException ex) {
            LogManager.getLogger(MainFrame.class.getName()).log(Level.ERROR, "", ex);
        }
    }

    void controler(int event) {
        switch (event) {

            case LOAD:
                cbowlrllite.setSelected(false);
                cbowlrl.setSelected(false);
                break;

        }
    }

    public void load(String fichier) {
        controler(LOAD);
        try {
            myCorese.load(fichier);
            appendMsg(myCapturer.getContent());
        } catch (EngineException |LoadException e) {
            appendMsg(e.toString());
            e.printStackTrace();
        }
    }

    /**
     * Crée un nouvel onglet requête avec le texte contenu dans un fichier
     */
    public String loadText() {
        String str = "";
        JFileChooser fileChooser = new JFileChooser(lCurrentPath);
        File selectedFile;
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            //Voici le fichier qu'on a selectionné
            selectedFile = fileChooser.getSelectedFile();
            setFileName(selectedFile.getName());
            lCurrentPath = selectedFile.getParent();
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(selectedFile);
                int n;
                while ((n = fis.available()) > 0) {
                    byte[] b = new byte[n];
                    //On lit le fichier
                    int result = fis.read(b);
                    if (result == -1) {
                        break;
                    }
                    //On remplit un string avec ce qu'il y a dans le fichier, "s" est ce qu'on va mettre dans la textArea de la requête
                    str = new String(b);
                    appendMsg("Loading file from path: " + selectedFile + "\n");
                }
                fis.close();
            } catch (IOException ex) {
                LOGGER.error(ex);
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException ex) {
                    }
                }
            }
        }
        return str;
    }

    public void loadQuery() {
        textQuery = loadText();
        newQuery(textQuery, getFileName());
    }
    
    void defQuery(String text, boolean run){
        textQuery = text;
        MyJPanelQuery panel = newQuery(textQuery);
        if (run){
            panel.exec(this, text);
        }
    }

    public void loadPipe() {
        //Load and run a pipeline
        Filter FilterRUL = new Filter(new String[]{"rdf", "ttl"}, "rdf files (*.rdf,*.ttl)");
        JFileChooser fileChooser = new JFileChooser(lCurrentPath);
        fileChooser.addChoosableFileFilter(FilterRUL);
        File selectedFile;
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            //Voici le fichier qu'on a selectionné
            selectedFile = fileChooser.getSelectedFile();
            lCurrentPath = selectedFile.getParent();
            myCorese.runPipeline(selectedFile.getAbsolutePath());
        }
    }

    /**
     * Charge un fichier Rule dans CORESE
     */
    public void loadRule() {
        Filter FilterRUL = new Filter(new String[]{"rul", "brul"}, "Rul files (*.rul)");
        load(FilterRUL);
    }

    public void loadRule(String fichier) {
        try {
            myCorese.load(fichier);
            appendMsg(myCapturer.getContent() + "\ndone.\n\n");
        } catch (EngineException |LoadException e) {
            appendMsg(e.toString());
            e.printStackTrace();
        }
    }

    public void loadRDF(String fichier) {
        try {
            myCorese.load(fichier);
            appendMsg(myCapturer.getContent() + "\ndone.\n\n");
        } catch (EngineException e) {
            appendMsg(e.toString());
            e.printStackTrace();
        } catch (LoadException e) {
            appendMsg(e.toString());
            e.printStackTrace();
        }
    }

    //Getteurs et setteurs utiles
    //donne l'onglet sélectionné
    public int getSelected() {
        return selected;
    }

    //Accède au contenu de du textArea de l'onglet query
    public String getTextQuery() {
        return textQuery;
    }

    //Accède au conteneur d'onglets de la fenêtre principale
    public JTabbedPane getConteneurOnglets() {
        return conteneurOnglets;
    }

    public MyJPanelListener getOngletlistener() {
        return ongletListener;
    }

    public GraphEngine getMyCorese() {
        return myCorese;
    }

    //Réinitialise Corese
    public void setMyCoreseNewInstance() {
        setMyCoreseNewInstance(cbrdfs.isSelected());
    }

    void setMyCoreseNewInstance(boolean rdfs) {
        if (myCorese != null) {
            myCorese.finish();
        }
        myCorese = GraphEngine.create(rdfs);
        myCorese.setOption(cmd);
    }
    
    void process(Command cmd) {
        Access.setMode(Access.Mode.GUI);
        String path = cmd.get(Command.WORKFLOW);
        if (path != null) {
            execWF(path, false);
        }
    }

    void setRDFSEntailment(boolean b) {
        Graph g = myCorese.getGraph();
        g.setRDFSEntailment(b);
    }

    public Logger getLogger() {
        return LOGGER;
    }

    String read(String name) throws LoadException, IOException {
        InputStream stream = getClass().getResourceAsStream(name);
        if (stream == null) {
            throw new IOException(name);
        }

        BufferedReader read = new BufferedReader(new InputStreamReader(stream));
        StringBuilder sb = new StringBuilder();
        String str = null;
        String NL = System.getProperty("line.separator");

        while (true) {
            str = read.readLine();
            if (str == null) {
                break;
            } else {
                sb.append(str);
                sb.append(NL);
            }
        }

        stream.close();
        return sb.toString();
    }

    public String getDefaultStylesheet() {
        return defaultStylesheet;
    }

    public String getSaveStylesheet() {
        return saveStylesheet;
    }

    public MyJPanelListener getOngletListener() {
        return ongletListener;
    }

    public void setOngletListener(MyJPanelListener ongletListener) {
        this.ongletListener = ongletListener;
    }

    public CaptureOutput getMyCapturer() {
        return myCapturer;
    }

    public ArrayList<JCheckBox> getListCheckbox() {
        return listCheckbox;
    }

    public void setListCheckbox(ArrayList<JCheckBox> listCheckbox) {
        this.listCheckbox = listCheckbox;
    }

    public ArrayList<JMenuItem> getListJMenuItems() {
        return listJMenuItems;
    }

    public void setListJMenuItems(ArrayList<JMenuItem> listJMenuItems) {
        this.listJMenuItems = listJMenuItems;
    }

    public boolean isKgram() {
        return isKgram;
    }

    public void setKgram(boolean isKgram) {
        this.isKgram = isKgram;
    }

    public JMenuItem getUndo() {
        return undo;
    }

    public JMenuItem getRedo() {
        return redo;
    }

    public MyJPanelQuery getPanel() {
        return current;
    }

    MyEvalListener getEvalListener() {
        return el;
    }

    public void setEvalListener(MyEvalListener el) {
        this.el = el;
    }

    /**
     *
     * For interacting with listener
     */
    public void setBuffer(Buffer b) {
        buffer = b;
    }

    void set(int event) {
        if (buffer != null) {
            buffer.set(event);
        }
    }
    
    
    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public static void main(String[] p_args) {
        CaptureOutput aCapturer = new CaptureOutput();
        LoggerContext context = (LoggerContext) LogManager.getContext();
        Configuration config = context.getConfiguration();
        PatternLayout layout = PatternLayout.createLayout("%m%n", null, config, null, Charset.defaultCharset(), false, false, null, null);
        MainFrame coreseFrame = new MainFrame(aCapturer, p_args);       
        coreseFrame.setStyleSheet();
        coreseFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        MyJPanelListener.listLoadedFiles.setCellRenderer(new MyCellRenderer());

    }
}
