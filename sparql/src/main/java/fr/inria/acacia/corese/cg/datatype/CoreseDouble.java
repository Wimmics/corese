package fr.inria.acacia.corese.cg.datatype;
import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.exceptions.CoreseDatatypeException;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br>
 * An implementation of the xsd:double datatype used by Corese
 * <br>
 * @author Olivier Savoie
 */

public  class CoreseDouble extends CoreseNumber{
	static final CoreseURI datatype=new CoreseURI(RDF.xsddouble);
	
	public CoreseDouble(String value) {
		super(value);
	}
	
	public CoreseDouble(double value) {
		super(value);
	}
	
	public IDatatype getDatatype(){
		return datatype;
	}
	
	public boolean isStringable(){ return false; }
	
	public boolean isOrdered(){ return true;}
	
	public boolean isRegExpable(){return false;}
	
	
	public int compare(IDatatype iod) throws CoreseDatatypeException {
		return iod.polyCompare(this);
	}
	
	public int polyCompare(CoreseDouble icod) throws CoreseDatatypeException {
		double d1 = icod.getdValue();
		return (d1 < dvalue) ? -1 : (d1 == dvalue ? 0 : 1);
	}
	
	public int polyCompare(CoreseLong icod) throws CoreseDatatypeException {
		double d1 = icod.getdValue();
		return (d1 < dvalue) ? -1 : (d1 == dvalue ? 0 : 1);
	}
	
	
	public boolean less(IDatatype iod)  throws  CoreseDatatypeException {
		return iod.polymorphGreater(this);
	}
	
	public boolean lessOrEqual(IDatatype iod) throws CoreseDatatypeException {
		return iod.polymorphGreaterOrEqual(this);
	}
	
	public boolean greater(IDatatype iod) throws CoreseDatatypeException {
		return iod.polymorphLess(this);
	}
	
	public boolean greaterOrEqual(IDatatype iod) throws CoreseDatatypeException {
		return iod.polymorphLessOrEqual(this);
	}
	
	public boolean equals(IDatatype iod) throws CoreseDatatypeException{
		return iod.polymorphEquals(this);
	}
	
	
	
	public IDatatype plus(IDatatype iod) {
		
		return iod.polyplus(this);
	}
	
	public IDatatype minus(IDatatype iod) {
		return iod.polyminus(this);
	}
	
	public IDatatype mult(IDatatype iod) {
		return iod.polymult(this);
	}
	
	public IDatatype div(IDatatype iod) {
		return iod.polydiv(this);
	}
	
	public String getNormalizedLabel(){
		String label = Double.toString(dvalue);
		String str = infinity(label);
		return (str==null) ? label : str;
	}
	
	static String infinity(String label){
		if (label.equals("Infinity") || label.equals("+Infinity")) {
			return  "INF";
		} else if (label.equals("-Infinity")) {
			return "-INF";
		}
		return null;
	}
	
	public static String getNormalizedLabel(String label){
		//System.out.println("CoreseDouble.java - label: "+label);
		String str = infinity(label);
		if (str!=null) return str;
		double v = Double.parseDouble(label);
		double floor = Math.floor(v);
		if(! DatatypeMap.SEVERAL_NUMBER_SPACE &&
			 floor == v && v <= Long.MAX_VALUE && v >= Long.MIN_VALUE){
			return Long.toString((long)floor);
		}
		else{
			return Double.toString(v);
		}
	}
	
	public String getLowerCaseLabel(){
		return Double.toString(dvalue);
	}
	
	
	
	public boolean polymorphGreaterOrEqual(CoreseDouble icod){
		return dvalue >= icod.getdValue();
	}
	
	public boolean polymorphGreater(CoreseDouble icod){
		return dvalue > icod.getdValue();
	}
	
	public boolean polymorphLessOrEqual(CoreseDouble icod){
		return dvalue <= icod.getdValue();
	}
	
	public boolean polymorphLess(CoreseDouble icod){
		return dvalue < icod.getdValue();
	}
	
	public boolean polymorphEquals(CoreseDouble icod){
		return dvalue == icod.getdValue();
	}
	
	
	/******************LONG*************************/
	
	public boolean polymorphGreaterOrEqual(CoreseLong icod){
		return dvalue >= icod.getdValue();
	}
	
	public boolean polymorphGreater(CoreseLong icod){
		return dvalue > icod.getdValue();
	}
	
	public boolean polymorphLessOrEqual(CoreseLong icod){
		return dvalue <= icod.getdValue();
	}
	public boolean polymorphLess(CoreseLong icod){
		return dvalue < icod.getdValue();
	}
	
	public boolean polymorphEquals(CoreseLong icod){
		return dvalue == icod.getdValue();
	}
	
	
	/******************************************************************/
	
	
	public IDatatype polyplus(CoreseDouble iod) {
		return new CoreseDouble(getdValue() + iod.getdValue());
	}
	
	public IDatatype polyminus(CoreseDouble iod) {
		return new CoreseDouble(iod.getdValue() - getdValue());
	}
	
	public IDatatype polymult(CoreseDouble iod) {
		return new CoreseDouble(getdValue() * iod.getdValue());
	}
	
	public IDatatype polydiv(CoreseDouble iod) {
		return new CoreseDouble(iod.getdValue() / getdValue());
	}
	
	
	
	
	public IDatatype polyplus(CoreseLong iod) {
		return new CoreseDouble(getdValue() + iod.getdValue());
	}
	
	public IDatatype polyminus(CoreseLong iod) {
		return new CoreseDouble(iod.getdValue() - getdValue());
	}
	
	public IDatatype polymult(CoreseLong iod) {
		return new CoreseDouble(getdValue() * iod.getdValue());
	}
	
	public IDatatype polydiv(CoreseLong iod) {
		return new CoreseDouble(iod.getdValue() / getdValue());
	}
	
	
	
}