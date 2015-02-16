package fr.inria.acacia.corese.persistent.ondisk;

/**
 * Store meta infor of string stored in file
 *
 * Meta.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 13 janv. 2015
 */
public class StringOnDiskMeta {

    //node id
    private int nid;
    //file id
    private int fid;
    private int offset;
    private int length;

    public StringOnDiskMeta(int nid, int fid, int offset, int length) {
        this.nid = nid;
        this.fid = fid;
        this.offset = offset;
        this.length = length;
    }

    public int getNid() {
        return nid;
    }

    public void setNid(int nid) {
        this.nid = nid;
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
        return "String " + "[" + nid + ", " + fid + ", " + offset + ", " + length + ']';
    }
}
