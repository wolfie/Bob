package com.github.wolfie.bob.internals.action;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.github.wolfie.bob.Defaults;
import com.github.wolfie.bob.Tuple;
import com.github.wolfie.bob.exception.InternalConsistencyException;
import com.github.wolfie.bob.exception.ProcessingError;
import com.github.wolfie.bob.internals._Action;
import com.github.wolfie.bob.internals._ArtifactProducer._FileProducer;
import com.github.wolfie.bob.util.Log;
import com.github.wolfie.bob.util.Log.LogLevel;
import com.github.wolfie.bob.util.Log.MultiLog;
import com.github.wolfie.bob.util.Util;

public class _Zip implements _Action, _FileProducer {
  
  private final Set<Tuple<String, String>> pathsToAdd = new HashSet<Tuple<String, String>>();
  private final Set<Tuple<String, _FileProducer>> artifactsSingleToAdd = new HashSet<Tuple<String, _FileProducer>>();
  private final Set<Tuple<String, _PathProducer>> artifactsPathToAdd = new HashSet<Tuple<String, _PathProducer>>();
  
  private String toPath = null;
  private boolean isProcessed = false;
  private File artifactFile;
  
  public _Zip add(final String path, final String entryName) {
    pathsToAdd.add(Tuple.of(entryName, path));
    return this;
  }
  
  public _Zip add(final _FileProducer action,
      final String entryName) {
    artifactsSingleToAdd.add(Tuple.of(entryName, action));
    return this;
  }
  
  public _Zip add(final _PathProducer action, final String entryPath) {
    artifactsPathToAdd.add(Tuple.of(entryPath, action));
    return this;
  }
  
  @Override
  public void process() {
    if (artifactsPathToAdd.isEmpty() && artifactsSingleToAdd.isEmpty()
        && pathsToAdd.isEmpty()) {
      Log.get().log("The Zip action had nothing to do.", LogLevel.INFO);
      return;
    }
    
    setDefaults();
    
    try {
      artifactFile = getDestination();
      final FileOutputStream out = new FileOutputStream(artifactFile);
      final ZipOutputStream zipStream = new ZipOutputStream(out);
      
      try {
        processPaths(zipStream);
        processSingleCompilations(zipStream);
        processPathCompilations(zipStream);
        isProcessed = true;
      }

      catch (final IOException e) {
        throw new ProcessingError(e);
      }

      finally {
        try {
          
          zipStream.close();
          
        } catch (final IOException e) {
          throw new ProcessingError(e);
        }
      }
    } catch (final FileNotFoundException e) {
      throw new ProcessingError(e);
    }
    
    final String shortLog = "Wrote " + artifactFile.getPath();
    final String longLog = "Wrote " + artifactFile.getAbsolutePath();
    Log.get().log(
        new MultiLog(shortLog, LogLevel.INFO).or(longLog,
            LogLevel.VERBOSE));
  }
  
  private void processPaths(final ZipOutputStream zipStream) throws IOException {
    for (final Tuple<String, String> entry : pathsToAdd) {
      final String entryName = entry.getFirst();
      final String path = entry.getSecond();
      final File file = new File(path);
      
      if (!file.canRead()) {
        Log.get().log(
            "Could not zip " + path
                + ", it doesn't exist or it's not readable.", LogLevel.WARNING);
      } else if (file.isFile()) {
        final String adjustedEntryName = getAdjustedSingleEntryName(entryName,
            file);
        addZipEntry(adjustedEntryName, file, zipStream);
      } else if (file.isDirectory()) {
        processDirPath(entryName, file, zipStream);
      } else {
        Log.get().log(
            "Could not zip " + path
                + ", it was neither a regular file nor a directory",
            LogLevel.WARNING);
      }
    }
  }
  
