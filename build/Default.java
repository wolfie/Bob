import com.github.wolfie.bob.annotation.Target;

public class Default {
  @Target(defaultTarget = true)
  public void build() {
    System.out.println(getClass().getName() + ".build()");
  }
}
