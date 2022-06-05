package fr.inria.corese.gui.editor.pane;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.*;

public class EditorPane {

    private JTextPane editor;
    private JTextArea lineCounter;
    private Font font;
    private String title;
    private UndoManager undoManager;
    private int old_line_number;

    public EditorPane(String title) {
        this.editor = new JTextPane();
        this.lineCounter = new JTextArea(10, 1);
        this.font = new Font("Sanserif", Font.BOLD, 16);
        this.undoManager = new UndoManager();
        this.title = title;
        this.initLineCounter();
        this.initEditor();
        this.old_line_number = 0;
    }

    private void initLineCounter() {
        this.lineCounter.setEditable(false);
        this.lineCounter.setFocusable(false);
        this.lineCounter.setBackground(new Color(230, 230, 230));
        this.lineCounter.setFont(this.font);
        this.lineCounter.setText("\n1");
    }

    private void initEditor() {
        this.editor.setBorder(BorderFactory.createTitledBorder(this.title + " editor:"));
        this.editor.setPreferredSize(new Dimension(400, 250));
        this.editor.setFont(this.font);
        // Activate undo/redo features
        this.undoRedoOperations();
       
        this.editor.getDocument().addDocumentListener(new DocumentListener() {

            private void updatelineCounter() {

                String editor_text = editor.getText();
                int nb_line = editor_text.length() - editor_text.replace("\n", "").length() + 2;

                if (nb_line != old_line_number) {
                    old_line_number = nb_line;
                    String text = "\n";
                    for (int i = 1; i < nb_line; i++) {
                        text += String.valueOf(i);
                        if (i != nb_line - 1) {
                            text += "\n";
                        }
                    }
                    lineCounter.setText(text);

                    lineCounter.setColumns(Integer.toString(nb_line - 1).length());
                }
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
    }

    private void undoRedoOperations(){
        this.editor.getDocument().addUndoableEditListener(
                new UndoableEditListener() {
                    public void undoableEditHappened(UndoableEditEvent e) {
                        undoManager.addEdit(e.getEdit());
                    }
                });
        // Detect ctrl-z and ctrl-y to undo and redo
        this.editor.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "undo");
        this.editor.getActionMap().put("undo", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                try {
                    undoManager.undo();
                } catch (CannotUndoException ex) {
                    System.out.println("Unable to undo: " + ex);
                }
            }
        });
        this.editor.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "redo");
        this.editor.getActionMap().put("redo", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                try {
                    undoManager.redo();
                } catch (CannotRedoException ex) {
                    System.out.println("Unable to redo: " + ex);
                }
            }
        });
    }
    public String getContent() {
        return this.editor.getText();
    }

    public void setContent(String content) {
        this.editor.setText(content);
    }

    public JScrollPane getPane() {
        JScrollPane scrollEditor = new JScrollPane(this.editor, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollEditor.setRowHeaderView(this.lineCounter);
        scrollEditor.setViewportView(this.editor);
        return scrollEditor;
    }

}
