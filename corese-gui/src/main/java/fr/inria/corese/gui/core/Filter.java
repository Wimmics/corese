package fr.inria.corese.gui.core;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class Filter extends FileFilter {

    private String[] extension;
    private String description;

    /**
     * 
     */
    public Filter(String description, String... extension) {
        this.description = description.concat(pretty(extension));
        this.extension = extension;
    }
    
    String pretty(String[] lext) {
        StringBuilder sb = new StringBuilder();
        sb.append(" (");
        int count = 0;
        for (String ext : lext) {
            if (count++>0) {
                sb.append(" ");
            }
            sb.append(".").append(ext);
        }
        sb.append(")");
        return sb.toString();
    }

    
    boolean appartient(String monExtension) {
        for (String extension1 : extension) {
            if (monExtension.equals(extension1)) {
                return true;
            }
        }
        return false;

    }

    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;    
        }
        String ext = null;
        String s = f.getName();		
        int i = s.lastIndexOf('.');	

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();		
        }
        return ext != null && appartient(ext);	
    }

    @Override
    public String getDescription() {
        return description;
    }

}
