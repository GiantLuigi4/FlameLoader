package tfc.flame;

import org.apache.bcel.util.ClassPath;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.jar.Manifest;

public class FlameURLLoader extends URLClassLoader {
	public FlameURLLoader(URL[] urls) {
		super(urls);
	}
	
	public FlameURLLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}
	
	public Class<?> load(String name, boolean resolve) throws ClassNotFoundException {
		return loadClass(name, resolve);
	}
	
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return this.loadClass(name, true);
	}
	
	Class<?> classLoader = null;
	
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
//		if (name.equals("java.lang.ClassLoader")) {
//			return classLoader;
//		}
		return super.findClass(name);
	}
	
	private final HashMap<String, byte[]> merges = new HashMap<>();
	private final HashMap<String, byte[]> replacements = new HashMap<>();
	
	private final HashMap<String, Function<String, byte[]>> replacementGetters = new HashMap<>();
	private final HashMap<String, BiFunction<String, byte[], byte[]>> asmAppliers = new HashMap<>();
	private final HashMap<String, Function<String, byte[]>> baseCodeGetters = new HashMap<>();
	
	public BiFunction<String, byte[], Class<?>> classDefiner = (name, bytes) -> defineClass(name, bytes, 0, bytes.length);
	
	public HashMap<String, Function<String, byte[]>> getReplacementGetters() {
		return replacementGetters;
	}
	
	public HashMap<String, BiFunction<String, byte[], byte[]>> getAsmAppliers() {
		return asmAppliers;
	}
	
	public HashMap<String, Function<String, byte[]>> getBaseCodeGetters() {
		return baseCodeGetters;
	}
	
	public ArrayList<Consumer<URL>> urlAddListeners = new ArrayList<>();
	
	public void addURL(URL url) {
		for (Consumer<URL> urlAddListener : urlAddListeners) urlAddListener.accept(url);
		super.addURL(url);
	}
	
	public void findReplacement(String name) {
		FlameConfig.field.append(name + "\n");
		String name1 = "";
		byte[] bytes1 = null;
		try {
			name1 = name.replace("merges.", "").replace("replacements.", "");
			name1 = name1.substring(name1.indexOf('.') + 1);
		} catch (Throwable ignored) {
		}
		if (bytes1 == null) {
			for (URL url : this.getURLs()) {
				if (bytes1 == null) {
					try {
						bytes1 = new ClassPath(url.getPath()).getBytes(name);
					} catch (Throwable err) {
						try {
							bytes1 = new ClassPath(url.getFile()).getBytes(name);
						} catch (Throwable ignored) {
						}
					}
				}
			}
			try {
				InputStream stream = this.getResourceAsStream(name);
				assert stream != null;
				bytes1 = new byte[stream.available()];
				stream.read(bytes1);
				stream.close();
			} catch (Throwable ignored) {
			}
		}
		if (name.startsWith("merges.")) {
			if (!merges.containsKey(name)) {
				merges.put(name1, bytes1);
				FlameConfig.field.append("Found merge for class: " + name1);
			} else {
				merges.replace(name1.replace("merges.", ""), merge(bytes1, merges.get(name1)));
			}
		} else if (name.startsWith("replacements.")) {
			FlameConfig.field.append("Found replacement for class: " + name1 + "\n");
			replacements.putIfAbsent(name1, bytes1);
		}
	}
	
	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
