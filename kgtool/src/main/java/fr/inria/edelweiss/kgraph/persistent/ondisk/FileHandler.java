package fr.inria.edelweiss.kgraph.persistent.ondisk;

import static fr.inria.edelweiss.kgraph.persistent.ondisk.Parameters.BEGIN;
import static fr.inria.edelweiss.kgraph.persistent.ondisk.Parameters.BUF_SIZE;
import static fr.inria.edelweiss.kgraph.persistent.ondisk.Parameters.EXT_TXT;
import static fr.inria.edelweiss.kgraph.persistent.ondisk.Parameters.FILE_NAME;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handler for manipulating disk file, channel, buffer, etc..
 * 
 * FileHandler.java
 *
 * @author Fuqi Song, Wimmics Inria I3S
 * @date 3 févr. 2015 new
 */
public class FileHandler {

    private File file;
    private RandomAccessFile randomAccessFile;
    private FileChannel fileChannel;
    private MappedByteBuffer buffer;
    private long bufferOffset;

    private int fid;
    private String fname;
    private long fsize = Parameters.MAX_FILE_SIZE;

    public FileHandler(int fid) {
        this(fid, null);
    }

    public FileHandler(int fid, String file) {
        try {
            this.fid = fid;
            this.file = (file == null) ? File.createTempFile(FILE_NAME, EXT_TXT) : new File(file);

            this.fname = this.file.getAbsolutePath();
            this.file.deleteOnExit();
            randomAccessFile = new RandomAccessFile(this.file, Parameters.MODE);
            fileChannel = randomAccessFile.getChannel();
            buffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, BEGIN, BUF_SIZE);
        } catch (IOException ex) {
            Logger.getLogger(FileHandler.class.getName()).log(Level.SEVERE, "id:" + fid + ", path:" + file, ex);
        }
    }

    /**
     * Get the file channel
     *
     * @return
     */
    public FileChannel getChannel() {
        return fileChannel;
    }

    /**
     * Get the byte buffer
     *
     * @return
     */
    public MappedByteBuffer getBuffer() {
        return buffer;
    }

    /**
     * Get file name
     *
     * @return
     */
    public String getFname() {
        return fname;
    }

    /**
     * Get file id
     *
     * @return
     */
    public int getFid() {
        return fid;
    }

    public void setFileSize(int size) {
        this.fsize = size;
    }

    /**
     * Allocate a new buffer using the default buffer_size
     *
     * @return
     */
    public MappedByteBuffer allocalteBuffer() {
        return this.allocalteBuffer(BUF_SIZE);
    }

    /**
     * Allocate a new buffer using given buffer_size
     *
     * @param buf
     * @return
     */
    public MappedByteBuffer allocalteBuffer(int buf) {
        try {
            this.buffer = this.fileChannel.map(FileChannel.MapMode.READ_WRITE, fileChannel.size(), buf);
            this.bufferOffset += buffer.capacity();
            return this.buffer;
        } catch (IOException ex) {
            Logger.getLogger(FileHandler.class.getName()).log(Level.SEVERE, "Allocalte buffer error <" + fname + ">!", ex);
        }
        return null;
    }

    /**
     * Get the offset of the buffer regarding to the mapped file
     *
     * @return
     */
    public long getBufferOffset() {
        return bufferOffset;
    }

    /**
     * Return the remaining space that be used for storing data
     *
     * @return
     */
    public long capacity() {
        return this.fsize - this.position();
    }

    /**
     * Return the position where can write data
     *
     * @return
     */
    public long position() {
        return this.bufferOffset + this.buffer.position();
    }

    /**
     * Close channel/connection to the file, clear buffer.
     */
    public void close() {
        try {
            buffer.force();
            buffer.clear();
            fileChannel.close();
            randomAccessFile.close();
            this.bufferOffset = BEGIN;
        } catch (IOException ex) {
            Logger.getLogger(FileHandler.class.getName()).log(Level.SEVERE, "Close connection <" + fname + "> errors!", ex);
        }
    }

    /**
     * Delete the file from disk manually
     *
     */
    public void delete() {
        this.close();
        file.delete();
    }
}
