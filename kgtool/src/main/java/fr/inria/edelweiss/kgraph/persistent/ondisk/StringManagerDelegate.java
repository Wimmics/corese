package fr.inria.edelweiss.kgraph.persistent.ondisk;

import static fr.inria.edelweiss.kgraph.persistent.ondisk.Parameters.BEGIN;
import static fr.inria.edelweiss.kgraph.persistent.ondisk.Parameters.BUF_SIZE;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * delegate class for read/write/delete string from file via file handler
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 13 janv. 2015
 */
public class StringManagerDelegate {

    private static StringManagerDelegate manager;
    private FileHandlersManager fhManager;

    private StringManagerDelegate() {
        fhManager = new FileHandlersManager(Parameters.CACHE_FILE_SIZE);
    }

    public static StringManagerDelegate getInstance() {
        return getInstance(false);
    }

    public static StringManagerDelegate getInstance(boolean newInstance) {
        if (newInstance || manager == null) {
            manager = new StringManagerDelegate();
        }

        return manager;
    }

    public String read(StringOnDiskMeta meta) {
        if (meta == null) {
            return null;
        }

        try {
            //== 1. get file hander from files pool ==
            FileHandler fh = fhManager.get(meta.getFid());

            //== 2. allocalte a new byte buffer for putting the read literal
            ByteBuffer literalBf = ByteBuffer.allocate(meta.getLength());
            //== 3. read via channel
            fh.getChannel().read(literalBf, meta.getOffset());
            return new String(literalBf.array(), Charset.forName(Parameters.ENCODING));
        } catch (IOException ex) {
            Logger.getLogger(StringManagerDelegate.class.getName()).log(Level.SEVERE, "Read " + meta + " error!", ex);
        }
        return null;
    }

    public StringOnDiskMeta write(int id, String literal) {

        FileHandler fhWirte = fhManager.get();

        //if the current file space is not sufficient, create new file 
        if (fhWirte == null || fhWirte.capacity() < literal.length()) {
            fhWirte = fhManager.createNewFile();
        }

        MappedByteBuffer buffer = fhWirte.getBuffer();
        long offset = fhWirte.position();

        int remainingInBuffer = BUF_SIZE - buffer.position();
        //** buffer is insuffcient 
        if (remainingInBuffer < literal.length()) {
            //put the first part of string [0, length - buf_size) to file
            buffer.put(literal.substring(BEGIN, remainingInBuffer).getBytes(Charset.forName(Parameters.ENCODING)));

            int remainingLiteral = literal.length() - remainingInBuffer;
            //if the rest of literal is smaller than the buffer
            if (remainingLiteral <= BUF_SIZE) {
                buffer = fhWirte.allocalteBuffer();
            } else {
                //bigger than the buffer, create temporarity a bigger buffer than can put all strings once
                int tmp_buf_size = (remainingLiteral % BUF_SIZE + 1) * BUF_SIZE;
                buffer = fhWirte.allocalteBuffer(tmp_buf_size);
            }
            ////put the second part of string [length - buf_size, end) to file
            buffer.put(literal.substring(remainingInBuffer).getBytes(Charset.forName(Parameters.ENCODING)));

        } else {//put the whole string to buffer -->> to file
            buffer.put(literal.getBytes(Charset.forName(Parameters.ENCODING)));
        }

        StringOnDiskMeta meta = new StringOnDiskMeta(id, fhWirte.getFid(), (int) offset, literal.length());
        return meta;
    }

    //copy the literals to new files
    public void delete(Map<Integer, StringOnDiskMeta> toDelete, Map<Integer, StringOnDiskMeta> literalsOnDisk) {
        StringManagerDelegate newManager = new StringManagerDelegate();
        Iterator<Entry<Integer, StringOnDiskMeta>> it = literalsOnDisk.entrySet().iterator();
        while (it.hasNext()) {
            Entry<Integer, StringOnDiskMeta> en = it.next();
            int id = en.getKey();
            StringOnDiskMeta meta = en.getValue();
            if (!toDelete.containsKey(id)) {
                String lit = manager.read(meta);
                StringOnDiskMeta meta2 = newManager.write(id, lit);
                literalsOnDisk.replace(id, meta2);
            }
        }
        System.out.println("[Delete on disk]: " + toDelete.size() + " records are deleted!");
        toDelete.clear();
        manager.clean();
        manager.fhManager = newManager.fhManager;
    }

    /**
     * Close the connections to files on disk, chanel, stream, buffer
     */
    public void clean() {
        fhManager.clear();
    }

    @Override
    public String toString() {
        return fhManager.toString();
    }

    public void load() {
        //initialize the file handlers from files on disk
        //only if the strings are persistent on disk
        //not used for now
    }
}
