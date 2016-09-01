/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.bgp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.logging.log4j.Level;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author macina
 */
public class AnalyzeResults {
    private org.apache.log4j.Logger logger = org.apache.log4j.LogManager.getLogger(AnalyzeResults.class);
    
    private List<File> logFile = new ArrayList<File>();
    private List<File> resultFile = new ArrayList<File>();
    private HashMap<String, Boolean> queryComparator = new HashMap<String, Boolean>();
    private int round =1;
    private  List<String> queriesName;
    private String base;
    
    public AnalyzeResults(String base, ArrayList<String> queriesName, int round){
        this.base=base;
        this.queriesName = new ArrayList<String>(queriesName);
        this.round = round;
        initFiles();
        
    }
    
    /**
     * 
     */
    public final void initFiles(){
        for(String query : queriesName){
            for(int i=0; i<round; i++){
                logFile.add( new File(base+query+"/"+query+"D"+i+".xml"));
                logFile.add( new File(base+query+"/"+query+"H"+i+".xml"));
                resultFile.add( new File(base+query+"/"+query+"ResultD"+i+".txt"));
                resultFile.add( new File(base+query+"/"+query+"ResultH"+i+".txt"));
            }
        }
        
    }
    
    /**
     * 
     */
    public void resultsComparator(){
        for(int i=0; i<resultFile.size(); i+=2){
            try {
                logger.info(resultFile.get(i)+" ?? "+resultFile.get(i+1));
                queryComparator.put(queriesName.get(i/2),FileUtils.contentEquals(resultFile.get(i), resultFile.get(i+1)));
            } catch (IOException ex) {
                LogManager.getLogger(AnalyzeResults.class.getName()).log(Level.ERROR, "", ex);
            }
        }
        
        for(String s : queriesName){
            logger.info("Query \""+s+"\": same results Default VS Hybrid ? ===> "+queryComparator.get(s).toString().toUpperCase());
        }
        
    }
    
    
//    public static void main(String[]args){
//        ArrayList<String> queries = new ArrayList<String>();
//        queries.add("zsimple");
//        queries.add("zminus");
//        queries.add("zunion");
//        queries.add("zfilters");
//        queries.add("zoptional");
//        queries.add("zall");
//        
//        AnalyzeResults analyzeResult = new AnalyzeResults("/home/macina/NetBeansProjects/corese/kg-dqp/src/main/resources/", queries, 1);
//        analyzeResult.resultsComparator();
//    }
}
