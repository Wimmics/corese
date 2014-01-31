package fr.inria.acacia.corese.gui.core;

import fr.inria.acacia.corese.gui.query.MyJPanelQuery;
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
import javax.swing.JOptionPane;
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

import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;

import fr.inria.acacia.corese.api.IEngine;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.acacia.corese.gui.event.MyEvalListener;
//import fr.inria.acacia.corese.gui.event.MyLoadListener;
//import fr.inria.acacia.corese.gui.event.MyQueryListener;
import fr.inria.acacia.corese.gui.query.Buffer;
import fr.inria.edelweiss.kgengine.GraphEngine;
import fr.inria.edelweiss.kgram.event.Event;
import java.util.logging.Level;

/**
 * Fenêtre principale, avec le conteneur d'onglets et le menu
 */
public class MainFrame extends JFrame implements ActionListener {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final String TITLE = "Corese/KGRAM 3.1 - Wimmics Inria I3S - 2014-01-29";
    // On déclare notre conteneur d'onglets
    protected static JTabbedPane conteneurOnglets;
    // Compteur pour le nombre d'onglets query créés 
    private ArrayList<Integer> nbreTab = new ArrayList<Integer>();
    private String l_path_courant = "user/home";
    private String l_path;
    //Variable true ou false pr déterminer le mode Kgram ou Corese	
    private boolean isKgram = true; //false;
    // Pour le menu 
    private JMenuItem loadRDF;
    private JMenuItem loadRDFs;
    private JMenuItem loadQuery;
    private JMenuItem loadPipe;
    private JMenuItem loadRule;
    private JMenuItem loadStyle;
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
    private JMenuItem runRules;
    private JMenuItem reset;
    private ButtonGroup myRadio;
    private JRadioButton kgramBox;
    private JRadioButton coreseBox;
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
    private JCheckBox checkBoxQuery;
    private JCheckBox checkBoxRule;
    private JCheckBox checkBoxVerbose;
    private JCheckBox checkBoxLoad;
    private JMenuItem validate;
//    private MyLoadListener ell;
//    private MyQueryListener eql;
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
    private String defaultTextQuery = "SELECT ?x ?t WHERE\n{\n ?x rdf:type ?t\n}";
//	private String defaultTextQuery = 
//		"prefix c: <http://www.inria.fr/acacia/comma#>\n"+
//		"construct {?x ?p ?y}\n"+
//		"where {\n"+
//		"?doc c:CreatedBy ?x ?x ?p ?y"+
//		"}";
//	
    // Relatif à Corese 
//	private EngineFactory ef = new EngineFactory();
    private IEngine myCorese = null;
    private CaptureOutput myCapturer = null;
    private final Logger logger = Logger.getLogger(MainFrame.class.getName());
    private MyEvalListener el;
    Buffer buffer;

