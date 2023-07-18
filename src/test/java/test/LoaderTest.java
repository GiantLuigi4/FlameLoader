package test;

import tfc.flame.loader.FlameLoader;
import tfc.flame.loader.IFlameLoader;
import tfc.flame.loader.util.JDKLoader;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;

// so, if I put this in the test class path, then mixin refuses to not load
public class LoaderTest {
    public static void main(String[] args) {
        try {
            ArrayList<URL> paths = new ArrayList<>();

//            File fl = new File("out");
//            if (fl.exists()) {
//                paths.add(new File(fl + "/test/classes").toURL());
//                paths.add(new File(fl + "/test/resources").toURL());
//            } else {
//                fl = new File("build/classes");
//                if (fl.exists())
//                    for (File file : Objects.requireNonNull(fl.listFiles()))
//                        paths.add(new File(file + "/test").toURL());
//                fl = new File("build/resources/test");
//                if (fl.exists())
//                    paths.add(fl.toURL());
//            }

            paths.add(new URL(
                    "jar:file:/" + new File("mixin-0.8.5.jar").getAbsoluteFile() + "!/"
            ));

            ClassLoader loader = (ClassLoader) JDKLoader.createLoader(paths.toArray(new URL[0]), FlameLoader.class.getClassLoader(), true);
            ((IFlameLoader) loader).addOverridePath(new File("").getAbsolutePath());

            Thread.currentThread().setContextClassLoader(loader);

            {
                Class<?> clazz = loader.loadClass("mixin.MixinLoader");
                clazz.getMethod("init").invoke(null);

                clazz = loader.loadClass("mixin.test.MixinTarget");
                clazz.getMethod("main", String[].class).invoke(null, (Object) args);
            }

            {
                Class<?> clazz = loader.loadClass("TestClass");
                System.out.println(clazz.getCanonicalName());
                Method m = clazz.getMethod("main", String[].class);
                m.invoke(null, (Object) args);
            }
        } catch (Throwable err) {
            err.printStackTrace();
        }
    }
}
