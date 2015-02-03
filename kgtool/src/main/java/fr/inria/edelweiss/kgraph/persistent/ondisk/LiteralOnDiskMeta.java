package fr.inria.edelweiss.kgraph.persistent.ondisk;

/**
 * Meta.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 13 janv. 2015
 */
public class LiteralOnDiskMeta {

    //node id
    private int nid;
    //file id
    private int fid;
    private int offset;
    private int length;

    public LiteralOnDiskMeta(int nid, int fid, int offset, int length) {
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
        return "Meta " + "[" + nid + ", " + fid + ", " + offset + ", " + length + ']';
    }
}
