package fr.inria.corese.core.print;

import static fr.inria.corese.core.print.JSONLDFormat.BRACE_LEFT;
import static fr.inria.corese.core.print.JSONLDFormat.BRACE_RIGHT;
import static fr.inria.corese.core.print.JSONLDFormat.OC_BRACE;
import static fr.inria.corese.core.print.JSONLDFormat.OC_NOCOMMA;
import static fr.inria.corese.core.print.JSONLDFormat.OC_NOKEY;
import static fr.inria.corese.core.print.JSONLDFormat.OC_LIST;
import static fr.inria.corese.core.print.JSONLDFormat.OC_NONE;
import static fr.inria.corese.core.print.JSONLDFormat.OC_SBRACKET;
import static fr.inria.corese.core.print.JSONLDFormat.SBRACKET_LEFT;
import static fr.inria.corese.core.print.JSONLDFormat.SBRACKET_RIGHT;
import static fr.inria.corese.core.print.JSONLDFormat.SP_COLON;
import static fr.inria.corese.core.print.JSONLDFormat.SP_COMMA;
import static fr.inria.corese.core.print.JSONLDFormat.SP_NL;
import static fr.inria.corese.core.print.JSONLDFormat.SP_TAB;
import java.util.ArrayList;
import java.util.List;

/**
 * Object class for Json-ld Object
 *
 * @author Fuqi Song, Wimmics - Inria I3S
 * @date Feb. 2014
 */
/**
 * Class of JSON Object including pair {key, object}, and modular type, which is
 * used to decide the form of printing list
 *
 */
class JSONLDObject {

    private String key;
    private Object object;
    private int modularType;

    private List listOfObjects;

    public JSONLDObject(String key, Object object, int type) {
        this(key, object);
        this.modularType = type;
    }

    public JSONLDObject(String key, Object object) {
        this(key);
        this.object = object;
    }

    public JSONLDObject(String key) {
        this.key = key;
    }

    public JSONLDObject(int modularType) {
        this("", modularType);
    }

    public JSONLDObject(String key, int modularType) {
        this(key);
        this.modularType = modularType;
    }

    public JSONLDObject() {
        this("");
    }

    public String getKey() {
        return key;
    }

    public Object getObject() {
        return object;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public int getModularType() {
        return modularType;
    }

    public void setModularType(int modularType) {
        this.modularType = modularType;
    }

    /**
     * Add an object to list of objects, and set the list as the object of this
     * instance
     *
     * @param jo
     */
    public void addObject(Object jo) {
        if (listOfObjects == null) {
            listOfObjects = new ArrayList();
        }

        if (jo instanceof JSONLDObject || jo instanceof String) {
            listOfObjects.add(jo);
        } else if (jo instanceof List) {
            listOfObjects.addAll((List) jo);
        }

        this.setObject(listOfObjects);
    }

    @Override
    public String toString() {
        return toStringBuilder().toString();
    }

    public StringBuilder toStringBuilder() {
        if (key == null || object == null) {
            return new StringBuilder();
        }

        StringBuilder sb = new StringBuilder();

        //1. add directly
        if (this.object instanceof String
                //|| this.object instanceof Object
                || this.object instanceof StringBuilder
                || this.object instanceof JSONLDObject) {
            if(this.key !=null && !this.key.isEmpty()) sb.append(this.key).append(SP_COLON);
            sb.append(this.object).append(SP_COMMA).append(SP_NL);

            //2. add recursively 
        } else if (this.object instanceof List) {
            List<JSONLDObject> list = (List<JSONLDObject>) this.object;
            String open = "", end = "";
            //2.1 decide the open/close type/form
            switch (modularType) {
                case OC_BRACE://@key:{..},
                    if (!this.key.isEmpty()) {
                        open = this.key + SP_COLON;
                    }
                    open += BRACE_LEFT + SP_NL;
                    end = BRACE_RIGHT + SP_COMMA + SP_NL;
                    break;
                case OC_NOCOMMA://@key{..}
                    if (!this.key.isEmpty()) {
                        open = this.key + SP_COLON;
                    }
                    open += BRACE_LEFT + SP_NL;
                    end = BRACE_RIGHT;
                    break;
                case OC_LIST:
                case OC_SBRACKET://@key[..]
                    if (!this.key.isEmpty()) {
                        open = this.key + SP_COLON;
                    }
                    open += SBRACKET_LEFT + SP_NL;
                    end = SBRACKET_RIGHT + SP_COMMA + SP_NL;
                    break;
                case OC_NOKEY://{..}
                    open = BRACE_LEFT + SP_NL;
                    end = BRACE_RIGHT + SP_NL;
                    break;
                case OC_NONE://..
                    break;
            }

            //2.2 add open/end and each object
            sb.append(open);
            for (JSONLDObject obj : list) {
                if (modularType == OC_NONE) {
                    sb.append(obj);
                } else {
                    sb.append(indent((obj).toStringBuilder(), 1));
                }
            }

            //2.3 remove comma of list object
            if (modularType != OC_NONE) {
                //remove the comma at the end of list
                eliminate(sb);
            }

            sb.append(end);
        } else {
            sb.append("Object type error!!!\n");
        }
        return sb;
    }

    //remove the comma at the end of each object or statement
    private void eliminate(StringBuilder block) {
        int length = block.length();
        //end with ","
        if (length > 0 && block.substring(length - 1).equals(SP_COMMA)) {
            block.deleteCharAt(length - 1);
        }

        //end with ",\n"
        if (length > 1 && block.substring(length - 2).equals(SP_COMMA + SP_NL)) {
            block.deleteCharAt(length - 2);
        }
    }

    // indent a block of text 
    private StringBuilder indent(StringBuilder block, int tabs) {
        StringBuilder sb = new StringBuilder();
        if (block == null || block.length() == 0) {
            return sb;
        }

        //1. check whether ending with new line, if not, add it
        int length = block.length(), index = 0;
        if (length > 0 && !block.substring(length - 1).equals(SP_NL)) {
            sb.append(SP_NL);
        }

        //2. read each line and add indentation at the beginning 
        while (index < length) {
            int end = block.indexOf(SP_NL, index) + 1;
            if (end == 0) {
                break;
            }

            sb.append(indentation(tabs)).append(block.substring(index, end));
            index = end;
        }
        sb.append(indentation(tabs - 1));
        return sb;
    }

    //get the string of indentation
    private String indentation(int tabs) {
        String indent = "";
        for (int i = 0; i < tabs; i++) {
            indent += SP_TAB;
        }
        return indent;
    }
}
