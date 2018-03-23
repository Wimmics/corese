package fr.inria.corese.sparql.storage.fs;

import fr.inria.corese.sparql.storage.api.Parameters;
import fr.inria.corese.sparql.storage.api.Parameters.type;
import static fr.inria.corese.sparql.storage.fs.Constants.BEGIN;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.LoggerFactory;

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
            LoggerFactory.getLogger(StringManagerDelegate.class.getName()).error( "Read " + meta + " error!", ex);
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

        FileHandler fhWrite = fhManager.get();
        byte[] bytesLiteral = literal.getBytes(Charset.forName(Constants.ENCODING));

        //if the current file space is not sufficient, create new file 
        if (fhWrite == null || fhWrite.capacity() < bytesLiteral.length) {
            fhWrite = fhManager.createNewFile();
        }

        //MappedByteBuffer buffer = fhWirte.getBuffer();
        long offset = fhWrite.position();

        int buf_size = params.get(type.BUF_SIZE);
        int remainingInBuffer = buf_size - fhWrite.getBuffer().position();
        //** buffer is insuffcient 
        if (remainingInBuffer < bytesLiteral.length) {
            //put the first part of string [0, length - buf_size) to file
            fhWrite.getBuffer().put(Arrays.copyOfRange(bytesLiteral, BEGIN, remainingInBuffer));

            int remainingLiteral = bytesLiteral.length - remainingInBuffer;
            //if the rest of literal is smaller than the buffer
            if (remainingLiteral <= buf_size) {
                fhWrite.allocalteBuffer();
                fhWrite.getBuffer().put(Arrays.copyOfRange(bytesLiteral, remainingInBuffer, bytesLiteral.length));
            } else {
                //bigger than the buffer, create temporarity a bigger buffer than can put all strings once
                //int tmp_buf_size = ((int) (remainingLiteral / buf_size) + 1) * buf_size;
                fhWrite.allocalteBuffer(remainingLiteral);
                fhWrite.getBuffer().put(Arrays.copyOfRange(bytesLiteral, remainingInBuffer, bytesLiteral.length));
                fhWrite.allocalteBuffer();
            }
            ////put the second part of string [length - buf_size, end) to file
            //buffer.put(Arrays.copyOfRange(bytesLiteral, remainingInBuffer, bytesLiteral.length));

        } else {//put the whole string to buffer -->> to file
            fhWrite.getBuffer().put(bytesLiteral);
        }

        // *** alternative ** to be verified
//        int stringToStore = bytesLiteral.length;
//        int start = 0;
//        while (stringToStore > 0) {
//            int availableBuffer = buf_size - fhWrite.getBuffer().position();
//
//            if (availableBuffer > stringToStore) {
//                fhWrite.getBuffer().put(Arrays.copyOfRange(bytesLiteral, start, start + stringToStore));
//                stringToStore = 0;
//            } else {
//                fhWrite.getBuffer().put(Arrays.copyOfRange(bytesLiteral, start, start + availableBuffer));
//                fhWrite.allocalteBuffer();
//                stringToStore -= availableBuffer;
//                start += availableBuffer;
//            }
//        }
        //** end ***

        //StringMeta meta = new StringMeta(id, fhWirte.getFid(), (int) offset, literal.length());
        StringMeta meta = new StringMeta(id, fhWrite.getFid(), (int) offset, bytesLiteral.length);
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
        LoggerFactory.getLogger(StringManagerDelegate.class.getName()).info( "{} records are deleted!", toDelete.size());

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
