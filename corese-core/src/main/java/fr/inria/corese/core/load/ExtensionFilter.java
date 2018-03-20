
package fr.inria.corese.core.load;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 *
 */
public class ExtensionFilter implements FileFilter {
        List<String> list;
        
        public ExtensionFilter(List<String> l){
            list = l;
        }
        
        public ExtensionFilter(){
            list = new ArrayList<String>();
        }
        
        public ExtensionFilter add(String ext){
            list.add(ext);
            return this;
        }

        @Override
        public boolean accept(File pathname) {
            if (list.isEmpty()){
                return true;
            }
            for (String ext : list){               
                if (pathname.getName().endsWith(ext)){
                    return true;
                }
            }
            return false;
        }
        
    }
