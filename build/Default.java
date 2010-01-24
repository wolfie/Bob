import com.github.wolfie.bob.Build;
import com.github.wolfie.bob.annotation.Target;
import com.github.wolfie.bob.result.Action;
import com.github.wolfie.bob.result.Actions;
import com.github.wolfie.bob.result.Clear;
import com.github.wolfie.bob.result.Jar;

public class Default extends Build {
  @Override
  @Target
  public Action build() {
    return Actions.from(new Clear(getDefaultBuildDirectory()), new Jar());
  }
}
