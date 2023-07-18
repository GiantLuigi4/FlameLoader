import org.objectweb.asm.tree.ClassNode;
import tfc.flame.loader.asm.ClassTransformer;
import tfc.flame.loader.asm.Phase;
import tfc.flame.loader.FlameLoader;

public class TestClass {
    static {
        ClassLoader theLoader = TestClass.class.getClassLoader();
        if (theLoader instanceof FlameLoader) {
            ((FlameLoader) theLoader).transformers.add(Phase.FIRST, new ClassTransformer() {
                @Override
                public ClassNode accept(ClassNode node) {
                    return null;
                }
            });
        }
    }
    
    public static void main(String[] args) {
        System.out.println("Test called!");
        System.out.println("I was loaded on " + TestClass.class.getClassLoader());
        
        Test2 test = new Test2();
        System.out.println(test);
    }
}
