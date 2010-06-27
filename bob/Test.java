import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.wolfie.bob.ProjectDescription;

public class Test {
  
  /**
   * @param args
   * @throws FileNotFoundException
   */
  public static void main(final String[] args) throws Exception {
    final String descMethod = getDescriptionMethod(new File(
        "/Users/wolfie/Documents/workspace/Bob/bob/Default.java"));
    
    final String c = String.format("import %s; class %s { %s }",
        ProjectDescription.class.getName(), "Foo", descMethod);
    System.out.println(c);
  }
  
  private static String getDescriptionMethod(final File file)
      throws IOException {
    
    final String code = readFileAsString(file);
    final String method = getMethodCode(code, "describeProject");
    
    return method;
  }
  
  private static String getMethodCode(String code,
      final String methodName, final Class<?>... params) {
    
    code = removeComments(code);
    
    final String modifiers = "(?:(?:private|public|protected|static|final|abstract)\\s+)*";
    final String returnType = "\\p{Alnum}+\\s+";
    final String escapedMethodName = Pattern.quote(methodName);
    final String param = getParamsRegex(params);
    
    final String regex = modifiers + returnType + escapedMethodName
        + "\\(\\s*" + param + "\\)\\s*\\{";
    final Matcher matcher = Pattern.compile(regex, Pattern.MULTILINE).matcher(
        code);
    
    if (matcher.find()) {
      final String firstLine = matcher.group(0);
      final String body = getMethodBody(code, matcher.end());
      return firstLine + body;
    } else {
      return null;
    }
  }
  
  private static String removeComments(final String code) {
    char currentQuote = 0;
    final StringBuilder builder = new StringBuilder(code.length());
    
    try {
      for (int i = 0; i < code.length(); i++) {
        final char c = code.charAt(i);
        final char c2;
        
        if (i + 1 < code.length()) {
          c2 = code.charAt(i + 1);
        } else {
          c2 = 0;
        }
        
        if (c == '\'' || c == '"') {
          if (currentQuote == 0) {
            currentQuote = c;
          } else {
            currentQuote = 0;
          }
          builder.append(c);
        } else if (currentQuote == 0 && c == '/' && c2 == '/') {
          while (code.charAt(++i) != '\n') {
            // empty loop
          }
        } else if (currentQuote == 0 && c == '/' && c2 == '*') {
          while (!(code.charAt(++i) == '*' && code.charAt(i + 1) == '/')) {
            // empty loop
          }
          i++;
        } else {
          builder.append(c);
        }
      }
    } catch (final IndexOutOfBoundsException e) {
      e.printStackTrace();
    }
    
    return builder.toString();
  }
  
  private static String getMethodBody(final String code,
      final int methodBodyStart) {
    
    final StringBuilder builder = new StringBuilder();
    
    char currentQuote = 0;
    int braceStackDepth = 0;
    for (int i = methodBodyStart; i < code.length() && braceStackDepth >= 0; i++) {
      final char c = code.charAt(i);
      builder.append(c);
      
      if (currentQuote == 0) {
        if (c == '{') {
          braceStackDepth++;
        } else if (c == '}') {
          braceStackDepth--;
        } else if (c == '\'' || c == '"') {
          currentQuote = c;
        }
      } else {
        if (c == currentQuote) {
          currentQuote = 0;
        }
      }
    }
    
    return builder.toString();
  }
  
  private static String getParamsRegex(final Class<?>[] params) {
    if (params == null || params.length == 0) {
      return "";
    }
    
    final StringBuilder builder = new StringBuilder();
    for (final Class<?> clazz : params) {
      final String fullName = Pattern.quote(clazz.getName());
      final String simpleName = Pattern.quote(clazz.getSimpleName());
      final String regex = String.format("(?:(?:%s|%s)\\s+\\p{Alnum}+\\s+",
          fullName, simpleName);
      builder.append(regex);
    }
    
    return builder.toString();
  }
  
  private static String readFileAsString(final File file)
      throws IOException {
    final StringBuilder fileData = new StringBuilder(1000);
    final BufferedReader reader = new BufferedReader(
              new FileReader(file));
    final char[] buf = new char[1024];
    int numRead = 0;
    while ((numRead = reader.read(buf)) != -1) {
      fileData.append(buf, 0, numRead);
    }
    reader.close();
    return fileData.toString();
  }
}
