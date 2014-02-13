/**
 * 
 */
package fr.inria.edelweiss.rif.javacc;

/**
 * Some tools to deal with escaped characters in input stream to parse
 */
public final class EscapeUtils {
	public static String unescape(String escapedString) {
		StringBuffer unescapedString = new StringBuffer(escapedString.length());
		StringBuffer unicodeCache = new StringBuffer(8);
		boolean escaped = false, isUnicode = false;
		for (int i = 0; i < escapedString.length(); i++) {
			char current = escapedString.charAt(i);
			if (isUnicode) {
				// If we just hit a backslash-u
				if (Character.isLetterOrDigit(current)) {
					unicodeCache.append(current);
				} else {
					unescapedString.append((char) Integer.parseInt(unicodeCache.toString(), 16));
					unicodeCache.setLength(0);
					unicodeCache.setLength(8);
					isUnicode = false;
				}
			} else if (escaped) {
				// If we just hit a backslash
				switch (current) {
					case '\\':
						unescapedString.append('\\');
						break;
					case 't':
						unescapedString.append('\t');
						break;
					case 'b':
						unescapedString.append('\b');
						break;
					case 'n':
						unescapedString.append('\n');
						break;
					case 'r':
						unescapedString.append('\r');
						break;
					case 'f':
						unescapedString.append('\f');
						break;
					case '"':
						unescapedString.append('"');
						break;
					case '\'':
						unescapedString.append('\'');
						break;
					case 'u':
						isUnicode = true;
				}
				escaped = false;
			} else {				
				// Normal behavior
				switch (current) {
					case '\\':
						escaped = true;
						break;
					default:
						unescapedString.append(current);
				}
			}
		}
		return unescapedString.toString();
	}
}
