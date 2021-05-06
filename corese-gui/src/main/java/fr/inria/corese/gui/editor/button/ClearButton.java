package fr.inria.corese.gui.editor.button;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import fr.inria.corese.gui.editor.pane.EditorPane;
import fr.inria.corese.gui.editor.pane.ResultPane;

public class ClearButton extends Button {

    private EditorPane editor;
    private ResultPane result;

    public ClearButton(EditorPane editor, ResultPane result) {
        super("Clear");
        this.editor = editor;
        this.result = result;
    }

    @Override
    protected ActionListener action() {

        ActionListener buttonClearListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int input = JOptionPane.showConfirmDialog(null, "Clear editor contents?", "Confirmation",
                        JOptionPane.OK_CANCEL_OPTION);

                if (input == 0) {
                    ClearButton.this.editor.setContent("");
                    ClearButton.this.result.setContent("");
                }
            }
        };
        return buttonClearListener;
    }

}
