package fr.inria.corese.gui.core;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

//import fr.inria.corese.sparql.gui.event.MyLoadListener;
//import fr.inria.corese.sparql.gui.event.MyQueryListener;
import fr.inria.corese.kgram.event.Event;

public class JFrameDebug extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JToolBar toolBarDebug;
    private JMenuItem next;
    private JMenuItem complete;
    private JMenuItem forward;
    private JMenuItem map;
    private JMenuItem success;
    private JMenuItem quit;
    private JCheckBox checkBoxQuery;
    private JCheckBox checkBoxRule;
    private JCheckBox checkBoxVerbose;
    private JCheckBox checkBoxLoad;
    private JPanel p;

//    private MyLoadListener ell;
//    private MyQueryListener eql;
    
	public JFrameDebug(final MainFrame coreseFrame){
		toolBarDebug = new JToolBar();

		p = new JPanel(); 
        next = new JMenuItem("Next        ");
        complete = new JMenuItem("Complete");
        forward = new JMenuItem("Forward");
        map = new JMenuItem("Map");
        success = new JMenuItem("Success");
        quit = new JMenuItem("Quit");
        checkBoxLoad = new JCheckBox("Load");
        checkBoxQuery = new JCheckBox("Query");
        checkBoxRule = new JCheckBox("Rule");
        checkBoxVerbose = new JCheckBox("Verbose");
                     
        
        // Raccourcis claviers
        
        next.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK));
        complete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.ALT_MASK));
        forward.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.ALT_MASK));
        map.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.ALT_MASK));
        next.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK));
        success.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
        quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.ALT_MASK));

        p.add(next);
        ActionListener l_NextListener = new ActionListener() {
        	public void actionPerformed(ActionEvent l_Event) {
        		coreseFrame.set(Event.STEP);        		
        	}
        };
        next.addActionListener(l_NextListener);
        
        p.add(complete);
        ActionListener l_SkipListener = new ActionListener() {
        	public void actionPerformed(ActionEvent l_Event) {
        		coreseFrame.set(Event.COMPLETE);
        		
        	}
        };
        complete.addActionListener(l_SkipListener);
        
        
        p.add(forward);
        ActionListener l_PlusListener = new ActionListener() {
        	public void actionPerformed(ActionEvent l_Event) {
        		coreseFrame.set(Event.FORWARD);
        		
        	}
        };
        forward.addActionListener(l_PlusListener);
        
        p.add(map);
        ActionListener l_MapListener = new ActionListener() {
        	public void actionPerformed(ActionEvent l_Event) {
        		coreseFrame.set(Event.MAP);
        		
        	}
        };
        map.addActionListener(l_MapListener);
        
        p.add(success);
        ActionListener l_SuccessListener = new ActionListener() {
  			
  			@Override
  			public void actionPerformed(ActionEvent e) {
  				coreseFrame.set(Event.SUCCESS);			
  			}
  		};
  		success.addActionListener(l_SuccessListener);
        
  		p.add(quit);
        ActionListener l_QuitListener = new ActionListener() {
        	public void actionPerformed(ActionEvent l_Event) {
        		coreseFrame.set(Event.QUIT);       		
        	}
        };
        quit.addActionListener(l_QuitListener);
        
        p.add(checkBoxLoad);
        checkBoxLoad.addItemListener (
        		new ItemListener() {
        			public void itemStateChanged(ItemEvent e) {
        				
//        				if(checkBoxLoad.isSelected() == true){        					
//        					ell = MyLoadListener.create();
//        					coreseFrame.getMyCorese().addEventListener(ell);
//        				}
//        				else{
//        					coreseFrame.getMyCorese().removeEventListener(ell);        				
//        				}
        			}
        		}
        );
        
        
        p.add(checkBoxQuery);
        checkBoxQuery.addItemListener (
        		new ItemListener() {
        			public void itemStateChanged(ItemEvent e) {
        				
//        				if(checkBoxQuery.isSelected() == true){
//        					eql = MyQueryListener.create();
//        					coreseFrame.getMyCorese().addEventListener(eql);
//        				}
//        				else{        					
//        					coreseFrame.getMyCorese().removeEventListener(eql);        				
//        				}
        			}
        		}
        );
        
        p.add(checkBoxRule);
        checkBoxRule.addItemListener (
        		new ItemListener() {
        			public void itemStateChanged(ItemEvent e) {
        				
   
        			}
        		}
        );
        
        
        p.add(checkBoxVerbose);
        checkBoxVerbose.addItemListener(
        		new ItemListener() {
  					
  					@Override
  					public void itemStateChanged(ItemEvent e) {
  						if(checkBoxVerbose.isSelected()){
  							coreseFrame.set(Event.VERBOSE);
  						}	
  						else{
  							coreseFrame.set(Event.NONVERBOSE);
  						}
  					}
  				}
        );
        p.add(toolBarDebug);
		
        
        setTitle("Debug");
        setSize(165, 240);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(false);

        add(p);
        
        
	}



	
	
}
