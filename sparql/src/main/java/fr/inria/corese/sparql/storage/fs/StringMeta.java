package fr.inria.corese.sparql.storage.fs;

/**
 * Store meta infor of string stored in file
 *
 * StringMeta.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 13 janv. 2015
 */
public class StringMeta {

    private int id; // string id
    private int fid; // file id
    private int offset;
    private int length;

    public StringMeta(int id, int fid, int offset, int length) {
        this.id = id;
        this.fid = fid;
        this.offset = offset;
        this.length = length;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFid() {
        return fid;
    }

    public void setFid(int fid) {
        this.fid = fid;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    @Override
    public String toString() {
        return "String " + "[" + id + ", " + fid + ", " + offset + ", " + length + ']';
    }
}
