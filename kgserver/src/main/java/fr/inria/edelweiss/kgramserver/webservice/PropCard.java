/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.kgramserver.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author gaignard
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType (name = "MonTypeDeRetour", propOrder = {"key","value"})
public class PropCard {

    @XmlElement(name="return",required = true)
    protected Object key;

    @XmlElement(name="return1",required = true)
    protected Object value;

    public PropCard() {
    }

    public Object getKey() {
        return key;
    }

    public void setKey(Object value) {
        this.key = value;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Table["+this.getKey()+"]="+this.getValue();
    }
   
} 
