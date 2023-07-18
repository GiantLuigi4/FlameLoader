package tfc.flame.loader;

import tfc.flame.loader.asm.ClassTransformer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

public interface IFlameLoader {
	/**
	 * gets a resource as an input stream
	 * see {@link IFlameLoader#findResource(String)}
	 *
	 * @param name the name of the resource
	 * @return the stream corresponding to said resource
	 */
	InputStream getResourceAsStream(String name);
	
	/**
	 * adds a package to force load on the flame loader instead of the parent loader
	 *
	 * @param name the name of the package
	 */
	void addPackageOverride(String name);
	
	/**
	 * finds the top most resource with the given name
	 *
	 * @param name the name of the resource to find
	 * @return the top most instance of that resource
	 */
	URL findResource(String name);
	
	/**
	 * finds an enumeration of resources with the provided name, acrossed all jars
	 *
	 * @param name the name of the resource
	 * @return the enumeration
	 * @throws IOException unsure when this'd happen
	 */
	Enumeration<URL> findResources(String name) throws IOException;
	
	/**
	 * looks for an already loaded class, and if not found, loads it or gets it from the parent
	 *
	 * @param name    the name of the class to load
	 * @param resolve idk
	 * @return the class either found, gotten, or loaded
	 * @throws ClassNotFoundException if the class cannot be found, should only happen if {@link IFlameLoader#getBytecode(String, boolean, ClassTransformer)} throws a class not found and the class isn't present in the parent loader
	 */
	Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException;
	
	/**
	 * gets a class from the class loader, but will not load said class if it is not found
	 *
	 * @param name the name of the class
	 * @return the class
	 */
	Class<?> getClassIfLoaded(String name);
	
	/**
	 * gets a copy of the list of urls that are valid for the class loader to load classes from
	 *
	 * @return the list of urls
	 */
	URL[] getClassPath();
	
	/**
	 * gets the bytecode of a class file from the loader
	 *
	 * @param name               the name of the class to get the bytecode of; this should be in dotted class notation
	 * @param runTransformers    whether or not to run class transformers, or get the raw bytecode
	 * @param stopTransformingOn the instance of the transformer at which you would like to stop transforming the class
	 * @return the bytecode of the class
	 * @throws ClassNotFoundException if the class is either not valid to load on the flame loader, or is not found on the flame loader
	 */
	byte[] getBytecode(String name, boolean runTransformers, ClassTransformer stopTransformingOn) throws ClassNotFoundException;
}
