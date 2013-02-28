package org.nuxeo.common.logging;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class StreamLogger extends OutputStream {

    protected static  class OutputStreamLogger extends StreamLogger {
        @Override
        protected void logMessage(Log log, String message) {
            log.info(message);
        }
    }

    protected static class ErrorStreamLogger extends StreamLogger {
        @Override
        protected void logMessage(Log log, String message) {
            log.error(message);
        }
    }

    public static void redirectOutput() {
        System.setOut(new PrintStream(new OutputStreamLogger()));
        System.setErr(new PrintStream(new ErrorStreamLogger()));
    }

    protected boolean closed = false;

    protected final StringBuffer buffer;

    protected final Log log = LogFactory.getLog(StreamLogger.class);

    public StreamLogger() {
        buffer = new StringBuffer();
    }

    @Override
    public void close() throws IOException {
        closed = true;
        super.close();
    }

    protected void checkClosed() throws EOFException {
        if (closed) {
            throw new EOFException("logger was closed");
        }
    }

    @Override
    public void write(int b) throws IOException {
        checkClosed();
        synchronized(this) {
            if ('\n' == b) {
                flush();
            } else {
                buffer.append((char)b);
            }
        }
    }

    @Override
    public void flush() {
        String message;
        synchronized(this) {
            message = buffer.toString();
            buffer.setLength(0);
        }
        logMessage(log, message);
    }

    protected abstract void logMessage(Log log, String message);
}
