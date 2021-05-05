package fr.inria.corese.gui.shacl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.logging.log4j.core.config.AwaitCompletionReliabilityStrategy;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.shacl.Shacl;
import fr.inria.corese.core.transform.Transformer;
import fr.inria.corese.gui.core.MainFrame;
import fr.inria.corese.sparql.exceptions.EngineException;

public class MyJPanelShacl extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final int FontSize = 16;

    private MainFrame mainFrame;

    private JTextPane shaclEditor;
    private JTextArea shaclResult;

    public MyJPanelShacl(final MainFrame coreseFrame) {
        super();
        this.shaclEditor = new JTextPane();
        this.shaclResult = new JTextArea();
        this.mainFrame = coreseFrame;
        this.initComponents();
    }

    public void writeShacl() {
        String shaclEditorContent = getShaclEditorContent();

        JFileChooser filechoose = new JFileChooser();
        int resultatEnregistrer = filechoose.showDialog(filechoose, "Save");

        // If user clicks on save button
        if (resultatEnregistrer == JFileChooser.APPROVE_OPTION) {
            String pathSelectFile = filechoose.getSelectedFile().toString();

            // Write file
            try {
                FileWriter myWriter = new FileWriter(pathSelectFile);
                myWriter.write(shaclEditorContent);
                myWriter.close();
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null,
                        ex);
            }
        }
    }

    private void initComponents() {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));

        ///////////////////
        // Buttons panel //
        ///////////////////
        JPanel buttonsPanel = initButtonsPanel();
        add(buttonsPanel);

        //////////////////
        // Editor panel //
        //////////////////
        JPanel editorPanel = new JPanel();
        editorPanel.setLayout(new BorderLayout());

        // Split plane
        JScrollPane shaclEditor = this.initShaclEditor();
        JScrollPane shaclResult = this.initShaclResult();
        final JSplitPane splitPlane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, shaclEditor, shaclResult);
        splitPlane.setContinuousLayout(true);

        editorPanel.add(splitPlane);

        add(editorPanel);
    }

    private JPanel initButtonsPanel() {
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout());

        // validate button
        ActionListener buttonValidateListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                // Load shape graph
                Graph shapeGraph = Graph.create();
                Load ld = Load.create(shapeGraph);

                String shaclString = getShaclEditorContent();
                if (shaclString.strip().isEmpty()) {
                    setShaclResultContent("Error : SHACL document is empty.");
                    return;
                }

                try {
                    ld.loadString(getShaclEditorContent(), Load.TURTLE_FORMAT);
                } catch (LoadException e1) {
                    setShaclResultContent("Error : malformed SHACL document.");
                    e1.printStackTrace();
                    return;
                }

                // Evaluation
                Shacl shacl = new Shacl(mainFrame.getMyCorese().getGraph(), shapeGraph);
                Graph result = null;
                try {
                    result = shacl.eval();
                } catch (EngineException e2) {
                    setShaclResultContent("Error : engine exception.");
                    e2.printStackTrace();
                    return;
                }

                Transformer transformer = Transformer.create(result, Transformer.TURTLE);
                setShaclResultContent(transformer.toString());
            }
        };
        JButton buttonValidate = new JButton("Validate");
        buttonValidate.setMaximumSize(new Dimension(200, 200));
        buttonValidate.addActionListener(buttonValidateListener);
        buttonsPanel.add(buttonValidate);

        // Clear button
        ActionListener buttonClearListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int input = JOptionPane.showConfirmDialog(null, 
                    "Clear editor contents?",
                    "Confirmation",
                    JOptionPane.OK_CANCEL_OPTION
                );

                if (input == 0) {
                    setShaclEditorContent("");
                    setShaclResultContent("");
                }
            }
        };

        JButton buttonClear = new JButton("Clear");
        buttonClear.setMaximumSize(new Dimension(200, 200));
        buttonClear.addActionListener(buttonClearListener);
        buttonsPanel.add(buttonClear);

        // Load button
        ActionListener buttonLoadListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Select a Turtle file");
                fileChooser.setAcceptAllFileFilterUsed(false);
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Turtle", "ttl");
                fileChooser.addChoosableFileFilter(filter);
                int returnValue = fileChooser.showOpenDialog(null);

                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    String pathSelectFile = fileChooser.getSelectedFile().toString();
                    String content = null;
                    try {
                        content = new String(Files.readAllBytes(Paths.get(pathSelectFile)));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    setShaclEditorContent(content);
                }

            }

        };
        
        JButton buttonLoad = new JButton("Load");
        buttonLoad.setMaximumSize(new Dimension(200, 200));
        buttonLoad.addActionListener(buttonLoadListener);
        buttonsPanel.add(buttonLoad);

        // save button
        ActionListener buttonSaveListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                writeShacl();
            }
        };

        JButton buttonSave = new JButton("Save");
        buttonSave.setMaximumSize(new Dimension(200, 200));
        buttonSave.addActionListener(buttonSaveListener);
        buttonsPanel.add(buttonSave);

        return buttonsPanel;
    }

    private JScrollPane initShaclEditor() {
        // Line counter
        JTextArea lineCounter = new JTextArea(10, 2);
        lineCounter.setEditable(false);
        lineCounter.setFocusable(false);
        lineCounter.setBackground(new Color(230, 230, 230));
        lineCounter.setFont(new Font("Sanserif", Font.BOLD, FontSize));
        lineCounter.setText("\n1");

        // Shacl editor
        this.shaclEditor.setBorder(BorderFactory.createTitledBorder("Shacl editor:"));
        this.shaclEditor.setPreferredSize(new Dimension(400, 250));
        this.shaclEditor.setFont(new Font("Sanserif", Font.BOLD, FontSize));
        this.shaclEditor.getDocument().addDocumentListener(new DocumentListener() {

            private void updatelineCounter() {

                String editor_text = shaclEditor.getText();
                int nb_line = editor_text.length() - editor_text.replace("\n", "").length() + 2;

                String text = "\n";
                for (int i = 1; i < nb_line; i++) {
                    text += String.valueOf(i);
                    if (i != nb_line - 1) {
                        text += "\n";
                    }
                }
                lineCounter.setText(text);
            }

            @Override
            public void changedUpdate(DocumentEvent arg0) {
                updatelineCounter();
            }

            @Override
            public void insertUpdate(DocumentEvent arg0) {
                updatelineCounter();
            }

            @Override
            public void removeUpdate(DocumentEvent arg0) {
                updatelineCounter();
            }

        });

        JScrollPane scrollShaclEditor = new JScrollPane(this.shaclEditor, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollShaclEditor.setRowHeaderView(lineCounter);
        scrollShaclEditor.setViewportView(this.shaclEditor);
        return scrollShaclEditor;
    }

    private JScrollPane initShaclResult() {
        this.shaclResult.setBorder(BorderFactory.createTitledBorder("Results:"));
        this.shaclResult.setEditable(false);
        this.shaclResult.setFont(new Font("Sanserif", Font.BOLD, FontSize));

        JScrollPane scrollShaclResult = new JScrollPane(this.shaclResult, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        return scrollShaclResult;
    }

    private String getShaclEditorContent() {
        return this.shaclEditor.getText();
    }

    private void setShaclEditorContent(String content) {
        this.shaclEditor.setText(content);
    }

    private void setShaclResultContent(String content) {
        this.shaclResult.setText(content);
    }

}