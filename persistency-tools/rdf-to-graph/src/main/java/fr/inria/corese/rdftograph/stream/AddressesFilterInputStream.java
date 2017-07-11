package fr.inria.corese.rdftograph.stream;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class AddressesFilterInputStream extends FilterInputStream implements CountLinesInterface {

	private int start, end;
	private int currentLineNumber;
	public static int INFINITE = -1;

	public AddressesFilterInputStream(InputStream input, int start, int end) {
		super(input);
		this.start = start;
		this.end = end;
		this.in = input;
	}

	/**
	 * Read one byte *
	 */
	@Override
	public int read() throws IOException {
		if (currentLineNumber < start) {
			int c;
			while ((currentLineNumber < start) && ((c = in.read()) != -1)) {
				if ((char) c == '\n') {
					currentLineNumber++;
				}
			}
		}
		if (currentLineNumber >= start && (currentLineNumber < end || end == INFINITE)) {
			int c = in.read();
			if (c == '\n') {
				currentLineNumber++;
			}
			return c;
		}
		return -1;
	}

	@Override
	public int read(byte[] b) throws IOException {
		int cpt = 0;
		int curChar;
		while (cpt < b.length && ((curChar = read()) != -1)) {
			b[cpt++] = (byte) curChar;
		}
		if (cpt == 0) {
			return -1;
		} else {
			return cpt;
		}
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		byte[] temp = new byte[len];
		int res = read(temp);
		if (res == -1) {
			return res;
		} else {
			for (int i = 0; i < len; i++) {
				b[off + i] = temp[i];
			}
			return res;
		}
	}

	@Override
	public int getLineNumber() {
		return currentLineNumber;
	}
};
