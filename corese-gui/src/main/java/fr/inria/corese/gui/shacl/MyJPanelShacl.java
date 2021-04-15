package fr.inria.corese.gui.shacl;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;

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

    private MainFrame MainFrame;

    private JTextPane shaclEditor;
    private JTextArea shaclResult;

    public MyJPanelShacl(final MainFrame coreseFrame) {
        super();
        this.shaclEditor = new JTextPane();
        this.shaclResult = new JTextArea();
        this.MainFrame = coreseFrame;
        this.initComponents();
    }

    private void initComponents() {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));

        /////////////////////////////
        // Configure button pannel //
        /////////////////////////////
        JPanel buttonsPannel = new JPanel();
        buttonsPannel.setLayout(new FlowLayout());

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
                Shacl shacl = new Shacl(MainFrame.getMyCorese().getGraph(), shapeGraph);
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
        buttonsPannel.add(buttonValidate);

        // Clear button
        ActionListener buttonClearListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setShaclEditorContent("");
                setShaclResultContent("");
            }
        };

        JButton buttonClear = new JButton("Clear");
        buttonClear.setMaximumSize(new Dimension(200, 200));
        buttonClear.addActionListener(buttonClearListener);
        buttonsPannel.add(buttonClear);

        add(buttonsPannel);

        /////////////////////////////
        // Configure editor pannel //
        /////////////////////////////
        JPanel editorPanel = new JPanel();
        editorPanel.setLayout(new BorderLayout());

        // Shacl editor
        this.shaclEditor.setBorder(BorderFactory.createTitledBorder("Shacl editor:"));
        this.shaclEditor.setPreferredSize(new Dimension(400, 250));
        this.shaclEditor.setFont(new Font("Sanserif", Font.BOLD, FontSize));

        JScrollPane scrollShaclEditor = new JScrollPane(this.shaclEditor);
        scrollShaclEditor.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        // Shacl result
        this.shaclResult.setBorder(BorderFactory.createTitledBorder("Results:"));
        this.shaclResult.setEditable(false);
        this.shaclResult.setFont(new Font("Sanserif", Font.BOLD, FontSize));

        JScrollPane scrollShaclResult = new JScrollPane(shaclResult);
        scrollShaclResult.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        // Split plane
        final JSplitPane splitPlane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollShaclEditor, scrollShaclResult);
        splitPlane.setContinuousLayout(true);
        editorPanel.add(splitPlane);

        add(editorPanel);
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
