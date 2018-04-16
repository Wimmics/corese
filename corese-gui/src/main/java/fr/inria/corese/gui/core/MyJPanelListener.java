package fr.inria.corese.gui.core;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.DateFormat;
import java.util.Date;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle;
import javax.swing.text.Document;


/**
 * Onglet Listener avec tout ce qu'il contient
 *
 * @author saguilel
 *
 */
public class MyJPanelListener extends JPanel implements MouseListener, ActionListener {

    private static final long serialVersionUID = 1L;
    private JButton buttonRefresh;
    private JButton buttonDebug;
    private JLabel labelLoadedFiles;
    private JLabel labelLogs;
    private JPanel paneListener;
    private JScrollPane scrollPaneList;
    private JScrollPane scrollPaneLogs;
    private JTextPane textPaneLogs;
    private DefaultListModel model;
    private boolean frameShow = true;
    protected static JList listLoadedFiles;		//list présent dans le JpanelListener
    private JFrameDebug myPop;
    MyPopup popupMenu = new MyPopup();				//On instancie la pop-up

    /**
     * Crée le Panel Listener que l'on ajoutera au conteneur d'onglets dans la
     * fenêtre principale
     *
     * @param coreseFrame
     */
    public MyJPanelListener(final MainFrame coreseFrame) {
        super();

        paneListener = new JPanel();
        setLayout(new BorderLayout(5, 5));
        add(paneListener, BorderLayout.CENTER);
        labelLoadedFiles = new JLabel();
        buttonRefresh = new JButton();
        buttonDebug = new JButton();
        scrollPaneList = new JScrollPane();

        model = new DefaultListModel();
        listLoadedFiles = new JList(model);
        listLoadedFiles.addMouseListener(this);
        scrollPaneList = new JScrollPane(listLoadedFiles);
        scrollPaneList.setPreferredSize(new Dimension(100, 120));

        labelLogs = new JLabel();
        scrollPaneLogs = new JScrollPane();
        textPaneLogs = new JTextPane();

        labelLoadedFiles.setText("Loaded files:");


        // Appelle la fonction Refresh
        buttonRefresh.setText("Reload");
        ActionListener l_RefreshListener = new ActionListener() {
            public void actionPerformed(ActionEvent l_Event) {
                refresh(coreseFrame);
            }
        };
        buttonRefresh.addActionListener(l_RefreshListener);

        myPop = new JFrameDebug(coreseFrame);
        WindowAdapter l_windowsClose = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                frameShow = true;
            }

