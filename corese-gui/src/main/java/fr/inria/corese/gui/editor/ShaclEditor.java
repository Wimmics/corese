package fr.inria.corese.gui.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import fr.inria.corese.gui.core.MainFrame;
import fr.inria.corese.gui.editor.button.ClearButton;
import fr.inria.corese.gui.editor.button.OpenButton;
import fr.inria.corese.gui.editor.button.SaveButton;
import fr.inria.corese.gui.editor.button.SyntaxCheckButton;
import fr.inria.corese.gui.editor.button.ValidateShaclButton;
import fr.inria.corese.gui.editor.pane.EditorPane;
import fr.inria.corese.gui.editor.pane.ResultPane;

public class ShaclEditor extends JPanel {

    private static final long serialVersionUID = 1L;

    private MainFrame mainFrame;

    private EditorPane editorPane;
    private ResultPane resultPane;

    public ShaclEditor(final MainFrame coreseFrame) {
        super();
        this.editorPane = new EditorPane("SHACL");
        this.resultPane = new ResultPane();
        this.mainFrame = coreseFrame;
        this.initComponents();
        this.initButtonsPanel();
        this.initEditorPanel();
    }

    private void initComponents() {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
    }

    private void initEditorPanel() {
        final JSplitPane splitPlane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.editorPane.getPane(),
                this.resultPane.getPane());
        splitPlane.setContinuousLayout(true);

        JPanel editorPanel = new JPanel();
        editorPanel.setLayout(new BorderLayout());
        editorPanel.add(splitPlane);

        add(editorPanel);
    }

    private void initButtonsPanel() {
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout());

        // Open button
        OpenButton openButton = new OpenButton(this.editorPane, this.mainFrame, "Select a SHACL file", true, "Turtle",
                "ttl");
        buttonsPanel.add(openButton);

        // Save button
        SaveButton saveButton = new SaveButton(this.editorPane, this.mainFrame);
        buttonsPanel.add(saveButton);

        buttonsPanel.add(Box.createRigidArea(new Dimension(15, 0)));

        // validate button
        SyntaxCheckButton syntaxButton = new SyntaxCheckButton(this.editorPane, this.resultPane);
        buttonsPanel.add(syntaxButton);

        // validate button
        ValidateShaclButton validateButton = new ValidateShaclButton(this.editorPane, this.resultPane, this.mainFrame);
        buttonsPanel.add(validateButton);

        buttonsPanel.add(Box.createRigidArea(new Dimension(15, 0)));

        // Clear button
        ClearButton clearButton = new ClearButton(this.editorPane, this.resultPane);
        buttonsPanel.add(clearButton);

        add(buttonsPanel);
    }

}
