import com.github.wolfie.bob.BobBuild;
import com.github.wolfie.bob.ProjectDescription;
import com.github.wolfie.bob.UtilTest;
import com.github.wolfie.bob.action.Jar;
import com.github.wolfie.bob.action.Zip;
import com.github.wolfie.bob.action.optional.JUnitTestRun;
import com.github.wolfie.bob.annotation.Target;

public class Default extends BobBuild {
  
  @Override
  protected ProjectDescription describeProject() {
    return ProjectDescription.getDefault()
        .jarFile("dist/lib/junit.jar");
  }
  
  @Target
  public JUnitTestRun test() {
    return new JUnitTestRun().run(UtilTest.class);
  }
  
  @Target(defaultTarget = true)
  public Jar build() {
    return new Jar()
        .withSources()
        .to("artifacts/bob.jar");
  }
  
  @Target
  public Zip pack() {
    return new Zip()
        .add("dist/bob", ".")
        .add("dist/bob.bat", ".")
        .add("LICENSE", ".")
        .add("README.mkdn", "README")
        .add(build(), "lib/bob.jar")
        .to("artifacts/bob.zip");
  }
}
