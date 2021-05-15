package tfc.flame;

/**
 * this is to force all class loaders loaded by the flame class loader to use FlameASM.
 * this will be implemented *eventually*
 */
public class FlameClassLoader extends ClassLoader {
	protected Class<?> defineClass0(String name, byte[] b, int off, int len)
			throws ClassFormatError
	{
		return defineClass(name, b, off, len, null);
	}
	
	public FlameClassLoader(ClassLoader parent) {
		super(parent);
	}
	
	public FlameClassLoader() {
		super(FlameClassLoader.class.getClassLoader());
	}
}
