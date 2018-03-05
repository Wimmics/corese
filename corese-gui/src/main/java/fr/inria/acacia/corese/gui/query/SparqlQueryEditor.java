/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.acacia.corese.gui.query;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import fr.inria.acacia.corese.gui.core.CompoundUndoManager;
import fr.inria.acacia.corese.gui.core.MainFrame;

/**
 *
 * @author edemairy
 */
public class SparqlQueryEditor extends JPanel implements Runnable, ActionListener, DocumentListener, FocusListener, CaretListener {

	private JScrollPane scrollPaneQuery;
	private JTextPane textPaneQuery;
	private JTextArea textAreaLines;
	private CompoundUndoManager undoManager = null;
	private boolean isColoring;
	private int temp3;
	private int temp;
	private Object start, end;
	private Highlighter highlighter;
	private MainFrame mainFrame;
        static int FontSize = 16;

	public SparqlQueryEditor() {
		this(null);
	}

	public SparqlQueryEditor(final MainFrame coreseFrame) {
		super();
		initComponents();
		mainFrame = coreseFrame;
	}
              
	private void initComponents() {
		textPaneQuery = new JTextPane();
		textPaneQuery.setName("textPaneQuery");

		textPaneQuery.setFont(new Font("Sanserif", Font.BOLD, FontSize));
		textPaneQuery.setPreferredSize(new Dimension(400, 250));
		textPaneQuery.setMargin(new Insets(1, 1, 1, 1));
		textPaneQuery.setText("Default text");

		StyledDocument doc = textPaneQuery.getStyledDocument();
		MutableAttributeSet attr = new SimpleAttributeSet();
		StyleConstants.setLineSpacing(attr, 0);
		doc.setParagraphAttributes(0, doc.getLength(), attr, false);

		//compteur de ligne pour la query
		textAreaLines = new JTextArea(10, 2);
		textAreaLines.setFont(new Font("Sanserif", Font.PLAIN, FontSize));
		textAreaLines.setEditable(false);
		textAreaLines.setFocusable(false);
		textAreaLines.setBackground(new Color(230, 230, 230));
		textAreaLines.setForeground(Color.black);
		textAreaLines.setAutoscrolls(true);
		textAreaLines.add(textPaneQuery);
		textAreaLines.setMargin(new Insets(3, 1, 1, 1));
		textAreaLines.setLayout(new BorderLayout());


		textPaneQuery.addFocusListener(this);
		textPaneQuery.getDocument().addDocumentListener(this);
		textPaneQuery.addCaretListener(this);

		undoManager = new CompoundUndoManager(textPaneQuery);
		checkLines(textPaneQuery, textAreaLines);

		scrollPaneQuery = new JScrollPane();
		scrollPaneQuery.setRowHeaderView(textAreaLines);
		scrollPaneQuery.setViewportView(textPaneQuery);

		setLayout(new BorderLayout());
		add(scrollPaneQuery);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		mainFrame.getConteneurOnglets().setSelectedIndex(0);
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		this.checkLines(textPaneQuery, textAreaLines);
		doColoringLater();

	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		this.checkLines(textPaneQuery, textAreaLines);
		doColoringLater();

	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		this.checkLines(textPaneQuery, textAreaLines);
		doColoringLater();


	}

	@Override
	public void focusGained(FocusEvent e) {
		doColoringLater();
	}

	@Override
	public void focusLost(FocusEvent e) {
		this.checkLines(textPaneQuery, textAreaLines);
		doColoringLater();
	}

	private void clearHighlights() {
		if (highlighter != null) {
			for (Highlighter.Highlight hl : highlighter.getHighlights())
				if (hl.getPainter() instanceof VarHighlightPainter)
					highlighter.removeHighlight(hl);
			if (start != null)
				highlighter.removeHighlight(start);
			if (end != null) 
				highlighter.removeHighlight(end);
			start = end = null;
			highlighter = null;
		}
	}
	
	private class VarHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {
	    public VarHighlightPainter(Color color) {
	        super(color);
	    }
	}

