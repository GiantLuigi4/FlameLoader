package tfc.flame.loader.util;

import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class FlameResource {
	public URL path;
	public byte[] data;

	public JarFile jar = null;
	public JarEntry entry = null;
	public Manifest mf = null;
	
	public FlameResource(URL path, byte[] data) {
		this.path = path;
		this.data = data;
		
		if (path.getProtocol().equals("jar")) {
			try {
				// TODO: support jars sourced from the internet..?
				URLConnection connection = path.openConnection();
				if (connection instanceof JarURLConnection) {
					JarURLConnection juc = (JarURLConnection) connection;
					jar = juc.getJarFile();
					entry = juc.getJarEntry();
					mf = juc.getManifest();
				}
//				String[] split = path.getFile().split("!");
//				jar = new JarFile(
//						new File(
//								split[0].substring("file:/".length())
//						)
//				);
//				entry = jar.getJarEntry(split[1]);
//				mf = jar.getManifest();
			} catch (Throwable err) {
				err.printStackTrace();
			}
		}
	}
}
