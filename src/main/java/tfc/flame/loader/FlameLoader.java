package tfc.flame.loader;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import tfc.flame.loader.asm.ClassTransformer;
import tfc.flame.loader.asm.PriorityPhaseList;
import tfc.flame.loader.util.FlameResource;
import tfc.flame.loader.util.JDKLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.jar.Manifest;

/**
 * Avoid referring to this directly
 * <p>
 * Refer to {@link JDKLoader} instead for getting an instance of a FlameLoader
 * And you can cast to {@link IFlameLoader} for methods specific to the Flame loader, or {@link ClassLoader} for regular class loader methods
 */
@Deprecated
public class FlameLoader extends URLClassLoader implements IFlameLoader {
	// TODO: better data struct?
	public PriorityPhaseList<ClassTransformer> transformers = new PriorityPhaseList<>();
	URL[] urls;
	boolean aggressive = false;
	ArrayList<String> packagesToAccept = new ArrayList<>();
	ArrayList<String> urlRoots = new ArrayList<>();

	/**
	 * @param urls       the list of urls to load classes from
	 * @param parent     the parent class loader
	 * @param aggressive only to be used in dev env; allows tfc.flame.loading classes from the FlameLoader before the parent loader
	 */
	public FlameLoader(URL[] urls, ClassLoader parent, boolean aggressive) {
		super(new URL[0], parent);
		this.urls = urls;
		this.aggressive = aggressive;
	}

	/**
	 * @param urls   the list of urls to load classes from
	 * @param parent the parent class loader
	 */
	public FlameLoader(URL[] urls, ClassLoader parent) {
		super(new URL[0], parent);
		this.urls = urls;
	}

	/**
	 * @param urls the list of urls to load classes from
	 */
	public FlameLoader(URL[] urls) {
		super(new URL[0]);
		this.urls = urls;
	}

	public void addPackageOverride(String name) {
		packagesToAccept.add(name);
	}

	public void addOverridePath(String name) {
		if (!name.startsWith("/")) name = "/" + name;
		urlRoots.add(name.replace(File.separatorChar, '/'));
	}

	@Override
	public URL findResource(String name) {
		URL[] urls = findResourceAndPath(name);

		if (urls == null) {
			if (getParent() != null) return getParent().getResource(name);
			return null;
		}

		return urls[0];
	}

	URL[] findResourceAndPath(String name) {
		for (URL path : urls) {
			String pt = path.toString();
			if (!pt.endsWith("/")) pt += "/";
			String pth = pt + name;

			try {
				URL url = new URL(pth);
				if (url.getProtocol().equals("file")) {
					if (new File(url.toString().substring("file:".length())).exists()) {
						return new URL[]{url, path};
					} else {
						continue;
					}
				}

				URLConnection connection = url.openConnection();

				if (connection instanceof HttpURLConnection) {
					HttpURLConnection huc = (HttpURLConnection) connection;

					if (huc.getResponseCode() != HttpURLConnection.HTTP_NOT_FOUND)
						return new URL[]{url, path};
				} else if (connection instanceof JarURLConnection) {
					JarURLConnection juc = (JarURLConnection) connection;

					if (juc.getJarFile() != null)
						return new URL[]{url, path};
				}
				// TODO: support others?
			} catch (Throwable ignored) {
			}
		}

		if (getParent() != null) {
			URL pUrl = getParent().getResource(name);
			if (pUrl != null)
				return new URL[]{pUrl, null};
		}

		return null;
	}

	@Override
	public Enumeration<URL> findResources(String name) throws IOException {
		// TODO: optimize

		ArrayList<URL> theUrls = new ArrayList<>();

		for (URL path : urls) {
			String pth = path.toString() + name;

			try {
				URL url = new URL(pth);
				if (url.getProtocol().equals("file")) {
					if (new File(url.toString().substring("file:/".length())).exists())
						theUrls.add(url);

					continue;
				}

				URLConnection connection = url.openConnection();

				if (connection instanceof HttpURLConnection) {
					HttpURLConnection huc = (HttpURLConnection) connection;

					if (huc.getResponseCode() != HttpURLConnection.HTTP_NOT_FOUND)
						theUrls.add(url);
				} else if (connection instanceof JarURLConnection) {
					JarURLConnection juc = (JarURLConnection) connection;

					if (juc.getJarFile() != null)
						theUrls.add(url);
				}
				// TODO: support others?
			} catch (Throwable ignored) {
			}
		}

		if (getParent() != null) {
			Enumeration<URL> urls = getParent().getResources(name);

			while (urls.hasMoreElements())
				theUrls.add(urls.nextElement());
		}

		Iterator<URL> itr = theUrls.iterator();
		return new Enumeration<URL>() {
			@Override
			public boolean hasMoreElements() {
				return itr.hasNext();
			}

			@Override
			public URL nextElement() {
				return itr.next();
			}
		};
	}

	protected Package definePackage(FlameResource resource, String name, Manifest manifest, URL base) {
		Package pkg = getPackage(name);
		// check if the package exists
		if (pkg != null) {
			// check that it's not tfc.flame.loading a class into a sealed package
			if (pkg.isSealed() && base != null && !pkg.isSealed(base)) {
				throw new RuntimeException("Cannot load class in a sealed package");
			} else {
				// TODO: ?????? java please
//				// check that it's not sealing an existing package
//				if (resource.mf != null) {
//					Attributes attr = SECRETS.getAttributes(resource.mf, name.replace('.', '/').concat("/"));
//
//					String sealed = null;
//
//					if (attr != null) sealed = attr.getValue(Attributes.Name.SEALED);
//					if (sealed == null && (attr = manifest.getMainAttributes()) != null)
//						sealed = attr.getValue(Attributes.Name.SEALED);
//
//					if ("true".equalsIgnoreCase(sealed)) {
//						throw new RuntimeException("Cannot seal an already existing package");
//					}
//				}
			}
		} else {
			if (resource.mf == null) {
				return definePackage(
						name, null, null, null,
						null, null, null, null
				);
			} else {
				// define the package if it does not exist
				pkg = definePackage(name, resource.mf, base);
			}
		}
		return pkg;
	}

