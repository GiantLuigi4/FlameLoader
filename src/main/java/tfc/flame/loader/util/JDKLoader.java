package tfc.flame.loader.util;

import tfc.flame.loader.FlameLoader;
import tfc.flame.loader.IFlameLoader;

import java.net.URL;

public class JDKLoader {
	public static IFlameLoader createLoader(URL[] urls) {
		return new FlameLoader(urls);
	}
	
	public static IFlameLoader createLoader(URL[] urls, ClassLoader parent) {
		return createLoader(urls, parent, false);
	}

	public static IFlameLoader createLoader(URL[] urls, ClassLoader parent, boolean aggressive) {
		return new FlameLoader(urls, parent, aggressive);
	}
}
