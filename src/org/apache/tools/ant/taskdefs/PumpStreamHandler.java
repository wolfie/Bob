/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.tools.ant.taskdefs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Copies standard output and error of subprocesses to standard output and error
 * of the parent process.
 * 
 * @since Ant 1.2
 */
public class PumpStreamHandler {
  
  private Thread outputThread;
  private Thread errorThread;
  private StreamPumper inputPump;
  
  private final OutputStream out;
  private final OutputStream err;
  private final InputStream input;
  
  /**
   * Construct a new <code>PumpStreamHandler</code>.
   * 
   * @param out
   *          the output <code>OutputStream</code>.
   * @param err
   *          the error <code>OutputStream</code>.
   * @param input
   *          the input <code>InputStream</code>.
   */
  public PumpStreamHandler(final OutputStream out, final OutputStream err,
                             final InputStream input) {
    this.out = out;
    this.err = err;
    this.input = input;
  }
  
  /**
   * Construct a new <code>PumpStreamHandler</code>.
   * 
   * @param out
   *          the output <code>OutputStream</code>.
   * @param err
   *          the error <code>OutputStream</code>.
   */
  public PumpStreamHandler(final OutputStream out, final OutputStream err) {
    this(out, err, null);
  }
  
  /**
   * Construct a new <code>PumpStreamHandler</code>.
   * 
   * @param outAndErr
   *          the output/error <code>OutputStream</code>.
   */
  public PumpStreamHandler(final OutputStream outAndErr) {
    this(outAndErr, outAndErr);
  }
  
  /**
   * Construct a new <code>PumpStreamHandler</code>.
   */
  public PumpStreamHandler() {
    this(System.out, System.err);
  }
  
  /**
   * Set the <code>InputStream</code> from which to read the standard output of
   * the process.
   * 
   * @param is
   *          the <code>InputStream</code>.
   */
  public void setProcessOutputStream(final InputStream is) {
    createProcessOutputPump(is, out);
  }
  
  /**
   * Set the <code>InputStream</code> from which to read the standard error of
   * the process.
   * 
   * @param is
   *          the <code>InputStream</code>.
   */
  public void setProcessErrorStream(final InputStream is) {
    if (err != null) {
      createProcessErrorPump(is, err);
    }
  }
  
  /**
   * Set the <code>OutputStream</code> by means of which input can be sent to
   * the process.
   * 
   * @param os
   *          the <code>OutputStream</code>.
   */
  public void setProcessInputStream(final OutputStream os) {
    if (input != null) {
      inputPump = createInputPump(input, os, true);
    } else {
      try {
        os.close();
      } catch (final IOException e) {
        // ignore
      }
    }
  }
  
  /**
   * Start the <code>Thread</code>s.
   */
  public void start() {
    outputThread.start();
    errorThread.start();
    if (inputPump != null) {
      final Thread inputThread = new Thread(inputPump);
      inputThread.setDaemon(true);
      inputThread.start();
    }
  }
  
  /**
   * Stop pumping the streams.
   */
  public void stop() {
    
    if (inputPump != null) {
      inputPump.stop();
    }
    
    try {
      err.flush();
    } catch (final IOException e) {
      // ignore
    }
    try {
      out.flush();
    } catch (final IOException e) {
      // ignore
    }
    finish(outputThread);
    finish(errorThread);
  }
  
  private static final long JOIN_TIMEOUT = 200;
  
  /**
   * Waits for a thread to finish while trying to make it finish quicker by
   * stopping the pumper (if the thread is a {@link ThreadWithPumper
   * ThreadWithPumper} instance) or interrupting the thread.
   * 
   * @since Ant 1.8.0
   */
  protected final void finish(final Thread t) {
    try {
      StreamPumper s = null;
      if (t instanceof ThreadWithPumper) {
        s = ((ThreadWithPumper) t).getPumper();
      }
      if (s != null && s.isFinished()) {
        return;
      }
      if (!t.isAlive()) {
        return;
      }
      
      t.join(JOIN_TIMEOUT);
      if (s != null && !s.isFinished()) {
        s.stop();
      }
      while ((s == null || !s.isFinished()) && t.isAlive()) {
        t.interrupt();
        t.join(JOIN_TIMEOUT);
      }
    } catch (final InterruptedException e) {
      // ignore
    }
  }
  
  /**
   * Get the error stream.
   * 
   * @return <code>OutputStream</code>.
   */
  protected OutputStream getErr() {
    return err;
  }
  
  /**
   * Get the output stream.
   * 
   * @return <code>OutputStream</code>.
   */
  protected OutputStream getOut() {
    return out;
  }
  
  /**
   * Create the pump to handle process output.
   * 
   * @param is
   *          the <code>InputStream</code>.
   * @param os
   *          the <code>OutputStream</code>.
   */
  protected void createProcessOutputPump(final InputStream is,
      final OutputStream os) {
    outputThread = createPump(is, os);
  }
  
  /**
   * Create the pump to handle error output.
   * 
   * @param is
   *          the input stream to copy from.
   * @param os
   *          the output stream to copy to.
   */
  protected void createProcessErrorPump(final InputStream is,
      final OutputStream os) {
    errorThread = createPump(is, os);
  }
  
  /**
   * Creates a stream pumper to copy the given input stream to the given output
   * stream.
   * 
   * @param is
   *          the input stream to copy from.
   * @param os
   *          the output stream to copy to.
   * @return a thread object that does the pumping.
   */
  protected Thread createPump(final InputStream is, final OutputStream os) {
    return createPump(is, os, false);
  }
  
  /**
   * Creates a stream pumper to copy the given input stream to the given output
   * stream.
   * 
   * @param is
   *          the input stream to copy from.
   * @param os
   *          the output stream to copy to.
   * @param closeWhenExhausted
   *          if true close the inputstream.
   * @return a thread object that does the pumping, subclasses should return an
   *         instance of {@link ThreadWithPumper ThreadWithPumper}.
   */
  protected Thread createPump(final InputStream is, final OutputStream os,
                                final boolean closeWhenExhausted) {
    final Thread result = new ThreadWithPumper(new StreamPumper(is, os,
                                                    closeWhenExhausted,
                                                    true));
    result.setDaemon(true);
    return result;
  }
  
  /**
   * Creates a stream pumper to copy the given input stream to the given output
   * stream. Used for standard input.
   * 
   * @since Ant 1.6.3
   */
  /* protected */StreamPumper createInputPump(final InputStream is,
      final OutputStream os,
                                final boolean closeWhenExhausted) {
    final StreamPumper pumper = new StreamPumper(is, os, closeWhenExhausted,
                                               false);
    pumper.setAutoflush(true);
    return pumper;
  }
  
  /**
   * Specialized subclass that allows access to the running StreamPumper.
   * 
   * @since Ant 1.8.0
   */
  protected static class ThreadWithPumper extends Thread {
    private final StreamPumper pumper;
    
    public ThreadWithPumper(final StreamPumper p) {
      super(p);
      pumper = p;
    }
    
    protected StreamPumper getPumper() {
      return pumper;
    }
  }
}
