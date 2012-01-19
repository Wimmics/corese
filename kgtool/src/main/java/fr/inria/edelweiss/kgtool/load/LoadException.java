package fr.inria.edelweiss.kgtool.load;

public class LoadException extends Exception {
	
	private Exception ex;
	private Object object;
	private String path;
	
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
	
	public static LoadException create(Exception e, String p){
		LoadException ee = new LoadException(e);
		ee.setPath(p);
		return ee;
	}
	
	public static LoadException create(Exception e, Object o){
		return new LoadException(e, o);
	}
	
	public static LoadException create(Exception e, Object o, String p){
		LoadException ee =  new LoadException(e, o);
		ee.setPath(p);
		return ee;
	}
	
	public String toString(){
		if (ex == null) return super.toString();
		
		String str = ex.getClass().getName() + " ";
		if (ex.getMessage()!=null){
			str += ex.getMessage();
		}
		else {
			str += ex.toString();
		}
		if (getObject() != null){
			str += "\n" + getObject();
		}
		else {
			str += "\n" + getPath();
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

	public void setPath(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}
	
	
	

}
