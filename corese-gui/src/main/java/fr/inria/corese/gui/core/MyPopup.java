package fr.inria.corese.gui.core;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * 
 * @author Maraninchi jerome
 * Juillet 2010
 *
 */

public class MyPopup  {

	private JPopupMenu popupMenu;
	private JMenuItem delete;
    private JMenuItem reload;
    private JMenuItem infos;

	
	public MyPopup(){
		popupMenu = new JPopupMenu();
		delete = new JMenuItem("Delete");
		reload = new JMenuItem("Reload");
		infos = new JMenuItem("About");
		popupMenu.add(delete);
		popupMenu.add(reload);
		popupMenu.add(infos);
	}
	

	
	public JPopupMenu getPopupMenu() {
			return popupMenu;
		}

	public void setPopupMenu(JPopupMenu popupMenu) {
		this.popupMenu = popupMenu;
	}

	public JMenuItem getDelete() {
		return delete;
	}


	public JMenuItem getReload() {
		return reload;
	}


	public JMenuItem getInfos() {
		return infos;
	}


	
}
