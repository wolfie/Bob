package com.github.wolfie.bob;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;

public class RedirectingJavaFileObject implements JavaFileObject {
  
  public RedirectingJavaFileObject(final JavaFileObject javaFileObject,
      final File destinationRoot) {
    if (!destinationRoot.isDirectory()) {
      throw new UnsupportedOperationException(
          "destinationRoot must be a directory");
    }
    
    final String name = javaFileObject.getName();
    final String path = destinationRoot.getPath();
    
  }
  
  @Override
  public Kind getKind() {
    return null;
  }
  
  @Override
  public boolean isNameCompatible(final String simpleName, final Kind kind) {
    // TODO Auto-generated method stub
    return false;
  }
  
  @Override
  public NestingKind getNestingKind() {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  public Modifier getAccessLevel() {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  public URI toUri() {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  public String getName() {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  public InputStream openInputStream() throws IOException {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  public OutputStream openOutputStream() throws IOException {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  public Reader openReader(final boolean ignoreEncodingErrors)
      throws IOException {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  public CharSequence getCharContent(final boolean ignoreEncodingErrors)
      throws IOException {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  public Writer openWriter() throws IOException {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  public long getLastModified() {
    // TODO Auto-generated method stub
    return 0;
  }
  
  @Override
  public boolean delete() {
    // TODO Auto-generated method stub
    return false;
  }
  
}
