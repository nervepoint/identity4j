package com.identity4j.util.http;

import java.io.IOException;
import java.io.OutputStream;

public interface HttpData {
	void writeData(OutputStream out) throws IOException;
}
