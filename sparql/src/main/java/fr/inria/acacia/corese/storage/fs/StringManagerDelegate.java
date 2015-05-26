package fr.inria.acacia.corese.storage.fs;

import fr.inria.acacia.corese.storage.api.Parameters;
import fr.inria.acacia.corese.storage.api.Parameters.type;
import static fr.inria.acacia.corese.storage.fs.Constants.BEGIN;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
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

    private FileHandlersManager fhManager;
    private final Parameters params;

    public StringManagerDelegate(Parameters params) {
        this.params = params;
        fhManager = new FileHandlersManager(this.params);
    }

    /**
     * Reading string from file using given meta info
     * 
     * @param meta
     * @return 
     */
    public String read(StringMeta meta) {
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
            return new String(literalBf.array(), Charset.forName(Constants.ENCODING));
        } catch (IOException ex) {
            Logger.getLogger(StringManagerDelegate.class.getName()).log(Level.SEVERE, "Read " + meta + " error!", ex);
            return null;
        }
    }

    /**
     * Write a string with id to file
     * 
     * @param id
     * @param literal
     * @return 
     */
    public StringMeta write(int id, String literal) {

        FileHandler fhWirte = fhManager.get();
        byte[] bytesLiteral = literal.getBytes(Charset.forName(Constants.ENCODING));

        //if the current file space is not sufficient, create new file 
        if (fhWirte == null || fhWirte.capacity() < bytesLiteral.length) {
            fhWirte = fhManager.createNewFile();
        }

        MappedByteBuffer buffer = fhWirte.getBuffer();
        long offset = fhWirte.position();

        int buf_size = params.get(type.BUF_SIZE);
        int remainingInBuffer = buf_size - buffer.position();
        //** buffer is insuffcient 
        if (remainingInBuffer < bytesLiteral.length) {
            //put the first part of string [0, length - buf_size) to file
            buffer.put(Arrays.copyOfRange(bytesLiteral, BEGIN, remainingInBuffer));

            int remainingLiteral = bytesLiteral.length - remainingInBuffer;
            //if the rest of literal is smaller than the buffer
            if (remainingLiteral <= buf_size) {
                buffer = fhWirte.allocalteBuffer();
            } else {
                //bigger than the buffer, create temporarity a bigger buffer than can put all strings once
                int tmp_buf_size = ((int) (remainingLiteral / buf_size) + 1) * buf_size;
                buffer = fhWirte.allocalteBuffer(tmp_buf_size);
            }
            ////put the second part of string [length - buf_size, end) to file
            buffer.put(Arrays.copyOfRange(bytesLiteral, remainingInBuffer, bytesLiteral.length));

        } else {//put the whole string to buffer -->> to file
            buffer.put(bytesLiteral);
        }

        StringMeta meta = new StringMeta(id, fhWirte.getFid(), (int) offset, literal.length());
        return meta;
    }

    /**
     * Delete strings from files
     * 
     * @param toDelete strings to be deleted
     * @param stringsOnDisk all the strings stored on disk
     */
    public synchronized void delete(Map<Integer, StringMeta> toDelete, Map<Integer, StringMeta> stringsOnDisk) {
        StringManagerDelegate newManager = new StringManagerDelegate(this.params);
        Iterator<Entry<Integer, StringMeta>> it = stringsOnDisk.entrySet().iterator();
        while (it.hasNext()) {
            Entry<Integer, StringMeta> en = it.next();
            int id = en.getKey();
            StringMeta meta = en.getValue();
            if (!toDelete.containsKey(id)) {
                String lit = this.read(meta);
                if (stringsOnDisk.containsKey(id)) {
                    stringsOnDisk.put(id, newManager.write(id, lit));
                }
            }
        }
        System.out.println("[Delete on disk]: " + toDelete.size() + " records are deleted!");
        
        this.clean();
        this.fhManager = newManager.fhManager;
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
}
