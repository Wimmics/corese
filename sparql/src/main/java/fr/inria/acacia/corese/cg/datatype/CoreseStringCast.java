package fr.inria.acacia.corese.cg.datatype;

import fr.inria.acacia.corese.api.IDatatype;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * @author Olivier Corby
 * @version 1.0
 *
 * A fake String to implement str(?x) see FunMarker
 * access a datatype value (?x) as a String
 */

public class CoreseStringCast extends CoreseString {
  IDatatype dt;

  public CoreseStringCast() {}

  public CoreseStringCast(IDatatype dd) {
    dt = dd;
  }

  public void setValue(IDatatype dd){
    dt = dd;
  }

  public String getValue() {
	  return dt.getNormalizedLabel();
  }

  public String getLowerCaseLabel() {
    return dt.getLowerCaseLabel();
  }

  public String getNormalizedLabel() {
    return dt.getNormalizedLabel();
  }


}