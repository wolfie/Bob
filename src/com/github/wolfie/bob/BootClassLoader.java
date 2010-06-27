package com.github.wolfie.bob;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class BootClassLoader extends ClassLoader {
  
  private final File methodClassFile;
  private byte[] bytes = null;
  
  private BootClassLoader(final File methodClassFile) {
    this.methodClassFile = methodClassFile;
  }
  
  @Override
  protected Class<?> findClass(final String name) throws ClassNotFoundException {
    if (bytes == null) {
      try {
        bytes = getBytesFromFile(methodClassFile);
      } catch (final IOException e) {
        throw new ClassNotFoundException(name, e);
      }
    }
    return defineClass(name, bytes, 0, bytes.length);
  }
  
  public static byte[] getBytesFromFile(final File file) throws IOException {
    final InputStream is = new FileInputStream(file);
    try {
      
      // Get the size of the file
      final long length = file.length();
      
      // You cannot create an array using a long type.
      // It needs to be an int type.
      // Before converting to an int type, check
      // to ensure that file is not larger than Integer.MAX_VALUE.
      if (length > Integer.MAX_VALUE) {
        System.err.println("file is too long");
        return null;
      }
      
      // Create the byte array to hold the data
      final byte[] bytes = new byte[(int) length];
      
      // Read in the bytes
      int offset = 0;
      int numRead = 0;
      while (offset < bytes.length
           && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
        offset += numRead;
      }
      
      // Ensure all the bytes have been read in
      if (offset < bytes.length) {
        throw new IOException("Could not completely read file "
            + file.getName());
      }
      
      return bytes;
    } finally {
      // Close the input stream and return bytes
      is.close();
    }
  }
  
  public static BootClassLoader get(final File classFile) {
    return AccessController
        .doPrivileged(new PrivilegedAction<BootClassLoader>() {
          @Override
          public BootClassLoader run() {
            return new BootClassLoader(classFile);
          }
        });
  }
}
