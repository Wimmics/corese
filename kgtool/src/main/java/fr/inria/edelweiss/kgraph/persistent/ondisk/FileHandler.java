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

    private int fid;
    private String fname;

    public FileHandler() throws IOException {
        this(null);
    }

    public FileHandler(String file) throws IOException {
        this.file = file == null ? new File(file) : File.createTempFile(FILE_NAME, EXT_TXT);

        this.fname = this.file.getAbsolutePath();
        this.file.deleteOnExit();
        randomAccessFile = new RandomAccessFile(this.file, Parameters.MODE);
        fileChannel = randomAccessFile.getChannel();
        buffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, BEGIN, BUF_SIZE);
    }

    public FileChannel getChannel() {
        return fileChannel;
    }

    public MappedByteBuffer getBuffer() {
        return buffer;
    }

    public MappedByteBuffer allocalteBuffer() throws IOException {
        this.buffer = this.fileChannel.map(FileChannel.MapMode.READ_WRITE, fileChannel.size(), BUF_SIZE);
        return this.buffer;
    }

    public void close() {
        try {
            buffer.clear();
            fileChannel.close();
            randomAccessFile.close();
        } catch (IOException ex) {
            Logger.getLogger(DiskFileManager.class.getName()).log(Level.SEVERE, "Close connection errors!", ex);
        }
    }
}
