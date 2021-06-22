package fr.inria.corese.gui.editor.button;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;

import fr.inria.corese.gui.core.MainFrame;
import fr.inria.corese.gui.editor.pane.EditorPane;

public class SaveButton extends Button {

    private EditorPane editor;
    private MainFrame mainFrame;

    public SaveButton(EditorPane editor, final MainFrame coreseFrame) {
        super("Save");
        this.editor = editor;
        this.mainFrame = coreseFrame;
    }

    @Override
    protected ActionListener action() {

        ActionListener buttonSaveListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                String shaclEditorContent = SaveButton.this.editor.getContent();

                JFileChooser fileChooser = new JFileChooser(SaveButton.this.mainFrame.getPath());
                int resultatEnregistrer = fileChooser.showDialog(fileChooser, "Save");

                // If user clicks on save button
                if (resultatEnregistrer == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();

                    // save current path
                    SaveButton.this.mainFrame.setPath(selectedFile.getParent());

                    String pathSelectFile = selectedFile.toString();
                    // Write file
                    try {
                        FileWriter myWriter = new FileWriter(pathSelectFile);
                        myWriter.write(shaclEditorContent);
                        myWriter.close();
                    } catch (IOException ex) {
                        java.util.logging.Logger.getLogger(MainFrame.class.getName())
                                .log(java.util.logging.Level.SEVERE, null, ex);
                    }
                }
            }
        };

        return buttonSaveListener;
    }

}