            @Override
            public void windowClosed(WindowEvent e) {
                frameShow = true;
            }
        };
        myPop.addWindowListener(l_windowsClose);

        buttonDebug.setText("Debug");
        ActionListener l_DebugListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (frameShow) {
                    myPop.setVisible(true);
                    frameShow = false;
                } else {
                    myPop.setVisible(false);
                    frameShow = true;
                }
            }
        };
        buttonDebug.addActionListener(l_DebugListener);

        //Ecoute le reload du pop-up
        ActionListener l_ReloadFileListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                reloadFileSelected(coreseFrame);
            }
        };
        popupMenu.getReload().addActionListener(l_ReloadFileListener);

        //Ecoute le delete du pop-up
        ActionListener l_DeleteFileListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getTextPaneLogs().setText(""); 	//supprime les logs de la fenêtre
                appendMsg("Delete Files : " + coreseFrame.getOngletListener().getModel().getElementAt(getListLoadedFiles().getSelectedIndex()) + "\n" + coreseFrame.getMyCapturer().getContent() + "\ndone.\n", null);
                getModel().remove(getListLoadedFiles().getSelectedIndex());		//vide l'élément sur lequel on double clic de la liste 
                refresh(coreseFrame);
            }
        };
        popupMenu.getDelete().addActionListener(l_DeleteFileListener);

        //Ecoute le about du pop-up
        ActionListener l_AboutFileListener = e -> {
            File file = new File(coreseFrame.getOngletListener().getModel().getElementAt(getListLoadedFiles().getSelectedIndex()).toString());
            String info = null, b = "Bytes";
            double size;
            String ext = MyCellRenderer.extension(file);
            if (ext.equals("rul")) {
                info = "Fichier contenant les règles d'inférences";
            } else if (ext.equals("rdfs")) {
                info = "Fichier contenant les éléments de base pour décrire l'ontologie (classe, propriétés) ";
            } else if (ext.equals("rdf")) {
                info = "Fichier qui permet d'exploiter le fichier .rdfs, il permet de définir chaque entité sous la forme d'un triplets (sujet, prédicat, objet)";

            }

            size = file.length();
            if (size > 1024) {
                size = size / 1024;
                b = "KB";
            }
            if (size > 2048) {
                size = size / 2048;
                b = "MB";
            }
            if (size > 4096) {
                size = size / 4096;
                b = "GB";
            }

            Date d = new Date(file.lastModified());
            String message = "Name : 		" + file
                    + "\nType : 		" + ext.toUpperCase() + " file"
                    + "\nMore information : " + info
                    + "\nModified :    " + DateFormat.getDateInstance(DateFormat.FULL).format(d)
                    + "\nSize :       " + String.format("8.1f", size) + " " + b
                    + "";
            JOptionPane.showMessageDialog(null, message);



        };
        popupMenu.getInfos().addActionListener(l_AboutFileListener);


        labelLogs.setText("Logs:");

        // Affiche les traces voulues 
        textPaneLogs.setEditable(false);
        textPaneLogs.setContentType("text/plain");
        textPaneLogs.setEditorKitForContentType("text/plain", textPaneLogs.getEditorKitForContentType("text/plain"));
        textPaneLogs.setFont(new Font("Monospaced", Font.PLAIN, 12));
        scrollPaneLogs.setViewportView(textPaneLogs);

        // Mise en forme
        GroupLayout pane_listenerLayout = new GroupLayout(paneListener);
        paneListener.setLayout(pane_listenerLayout);

        GroupLayout.ParallelGroup hParallel1 = pane_listenerLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.SequentialGroup hSeq1 = pane_listenerLayout.createSequentialGroup();
        GroupLayout.ParallelGroup hParallel2 = pane_listenerLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.SequentialGroup hSeq2 = pane_listenerLayout.createSequentialGroup();
        GroupLayout.ParallelGroup hParallel3 = pane_listenerLayout.createParallelGroup(GroupLayout.Alignment.TRAILING);
        GroupLayout.SequentialGroup hSeq3 = pane_listenerLayout.createSequentialGroup();
        GroupLayout.SequentialGroup hSeq4 = pane_listenerLayout.createSequentialGroup();
        GroupLayout.SequentialGroup hSeq5 = pane_listenerLayout.createSequentialGroup();

        hParallel3.addGroup(hSeq4);
        hParallel3.addGroup(hSeq5);
        hSeq3.addGap(18, 18, 18);
        hSeq3.addGroup(hParallel3);
        hSeq2.addComponent(labelLoadedFiles);
        hSeq2.addGap(378);


        hSeq2.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 257, Short.MAX_VALUE);
        hSeq2.addComponent(buttonDebug);
        hSeq2.addGap(18, 18, 18);
        hSeq2.addComponent(buttonRefresh);
        hParallel2.addGroup(hSeq2);
        hParallel2.addComponent(scrollPaneList, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        hParallel2.addComponent(labelLogs);
        hParallel2.addGroup(hSeq3);
        hParallel2.addComponent(scrollPaneLogs, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        hSeq1.addContainerGap();
        hSeq1.addGroup(hParallel2);
        hParallel1.addGroup(hSeq1);

        pane_listenerLayout.setHorizontalGroup(hParallel1);


        GroupLayout.ParallelGroup vParallel1 = pane_listenerLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.ParallelGroup vParallel2 = pane_listenerLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.ParallelGroup vParallel3 = pane_listenerLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.ParallelGroup vParallel4 = pane_listenerLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.ParallelGroup vParallel5 = pane_listenerLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.SequentialGroup vSeq1 = pane_listenerLayout.createSequentialGroup();
        GroupLayout.SequentialGroup vSeq2 = pane_listenerLayout.createSequentialGroup();

        vSeq2.addGroup(vParallel4);
        vSeq2.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);


        vSeq2.addGroup(vParallel5);
        vParallel3.addGroup(vSeq2);
        vParallel2.addComponent(labelLoadedFiles);
        vParallel2.addComponent(buttonRefresh);
        vParallel2.addComponent(buttonDebug);
        vSeq1.addContainerGap();
        vSeq1.addGroup(vParallel2);
        vSeq1.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        vSeq1.addComponent(scrollPaneList, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
        vSeq1.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        vSeq1.addGroup(vParallel3);
        vSeq1.addGap(1, 1, 1);
        vSeq1.addComponent(labelLogs);
        vSeq1.addContainerGap();
        vSeq1.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        vSeq1.addComponent(scrollPaneLogs, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        vSeq1.addContainerGap();
        vParallel1.addGroup(vSeq1);

        pane_listenerLayout.setVerticalGroup(vParallel1);

    }

    public JList getListLoadedFiles() {
        return listLoadedFiles;
    }

    /**
     * Ré-initialise CORESE et Recharge tous les fichiers précédemment chargés
     *
     * @param coreseFrame
     */
    public void refresh(MainFrame coreseFrame) {
        //coreseFrame.setMyCoreseNewInstance();
        appendMsg("reload ...\n" + coreseFrame.getMyCapturer().getContent() + "\n", coreseFrame);
        coreseFrame.setMyCoreseNewInstance();
        if (model.getSize() != 0) {
            for (int i = 0; i < model.getSize(); i++) {
                String file = (String) model.getElementAt(i);
                appendMsg("Loading: " + file + "\n", coreseFrame );
                if (file.endsWith(".rul")) {
                    coreseFrame.loadRule(file);
                } else {
                    coreseFrame.load(file);
                }
            }
        }
    }

    /**
     * Affiche les messages dans le textPaneLogs
     *
     * @param msg
     * @param coreseFrame
     */
    private void appendMsg(String msg, MainFrame coreseFrame) {
        final Document doc = textPaneLogs.getDocument();
        try {
            doc.insertString(textPaneLogs.getDocument().getLength(), msg , null);
        } catch (Exception l_InnerException) {
            coreseFrame.getLogger().fatal("Output capture problem:", l_InnerException);
        }
    }

    //getteurs et setteurs utiles
    public void setPaneListener(JPanel pane_listener) {
        this.paneListener = pane_listener;
    }

    public DefaultListModel getModel() {
        return model;
    }

    public JTextPane getTextPaneLogs() {
        return textPaneLogs;
    }

    public JScrollPane getScrollPaneLog() {
        return scrollPaneLogs;
    }

    /**
     * Permet de reload le fichier sélectionner dans la liste par
     * l'intermédiaire du pop-up
     *
     * @param coreseFrame
     */
    public void reloadFileSelected(MainFrame coreseFrame) {
        //coreseFrame.setMyCoreseNewInstance();
        Object files = coreseFrame.getOngletListener().getModel().getElementAt(getListLoadedFiles().getSelectedIndex());
        String ext = MyCellRenderer.extension(files);
        if (ext.equals("rul")) {
            coreseFrame.loadRule((String) coreseFrame.getOngletListener().getModel().getElementAt(getListLoadedFiles().getSelectedIndex()));
        } else if (ext.equals("rdfs") || ext.equals("owl")) {
            coreseFrame.load((String) coreseFrame.getOngletListener().getModel().getElementAt(getListLoadedFiles().getSelectedIndex()));
        } else if (ext.equals("rdf")) {
            coreseFrame.loadRDF((String) coreseFrame.getOngletListener().getModel().getElementAt(getListLoadedFiles().getSelectedIndex()));
        }
    }

    @Override
    /**
     * Permet d'afficher une fenêtre popups pour le fichier lors d'un
     * double-clic
     */
    public void mouseClicked(MouseEvent e) {

        if (e.getButton() == MouseEvent.BUTTON3 && getModel().getSize() != 0) {
            popupMenu.getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
            MyCellRenderer myCell = new MyCellRenderer();
            int index = myCell.getMaList().locationToIndex(e.getPoint());
            myCell.getMaList().setSelectedIndex(index);
        }


    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }
}