  /**
   * If the Zip entry ends with "/" or
   * "\", append the original file's name. If the Zip entry is ".", get the
   * file's original name. Otherwise leave it as-is.
   */
  private String getAdjustedSingleEntryName(final String entryName,
      final File originalFile) {
    if (entryName.endsWith("/") || entryName.endsWith("\\")) {
      return entryName + originalFile.getName();
    } else if (entryName.equals(".")) {
      return originalFile.getName();
    } else {
      return entryName;
    }
  }
  
  private static void processDirPath(final String entryName,
      final File directory, final ZipOutputStream zipStream) throws IOException {
    for (final File file : directory.listFiles()) {
      if (file.isDirectory()) {
        processDirPath(entryName + "/" + file.getName(), file, zipStream);
      } else {
        addZipEntry(entryName + "/" + file.getName(), file, zipStream);
      }
    }
  }
  
  private void processSingleCompilations(final ZipOutputStream zipStream)
      throws IOException {
    for (final Tuple<String, _FileProducer> entry : artifactsSingleToAdd) {
      final String entryName = entry.getFirst();
      final _FileProducer producer = entry.getSecond();
      
      final File file;
      try {
        Util.enterLog(producer);
        file = producer.get();
      } finally {
        Log.get().exit();
      }
      
      final String adjustedEntryName = getAdjustedSingleEntryName(entryName,
          file);
      addZipEntry(adjustedEntryName, file, zipStream);
    }
  }
  
  private void processPathCompilations(final ZipOutputStream zipStream)
      throws IOException {
    for (final Tuple<String, _PathProducer> entry : artifactsPathToAdd) {
      final String entryPath = entry.getFirst();
      final _PathProducer producer = entry.getSecond();
      
      final File directory;
      try {
        Util.enterLog(producer);
        directory = producer.get();
      } finally {
        Log.get().exit();
      }
      
      processDirPath(entryPath, directory, zipStream);
    }
  }
  
  @Override
  public _Zip to(final String path) {
    toPath = path;
    return this;
  }
  
  private void setDefaults() {
    if (toPath == null) {
      toPath = Defaults.ZIP_PATH;
    }
  }
  
  private File getDestination() {
    if (toPath != null) {
      final File destination = new File(toPath);
      
      if (destination.exists()) {
        Util.delete(destination);
      }
      
      final File parentFile = destination.getParentFile();
      if (!parentFile.exists()) {
        Util.createDir(parentFile);
      }
      
      return destination;
      
    } else {
      throw new InternalConsistencyException("No destination path defined");
    }
  }
  
  /**
   * See <a href=
   * "http://stackoverflow.com/questions/1281229/how-to-use-jaroutputstream-to-create-a-jar-file"
   * >Stack Overflow</a> for explanation on method.
   * 
   * @param entryName
   * @param source
   *          The {@link File} to add to the Jar.
   * @param archiveDestinationPrefix
   * @param target
   *          The Jar's {@link JarOutputStream}.
   * @throws IOException
   */
  protected static void addZipEntry(final String entryName, final File source,
      final ZipOutputStream target) throws IOException {
    
    /*
     * TODO: both _Jar and _Zip use effectively the same method... should be
     * refactored, probably
     */

    // Adjusted Zip entry name (for Windows-style paths)
    final String adjustedEntryName = entryName.replace('\\', '/');
    
    Log.get().log("Compressing " + adjustedEntryName, LogLevel.DEBUG);
    
    final ZipEntry entry = new ZipEntry(adjustedEntryName);
    entry.setTime(source.lastModified());
    target.putNextEntry(entry);
    
    final BufferedInputStream in = new BufferedInputStream(new FileInputStream(
        source));
    
    try {
      final byte[] buffer = new byte[1024];
      while (true) {
        final int count = in.read(buffer);
        if (count == -1) {
          break;
        }
        target.write(buffer, 0, count);
      }
      target.closeEntry();
    } finally {
      in.close();
    }
  }
  
  @Override
  public File get() {
    if (!isProcessed) {
      process();
    }
    
    return artifactFile;
  }
  
  @Override
  public boolean isProcessed() {
    return isProcessed;
  }
}