    /**
     * Crée la fenêtre principale, initialise Corese
     *
     * @param aCapturer
     * @param p_PropertyPath
     */
    public MainFrame(CaptureOutput aCapturer, String p_PropertyPath) {
        super();
        this.setTitle(TITLE); //+ Corese.version + " - " + Corese.date );
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(900, 700);
        this.setMinimumSize(this.getSize());
        this.setLocationRelativeTo(null);
        this.setResizable(true);




        saveStylesheet =
                "graph {\n"
                + "\t  color:grey;\n"
                + "}\n"
                + "node {\n"
                + "\t  text-size:9;\n"
                + "\t  text-color:black;\n"
                + "\t  text-style:bold;\n"
                + "\t  text-align:center;\n"
                + "\t  width:17;\n"
                + "\t  color:lightblue;\n"
                + "\t  node-shape:text-ellipse;\n"
                + "}\n"
                + "node.Literal {\n"
                + "\t  text-size:9;\n"
                + "\t  text-color:black;\n"
                + "\t  text-style:bold;\n"
                + "\t  text-align:center;\n"
                + "\t  width:17;\n"
                + "\t  color:orange;\n"
                + "\t  node-shape:text-box;\n"
                + "}\n"
                + "node.Blank {\n"
                + "\t  text-size:9;\n"
                + "\t  text-color:black;\n"
                + "\t  text-style:bold;\n"
                + "\t  text-align:center;\n"
                + "\t  width:17;\n"
                + "\t  color:yellow;\n"
                + "\t  node-shape:text-ellipse;\n"
                + "}\n"
                + "node.Class {\n"
                + "\t  text-size:9;\n"
                + "\t  text-color:black;\n"
                + "\t  text-style:bold;\n"
                + "\t  text-align:center;\n"
                + "\t  width:17;\n"
                + "\t  color:blue;\n"
                + "\t  node-shape:text-ellipse;\n"
                + "}\n"
                + "edge {\n"
                + "\t  text-color:black;\n"
                + "\t  text-size:8;\n"
                + "\t  width:1;\n"
                + "\t  color:grey;\n"
                + "\t  text-align:center;\n"
                + "}";


        defaultStylesheet = saveStylesheet;


        //Initialise Corese
        myCapturer = aCapturer;
//		ef.setProperty(EngineFactory.PROPERTY_FILE, p_PropertyPath);
//	    ef.setProperty(EngineFactory.ENGINE_RULE_RUN, "true"); 
        // myCorese = ef.newInstance();
        setMyCoreseNewInstance();
//	    myCorese.setProperty(EngineFactory.ENGINE_NAMESPACE,
//					 "c http://www.inria.fr/acacia/comma# ");

        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");


        // this.setIconImage(new ImageIcon(java.lang.ClassLoader.getSystemResource(resourcePath + "corese_icon.gif")).getImage());
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
                    // s : texte par défaut dans la requête
                    textQuery = defaultTextQuery;
                    //Crée un nouvel onglet Query
                    newQuery();
                }
            }
        });
        this.setVisible(true);

        addWindowListener(new WindowAdapter() {
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



    }

    /**
     * Affiche du texte dans le panel logs *
     */
    public void appendMsg(String msg) {
        final Document doc = ongletListener.getTextPaneLogs().getDocument();
        try {
            doc.insertString(ongletListener.getTextPaneLogs().getDocument().getLength(), msg, null);

            //Place l'ascenceur en bas à chaque ajout de texte
            ongletListener.getScrollPaneLog().revalidate();
            int length = ongletListener.getTextPaneLogs().getDocument().getLength();
            ongletListener.getTextPaneLogs().setCaretPosition(length);
        } catch (Exception l_InnerException) {
            logger.fatal("Output capture problem:", l_InnerException);
        }
    }
    int n = 0;

    /**
     * Crée un onglet Query *
     */
    public void newQuery() {
        n++;
        //supprime l'onglet "+", ajoute un onglet Query, puis recrée l'onglet "+" à la suite	
        conteneurOnglets.remove(plus);
        MyJPanelQuery temp = new MyJPanelQuery(this);

        monTabOnglet.add(temp);
        nbreTab.add(n);
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

        loadQuery = new JMenuItem("Load Query");
        loadQuery.addActionListener(this);

        loadPipe = new JMenuItem("Load Pipeline");
        loadPipe.addActionListener(this);

        loadStyle = new JMenuItem("Load Style");
        loadStyle.addActionListener(this);

        saveQuery = new JMenuItem("Save Query");
        saveQuery.addActionListener(this);

        saveResult = new JMenuItem("Save Result");
        saveResult.addActionListener(this);

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
//        coreseBox.addActionListener(this);
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
        JMenu aboutMenu = new JMenu("?");

        //On ajoute tout au menu
        fileMenu.add(loadRDFs);
        fileMenu.add(loadRule);
        fileMenu.add(loadRDF);
        fileMenu.add(loadQuery);
        fileMenu.add(loadPipe);
        fileMenu.add(saveQuery);
        fileMenu.add(saveResult);
        fileMenu.add(loadAndRunRule);
        fileMenu.add(loadStyle);

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
        engineMenu.add(reset);
        engineMenu.add(refresh);
//        engineMenu.add(coreseBox);
//        myRadio.add(coreseBox);
        engineMenu.add(kgramBox);
        myRadio.add(kgramBox);
        aboutMenu.add(apropos);
        aboutMenu.add(tuto);
        aboutMenu.add(doc);




        aboutMenu.add(help);
        ActionListener l_HelpListener = new ActionListener() {
            public void actionPerformed(ActionEvent l_Event) {
                set(Event.HELP);
            }
        };
        help.addActionListener(l_HelpListener);

        debugMenu.add(next);
        ActionListener l_NextListener = new ActionListener() {
            public void actionPerformed(ActionEvent l_Event) {
                set(Event.STEP);
            }
        };
        next.addActionListener(l_NextListener);


        debugMenu.add(complete);
        ActionListener l_SkipListener = new ActionListener() {
            public void actionPerformed(ActionEvent l_Event) {
                set(Event.COMPLETE);

            }
        };
        complete.addActionListener(l_SkipListener);


        debugMenu.add(forward);
        ActionListener l_PlusListener = new ActionListener() {
            public void actionPerformed(ActionEvent l_Event) {
                set(Event.FORWARD);

            }
        };
        forward.addActionListener(l_PlusListener);

        debugMenu.add(map);
        ActionListener l_MapListener = new ActionListener() {
            public void actionPerformed(ActionEvent l_Event) {
                set(Event.MAP);

            }
        };
        map.addActionListener(l_MapListener);

        debugMenu.add(success);
        ActionListener l_SuccessListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                set(Event.SUCCESS);
            }
        };
        success.addActionListener(l_SuccessListener);

        debugMenu.add(quit);
        ActionListener l_QuitListener = new ActionListener() {
            public void actionPerformed(ActionEvent l_Event) {
                set(Event.QUIT);
            }
        };
        quit.addActionListener(l_QuitListener);

        debugMenu.add(checkBoxLoad);
        checkBoxLoad.addItemListener(
                new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
//        				if(checkBoxLoad.isSelected() == true){        					
//        					ell = MyLoadListener.create();
//        					getMyCorese().addEventListener(ell);
//        				}
//        				else{
//        					getMyCorese().removeEventListener(ell);        				
//        				}
            }
        });


        debugMenu.add(checkBoxQuery);
        checkBoxQuery.addItemListener(
                new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
//        				if(checkBoxQuery.isSelected() == true){
//        					eql = MyQueryListener.create();
//        					getMyCorese().addEventListener(eql);
//        				}
//        				else{        					
//        					getMyCorese().removeEventListener(eql);        				
//        				}
            }
        });

        debugMenu.add(checkBoxRule);
        checkBoxRule.addItemListener(
                new ItemListener() {
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
            public void actionPerformed(ActionEvent l_Event) {
                l_path = null;
                JFileChooser fileChooser = new JFileChooser(l_path_courant);
                fileChooser.setMultiSelectionEnabled(true);
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File l_Files[] = fileChooser.getSelectedFiles();
                    for (File f : l_Files) {
                        l_path = f.getAbsolutePath();
                        l_path_courant = f.getParent();   //recupere le dossier parent du fichier que l'on charge

                        if (l_path != null) {
//						        	myCorese.validate(l_path);
//						        	for (fr.inria.acacia.corese.event.Event e : myCorese.report()){
//							        	appendMsg(e.toString());
//						        	}
                        }
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
        menuBar.add(aboutMenu);


        setJMenuBar(menuBar);



        //S'il n'y a pas encore d'onglet Query ces options sont inutilisables
        if (nbreTab.size() == 0) {
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

    //Actions du menu
    public void actionPerformed(ActionEvent e) {
        //Appelle la fonction pour le chargement d'un fichier RDFS/OWL
        if (e.getSource() == loadRDFs) {
            loadRDFs();

        } //Appelle la fonction pour le chargement d'un fichier query
        else if (e.getSource() == loadQuery) {
            loadQuery();
        } else if (e.getSource() == loadPipe) {
            loadPipe();
        } //Appelle la fonction pour le chargement d'un fichier rule
        else if (e.getSource() == loadRule) {
            loadRule();
        } //Appelle la fonction pour le chargement d'un fichier RDF
        else if (e.getSource() == loadRDF) {
            loadRDF();
        } //sauvegarde la requête dans un fichier texte (.txt)
        else if (e.getSource() == saveQuery) {

            // Créer un JFileChooser
            JFileChooser filechoose = new JFileChooser();
            // Le bouton pour valider l’enregistrement portera la mention enregistrer
            String approve = "Save";
            int resultatEnregistrer = filechoose.showDialog(filechoose, approve); // Pour afficher le JFileChooser…
            // Si l’utilisateur clique sur le bouton enregistrer
            if (resultatEnregistrer == JFileChooser.APPROVE_OPTION) {
                // Récupérer le nom du fichier qu’il a spécifié
                String myFile = (filechoose.getSelectedFile().toString());
                // Si ce nom de fichier finit par .txt ou .TXT, ne rien faire et passer à la suite
                if (!myFile.endsWith(".txt") || !myFile.endsWith(".TXT")) {
                    myFile = myFile + ".txt";
                    try {
                        // Créer un objet java.io.FileWriter avec comme argument le mon du fichier dans lequel enregsitrer
                        FileWriter lu = new FileWriter(myFile);
                        // Mettre le flux en tampon (en cache)
                        BufferedWriter out = new BufferedWriter(lu);
                        // Mettre dans le flux le contenu de la zone de texte
                        out.write(current.getTextPaneQuery().getText());
                        // Fermer le flux 
                        out.close();

                    } catch (IOException er) {
                        er.printStackTrace();
                    }
                }
            }
        } else if (e.getSource() == loadStyle) {
            String style = loadText();
            defaultStylesheet = style;
        } //Sauvegarde le résultat sous forme XML dans un fichier texte
        else if (e.getSource() == saveResult) {
            JFileChooser filechoose = new JFileChooser();
            // Le bouton pour valider l’enregistrement portera la mention enregistrer
            String approve = "Save";
            int resultatEnregistrer = filechoose.showDialog(filechoose, approve); // Pour afficher le JFileChooser…
            // Si l’utilisateur clique sur le bouton enregistrer
            if (resultatEnregistrer == JFileChooser.APPROVE_OPTION) {
                // Récupérer le nom du fichier qu’il a spécifié
                String myFile = filechoose.getSelectedFile().toString();
                // Si ce nom de fichier finit par .txt ou .TXT, ne rien faire et passer à la suite
                if (!myFile.endsWith(".txt") || !myFile.endsWith(".TXT")) {
                    myFile = myFile + ".txt";
                    try {
                        // Créer un objet java.io.FileWriter avec comme argument le mon du fichier dans lequel enregsitrer
                        FileWriter lu = new FileWriter(myFile);
                        // Mettre le flux en tampon (en cache)
                        BufferedWriter out = new BufferedWriter(lu);
                        // Mettre dans le flux le contenu de la zone de texte
                        out.write(current.getTextAreaXMLResult().getText());
                        // Fermer le flux 
                        out.close();

                    } catch (IOException er) {
                        er.printStackTrace();
                    }
                }

            }
        } // Charge et exécute une règle directement
        else if (e.getSource() == loadAndRunRule) {
            l_path = null;
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(true);
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File l_Files[] = fileChooser.getSelectedFiles();
                for (File f : l_Files) {
                    l_path = f.getAbsolutePath();
                    if (l_path != null) {
                        try {
                            myCorese.load(l_path);
                            appendMsg("Loading file from path : " + f.getAbsolutePath() + "\n");
                            appendMsg(myCapturer.getContent() + "\ndone.\n\n");
                            ongletListener.getModel().addElement(l_path);
                            myCorese.runRuleEngine();
                            appendMsg("\n rules applied... \n" + myCapturer.getContent() + "\ndone.\n");
                        } catch (EngineException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        } // Couper, copier, coller
        else if (e.getSource() == cut) {
            if (nbreTab.size() != 0) {
                current.getTextPaneQuery().cut();
            }
        } //utilisation de la presse papier pour le copier coller
        else if (e.getSource() == copy) {
            if (nbreTab.size() != 0) {
                current.getTextPaneQuery().copy();
            }
        } else if (e.getSource() == paste) {
            if (nbreTab.size() != 0) {
                current.getTextPaneQuery().paste();
            }
        } //Dupliquer une requête
        else if (e.getSource() == duplicate) {
            if (nbreTab.size() != 0) {
                String toDuplicate;
                toDuplicate = current.getTextPaneQuery().getText();
                textQuery = toDuplicate;
                newQuery();
            }
        } //Dupliquer une requête à partir du texte sélectionné
        else if (e.getSource() == duplicateFrom) {
            if (nbreTab.size() != 0) {
                String toDuplicate;
                toDuplicate = current.getTextPaneQuery().getSelectedText();
                textQuery = toDuplicate;
                newQuery();
            }
        } //Commente une sélection dans la requête
        else if (e.getSource() == comment) {
            if (nbreTab.size() != 0) {
                String line = "";
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
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
                current.getTextPaneQuery().setText(result);
            }
        } //crée un nouvel onglet requête
        else if (e.getSource() == newQuery) {
            textQuery = defaultTextQuery;
            newQuery();
        } //Applique les règles chargées
        else if (e.getSource() == runRules) {
            myCorese.runRuleEngine();
            appendMsg("\n rules applied... \n" + myCapturer.getContent() + "\ndone.\n");
        } //Remet tout à zéro
        else if (e.getSource() == reset) {
            ongletListener.getTextPaneLogs().setText("");
            ongletListener.getListLoadedFiles().removeAll();
            ongletListener.getModel().removeAllElements();
            setMyCoreseNewInstance();
            appendMsg("reset... \n" + myCapturer.getContent() + "\ndone.\n");
        } //Recharge tous les fichiers déjà chargés
        else if (e.getSource() == refresh) {
            ongletListener.refresh(this);
        } else if (e.getSource() == apropos) {
            JOptionPane.showMessageDialog(apropos, "Corese");
        } else if (e.getSource() == tuto) {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                try {
                    Desktop.getDesktop().browse(new URI("http://www-sop.inria.fr/teams/edelweiss/wiki/wakka.php?wiki=CoreseTutorial"));
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (URISyntaxException e1) {
                    e1.printStackTrace();
                }
            }
        } else if (e.getSource() == doc) {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                try {
                    Desktop.getDesktop().browse(new URI("http://graphstream.sourceforge.net/tutorials/tut105.html"));
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (URISyntaxException e1) {
                    e1.printStackTrace();
                }
            }
        } else if (e.getSource() == kgramBox) {
            isKgram = true;
            //DatatypeMap.setLiteralAsString(true);
            for (int i = 0; i < monTabOnglet.size(); i++) {
                MyJPanelQuery temp = monTabOnglet.get(i);
                temp.getButtonTKgram().setEnabled(true);
            }
        }

//		else if(e.getSource()==coreseBox ){
//			isKgram=false;
//			//DatatypeMap.setLiteralAsString(true);
//			for(int i=0;i<monTabOnglet.size();i++){
//				MyJPanelQuery temp = monTabOnglet.get(i);
//				temp.getButtonTKgram().setEnabled(false);
//			}	
//		}

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
        Filter FilterRDF = new Filter(new String[]{"rdf", "ttl"}, "RDF files (*.rdf,*.ttl)");
        load(FilterRDF);
    }

    public void loadRDFs() {
        Filter FilterRDFS = new Filter(new String[]{"rdfs", "owl", "ttl"}, "les fichiers RDFS/OWL (*.rdfs,*.owl,*.ttl)");
        load(FilterRDFS);
    }

    /**
     * Charge un fichier dans CORESE
     */
    public void load(Filter filter) {
        l_path = null;
        JFileChooser fileChooser = new JFileChooser(l_path_courant);
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.addChoosableFileFilter(filter);
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File l_Files[] = fileChooser.getSelectedFiles();

            DefaultListModel model = getOngletListener().getModel();
            for (File f : l_Files) {
                l_path = f.getAbsolutePath();
                l_path_courant = f.getParent();
                if (l_path != null) {
                    if (!model.contains(l_path)) {
                        model.addElement(l_path);
                    }
                    appendMsg("Loading " + extension(l_path) + " File from path : " + l_path + "\n");
                    load(l_path);
                }
            }
        }
    }

    public void load(String fichier) {
        try {
            myCorese.load(fichier);
            appendMsg(myCapturer.getContent());
        } catch (EngineException e) {
            e.printStackTrace();
        }
    }

    /**
     * Crée un nouvel onglet requête avec le texte contenu dans un fichier
     */
    public String loadText() {
        String str = "";
        JFileChooser fileChooser = new JFileChooser(l_path_courant);
        File selectedFile;
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            //Voici le fichier qu'on a selectionné
            selectedFile = fileChooser.getSelectedFile();
            l_path_courant = selectedFile.getParent();
            try {
                FileInputStream fis = new FileInputStream(selectedFile);
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
            } catch (IOException err) {
            }
        }
        return str;
    }

    public void loadQuery() {
        textQuery = loadText();
        newQuery();
    }

    public void loadPipe() {
        //Load and run a pipeline
        Filter FilterRUL = new Filter(new String[]{"rdf", "ttl"}, "rdf files (*.rdf,*.ttl)");
        JFileChooser fileChooser = new JFileChooser(l_path_courant);
        fileChooser.addChoosableFileFilter(FilterRUL);
        File selectedFile;
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            //Voici le fichier qu'on a selectionné
            selectedFile = fileChooser.getSelectedFile();
            l_path_courant = selectedFile.getParent();
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
        } catch (EngineException e) {
            e.printStackTrace();
        }
    }

    public void loadRDF(String fichier) {
        try {
            myCorese.load(fichier);
            appendMsg(myCapturer.getContent() + "\ndone.\n\n");
        } catch (EngineException e) {
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

    public IEngine getMyCorese() {
        return myCorese;
    }

    //Réinitialise Corese
    public void setMyCoreseNewInstance() {
        //if (isKgraph){
        myCorese = GraphEngine.create();
        //}
//		else {
//			myCorese = ef.newInstance();
//		}
    }

    public Logger getLogger() {
        return logger;
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

    public static void main(String[] p_args) {

        CaptureOutput aCapturer = new CaptureOutput();
        Logger.getLogger("fr.inria.acacia.corese").addAppender(new WriterAppender(new PatternLayout("%m%n"), aCapturer));
        MainFrame coreseFrame = null;
        if (p_args.length > 0) {
            coreseFrame = new MainFrame(aCapturer, p_args[0]);
        } else {
            coreseFrame = new MainFrame(aCapturer, null);
        }
        coreseFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        MyJPanelListener.listLoadedFiles.setCellRenderer(new MyCellRenderer());


    }
}
