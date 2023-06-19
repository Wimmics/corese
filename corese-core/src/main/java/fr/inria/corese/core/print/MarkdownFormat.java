package fr.inria.corese.core.print;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;

/**
 * Format the results of a SPARQL query in Markdown.
 */
public class MarkdownFormat extends QueryResultFormat {

    static final String SEP = " | ";

    // The mappings to format
    private Mappings mappings;
    // The variables to display
    private List<String> select;
    // The maximum width of each variable
    private Map<String, Integer> maxWidthMap;

    private boolean isAsk;

    /**
     * Constructor
     * @param m the mappings to format
     */
    private MarkdownFormat(Mappings m) {
        mappings = m;
        maxWidthMap = new HashMap<>();
        this.setQuery(m.getQuery());
        this.isAsk = m.getAST().isAsk();
    }

    /**
     * Create a MarkdownFormat object
     * @param m the mappings to format
     * @return a MarkdownFormat object
     */
    public static MarkdownFormat create(Mappings m) {
        return new MarkdownFormat(m);
    }

    /**
     * Initialise select and maxWidthMap
     * @param query the query to format
     */
    private void setQuery(Query query) {
        this.select = new ArrayList<>();
        for (Node node : query.getSelect()) {
            if (accept(node.getLabel())) {
                this.select.add(node.getLabel());
                // Initialise with the length of the variable name
                this.maxWidthMap.put(node.getLabel(), node.getLabel().length());
            }
        }
    }

    /**
     * Print the mappings in Markdown format
     */
    public String toString() {
        if (isAsk) {
            return toStringAsk();
        } else {
            return toStringSelect();
        }
    }

    /**
     * Print the mappings in Markdown format for a SELECT query
     * @return the mappings in Markdown format
     */
    private String toStringSelect() {
        // Calculate maximum widths before generating the table
        this.calculateMaxWidths();

        StringBuilder str = new StringBuilder(this.variables());
        str.append(System.lineSeparator());
        str.append(headerSeparator());
        str.append(System.lineSeparator());
        str.append(values());
        return str.toString();
    }

    /**
     * Print the mappings in Markdown format for an ASK query
     * @return "true" if the mappings are not empty, "false" otherwise
     */
    private String toStringAsk() {
        StringBuilder str = new StringBuilder();
        if (this.mappings.size() > 0) {
            str.append("true");
        } else {
            str.append("false");
        }
        return str.toString();
    }

   /**
    * Print the variables of the query in Markdown header format
    * @return the variables of the query in Markdown header format
    */ 
    private String variables() {
        StringBuilder str = new StringBuilder("| ");
        for (String var : select) {
            str.append(pad(var, maxWidthMap.get(var)));
            str.append(SEP);
        }
        str.setLength(str.length() - 1);// To delete the last space
        return str.toString();
    }

    /**
     * Print separator between header and values in Markdown format
     * @return separator between header and values in Markdown format
     */
    private String headerSeparator() {
        StringBuilder str = new StringBuilder("| ");
        for (String var : select) {
            char[] chars = new char[maxWidthMap.get(var)];
            Arrays.fill(chars, '-');
            str.append(new String(chars));
            str.append(SEP);
        }
        str.setLength(str.length() - 1); // To delete the last space
        return str.toString();
    }

    /**
     * Print the values of the mappings in Markdown format table
     * @return the values of the mappings in Markdown format
     */
    private String values() {
        StringBuilder str = new StringBuilder();
        int size = this.mappings.size();
        int count = 0;

        for (Mapping map : this.mappings) {
            str.append(value(map));
            if (count < size - 1) {
                str.append(System.lineSeparator());
            }
            count++;
        }
        return str.toString();
    }

    /**
     * Print the values of a mapping in Markdown format
     * @param map the mapping to format
     * @return the values of the mapping in Markdown format
     */
    private String value(Mapping map) {
        StringBuilder str = new StringBuilder("| ");
        for (String var : select) {
            str.append(pad(value(map, var), maxWidthMap.get(var)));
            str.append(SEP);
        }
        str.setLength(str.length() - 1); // To delete the last space
        return str.toString();
    }

    /**
     * Format a variable value into a string
     * 
     * @param map the mapping to format
     * @param var the variable to format
     * @return string representation of the variable value
     */
    private String value(Mapping map, String var) {
        Node node = map.getNode(var);
        if (node == null) {
            return "";
        }

        String label = node.getLabel();

        if (node.getDatatypeValue().isURI()) {
            // Add <> around the URL
            return "<" + label + ">";
        } else if (node.getDatatypeValue().isLiteral()) {
            String language = node.getDatatypeValue().getLang();
            String datatype = node.getDatatypeValue().getDatatypeURI();
            if (language != null && !language.isEmpty()) {
                // Add language tag for literals with a language
                return "\"" + label + "\"@" + language;
            } else if (datatype != null && !datatype.isEmpty()) {
                // Add datatype for typed literals
                return "\"" + label + "\"^^<" + datatype + ">";
            } else {
                // Add quotes around untyped literals
                return "\"" + label + "\"";
            }
        }
        return label;
    }

    /**
     * calculate the maximum width of each variable
     */
    private void calculateMaxWidths() {
        for (Mapping map : this.mappings) {
            for (String var : this.select) {
                String value = value(map, var);
                this.maxWidthMap.put(var, Math.max(this.maxWidthMap.get(var), value.length()));
            }
        }
    }

    /**
     * add spaces to the end of a string to reach a given width
     */
    private String pad(String str, int width) {
        return String.format("%1$-" + width + "s", str);
    }
}