	boolean checkPackage(String name, URL[] path) {
		boolean acceptedURL = false;

		int i = name.lastIndexOf('.');
		if (i != -1) {
			String pkgname = name.substring(0, i);
			acceptedURL = packagesToAccept.contains(pkgname);

			if (!acceptedURL) {
				for (String s : packagesToAccept) {
					if (pkgname.startsWith(s)) {
						acceptedURL = true;
						break;
					}
				}
			}
		}

		if (!urlRoots.isEmpty()) {
			if (path[1] == null) {
				URL url = getParent().getResource(name.replace(".", "/") + ".class");

				String pth = url.getFile();
				for (String urlRoot : urlRoots) {
					if (pth.startsWith(urlRoot))
						return true;
				}
			}
		}

		return acceptedURL;
	}

	@Override
	public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		URL[] path = findResourceAndPath(name.replace(".", "/") + ".class");

		boolean acceptedURL = checkPackage(name, path);
		
		if (aggressive || acceptedURL) {
			if (name.startsWith("java")) return super.loadClass(name, resolve);
			if (name.startsWith("tfc.flame.loader")) return super.loadClass(name, resolve);
			if (name.startsWith("org.objectweb.asm")) return super.loadClass(name, resolve);
			
			Class<?> c = findLoadedClass(name);
			if (c != null) return c;
			
			try {
				c = findClass(name, path);
				if (c != null) return c;
			} catch (Throwable err) {
				err.printStackTrace();
			}
		}

		return super.loadClass(name, resolve);
	}

	public Class<?> getClassIfLoaded(String name) {
		return findLoadedClass(name);
	}

	URL extractJarPth(URL src) {
		try {
			if (src.getProtocol().equals("jar"))
				return new URL(src.toString().split("!", 2)[0] + "!/");
		} catch (Throwable ignored) {
		}
		return null;
	}

	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {
		URL[] path = findResourceAndPath(name.replace(".", "/") + ".class");
		return findClass(name, path);
	}

	public Class<?> findClass(String name, URL[] path) throws ClassNotFoundException {
		if (path == null)
			return null;

		if (path[1] == null) {
			boolean acceptedURL = checkPackage(name, path);

			if (acceptedURL)
				path[1] = extractJarPth(path[0]);
			else return null;
		}

		byte[] data = getBytecode(name, true, null);

		if (data == null || data.length == 0)
			return null;

		FlameResource resource = new FlameResource(path[0], data);

		int i = name.lastIndexOf('.');
		if (i != -1) {
			String pkgname = name.substring(0, i);
			// load package if needed
			definePackage(resource, pkgname, resource.mf, path[1]);
		}

		if (path[1] != null) {
			CodeSigner[] signers = null;
			if (resource.entry != null) signers = resource.entry.getCodeSigners();
			CodeSource cs = new CodeSource(path[1], signers);
			return super.defineClass(name, data, 0, data.length, cs);
		} else return super.defineClass(name, data, 0, data.length);
	}

	public URL[] getClassPath() {
		return Arrays.copyOf(urls, urls.length);
	}

	public byte[] getBytecode(String name, boolean runTransformers, ClassTransformer stopTransformingOn) throws ClassNotFoundException {
		URL[] path = findResourceAndPath(name.replace(".", "/") + ".class");

		byte[] data = null;

		ClassNotFoundException ex = null;

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			//noinspection resource
			InputStream is = path[0].openStream();
			// read all bytes
			while (true) {
				byte[] data1 = new byte[is.available()];

				int read = is.read(data1);
				if (read == -1)
					break;
				// buffered input stream can be scuffed
				if (read == 0) {
					int b = is.read();
					if (b == -1) break;
					else baos.write(b);
				}

				baos.write(data1);
			}

			data = baos.toByteArray();
		} catch (Throwable err) {
			// deffer exception to allow for creating classes at runtime
			ex = new ClassNotFoundException("Could not find class " + name + " in " + path[1], err);
			ex.fillInStackTrace();
		}

		if (runTransformers) {
			// read class to node
			ClassNode[] node = new ClassNode[]{new ClassNode()};
			boolean[] changed = new boolean[]{false};

			if (data != null) {
				ClassReader reader = new ClassReader(data);
				reader.accept(node[0], ClassReader.EXPAND_FRAMES);
			}

			boolean[] hit = new boolean[]{false};

			// run transformation
			transformers.forEach((transformer) -> {
				if (hit[0]) return;

				if (transformer == stopTransformingOn) {
					hit[0] = true;
					return;
				}

				ClassNode res = transformer.accept(node[0]);
				if (res != null) {
					node[0] = res;
					changed[0] = true;
				}
			});

			// write class back to bytes
			if (changed[0]) {
				ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
				node[0].accept(writer);
				data = writer.toByteArray();
			}
		}

		if (data == null) {
			if (ex != null)
				throw ex;
			else throw new ClassNotFoundException("What");
		}

		return data;
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		return super.getResourceAsStream(name);
	}
}
