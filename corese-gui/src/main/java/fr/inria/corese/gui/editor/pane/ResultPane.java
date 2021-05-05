package fr.inria.corese.gui.editor.pane;

import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

public class ResultPane {

    private JTextPane result;
    private Font font;

    public ResultPane() {
        this.result = new JTextPane();
        this.font = new Font("Sanserif", Font.BOLD, 16);
        this.initResult();
    }

    private void initResult() {
        this.result.setBorder(BorderFactory.createTitledBorder("Results:"));
        this.result.setEditable(false);
        this.result.setFont(this.font);
    }

    public void setContent(String content) {
        this.result.setText(content);
    }

    public JScrollPane getPane() {
        JScrollPane scrollResult = new JScrollPane(this.result, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        return scrollResult;
    }

}
