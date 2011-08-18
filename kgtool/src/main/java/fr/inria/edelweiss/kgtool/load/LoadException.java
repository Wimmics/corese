package fr.inria.edelweiss.kgtool.load;

public class LoadException extends Exception {
	
	private Exception ex;
	private Object object;
	
	LoadException(Exception ee){
		this.set(ee);
	}
	
	private void set(Exception ee) {
		ex = ee;
	}

	LoadException(Exception ee, Object oo){
		this.set(ee);
		this.setObject(oo);
	}
	
	public static LoadException create(Exception e){
		return new LoadException(e);
	}
	
	public static LoadException create(Exception e, Object o){
		return new LoadException(e, o);
	}
	
	public String toString(){
		String str = getException().getMessage();
		if (getObject() != null){
			str += "\n" + getObject();
		}
		return str;
	}

	public Exception getException() {
		return ex;
	}

	void setObject(Object object) {
		this.object = object;
	}

	public Object getObject() {
		return object;
	}
	
	
	

}