//		if (name.equals("java.lang.ClassLoader")) {
//			classLoader = loadClass("tfc.flame.FlameClassLoader");
//			return classLoader;
//		}
		
		if (name.startsWith("tfc.flame") ||
//				name.startsWith("com.mojang.serialization") ||
//				name.startsWith("com.mojang.datafixers")
				name.startsWith("com.mojang")
		) {
			try {
				return this.getParent().loadClass(name);
			} catch (Throwable err) {
				if (
						name.startsWith("tfc.flame") &&
						!name.substring("tfc.flame.a".length()).contains(".")
				)
					throw new SecurityException("Tried to load class in invalid namespace: \"tfc.flame\"");
			}
		}
		synchronized (this.getClassLoadingLock(name)) {
			if (FlameConfig.log_classnames) FlameConfig.field.append(name + "\n");
			Class<?> c = this.findLoadedClass(name);
			if (c == null) {
//				long t0 = System.nanoTime();
				try {
					byte[] bytes1 = null;
					for (URL url : this.getURLs()) {
						if (bytes1 == null) {
							try {
								bytes1 = new ClassPath(url.getPath()).getBytes(name);
							} catch (Throwable ignored) {
							}
						}
					}
					if (bytes1 == null) {
						try {
							InputStream stream = this.getResourceAsStream(name);
							assert stream != null;
							bytes1 = new byte[stream.available()];
							stream.read(bytes1);
							stream.close();
						} catch (Throwable ignored1) {
						}
					}
					if (bytes1 == null) {
						for (Function<String, byte[]> function : baseCodeGetters.values()) {
							bytes1 = function.apply(name);
						}
					}
					if (replacements.containsKey(name)) {
						bytes1 = replacements.get(name);
					} else if (bytes1 != null && merges.containsKey(name)) {
						FlameConfig.field.append("Merging class: " + name + " with modded versions of said class.\n");
						FlameConfig.field.append("Things will probably go wrong.\n");
						bytes1 = merge(bytes1, merges.get(name));
					}
					//Use replacement getters
					for (Function<String, byte[]> function : replacementGetters.values()) {
						byte[] bytes2 = function.apply(name);
						if (bytes2 != null) {
							bytes1 = bytes2;
						}
					}
					//Handle ASM
					for (BiFunction<String, byte[], byte[]> function : asmAppliers.values()) {
						bytes1 = function.apply(name, bytes1);
					}
					if (FlameConfig.log_bytecode) FlameConfig.field.append(Arrays.toString(bytes1) + "\n");
					//Define if possible
//					try {
//						Class<?> clazz = this.getClass();
//						Method m = clazz.getDeclaredMethod("defineClass1", String.class, byte[].class, int.class, int.class, ProtectionDomain.class, String.class);
//						m.setAccessible(true);
//						return (Class<?>) m.invoke(getParent(), bytes1, 0, bytes1.length);
//					} catch (Throwable ignored) {
//					}
					if (bytes1 != null) c = classDefiner.apply(name, bytes1);
//					if (bytes1 != null) c = this.defineClass(bytes1, 0, bytes1.length);
					//Load from parent
					if (c == null && this.getParent() != null) c = this.getParent().loadClass(name);
				} catch (ClassNotFoundException err) {
					FlameConfig.logError(err);
				}
				
				if (c == null) {
//					long t1 = System.nanoTime();
					c = this.findClass(name);
//					PerfCounter.getParentDelegationTime().addTime(t1 - t0);
//					PerfCounter.getFindClassTime().addElapsedTimeFrom(t1);
//					PerfCounter.getFindClasses().increment();
				}
			}
			
			if (resolve) {
				this.resolveClass(c);
			}
			
			return c;
		}
	}
	
	public Package definePackage(String name, Manifest mf, URL url) {
		return super.definePackage(name, mf, url);
	}
	
	public Package getPackage(String name) {
		return super.getPackage(name);
	}
	
	public Class<?> define(String name, byte[] bytes) {
		return defineClass(name, bytes, 0, bytes.length);
	}
	
	public Class<?> define(String name, byte[] bytes, CodeSource source) {
		return defineClass(name, bytes, 0, bytes.length, source);
	}
	
	private byte[] merge(byte[] source, byte[] to_merge) {
		int char_source = 0;
		int char_merge = 0;
		ArrayList<Byte> newBytes = new ArrayList<>();
		while (char_source < source.length && char_merge < to_merge.length) {
			boolean added = false;
			newBytes.add(source[char_source]);
			if (source[char_source] != to_merge[char_merge]) {
				added = true;
				newBytes.add(to_merge[char_merge]);
			} else {
				newBytes.add(to_merge[char_source]);
			}
			char_merge++;
			if (!added) {
				char_source++;
			}
		}
		byte[] newBytesReturn = new byte[newBytes.size()];
		for (int i = 0; i < newBytes.size(); i++) {
			newBytesReturn[i] = newBytes.get(i);
		}
		return newBytesReturn;
	}
}
