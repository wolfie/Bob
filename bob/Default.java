import com.github.wolfie.bob.UtilTest;
import com.github.wolfie.bob.action.Action;
import com.github.wolfie.bob.action.Compilation;
import com.github.wolfie.bob.action.Jar;
import com.github.wolfie.bob.action.optional.JUnitTestRun;
import com.github.wolfie.bob.annotation.Target;

public class Default {
  
  private static Compilation getBobCompilation() {
    return new Compilation()
        .useJarsAt("dist/lib");
  }
  
  public static Compilation compileAll() {
    return new Compilation()
        .use(compileProject())
        .use(compileTests());
  }
  
  public static Compilation compileProject() {
    return new Compilation().from("src");
  }
  
  public static Compilation compileTests() {
    return new Compilation().from("test");
  }
  
  /**
   * A build target required to Bob into its executable jar.
   */
  @Target(defaultTarget = true)
  public Action bobJar() {
    return new Jar()
        .from(getBobCompilation())
        .withSources()
        .to("artifacts/bob.jar");
  }
  
  @Target
  public Action test() {
    return new JUnitTestRun()
        .run(UtilTest.class)
        .targetsFrom(getBobCompilation());
  }
}
