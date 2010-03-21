import com.github.wolfie.bob.action.Action;
import com.github.wolfie.bob.action.Jar;
import com.github.wolfie.bob.annotation.Target;

public class Default {
  @Target(defaultTarget = true)
  public Action build() {
    return new Jar("result/bob.jar");
  }
}
