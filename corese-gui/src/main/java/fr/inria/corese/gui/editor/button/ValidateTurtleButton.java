package fr.inria.corese.gui.editor.button;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.gui.editor.pane.EditorPane;
import fr.inria.corese.gui.editor.pane.ResultPane;

public class ValidateTurtleButton extends Button {

    private EditorPane editorPane;
    private ResultPane resultPane;

    public ValidateTurtleButton(EditorPane editorPane, ResultPane resultPane) {
        super("Validate");
        this.editorPane = editorPane;
        this.resultPane = resultPane;
    }

    @Override
    protected ActionListener action() {

        ActionListener buttonValidateListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                String editorTurtleContent = editorPane.getContent();

                // Test if empty
                if (editorTurtleContent.strip().isEmpty()) {
                    resultPane.setContent("Error : Turtle document is empty.");
                    return;
                }

                // Try to load Turtle file
                Graph turtleGraph = Graph.create();
                Load ld = Load.create(turtleGraph);
                try {
                    ld.loadString(editorTurtleContent, Load.TURTLE_FORMAT);
                    resultPane.setContent("Turtle validated");
                } catch (LoadException e1) {
                    //resultPane.setContent("Error: malformed Turle document.");
                    resultPane.setContent(e1.getMessage());
                    return;
                }

                // Evaluation
                // get editor content : editorTurtleContent
                // set content in result : resultPane.setContent("CONTENT")

                // Format and export result
                // Transformer transformer = Transformer.create(result, Transformer.TURTLE);
                // resultPane.setContent(transformer.toString());
            }
        };
        return buttonValidateListener;
    }

}
