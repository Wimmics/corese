package fr.inria.corese.gui.editor.button;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.shacl.Shacl;
import fr.inria.corese.core.transform.Transformer;
import fr.inria.corese.gui.core.MainFrame;
import fr.inria.corese.gui.editor.pane.EditorPane;
import fr.inria.corese.gui.editor.pane.ResultPane;
import fr.inria.corese.sparql.exceptions.EngineException;

public class ValidateShaclButton extends Button {

    private EditorPane editorPane;
    private ResultPane resultPane;
    private MainFrame mainFrame;

    public ValidateShaclButton(EditorPane editorPane, ResultPane resultPane, final MainFrame coreseFrame) {
        super("Validate");
        this.editorPane = editorPane;
        this.resultPane = resultPane;
        this.mainFrame = coreseFrame;
    }

    @Override
    protected ActionListener action() {

        ActionListener buttonValidateListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                // Load shape graph
                Graph shapeGraph = Graph.create();
                Load ld = Load.create(shapeGraph);

                String shaclString = ValidateShaclButton.this.editorPane.getContent();
                if (shaclString.strip().isEmpty()) {
                    ValidateShaclButton.this.resultPane.setContent("Error : SHACL document is empty.");
                    return;
                }

                try {
                    ld.loadString(ValidateShaclButton.this.editorPane.getContent(), Load.TURTLE_FORMAT);
                } catch (LoadException e1) {
                    ValidateShaclButton.this.resultPane.setContent("Error : malformed SHACL document.");
                    e1.printStackTrace();
                    return;
                }

                // Evaluation
                Shacl shacl = new Shacl(ValidateShaclButton.this.mainFrame.getMyCorese().getGraph(), shapeGraph);
                Graph result = null;
                try {
                    result = shacl.eval();
                } catch (EngineException e2) {
                    ValidateShaclButton.this.resultPane.setContent("Error : engine exception.");
                    e2.printStackTrace();
                    return;
                }

                Transformer transformer = Transformer.create(result, Transformer.TURTLE);
                ValidateShaclButton.this.resultPane.setContent(transformer.toString());
            }
        };
        return buttonValidateListener;
    }

}
