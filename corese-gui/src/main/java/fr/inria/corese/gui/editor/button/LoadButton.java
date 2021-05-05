package fr.inria.corese.gui.editor.button;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import fr.inria.corese.gui.editor.pane.EditorPane;

public class LoadButton extends Button {

    private EditorPane editor;
    private String dialogTitle = "Select a file";
    private String path = null;

    private Boolean hasFilter = false;
    private Boolean acceptAllFileFilter;
    private String filterName;
    private String filterExtension;

    public LoadButton(EditorPane editor) {
        super("Load");
        this.editor = editor;
    }

    public LoadButton(EditorPane editor, String path) {
        this(editor);
        this.path = path;
    }

    public LoadButton(EditorPane editor, String path, String dialogTitle, Boolean acceptAllFileFilter,
            String filterName, String filterExtension) {
        this(editor, path);
        this.path = path;
        this.hasFilter = true;
        this.acceptAllFileFilter = acceptAllFileFilter;
        this.filterName = filterName;
        this.filterExtension = filterExtension;
    }

    @Override
    protected ActionListener action() {

        ActionListener buttonLoadListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser(LoadButton.this.path);
                fileChooser.setDialogTitle(LoadButton.this.dialogTitle);

                if (LoadButton.this.hasFilter) {
                    fileChooser.setAcceptAllFileFilterUsed(LoadButton.this.acceptAllFileFilter);
                    FileNameExtensionFilter filter = new FileNameExtensionFilter(LoadButton.this.filterName,
                            LoadButton.this.filterExtension);
                    fileChooser.addChoosableFileFilter(filter);

                }
                int returnValue = fileChooser.showOpenDialog(null);

                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    String pathSelectFile = fileChooser.getSelectedFile().toString();
                    String content = null;
                    try {
                        content = new String(Files.readAllBytes(Paths.get(pathSelectFile)));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    LoadButton.this.editor.setContent(content);
                }

            }

        };
        return buttonLoadListener;
    }

}
