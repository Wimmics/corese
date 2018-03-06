package fr.inria.corese.gui.core;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

/**
 * 
 * @author Maraninchi jerome
 * Juillet 2010
 *
 */


public class MyCellRenderer extends JPanel implements ListCellRenderer{

	private JList maList;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	
	private Color foreground=Color.black;
	private Color background=Color.white;
	private String extension;
	private String path;
	
	public MyCellRenderer(){
		maList = MyJPanelListener.listLoadedFiles;
	}
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(background);
		g2.fillRect(0, 0, getWidth(), getHeight());
		g2.setColor(foreground);
		if(path!=null){
			g2.drawString(path,2,getHeight()/2+4);
		}	
	}
	
	public static String extension(Object o){		
		String extension = null;
		String s =String.valueOf(o);
		int i=s.lastIndexOf('.');	//récupére l'index a partir duquel il faut couper
		
		if(i>0 && i<s.length() - 1)
			extension = s.substring(i+1).toLowerCase();		//on récupére l'extension
		return extension;	//on teste s'il faut accepter ou refuser
	}
	
	@Override	
	public Component getListCellRendererComponent(JList maList, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		
		path=value.toString();
		extension = extension(value.toString());
		
                if (extension == null){}
                else if(extension.equals("rdfs") || extension.equals("owl") ){
    		foreground = Color.blue;
  			background=Color.white;	
		}
		else if(extension.equals("rul")){ 		
			foreground = Color.RED;
  			background=Color.white;			
		}
		else if(extension.equals("rdf")){
			foreground = Color.black;
			background=Color.white;
		}
		else{
			background=Color.white;
		}
		 setEnabled(maList.isEnabled());
	     setFont(maList.getFont());
	     

		return this;
	}
	
	public Dimension getPreferredSize(){
		return new Dimension(100,20);
	}
	
	public JList getMaList() {
		return maList;
	}

	
}
