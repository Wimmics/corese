package fr.inria.corese.gui.editor.button;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.gui.core.MainFrame;
import fr.inria.corese.gui.editor.pane.EditorPane;
import fr.inria.corese.gui.editor.pane.ResultPane;
import fr.inria.corese.sparql.exceptions.EngineException;

public class LoadButton extends Button {

    private EditorPane editor;
    private ResultPane result;
    private MainFrame mainFrame;

    public LoadButton(EditorPane editor, ResultPane result, final MainFrame coreseFrame) {
        super("Load");
        this.editor = editor;
        this.result = result;
        this.mainFrame = coreseFrame;
    }

    @Override
    protected ActionListener action() {

        ActionListener buttonLoadListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                String editorContent = editor.getContent();

                // Test if empty
                if (editorContent.strip().isEmpty()) {
                    result.setContent("Error : Document is empty.");
                    return;
                }

                // Load editor content in Corese
                try {
                    LoadButton.this.mainFrame.getMyCorese().loadRDF(editorContent, Load.TURTLE_FORMAT);
                } catch (EngineException | LoadException e1) {
                    e1.printStackTrace();
                    return;
                }

                // Confirmation
                result.setContent("Document is loaded in Corese");
                LoadButton.this.mainFrame.appendMsg("Loaded from editor …" );

            }

        };
        return buttonLoadListener;
    }

}
