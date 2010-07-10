import com.github.wolfie.bob.BobBuild;
import com.github.wolfie.bob.ProjectDescription;
import com.github.wolfie.bob.UtilTest;
import com.github.wolfie.bob.action.Action;
import com.github.wolfie.bob.action.Jar;
import com.github.wolfie.bob.action.optional.JUnitTestRun;
import com.github.wolfie.bob.annotation.Target;

public class Default extends BobBuild {
  
  @Override
  protected ProjectDescription describeProject() {
    return new ProjectDescription()
        .jarPath("lib")
        .jarFile("dist/lib/junit.jar")
        .sourcePath("src")
        .sourcePath("test");
  }
  
  @Target
  public Action test() {
    return new JUnitTestRun().run(UtilTest.class);
  }
  
  @Target(defaultTarget = true)
  public Action build() {
    return new Jar()
        .withSources()
        .to("artifacts/bob.jar");
  }
}
