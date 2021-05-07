package fr.inria.corese.gui.editor.button;

import java.awt.event.ActionListener;

import javax.swing.JButton;

public abstract class Button extends JButton {

    public Button(String title) {
        addActionListener(this.action());
        setText(title);
    }

    protected abstract ActionListener action();
}