package fr.inria.acacia.corese.gui.core;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
 
/*
**  This class will merge individual edits into a single larger edit.
**  That is, characters entered sequentially will be grouped together and
**  undone as a group. Any attribute changes will be considered as part
**  of the group and will therefore be undone when the group is undone.
*/
public class CompoundUndoManager extends UndoManager
	implements UndoableEditListener, DocumentListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public CompoundEdit compoundEdit;
	private JTextPane editor;
 
	//  These fields are used to help determine whether the edit is an
	//  incremental edit. For each character added the offset and length
	//  should increase by 1 or decrease by 1 for each character removed.
 
	private int lastOffset;
	private int lastLength;
 
	public CompoundUndoManager(JTextPane editor)
	{
		this.editor = editor;
		editor.getDocument().addUndoableEditListener( this );
	}
 
	/*
	**  Add a DocumentLister before the undo is done so we can position
	**  the Caret correctly as each edit is undone.
	*/
        @Override
	public void undo()
	{
		editor.getDocument().addDocumentListener( this );
		super.undo();
		editor.getDocument().removeDocumentListener( this );
	}
 
	/*
	**  Add a DocumentLister before the redo is done so we can position
	**  the Caret correctly as each edit is redone.
	*/
        @Override
	public void redo()
	{
		editor.getStyledDocument().addDocumentListener( this );
		super.redo();
		editor.getStyledDocument().removeDocumentListener( this );
	}
 
	/*
	**  Whenever an UndoableEdit happens the edit will either be absorbed
	**  by the current compound edit or a new compound edit will be started
	*/
        @Override
	public void undoableEditHappened(UndoableEditEvent e)
	{
		//  Start a new compound edit
 
		if (compoundEdit == null)
		{
			compoundEdit = startCompoundEdit( e.getEdit() );
			lastLength = editor.getStyledDocument().getLength();
			return;
		}
 
               // genuine code:
//		AbstractDocument.DefaultDocumentEvent event =
//			(AbstractDocument.DefaultDocumentEvent) e.getEdit();
               // with interface:
               // DocumentEvent event = (DocumentEvent) e.getEdit();
               
               // But in Java 9: AbstractDocument$DefaultDocumentEventUndoableWrapper
            
               	//if  (event.getType().equals(DocumentEvent.EventType.CHANGE))
                // hack for Java 9
		if  (e.getEdit().getPresentationName().contains("change")) 
		{   
			compoundEdit.addEdit( e.getEdit() );
			return;
		}
 
		//  Check for an incremental edit or backspace.
		//  The change in Caret position and Document length should be either
		//  1 or -1 .
 
		int offsetChange = editor.getCaretPosition() - lastOffset;
		int lengthChange = editor.getStyledDocument().getLength() - lastLength;
 
		if (Math.abs(offsetChange) == 1
		&&  Math.abs(lengthChange) == 1)
		{
			compoundEdit.addEdit( e.getEdit() );
			lastOffset = editor.getCaretPosition();
			lastLength = editor.getStyledDocument().getLength();
			return;
		}
 
		//  Not incremental edit, end previous edit and start a new one
 
		compoundEdit.end();
		compoundEdit = startCompoundEdit( e.getEdit() );
	}
 
	/*
	**  Each CompoundEdit will store a group of related incremental edits
	**  (ie. each character typed or backspaced is an incremental edit)
	*/
	private CompoundEdit startCompoundEdit(UndoableEdit anEdit)
	{
		//  Track Caret and Document information of this compound edit
 
		lastOffset = editor.getCaretPosition();
		lastLength = editor.getStyledDocument().getLength();
 
		//  The compound edit is used to store incremental edits
 
		compoundEdit = new MyCompoundEdit();
		compoundEdit.addEdit( anEdit );
 
		//  The compound edit is added to the UndoManager. All incremental
		//  edits stored in the compound edit will be undone/redone at once
 
		addEdit( compoundEdit );
		return compoundEdit;
	}
 
	//  Implement DocumentListener
	//
	// 	Updates to the Document as a result of Undo/Redo will cause the
	//  Caret to be repositioned
 
        @Override
	public void insertUpdate(final DocumentEvent e)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				int offset = e.getOffset() + e.getLength();
				offset = Math.min(offset, editor.getDocument().getLength());
				editor.setCaretPosition( offset );
			}
		});
	}
 
        @Override
	public void removeUpdate(DocumentEvent e)
	{
		editor.setCaretPosition(e.getOffset());
	}
 
        @Override
	public void changedUpdate(DocumentEvent e) 	{}
 
 
	class MyCompoundEdit extends CompoundEdit
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

                @Override
		public boolean isInProgress()
		{
			//  in order for the canUndo() and canRedo() methods to work
			//  assume that the compound edit is never in progress
 
			return false;
		}
 
                @Override
		public void undo()
		{
			//  End the edit so future edits don't get absorbed by this edit
 
			if (compoundEdit != null)
				compoundEdit.end();
 
			super.undo();
 
			//  Always start a new compound edit after an undo
 
			compoundEdit = null;
			
		}
 
	}
}	