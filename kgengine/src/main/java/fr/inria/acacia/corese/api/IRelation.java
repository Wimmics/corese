package fr.inria.acacia.corese.api;



public interface IRelation {
	public static final int ISUBJECT	= 0;
	public static final int IOBJECT		= 1;
	public static final int IPROPERTY	= 2;
	public static final int ISOURCE		= 3;
	public static final int IMETA		= 4;

	
	public IResource getArg(int i);
	
	public int nbNode();
	
	public int getArity();

	public IType getIType();
	
	public String getType();

}
