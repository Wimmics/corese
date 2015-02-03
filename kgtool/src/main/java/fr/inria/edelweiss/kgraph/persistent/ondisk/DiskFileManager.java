package fr.inria.edelweiss.kgraph.persistent.ondisk;

import static fr.inria.edelweiss.kgraph.persistent.ondisk.Parameters.BEGIN;
import static fr.inria.edelweiss.kgraph.persistent.ondisk.Parameters.BUF_SIZE;
import static fr.inria.edelweiss.kgraph.persistent.ondisk.Parameters.FILE_NAME;
import static fr.inria.edelweiss.kgraph.persistent.ondisk.Parameters.EXT_TXT;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DiskFileManager.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 13 janv. 2015
 */
public class DiskFileManager {

    private MappedByteBuffer buffer;
    private RandomAccessFile memoryMappedFile;
    private FileChannel inChannel;
    private ByteBuffer literalBf;
    private File current;
    private long bufferOffset;


    private static DiskFileManager manager;
    private LRUCacheFileHandler fileHandlers;

    private DiskFileManager(String file) {
        try {
            if (file == null) {
                //create temporary file, deleted on exit automatically
                //tmp = File.createTempFile(FILE_NAME, EXT_TXT, new File("/Users/fsong/NetBeansProjects/"));
                current = File.createTempFile(FILE_NAME, EXT_TXT);
            } else {
                current = new File(file);
            }

            current.deleteOnExit();
            memoryMappedFile = new RandomAccessFile(current, Parameters.MODE);
            inChannel = memoryMappedFile.getChannel();
            buffer = inChannel.map(FileChannel.MapMode.READ_WRITE, BEGIN, BUF_SIZE);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DiskFileManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DiskFileManager.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static DiskFileManager getInstance() {
        return getInstance(null, false);
    }

    public static DiskFileManager getInstance(String file, boolean newInstance) {
        if (newInstance || manager == null) {
            manager = new DiskFileManager(file);
        }

        return manager;
    }

    public String read(LiteralOnDiskMeta meta) throws IOException {
        //allocalte a new byte buffer for putting the read literal
        literalBf = ByteBuffer.allocate(meta.getLength());
        //read via channel
        inChannel.read(literalBf, meta.getOffset());
        return new String(literalBf.array(), Charset.forName(Parameters.ENCODING));

    }

    public LiteralOnDiskMeta write(int id, String literal) throws IOException {
        // === save to a single file ===

        long offset = bufferOffset + buffer.position();
        int remaining = BUF_SIZE - buffer.position();
        if (remaining < literal.length()) {
            //put the first part of string [0, length - buf_size) to file
            buffer.put(literal.substring(BEGIN, remaining).getBytes(Charset.forName(Parameters.ENCODING)));
            //map new buffer area
            buffer = inChannel.map(FileChannel.MapMode.READ_WRITE, inChannel.size(), BUF_SIZE);
            bufferOffset += buffer.capacity();
            ////put the second part of string [length - buf_size, end) to file
            buffer.put(literal.substring(remaining).getBytes(Charset.forName(Parameters.ENCODING)));
        } else {//put the whole string to buffer -->> to file
            buffer.put(literal.getBytes(Charset.forName(Parameters.ENCODING)));
        }

        LiteralOnDiskMeta meta = new LiteralOnDiskMeta(id, 0, (int) offset , literal.length());
        return meta;
    }

//    //delete the literals in single files
//    public void delete(Map<Integer, LiteralOnDiskMeta> toDelete) {
//        int n = toDelete.size();
//        // === 1 delete literals that saved in single files
//        Iterator<Entry<Integer, LiteralOnDiskMeta>> it = toDelete.entrySet().iterator();
//        while (it.hasNext()) {
//            LiteralOnDiskMeta meta = it.next().getValue();
//            if (meta.getType() == SINGLE) {
//                new File(meta.getFile()).delete();
//                it.remove();
//            }
//        }
//
//        System.out.println("[Delete on disk]: " + n + " records in single files deleted!");
//    }

    //copy the literals to a new file
    public void delete(Map<Integer, LiteralOnDiskMeta> toDelete, Map<Integer, LiteralOnDiskMeta> literalsOnDisk) {
        // === 2 copy the literals to a new file
        DiskFileManager newManager = new DiskFileManager(null);
        Iterator<Entry<Integer, LiteralOnDiskMeta>> it = literalsOnDisk.entrySet().iterator();
        try {
            while (it.hasNext()) {
                Entry<Integer, LiteralOnDiskMeta> en = it.next();
                int id = en.getKey();
                LiteralOnDiskMeta meta = en.getValue();
                if (!toDelete.containsKey(id)) {
                    String lit = manager.read(meta);
                    LiteralOnDiskMeta meta2 = newManager.write(id, lit);
                    literalsOnDisk.replace(id, meta2);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(DiskFileManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            System.out.println("[Delete on disk]: " + toDelete.size() + " records in collective file are deleted!");
            toDelete.clear();
            manager.closeConnection();
            manager.current.delete();
            manager.current = newManager.current;
            manager.bufferOffset = newManager.bufferOffset;
            manager.buffer = newManager.buffer;
            manager.inChannel = newManager.inChannel;
            manager.memoryMappedFile = newManager.memoryMappedFile;
        }
    }

    /**
     * Close the connections to files on disk, chanel, stream, buffer
     */
    public void closeConnection() {
        try {
            buffer.clear();
            inChannel.close();
            memoryMappedFile.close();
        } catch (IOException ex) {
            Logger.getLogger(DiskFileManager.class.getName()).log(Level.SEVERE, "Close connection errors!", ex);
        }
    }

}
