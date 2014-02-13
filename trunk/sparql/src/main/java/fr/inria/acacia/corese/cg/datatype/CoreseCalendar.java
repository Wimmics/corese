package fr.inria.acacia.corese.cg.datatype;

import java.util.GregorianCalendar;

/**
 * <p>Title: Corese</p>
 * <p>Description: A Semantic Search Engine</p>
 * <p>Copyright: Copyright INRIA (c) 2007</p>
 * <p>Company: INRIA</p>
 * <p>Project: Acacia</p>
 * <br> 
 * This class is used to create and manage a CoreseDate (xsd:DateTime)
 * <br>
 * @author Olivier Corby & Olivier Savoie
 */

public class CoreseCalendar extends GregorianCalendar {
	
	/** Use to keep the class version, to be consistent with the interface Serializable.java */
	private static final long serialVersionUID = 1L;
	
      float rest = 0; // if float number of seconds, numbers after 0.
      boolean Z = false;
      String num = null, zone = "";
      
      CoreseCalendar(){}

      CoreseCalendar(int yy, int mm, int dd){
        super(yy, mm, dd);
      }
      
      void setZ(boolean z){
    	  this.Z = z;
      }
      
      boolean getZ(){
    	  return Z;
      }
      
      void setDZone(String z){
    	  this.zone = z;
      }
      
      String getDZone(){
    	  return zone;
      }
      
      void setNum(String n){
    	  this.num = n;
      }
      
      String getNum(){
    	  return num;
      }

      void setSeconds(float f){
        rest = f;
      }

      float getSeconds(){
        return rest;
      }
      
      /**
         * 1 BC -> -1
         * 2 BC -> -2
         */
        int theYear(){
            int year = get(GregorianCalendar.YEAR);
            if (get(GregorianCalendar.ERA) == GregorianCalendar.BC){
                year = -year;
            }
            return year;
        }

  }


