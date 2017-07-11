/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.corese.rdftograph.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Enumeration;

/**
 *
 * @author edemairy
 */
public class CoreseSequenceInputStream extends SequenceInputStream implements CountLinesInterface {

	private int currentLineNumber;

	public CoreseSequenceInputStream(InputStream result, InputStream newStream) {
		super(result, newStream);
	}

	@Override
	public int read(byte b[], int off, int len) throws IOException {
		int result = super.read(b, off, len);
		for (int i = 0; i<result; i++) {
			char c = (char) b[i];
			System.out.print(c);
			if (c == '\n') {
				currentLineNumber++;
			} // else the character is ignored
		}
		return result;
	}

	@Override
	public int getLineNumber() {
		return currentLineNumber;
	}
}
