package fr.inria.corese.sparql.storage.fs;

import fr.inria.corese.sparql.storage.api.Parameters;
import fr.inria.corese.sparql.storage.api.Parameters.type;
import static fr.inria.corese.sparql.storage.fs.Constants.BEGIN;
import static fr.inria.corese.sparql.storage.fs.Constants.EXT_TXT;
import static fr.inria.corese.sparql.storage.fs.Constants.FILE_NAME;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import org.slf4j.LoggerFactory;

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
	private long fsize;

	private Parameters params;

	/**
	 * Constructor
	 *
	 * @param fid file id
	 * @param file file path
	 * @param params parameters
	 */
	public FileHandler(int fid, String file, Parameters params) {
		try {
			this.fid = fid;
			this.params = params;
			this.file = (file == null) ? File.createTempFile(FILE_NAME, EXT_TXT) : new File(file);
			this.fsize = this.params.get(type.MAX_FILE_SIZE);

			this.fname = this.file.getAbsolutePath();
			this.file.deleteOnExit();
			randomAccessFile = new RandomAccessFile(this.file, Constants.CHANNEL_MODE);
			fileChannel = randomAccessFile.getChannel();
			buffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, BEGIN, this.params.get(type.BUF_SIZE));
		} catch (IOException ex) {
			LoggerFactory.getLogger(FileHandler.class.getName()).error("id:" + fid + ", path:" + file, ex);
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

	/**
	 * Allocate a new buffer using the default buffer_size
	 *
	 */
	public void allocalteBuffer() {
		this.allocalteBuffer(this.params.get(type.BUF_SIZE));
	}

	/**
	 * Allocate a new buffer using given buffer_size
	 *
	 * @param buf
	 */
	public void allocalteBuffer(int buf) {
		try {
			this.buffer = this.fileChannel.map(FileChannel.MapMode.READ_WRITE, fileChannel.size(), buf);
			this.bufferOffset += buffer.capacity();
		} catch (IOException ex) {
			LoggerFactory.getLogger(FileHandler.class.getName()).error("Allocalte buffer error <" + fname + ">!", ex);
		}
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
			file.delete();
		} catch (IOException ex) {
			LoggerFactory.getLogger(FileHandler.class.getName()).error("Close connection <" + fname + "> errors!", ex);
		}
	}

//    /**
//     * Delete the file from disk manually
//     *
//     */
//    public void delete() {
//        this.close();
//        file.delete();
//    }
}
