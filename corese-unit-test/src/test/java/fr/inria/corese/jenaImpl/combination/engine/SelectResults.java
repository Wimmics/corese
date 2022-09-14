package fr.inria.corese.jenaImpl.combination.engine;

import java.util.ArrayList;
import java.util.HashMap;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;

public class SelectResults {

    private ArrayList<HashMap<String, Node>> result_list;

    public SelectResults(Mappings mappings) {
        this.result_list = new ArrayList<HashMap<String, Node>>();

        for (Mapping m : mappings) {
            HashMap<String, Node> result = new HashMap<>();
            for (Node selected : mappings.getSelect()) {
                result.put(selected.getLabel(), m.getNodeValue(selected));
            }

            this.result_list.add(result);
        }
    }

    public ArrayList<HashMap<String, Node>> getResultList() {
        return this.result_list;
    }

    @Override
    public String toString() {
        String result = "";

        result += this.result_list.size() + " results\n";

        for (HashMap<String, Node> el : this.result_list) {
            result += el + "\n";
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        // If the object is compared with itself then return true
        if (obj == this) {
            return true;
        }

        /*
         * Check if o is an instance of Complex or not "null instanceof [type]" also
         * returns false
         */
        if (!(obj instanceof SelectResults)) {
            return false;
        }
        SelectResults other = (SelectResults) obj;

        // Compare result list
        Boolean equal = true;
        equal &= this.result_list.containsAll(other.result_list);
        equal &= other.result_list.containsAll(this.result_list);

        return equal;
    }

    @Override
    public int hashCode() {
        return this.result_list.hashCode();
    }

    public boolean equals(SelectResults... results_list) {
        if (result_list == null || results_list.length == 0) {
            return false;
        }

        for (SelectResults results : results_list) {
            if (!this.equals(results)) {
                return false;
            }
        }

        return true;
    }

}
