package tfc.flame.util;

import tfc.flame.loader.FlameLoader;
import tfc.flame.loader.IFlameLoader;

import java.net.URL;

public class JDKLoader {
	public static IFlameLoader createLoader(URL[] urls) {
//		try {
//			Class<?> SECRETS = Class.forName("sun.misc.SharedSecrets");
//			return new LegacyFlameLoader(urls);
//		} catch (Throwable ignored) {
		return new FlameLoader(urls);
//		}
	}
	
	public static IFlameLoader createLoader(URL[] urls, ClassLoader parent) {
		return createLoader(urls, parent, false);
	}
	
	public static IFlameLoader createLoader(URL[] urls, ClassLoader parent, boolean aggressive) {
//		try {
//			// check for if legacy loader should be used
//			Class<?> SECRETS = Class.forName("sun.misc.SharedSecrets");
//			return new LegacyFlameLoader(urls, parent, aggressive);
//		} catch (Throwable ignored) {
		return new FlameLoader(urls);
//		}
	}
}
