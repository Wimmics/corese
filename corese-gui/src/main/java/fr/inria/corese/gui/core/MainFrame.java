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
import fr.inria.corese.gui.editor.ShaclEditor;
import fr.inria.corese.gui.editor.TurtleEditor;
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
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.core.transform.TemplatePrinter;
import fr.inria.corese.core.transform.Transformer;
import fr.inria.corese.shex.shacl.Shex;
import fr.inria.corese.sparql.exceptions.SafetyException;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.NSManager;
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
    private static MainFrame singleton ;
    private static final long serialVersionUID = 1L;
    private static final int LOAD = 1;
    private static final String TITLE = "Corese 4.2 - Wimmics INRIA I3S - 2021-05-01";
    // On déclare notre conteneur d'onglets
    protected static JTabbedPane conteneurOnglets;
    // Compteur pour le nombre d'onglets query créés
    private ArrayList<Integer> nbreTab = new ArrayList<Integer>();
    private String lCurrentPath = "user/home";
    private String lCurrentRule = "user/home";

    private String lPath;
    private String fileName = "";
    //Variable true ou false pour déterminer le mode Kgram ou Corese
    private boolean isKgram = true;
    boolean trace = false;
    // Pour le menu
    private JMenuItem loadRDF;
    private JMenuItem loadSHACL, loadShex;
    private JMenuItem loadSHACLShape;
    private JMenuItem loadQuery;
    private JMenuItem execWorkflow, loadWorkflow, loadRunWorkflow;
    private JMenuItem loadRule;
    private JMenuItem loadStyle;
    private JMenuItem cpTransform, shex;
    private JMenuItem saveQuery;
    private JMenuItem saveResult;
    private JMenuItem loadAndRunRule;
    private JMenuItem refresh;
    private JMenuItem exportRDF;
    private JMenuItem exportTurtle;
    private JMenuItem exportOwl;
    private JMenuItem exportJson;
    private JMenuItem exportTrig;
    private JMenuItem copy;
    private JMenuItem cut;
    private JMenuItem paste;
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
    private JMenuItem iselect, iselecttuple, igraph, iconstruct, iask, idescribe,
            iserviceCorese, iserviceDBpedia, ifederate,
            iinsert, iinsertdata, idelete, ideleteinsert,
            iturtle, in3, irdfxml, ijson, itrig, ispin, iowl, 
            ientailment, irule, ierror, ifunction, ical, iowlrl;
    private JMenuItem itypecheck, ipredicate, ipredicatepath;
    HashMap<Object, DefQuery> itable;
    private JCheckBox checkBoxQuery;
    private JCheckBox checkBoxRule;
    private JCheckBox checkBoxVerbose;
    private JCheckBox checkBoxLoad;
    private JCheckBox cbrdfs, cbowlrl, cbowlrllite, cbowlrlext, cbtrace, cbnamed;
    private JCheckBox cbshexClosed, cbshexExtend, cbshexCard, cbshexshex;
    private JMenuItem validate;
    //style correspondant au graphe
    private String defaultStylesheet, saveStylesheet;
    private ArrayList<JCheckBox> listCheckbox;	//list qui stocke les JCheckBoxs présentes sur le JPanelListener
    private ArrayList<JMenuItem> listJMenuItems;	//list qui stocke les Boutons présents sur le JPanelListener
    // Les 4 types d'onglets
    private ArrayList<MyJPanelQuery> monTabOnglet;
    private JPanel plus;
    private MyJPanelQuery current;
    private MyJPanelListener ongletListener;
    private ShaclEditor ongletShacl;
    private TurtleEditor ongletTurtle;
    // Pour connaître l'onglet selectionné
    protected int selected;
    // Texte dans l'onglet requête
    private String textQuery;
    private static final String SHACL_SHACL = NSManager.SHACL_SHACL;
    // Texte par défaut dans l'onglet requête
    private static final String DEFAULT_SELECT_QUERY = "select.rq";
    private static final String DEFAULT_TUPLE_QUERY = "selecttuple.rq";
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
    private static final String URI_CORESE = "http://project.inria.fr/corese";
    private static final String URI_GRAPHSTREAM = "http://graphstream-project.org/";
    int nbTabs = 0;
    
    boolean shexClosed = true, shexCard = true, shexExtend = true;
    private boolean shexSemantics = false;
    
    Command cmd;
    
    static {
        // false: load files into named graphs
        // true:  load files into kg:default graph
        Load.setDefaultGraphValue(false);
    }
    
    class DefQuery {

        private String query;
        private String name;
        
        DefQuery(String n, String q) {
            query = q; name = n;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    /**
     * Crée la fenêtre principale, initialise Corese
     *
     * @param aCapturer
     * @param pPropertyPath
     */
    public MainFrame(CaptureOutput aCapturer, String[] args) {
        super(); 
        Access.setMode(Access.Mode.GUI); // before command
        cmd = new Command(args).init();
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

        listCheckbox = new ArrayList<>();
        listJMenuItems = new ArrayList<>();

        //Création et ajout de notre conteneur d'onglets à la fenêtre
        conteneurOnglets = new JTabbedPane();
        this.getContentPane().add(conteneurOnglets, BorderLayout.CENTER);

        //Création et ajout des deux onglets "Listener" et "+"
        monTabOnglet = new ArrayList<MyJPanelQuery>();
        ongletListener = new MyJPanelListener(this);
        ongletShacl = new ShaclEditor(this);
        ongletTurtle = new TurtleEditor(this);
        plus = new JPanel();
        conteneurOnglets.addTab("System", ongletListener);
        conteneurOnglets.addTab("Shacl editor", ongletShacl);
        conteneurOnglets.addTab("Turtle editor", ongletTurtle);
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
        
    public void focusMessagePanel() {
        getConteneurOnglets().setSelectedIndex(0);
    }

    void execPlus() {
        execPlus("", defaultQuery);
    }

    void execPlus(String name, String str) {
        // s : texte par défaut dans la requête
        textQuery = str;
        //Crée un nouvel onglet Query
        newQuery(str, name);
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
        Document currentDoc = ongletListener.getTextPaneLogs().getDocument();
        try {
            currentDoc.insertString(currentDoc.getLength(), msg, null);

            //Place l'ascenceur en bas à chaque ajout de texte
            ongletListener.getScrollPaneLog().revalidate();
            int length = currentDoc.getLength();
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
         * Query il y a donc 5 composants au conteneur d'onglet (Listener, Shacl
         * , turtle, Query, +) On différencie si c'est le 1er onglet créé ou non car le
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
        if (conteneurOnglets.getComponentCount() == 5) {
            //On applique la croix fermante sur le 4eme composant (l'onglet tout juste créé)
            initTabComponent(3);
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
        loadRDF = new JMenuItem("Dataset");
        loadRDF.addActionListener(this);
        loadRDF.setToolTipText("Load Dataset");
       
        loadRule = new JMenuItem("Rule");
        loadRule.addActionListener(this);
        loadRule.setToolTipText("Load file with inferencing rules");
        
        loadAndRunRule = new JMenuItem("Load & Run Rule");
        loadAndRunRule.addActionListener(this);
        
        loadSHACL = new JMenuItem("SHACL");
        loadSHACL.addActionListener(this);
        loadSHACL.setToolTipText("Load SHACL");
        
        loadSHACLShape = new JMenuItem("SHACL Shape Validator");
        loadSHACLShape.addActionListener(this);
        loadSHACLShape.setToolTipText("Load SHACL Shape Validator");
        
        loadQuery = new JMenuItem("Query");
        loadQuery.addActionListener(this);
        
        loadShex = new JMenuItem("Shex");
        loadShex.addActionListener(this);
        loadShex.setToolTipText("Load Shex");
        
        loadWorkflow = new JMenuItem("Workflow");
        loadWorkflow.addActionListener(this);
        
        loadRunWorkflow = new JMenuItem("Load & Run Workflow");
        loadRunWorkflow.addActionListener(this);

        loadStyle = new JMenuItem("Style");
        loadStyle.addActionListener(this);

        refresh = new JMenuItem("Reload");
        refresh.addActionListener(this);
        
        exportRDF = new JMenuItem("RDF/XML");
        exportRDF.addActionListener(this);
        exportRDF.setToolTipText("Export graph in RDF/XML format");

        exportTurtle = new JMenuItem("Turtle");
        exportTurtle.addActionListener(this);
        exportTurtle.setToolTipText("Export graph in Turtle format");

        exportOwl = new JMenuItem("OWL");
        exportOwl.addActionListener(this);
        exportOwl.setToolTipText("Export graph in OWL format");

        exportJson = new JMenuItem("JSON");
        exportJson.addActionListener(this);
        exportJson.setToolTipText("Export graph in JSON format");

        exportTrig = new JMenuItem("TriG");
        exportTrig.addActionListener(this);
        exportTrig.setToolTipText("Export graph in TriG format");
        
        execWorkflow = new JMenuItem("Process Workflow");
        execWorkflow.addActionListener(this);

        cpTransform = new JMenuItem("Compile Transformation");
        cpTransform.addActionListener(this);
        
        shex = new JMenuItem("Translate Shex to Shacl");
        shex.addActionListener(this);

        saveQuery = new JMenuItem("Save Query");
        saveQuery.addActionListener(this);

        saveResult = new JMenuItem("Save Result");
        saveResult.addActionListener(this);

        itable = new HashMap<>();

        iselect = defItem("Select", DEFAULT_SELECT_QUERY);
        iselecttuple = defItem("Select Tuple", DEFAULT_TUPLE_QUERY);
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

        ientailment = defItem("RDFS Entailment", DEFAULT_ENTAILMENT_QUERY);
        irule = defItem("Rule/OWL RL", DEFAULT_RULE_QUERY);      
        ierror = defItem("Constraint", "constraint.rq");      
        iowlrl = defItem("OWL RL Check", "owlrl.rq");

        iturtle = defItem("Turtle", DEFAULT_TEMPLATE_QUERY);
        in3 = defItem("NTriple", "n3.rq");
        irdfxml = defItem("RDF/XML", DEFAULT_RDF_XML_QUERY);
        ijson = defItem("JSON", "json.rq");
        itrig = defItem("Trig", DEFAULT_TRIG_QUERY);
        ispin = defItem("SPIN", DEFAULT_SPIN_QUERY);
        iowl = defItem("OWL", DEFAULT_OWL_QUERY);
        
        itypecheck = defItem("Engine",   "shacl/typecheck.rq");
        ipredicate = defItem("Predicate", "shacl/predicate.rq");
        ipredicatepath = defItem("Predicate Path", "shacl/predicatepath.rq");

        cut = new JMenuItem("Cut");
        cut.addActionListener(this);
        copy = new JMenuItem("Copy");
        copy.addActionListener(this);
        paste = new JMenuItem("Paste ");
        paste.addActionListener(this);
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
        cbowlrlext = new JCheckBox("OWL RL Extended");
        cbowlrllite = new JCheckBox("OWL RL Lite");
        cbowlrl = new JCheckBox("OWL RL");
        cbnamed = new JCheckBox("Load Named");
        
        cbshexCard   =  new JCheckBox("Cardinality");
        cbshexClosed =  new JCheckBox("Closed");
        cbshexExtend =  new JCheckBox("Shacl Extension");
        cbshexshex =  new JCheckBox("Shex Semantics");
        
        checkBoxLoad = new JCheckBox("Load");
        checkBoxQuery = new JCheckBox("Query");
        checkBoxRule = new JCheckBox("Rule");
        checkBoxVerbose = new JCheckBox("Verbose");
        validate = new JMenuItem("Validate");

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
        JMenu shaclMenu = new JMenu("Shacl");
        JMenu shexMenu = new JMenu("Shex");
        JMenu eventMenu = new JMenu("Event");        
        JMenu explainMenu = new JMenu("Explain");
        JMenu aboutMenu = new JMenu("?");
        
        JMenu fileMenuLoad = new JMenu("Load");
        JMenu fileMenuSaveGraph = new JMenu("Savge graph");

        //On ajoute tout au menu
        fileMenu.add(fileMenuLoad);
        fileMenuLoad.add(loadRDF);
        fileMenuLoad.add(loadRule);
        fileMenuLoad.add(loadAndRunRule);
        fileMenuLoad.add(loadSHACL);
        fileMenuLoad.add(loadSHACLShape);
        fileMenuLoad.add(loadQuery);
        fileMenuLoad.add(loadShex);
        fileMenuLoad.add(loadWorkflow);
        fileMenuLoad.add(loadRunWorkflow);
        fileMenuLoad.add(loadStyle);

        fileMenu.add(refresh);
        
        fileMenu.add(execWorkflow);
        fileMenu.add(cpTransform);
        fileMenu.add(shex);

        fileMenu.add(fileMenuSaveGraph);
        fileMenuSaveGraph.add(exportRDF);
        fileMenuSaveGraph.add(exportTurtle);
        fileMenuSaveGraph.add(exportOwl);
        fileMenuSaveGraph.add(exportJson);
        fileMenuSaveGraph.add(exportTrig);

        fileMenu.add(saveQuery);
        fileMenu.add(saveResult);

        queryMenu.add(iselect);
        queryMenu.add(iselecttuple);
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
        explainMenu.add(ierror);
        explainMenu.add(iowlrl);

        templateMenu.add(iturtle);
        templateMenu.add(in3);
        templateMenu.add(irdfxml);
        templateMenu.add(ijson);
        templateMenu.add(itrig);
        templateMenu.add(ispin);
        templateMenu.add(iowl);
        
        shaclMenu.add(itypecheck);
        shaclMenu.add(defItem("Fast Engine", "shacl/fastengine.rq"));
        shaclMenu.add(ipredicate);
        shaclMenu.add(ipredicatepath);
        shaclMenu.add(defItem("Constraint Function", "shacl/extension.rq"));
        shaclMenu.add(defItem("Path Function", "shacl/funpath.rq"));
        shaclMenu.add(defItem("Path Linked Data", "shacl/service.rq"));
        
        shexMenu.add(cbshexCard);
        //shexMenu.add(cbshexClosed);
        shexMenu.add(cbshexExtend);
        //shexMenu.add(cbshexshex);
        
        eventMenu.add(defItemFunction("SPARQL Query",   "event/query.rq"));
        eventMenu.add(defItemFunction("SPARQL Update",  "event/update.rq"));
        eventMenu.add(defItemFunction("SHACL",          "event/shacl.rq"));
        eventMenu.add(defItemFunction("Rule",           "event/rule.rq"));
        eventMenu.add(defItemFunction("Entailment",     "event/entailment.rq"));

        eventMenu.add(defItemFunction("Unit",           "event/unit.rq"));
        eventMenu.add(defItemFunction("Romain",         "event/romain.rq"));
        eventMenu.add(defItemFunction("XML",            "event/xml.rq"));
        eventMenu.add(defItemFunction("JSON",           "event/json.rq"));
        
        eventMenu.add(defItemFunction("GUI",            "event/gui.rq"));

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
//        engineMenu.add(coreseBox);
//        myRadio.add(coreseBox);
        // engineMenu.add(kgramBox);
        engineMenu.add(cbrdfs);
        engineMenu.add(cbowlrl);
        //engineMenu.add(cbowlrllite);
        engineMenu.add(cbowlrlext);
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
        
        cbshexClosed.setEnabled(true);
        cbshexClosed.setSelected(true);
        cbshexClosed.addItemListener(
                new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                shexClosed = cbshexClosed.isSelected();
            }
        });
        
        cbshexCard.setEnabled(true);
        cbshexCard.setSelected(true);
        cbshexCard.addItemListener(
                new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                shexCard = cbshexCard.isSelected();
            }
        });
        
        cbshexExtend.setEnabled(true);
        cbshexExtend.setSelected(true);
        cbshexExtend.addItemListener(
                new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                shexExtend = cbshexExtend.isSelected();
            }
        });
        
        
        cbshexshex.setEnabled(true);
        cbshexshex.setSelected(false);
        cbshexshex.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                setShexSemantics(cbshexshex.isSelected());
            }
        });

        cbowlrl.setEnabled(true);
        cbowlrl.setSelected(false);
        cbowlrl.addItemListener(
                new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                setOWLRL(cbowlrl.isSelected(), RuleEngine.OWL_RL);
            }
        });

        cbowlrllite.setEnabled(true);
        cbowlrllite.setSelected(false);
        cbowlrllite.addItemListener(
                new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                setOWLRL(cbowlrllite.isSelected(), RuleEngine.OWL_RL_LITE);
            }
        });
        
        
        cbowlrlext.setEnabled(true);
        cbowlrlext.setSelected(false);
        cbowlrlext.addItemListener(
                new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                setOWLRL(cbowlrlext.isSelected(), RuleEngine.OWL_RL_EXT);
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
        menuBar.add(shaclMenu);
        menuBar.add(shexMenu);
        menuBar.add(eventMenu);
        menuBar.add(explainMenu);
        menuBar.add(aboutMenu);

        setJMenuBar(menuBar);

        //S'il n'y a pas encore d'onglet Query ces options sont inutilisables
        if (nbreTab.isEmpty()) {
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
    
    JMenuItem defItem(String name, String q) {
        return defItemBasic(QUERY, name, q);
    }
    
    JMenuItem defItemFunction(String name, String q) {
        return defItemBasic("/function/", name, q);
    }

    JMenuItem defItemBasic(String root, String name, String q) {
        JMenuItem it = new JMenuItem(name);
        it.addActionListener(this);
        try {
            String str = read(root + q);
            itable.put(it, new DefQuery(q, str));
        } catch (LoadException | IOException ex) {
            LOGGER.error(ex);
        }
        return it;
    }

    private void setOWLRL(boolean selected, int owl) {
        if (selected) {
            Entailment e = new Entailment(myCorese);
            e.setOWLRL(selected, owl);
            e.setTrace(trace);
            e.process();
        }
    }

    //Actions du menu
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loadQuery) {
            loadQuery();
        } else if (e.getSource() == loadRule) {
            loadRule();
        } 
        else if (e.getSource() == loadRDF || e.getSource() == loadSHACL) {
            loadRDF();
        } 
        else if (e.getSource() == loadSHACLShape) {
            basicLoad(SHACL_SHACL);
        } 
        else if (e.getSource() == loadShex) {
            shex(true);
        }
        else if (e.getSource() == execWorkflow) {
            execWorkflow();
        } else if (e.getSource() == loadWorkflow) {
            loadWorkflow(false);
        } else if (e.getSource() == loadRunWorkflow) {
            loadWorkflow(true);
        } 
        else if (e.getSource() == cpTransform) {
            compile();
        } 
        else if (e.getSource() == shex) {
            shex(false);
        } 
        //sauvegarde la requête dans un fichier texte (.txt)
        else if (e.getSource() == saveQuery) {
          saveQuery();
        } else if (e.getSource() == loadStyle) {
            String style = loadText();
            defaultStylesheet = style;
        } //Sauvegarde le résultat sous forme XML dans un fichier texte
        else if (e.getSource() == saveResult) {
            save(current.getTextAreaXMLResult().getText());
        } // Exporter le graph au format RDF/XML
        else if (e.getSource() == exportRDF) {
            saveGraph(Transformer.RDFXML);
        } // Exporter le graph au format Turle
        else if (e.getSource() == exportTurtle) {
            saveGraph(Transformer.TURTLE);
        } // Exporter le graph au format OWL
        else if (e.getSource() == exportOwl) {
            saveGraph(Transformer.OWL);
        } // Exporter le graph au format Json
        else if (e.getSource() == exportJson) {
            saveGraph(Transformer.JSON);
        } // Exporter le graph au format TriG
        else if (e.getSource() == exportTrig) {
            saveGraph(Transformer.TRIG);
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
            try {
                runRules(false);
            } catch (EngineException ex) {
                LOGGER.error(ex.getMessage());
            }
        } else if (e.getSource() == runRulesOpt) {
            try {
                runRules(true);
            } catch (EngineException ex) {
                LOGGER.error(ex.getMessage());
            }
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
            // Button Explain
            DefQuery def = itable.get(e.getSource());
            execPlus(def.getName(), def.getQuery());
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
                        lCurrentPath = f.getParent(); 
                        myCorese.load(lPath);
                        appendMsg("Loading file from path : " + f.getAbsolutePath() + "\n");
                        appendMsg(myCapturer.getContent() + "\ndone.\n\n");
                        // do not record because we do not want that this rule based be reloaded
                        // when we perform Engine/Reload
                        //ongletListener.getModel().addElement(lPath);
                        Date d1 = new Date();
                        boolean b = myCorese.runRuleEngine();
                        Date d2 = new Date();
                        System.out.println("Time: " + (d2.getTime()-d1.getTime())/1000.0);
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

    void saveGraph(String format) {
        Graph graph = myCorese.getGraph();
        Transformer transformer = Transformer.create(graph, format);
        try {
            save(transformer.transform());
        } catch (EngineException ex) {
            LOGGER.error(ex);
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
                write(str, myFile);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
        }
    }
    
    void write(String str, String path) throws IOException {
        // Créer un objet java.io.FileWriter avec comme argument le mon du fichier dans lequel enregsitrer
        FileWriter lu = new FileWriter(path);
        // Mettre le flux en tampon (en cache)
        BufferedWriter out = new BufferedWriter(lu);
        // Mettre dans le flux le contenu de la zone de texte
        out.write(str);
        // Fermer le flux
        out.close();
    }

    void runRules(boolean opt) throws EngineException {
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


    void display() {
        for (int i = 0; i < getOngletListener().getModel().getSize(); i++) {
            System.out.println("GUI: " + ongletListener.getModel().get(i).toString());
        }
    }
    
    void loadRDF() {
        loadDataset();
    }

    
    void loadDataset() {
        Filter FilterRDF  = new Filter("RDF", "rdf", "ttl", "trig", "jsonld", "html");
        Filter FilterRDFS = new Filter("RDFS/OWL", "rdfs", "owl", "ttl");
        Filter FilterOWL = new Filter("OWL", "owl");
        Filter FilterDS   = new Filter("Dataset", "rdf", "rdfs", "owl", "ttl", "html");        
        load(FilterRDF, FilterRDFS, FilterOWL, FilterDS);
    }

    void execWorkflow() {
        Filter FilterRDF = new Filter("Workflow", "ttl", "sw");
        load(FilterRDF, true, true, false);
    }
    
    void loadWorkflow(boolean run) {
        Filter FilterRDF = new Filter("Workflow", "ttl", "sw");
        load(FilterRDF, true, false, run);
    }
    
    /**
     * Charge un fichier dans CORESE
     */
    void load(Filter... filter) {
        load(false, false, false, filter);
    }
    

    /**
     * wf: load a Workflow
     * exec: run Workflow using std Worklow engine
     * run: set the queries in query panels an run the queries in the GUI
     */
    public void load(Filter filter, boolean wf, boolean exec, boolean run) {
        load(wf, exec, run, filter);
    }
    
    
    void load(boolean wf, boolean exec, boolean run, Filter... filter) {
        controler(LOAD);
        lPath = null;
        JFileChooser fileChooser = new JFileChooser(lCurrentPath);
        fileChooser.setMultiSelectionEnabled(true);
        for (Filter f : filter){
            fileChooser.addChoosableFileFilter(f);
        }
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
    
    void basicLoad(String path) {
        DefaultListModel model = getOngletListener().getModel();
        if (!model.contains(path)) {
            model.addElement(path);
        }
        appendMsg("Loading " + path + "\n");
        load(path);
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
        } catch (SafetyException ex) {
            LOGGER.error(ex);
            appendMsg(ex.toString());
        }
    }
    
    void defQuery(WorkflowProcess wp, boolean run) {
        if (wp.getProcessList() != null) {
            for (WorkflowProcess wf : wp.getProcessList()) {
                if (wf.isQuery()) {
                    defQuery(wf.getQueryProcess().getQuery(), wf.getPath(), run);
                } else {
                    defQuery(wf, run);
                }
            }
        }
    }

    String selectPath() {
        return selectPath(null);
    }
    
    String selectPath(String title, String... ext) {
        lPath = null;
        JFileChooser fileChooser = new JFileChooser(lCurrentPath);
        
        if (ext != null && ext.length>0) {
            Filter filter = new Filter(title, ext);
            fileChooser.addChoosableFileFilter(filter);
        }
        
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int returnValue = fileChooser.showOpenDialog(null);
        
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File l_Files[] = fileChooser.getSelectedFiles();

            DefaultListModel model = getOngletListener().getModel();
            for (File f : l_Files) {
                lPath = f.getAbsolutePath();
                lCurrentPath = f.getParent();
                return lPath;
            }
        }
        return null;
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
    
    void shex(boolean load) {
        String path = selectPath("Shex", "shex");
        if (path != null) {
            Shex shex = new Shex().setExtendShacl(shexExtend)
                    .setClosed(shexClosed)
                    .setExpCardinality(shexCard);
            String name = path.replace(".shex", "shex.ttl");
            try {
                shex.parse(path);
                System.out.println(shex.getStringBuilder());
                System.out.println("Result in: " + name);
                write(shex.getStringBuilder().toString(), name);
                appendMsg(String.format("Translate %s into %s\n", path, name));
                if (load) {
                    loadRDF(name);
                }
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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
            Date d1 = new Date();
            myCorese.load(fichier);
            Date d2 = new Date();
            appendMsg(myCapturer.getContent());
            System.out.println("Load time: " +(d2.getTime() - d1.getTime())/1000.0);
        } catch (EngineException |LoadException e) {
            appendMsg(e.toString());
            e.printStackTrace();
        }
    }

    /**
     * Crée un nouvel onglet requête avec le texte contenu dans un fichier
     */
    public String loadText() {
        return loadText(null);
    }

    public String loadText(String title, String... ext) {
        String str = "";
        JFileChooser fileChooser = new JFileChooser(lCurrentPath);
        
        if (ext != null && ext.length>0) {
            Filter filter = new Filter(title, ext);
            fileChooser.addChoosableFileFilter(filter);
        }
        
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
        textQuery = loadText("Query", "rq");
        newQuery(textQuery, getFileName());
    }
    
    void defQuery(String text, String name, boolean run){
        textQuery = text;
        MyJPanelQuery panel = newQuery(textQuery, name);
        if (run){
            panel.exec(this, text);
        }
    }

    public void loadPipe() {
        //Load and run a pipeline
        Filter FilterRUL = new Filter("RDF", "rdf", "ttl");
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
        Filter FilterRUL = new Filter("Rule" ,"rul", "brul");
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
        String path = cmd.get(Command.WORKFLOW);
        if (path != null) {
            execWF(path, false);
        }
        try {
            init();
        } catch (EngineException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }
    
    void init() throws EngineException {
        QueryProcess exec = QueryProcess.create(Graph.create());
        System.out.println("Import: SHACL");
        exec.imports(QueryProcess.SHACL);
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

    public MyJPanelQuery getPanel() {
        return current;
    }

    MyEvalListener getEvalListener() {
        return el;
    }

    public void setEvalListener(MyEvalListener el) {
        this.el = el;
    }

    public String getLCurrentPath() {
        return this.lCurrentPath;
    }

    public void setLCurrentPath(String lCurrentPath) {
        this.lCurrentPath = lCurrentPath;
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
        setSingleton(coreseFrame);
    }
    
    public void show(String text) {
        getPanel().display(text);
    }
    
    public static void display(String text) {
        if (getSingleton() != null) {
            getSingleton().appendMsg(text);
            getSingleton().appendMsg("\n");
        }
    }
    
    public static void newline() {
        if (getSingleton() != null) {
            getSingleton().appendMsg("\n");
        }
    }

    /**
     * @return the shexSemantics
     */
    public boolean isShexSemantics() {
        return shexSemantics;
    }

    /**
     * @param shexSemantics the shexSemantics to set
     */
    public void setShexSemantics(boolean shexSemantics) {
        this.shexSemantics = shexSemantics;
    }

    public static MainFrame getSingleton() {
        return singleton;
    }

    public static void setSingleton(MainFrame aSingleton) {
        singleton = aSingleton;
    }
}
