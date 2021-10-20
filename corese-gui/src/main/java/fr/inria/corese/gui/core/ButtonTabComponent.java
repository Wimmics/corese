package fr.inria.corese.gui.core;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.*;

/**
 * Crée la croix fermante sur les onglets Contient un JButton pour fermer
 * l'onglet et un JLabel pour montrer le texte lorsqu'on passe sur le bouton
 */
@SuppressWarnings("serial")
public class ButtonTabComponent extends JPanel {

    private final JTabbedPane pane;

    /**
     * Ajoute le bouton fermant à l'onglet
     *
     * @param coreseFrame
	 *
     */
    public ButtonTabComponent(final JTabbedPane pane, final MainFrame coreseFrame) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        if (pane == null) {
            throw new NullPointerException("TabbedPane is null");
        }
        this.pane = pane;
        setOpaque(false);

        JLabel label = new JLabel() {
            public String getText() {
                int i = pane.indexOfTabComponent(ButtonTabComponent.this);
                if (i != -1) {
                    return pane.getTitleAt(i);
                }
                return null;
            }
        };

        add(label);
        //espace le bouton et le label
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        //le bouton
        JButton button = new TabButton(coreseFrame);
        add(button);
        setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

    }

    public class TabButton extends JButton implements ActionListener {

        public TabButton(final MainFrame coreseFrame) {
            int size = 17;
            setPreferredSize(new Dimension(size, size));
            setToolTipText("close this tab");
            // Apparence du bouton
            setUI(new BasicButtonUI());
            // Pour la trensparence
            setContentAreaFilled(false);
            setFocusable(false);
            setBorder(BorderFactory.createEtchedBorder());
            setBorderPainted(false);
            // Même listener pour tous les boutons
            addMouseListener(buttonMouseListener);
            setRolloverEnabled(true);
            ActionListener closeTab = new ActionListener() {
                public void actionPerformed(ActionEvent l_Event) {
                    int i = pane.indexOfTabComponent(ButtonTabComponent.this);
                    if (i != -1) {

                        // Si l'on ferme le dernier onglet avant le "+" sachant qu'il est sélectionné on sélectionne l'onglet précédent avant de le fermer
                        if ((coreseFrame.getConteneurOnglets().getSelectedIndex() == coreseFrame.getConteneurOnglets().getComponentCount() - 3)
                                && i == coreseFrame.getConteneurOnglets().getSelectedIndex()) {
                            coreseFrame.getConteneurOnglets().setSelectedIndex(coreseFrame.getSelected() - 1);
                        } // Sinon le même reste sélectionné
                        else {
                            coreseFrame.getConteneurOnglets().setSelectedIndex(coreseFrame.getSelected());
                        }
                        // On supprime l'onglet
                        pane.remove(i);

                    }
                }
            };
            addActionListener(closeTab);
        }

        public void updateUI() {
        }

        // dessine la croix
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            if (getModel().isPressed()) {
                g2.translate(1, 1);
            }
            g2.setStroke(new BasicStroke(2));
            g2.setColor(Color.BLACK);
            if (getModel().isRollover()) {
                g2.setColor(Color.RED);
            }
            int delta = 6;
            g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight()
                    - delta - 1);
            g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight()
                    - delta - 1);
            g2.dispose();
        }

        public void actionPerformed(ActionEvent e) {
            // Pour éviter une erreur ...
        }

    }

    private final static MouseListener buttonMouseListener = new MouseAdapter() {
        public void mouseEntered(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(true);
            }
        }

        public void mouseExited(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(false);
            }
        }
    };

}
