package fr.inria.corese.gui.editor.button;

import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.JButton;

public abstract class Button extends JButton {

    public Button(String title) {
        setMaximumSize(new Dimension(200, 300));
        addActionListener(this.action());
        setText(title);
    }

    protected abstract ActionListener action();
}