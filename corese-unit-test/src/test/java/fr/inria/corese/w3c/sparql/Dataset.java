package fr.inria.corese.w3c.sparql;

import java.util.ArrayList;
import java.util.List;

import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;

public class Dataset {

	ArrayList<String> uris, names;

	Dataset() {
		uris = new ArrayList<String>();
		names = new ArrayList<String>();
	}

	Dataset init(Mappings lm, String vname, String vuri) {

		for (Mapping map : lm) {
			Node uri = map.getNode(vuri);
			Node name = map.getNode(vname);
			if (uri != null) {
				if (name != null) {
					add(name.getLabel(), uri.getLabel());
				} else {
					addURI(uri.getLabel());
				}
			}
		}

		return this;
	}

	void add(String name, String uri) {
		int i = 0;
		for (String n : names) {
			if (n.equals(name) && uris.get(i).equals(uri)) {
				return;
			}
			i++;
		}
		uris.add(uri);
		names.add(name);
	}

	void addURI(String uri) {
		if (!uris.contains(uri)) {
			uris.add(uri);
		}
	}

	List<String> getURIs() {
		return uris;
	}

	List<String> getNames() {
		return names;
	}

	String getNameOrURI(int i) {
		if (names.size() > 0) {
			return names.get(i);
		} else {
			return uris.get(i);
		}
	}
}