	@Override
	public void caretUpdate(CaretEvent e) {
		Highlighter.HighlightPainter goodPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.cyan);
		Highlighter.HighlightPainter badPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.magenta);

		clearHighlights();
		JTextComponent source = (JTextComponent) e.getSource();
		highlighter = source.getHighlighter();
		Document doc = source.getDocument();

		if (e.getDot() == 0)
			return;

		int closeParen = e.getDot() - 1;
		try {
			char c = getCharAt(doc, closeParen);
			if (c == ')' || c == '}' || c == ']') {
				int openParen = findMatchingParen(doc, closeParen);
				if (openParen >= 0) {
					char c2 = getCharAt(doc, openParen);
					if (c2 == '(' && c == ')'
							|| c2 == '{' && c == '}'
							|| c2 == '[' && c == ']') {
						start = highlighter.addHighlight(openParen, openParen + 1, goodPainter);
						end = highlighter.addHighlight(closeParen, closeParen + 1, goodPainter);
					} else {
						start = highlighter.addHighlight(openParen, openParen + 1, badPainter);
						end = highlighter.addHighlight(closeParen, closeParen + 1, badPainter);
					}
				} else {
					end = highlighter.addHighlight(closeParen, closeParen + 1, badPainter);
				}
			} 
			// Marking all occurrences of current variable token
			// Known bug: $t and ?t are considered different variables
			String text = doc.getText(0, doc.getLength());
			String[] tokens = text.split("\\s"); // This is lazy.
			for (String token : tokens)
				if (token.length() > 1)
					if (token.startsWith("?") || token.startsWith("$"))
						for (int index = text.indexOf(token);
								index >= 0;
								index = text.indexOf(token, index + 1))
							if (index <= e.getDot() && (index + token.length()) >= e.getDot())
								this.highlightVars(token, text);
		} catch (BadLocationException e1) {	e1.printStackTrace(); }

	}

	private void highlightVars(String var, String text) {
		Highlighter.HighlightPainter varPainter = new VarHighlightPainter(Color.yellow);
		try {
			int pos = 0;
			while ((pos = text.indexOf(var, pos)) >= 0) {
				highlighter.addHighlight(pos, pos + var.length(), varPainter);
				pos += var.length();
			}
		} catch (BadLocationException e) {}
	}

	/**
	 * Returns the character at position p in the document
	 */
	private static char getCharAt(Document doc, int p)
			throws BadLocationException {
		return doc.getText(p, 1).charAt(0);
	}

	private static int findMatchingParen(Document d, int paren)
			throws BadLocationException {
		int parenCount = 1;
		int i = paren - 1;
		for (; i >= 0; i--) {
			char c = getCharAt(d, i);
			switch (c) {
			case ')':
			case '}':
			case ']':
				parenCount++;
				break;
			case '(':
			case '{':
			case '[':
				parenCount--;
				break;
			}
			if (parenCount == 0) {
				break;
			}
		}
		return i;
	}

	public CompoundUndoManager getUndoManager() {
		return undoManager;
	}

	public void setQueryText(String newRequest) {
		textPaneQuery.setText(newRequest);
	}

	private void search() {

		/**
		 * Initialisation des variables
		 */
		int startQuote = 0, startComm = 0, start = 0, stop = 0;
		CharSequence content = null;
		Document d = textPaneQuery.getDocument();
		boolean b_word = false, b_wordUpper = false;
		boolean b_quote = false;
		boolean b_function = false, b_functionUpper = false;

		try {
			content = d.getText(0, d.getLength());
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/**
		 * Les différents ArrayList nous permettant de stocker les indexs des mots à colorier et également les mots qu'il faut rechercher
		 */
		ArrayList<Integer> tabStartWord = new ArrayList<Integer>();
		ArrayList<Integer> tabStopWord = new ArrayList<Integer>();

		ArrayList<Integer> tabStartComm = new ArrayList<Integer>();
		ArrayList<Integer> tabStopComm = new ArrayList<Integer>();

		ArrayList<Integer> tabStartQuote = new ArrayList<Integer>();
		ArrayList<Integer> tabStopQuote = new ArrayList<Integer>();

		ArrayList<Integer> tabStartFunction = new ArrayList<Integer>();
		ArrayList<Integer> tabStopFunction = new ArrayList<Integer>();

		ArrayList<String> listWords = new ArrayList<String>();
		ArrayList<String> listFunctions = new ArrayList<String>();

		/**
		 * Remplit les deux ArraList
		 */
		listWords.add("select");
		listWords.add("where");
		listWords.add("optional");
		listWords.add("filter");
		listWords.add("prefix");
		listWords.add("construct");
		listWords.add("describe");
		listWords.add("union");
		listWords.add("template");

		listWords.add("base");
		listWords.add("ask");
		listWords.add("order by");
		listWords.add("group by");
		listWords.add("limit");
		listWords.add("offset");
		listWords.add("distinct");
		listWords.add("reduced");
		listWords.add("from");
		listWords.add("from named");
		listWords.add("graph");
		listWords.add("not");
		listWords.add("exists");

		listFunctions.add("regex");
		listFunctions.add("bound");
		listFunctions.add("isIRI");
		listFunctions.add("isBlank");
		listFunctions.add("isLiteral");
		listFunctions.add("str");
		listFunctions.add("lang");
		listFunctions.add("logical-or");
		listFunctions.add("logical-and");
		listFunctions.add("RDFterm-equal");
		listFunctions.add("sameTerm");
		listFunctions.add("langMatches");

		/**
		 * On définit les différentes polices à utiliser.
		 */
		StyleContext sc = new StyleContext();
		Style style = sc.addStyle("style", null);
		style.addAttribute(StyleConstants.Foreground, new Color(255, 102, 102));

		Style normal = sc.addStyle("normal", null);
		normal.addAttribute(StyleConstants.Foreground, Color.BLACK);

		Style comm = sc.addStyle("comm", null);
		comm.addAttribute(StyleConstants.Foreground, Color.GREEN);
		comm.addAttribute(StyleConstants.CharacterConstants.Italic, Boolean.TRUE);

		Style quote = sc.addStyle("quote", null);
		quote.addAttribute(StyleConstants.Foreground, Color.BLUE);

		Style function = sc.addStyle("function", null);
		function.addAttribute(StyleConstants.CharacterConstants.Bold, Boolean.TRUE);

		/**
		 * Pour la recherche des words
		 */
		for (int j = 0; j < listWords.size(); j++) {
			for (int i = temp; i < d.getLength(); i++) {
				//fichier temporaire qui tronque le fichier result a partir du String=toSearch afin de trouver les prochains String
				b_word = content.toString().contains(listWords.get(j));
				b_wordUpper = content.toString().contains(listWords.get(j).toUpperCase());
				if (b_word == true || b_wordUpper == true) {
					if (b_word == true) {
						start = content.toString().indexOf(listWords.get(j));		//stocke dans le tableau afin de le surligner plus tard
						assert (start > -1);
						stop = start + listWords.get(j).length();
					} else {
						start = content.toString().indexOf(listWords.get(j).toUpperCase());	//	Cherche le mot en majuscule
						assert (start > -1);
						stop = start + listWords.get(j).toUpperCase().length();
					}
					char temp2;
					char temp3;
					if (start == 0 && stop == content.length()) {
						temp2 = ' ';
						temp3 = ' ';
					} else if (start == 0) {							//le cas du début pas besoin d'espace avant le mot
						temp2 = ' ';
						temp3 = content.charAt(stop);
					} else if (stop == content.length()) {		//le cas de la fin pas besoin d'espace après le mot
						temp2 = content.charAt(start - 1);
						temp3 = ' ';
					} else {											//le cas général
						temp2 = content.charAt(start - 1);
						temp3 = content.charAt(stop);
					}

					if ((temp2 == ' ' || temp2 == '\n' || temp2 == '}') && (temp3 == ' ' || temp3 == '\n' || temp3 == '{')) {		//on vérifie alors qu'on est bien entre deux blancs ou entre un saut à la ligne
						tabStartWord.add(start + temp);
						tabStopWord.add(stop + temp);
					}

					//permet de faire la relation entre le fichier temporaire et le fichier de base (JtextArea)
					//maintien à jour les chiffres stop et start faussé par le subSequence
					temp += stop;
					content = (content.subSequence(stop, content.length()));

				}

			}
			try {
				content = d.getText(0, d.getLength());
				stop = 0;
				start = 0;
				temp = 0;
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		/**
		 * Pour la recherche des # (les commentaires)
		 */
		for (int j = 0; j < getTextAreaLines().getLineCount() - 1; j++) {
			int lineStartOffset = 0;
			if (lineStartOffset == d.getLength()) {
				break;
			}
			if (content.toString().charAt(lineStartOffset) == '#') {
				startComm = lineStartOffset;
				tabStartComm.add(startComm);							//on ajoute dans notre arraylist l'index de début
				while (content.charAt(startComm) != '\n' && startComm < (d.getLength() - 1)) {				//on recherche le saut de ligne afin de déterminer l'index du caractère final
					startComm++;
				}
				if (j == getTextAreaLines().getLineCount() - 2) {			//utilisation du if pour colorier différement selon la fin du commentaire (saut de ligne ou fin de requête)
					tabStopComm.add(startComm + 1);
				} else {
					tabStopComm.add(startComm);
				}
			}
		}

		try {
			content = d.getText(0, d.getLength());

		} catch (BadLocationException e) {
			e.printStackTrace();
		}

		/**
		 * Pour la recherche des " (chaines de caractères)
		 */
		boolean isComm = false;
		int pair = 1;  													//variable pour déterminer si "ouvrant ou "fermant
		for (int i = temp3; i < d.getLength(); i++) {							//permet de mettre d'un certaine couleur les charactères entre ""
			startQuote = 0;
			isComm = false;
			b_quote = content.toString().contains("\"");
			if (b_quote == true) {
				startQuote = content.toString().indexOf("\"");
				for (int j = 0; j < tabStartComm.size(); j++) {			//si le guillemet se situe dans un commentaire => met a jour le temp a la fin du comm et on ne rempli pas le tableau
					if (startQuote <= (tabStopComm.get(j) - temp3) && startQuote >= (tabStartComm.get(j) - temp3)) {
						isComm = true;
						temp3 += startQuote + 1;
					}
				}
				if (!isComm) {
					if (pair >= 0 && pair % 2 == 1) {											//si guillement ouvrant on range l'index du caractère courant dans l'arraylist d'index de début
						tabStartQuote.add(startQuote + temp3);
					} else {													//sinon on range l'index du caractère  dans l'arraylist d'index de fin
						temp3 += 2;											//on incrémente de 2 pour colorier les guillemets également
						tabStopQuote.add(startQuote + temp3);
					}
					pair++;
					temp3 += startQuote;
				}
				content = (content.subSequence(startQuote + 1, content.length()));	//decoupe la chaine pour trouver le prochain guillemet
			}

		}
		try {
			content = d.getText(0, d.getLength());
			temp3 = 0;
		} catch (BadLocationException e) {
			e.printStackTrace();
		}

		/**
		 * Pour la recherche des fonctions
		 */
		for (int j = 0; j < listFunctions.size(); j++) {
			for (int i = temp; i < d.getLength(); i++) {
				//fichier temporaire qui tronque le fichier result a partir du String=toSearch afin de trouver les prochains String
				b_function = content.toString().contains(listFunctions.get(j));
				b_functionUpper = content.toString().contains(listFunctions.get(j).toUpperCase());
				if (b_function == true || b_functionUpper == true) {
					if (b_function) {
						start = content.toString().indexOf(listFunctions.get(j));		//stocke dans le tableau afin de le surligner plus tard
						assert (start > -1);
						stop = start + listFunctions.get(j).length();
					} else {
						start = content.toString().indexOf(listFunctions.get(j).toUpperCase());		//stocke dans le tableau afin de le surligner plus tard
						assert (start > -1);
						stop = start + listFunctions.get(j).toUpperCase().length();
					}
					char temp2;
					char temp3 = 0;
					if (start == 0 && stop == content.length()) {
						temp2 = ' ';
						temp3 = ' ';
					} else if (start == 0) {							//le cas du début pas besoin d'espace avant le mot
						temp2 = ' ';
						temp3 = content.charAt(stop);
					} else if (stop == content.length()) {		//le cas de la fin pas besoin d'espace après le mot
						temp2 = content.charAt(start - 1);
					} else {									//le cas général
						temp2 = content.charAt(start - 1);
						temp3 = content.charAt(stop);
					}
					if ((temp2 == ' ' || temp2 == '\n') && (temp3 == '(')) {		//on vérifie alors qu'on est bien entre deux blancs ou entre un saut à la ligne
						tabStartFunction.add(start + temp);
						tabStopFunction.add(stop + temp);			//pr pas prendre la parenthèse
					}

					//permet de faire la relation entre le fichier temporaire et le fichier de base (JtextArea)
					//maintien à jour les chiffres stop et start faussé par le subSequence
					temp += stop;
					content = (content.subSequence(stop, content.length()));

				}

			}
			try {
				content = d.getText(0, d.getLength());
				stop = 0;
				start = 0;
				temp = 0;
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}


		textPaneQuery.getStyledDocument().setCharacterAttributes(0, content.length(), normal, true);

		//ici on colorie le texte en fonction des données recueillis auparavant
		for (int i = 0; i < tabStartWord.size(); i++) {
			textPaneQuery.getStyledDocument().setCharacterAttributes(tabStartWord.get(i), tabStopWord.get(i) - tabStartWord.get(i), style, false);
		}
		for (int i = 0; i < tabStartFunction.size(); i++) {
			textPaneQuery.getStyledDocument().setCharacterAttributes(tabStartFunction.get(i), tabStopFunction.get(i) - tabStartFunction.get(i), function, false);
		}
		for (int i = 0; i < tabStopQuote.size(); i++) {
			textPaneQuery.getStyledDocument().setCharacterAttributes(tabStartQuote.get(i), tabStopQuote.get(i) - tabStartQuote.get(i), quote, false);
		}
		for (int i = 0; i < tabStartComm.size(); i++) {
			textPaneQuery.getStyledDocument().setCharacterAttributes(tabStartComm.get(i), tabStopComm.get(i) - tabStartComm.get(i), comm, false);
		}

	}

	public JTextArea getTextAreaLines() {
		return textAreaLines;
	}

	public JTextPane getTextPaneQuery() {
		return textPaneQuery;
	}

	@Override
	public void run() {
		isColoring = true;
		try {
			this.search();

		} finally {
			isColoring = false;
		}
	}

	void search(CharSequence temps, String toSearch, JTextArea textAreaXMLResult, String message) {
		int start = 0, stop = 0;
		ArrayList<Integer> tabStart = new ArrayList<Integer>();
		ArrayList<Integer> tabStop = new ArrayList<Integer>();
		boolean b = temps.toString().contains(toSearch);
		for (int i = temp; i < temps.length(); i++) {
			//fichier temporaire qui tronque le fichier result a partir du String=toSearch afin de trouver les prochains String
			b = temps.toString().contains(toSearch);
			if (b == true) {
				start = temps.toString().indexOf(toSearch);		//stocke dans le tableau afin de le surligner plus tard
				tabStart.add(start + temp);
				assert (start > -1);
				stop = start + toSearch.length();
				tabStop.add(stop + temp);
				//permet de faire la relation entre le fichier temporaire et le fichier de base (JtextArea)
				//maintien à jour les chiffres stop et start faussé par le subSequence
				temp += stop;
				temps = (temps.subSequence(stop, temps.length()));
			} else {
				break;
			}

		}
		if (tabStart.size() > 0) {
			textAreaXMLResult.setCaretPosition(tabStart.get(0));
		}
		//remise a zéro
		temps = textAreaXMLResult.getText();
		stop = 0;
		start = 0;
		temp = 0;

		try {
			for (int i = 0; i < tabStart.size(); i++) {
				textAreaXMLResult.getHighlighter().addHighlight(tabStart.get(i), tabStop.get(i), DefaultHighlighter.DefaultPainter);
			}
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
		if (tabStart.size() == 0 && !toSearch.equals("")) {
			JOptionPane.showMessageDialog(textPaneQuery, toSearch + " n'apparait pas dans le résultat", "info", JOptionPane.INFORMATION_MESSAGE);
		}
		if (toSearch.equals("")) {
			JOptionPane.showMessageDialog(textPaneQuery, "Veuillez entrer une chaine de caractere", "info", JOptionPane.INFORMATION_MESSAGE);
			JOptionPane.showInputDialog("Search", message);
		}

	}

	private void doColoringLater() {
		if (!isColoring) {
			SwingUtilities.invokeLater(this);
		}
	}

	private void checkLines(JTextComponent textComponentInput, JTextComponent textComponentOutput) {
		String text = "";
		Document doc2 = textComponentInput.getDocument();
		int lineCount = doc2.getDefaultRootElement().getElementCount();
		for (int i = 1; i < lineCount + 1; i++) {
			text += String.valueOf(i) + "\n";
		}
		textComponentOutput.setText(text);
	}

	public void refreshColoring() {
		doColoringLater();
	}

	public String getQueryText() {
		return getTextPaneQuery().getText();
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setResizable(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		SparqlQueryEditor editor = new SparqlQueryEditor(null);
		frame.setContentPane(editor);
		frame.setSize(new Dimension(200, 200));
		frame.setVisible(true);
	}
}
