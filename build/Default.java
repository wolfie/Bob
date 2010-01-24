import com.github.wolfie.bob.Build;
import com.github.wolfie.bob.result.Action;
import com.github.wolfie.bob.result.Actions;
import com.github.wolfie.bob.result.Clear;
import com.github.wolfie.bob.result.Jar;

public class Default extends Build {
  @Override
  public Action build() {
    return Actions.from(new Clear(Build.getDefaultBuildDirectory()), new Jar());
  }
}
