package example;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import tfc.flame.IFlameMod;
import tfc.flame.asm.ITransformerEntry;
import tfc.flame.loader.IFlameLoader;
import tfc.flame.util.JDKLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Objects;

public class Entry {
	public static void main(String[] args) {
		ArrayList<URL> urls = new ArrayList<>();
		
		File modsFolder = new File("flame_mods");
		if (modsFolder.exists()) {
			for (File file : Objects.requireNonNull(modsFolder.listFiles())) {
				try {
					urls.add(file.toURL());
				} catch (Throwable ignored) {
				}
			}
		} else {
			modsFolder.mkdirs();
		}
		
		IFlameLoader loader = JDKLoader.createLoader(urls.toArray(new URL[0]), Entry.class.getClassLoader(), true);
		// tell flame to load the game on the loader
		loader.addPackageOverride("example.main");
		
		ArrayList<ModContainer> mods = new ArrayList<>();
		
		// discover mods
		try {
			Enumeration<URL> modUrls = loader.findResources("mod.json");
			
			Gson gson = new Gson();
			
			while (modUrls.hasMoreElements()) {
				URL url = modUrls.nextElement();
				try {
					InputStream is = url.openStream();
					
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
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
					
					JsonObject obj = gson.fromJson(baos.toString(), JsonObject.class);
					
					IFlameMod mod = null;
					if (obj.has("entry")) {
						mod = (IFlameMod) loader.loadClass(
								obj.getAsJsonPrimitive("entry").getAsString(), true
						).newInstance();
					}
					
					ITransformerEntry transformer = null;
					if (obj.has("entry")) {
						transformer = (ITransformerEntry) loader.loadClass(
								obj.getAsJsonPrimitive("transformer").getAsString(), true
						).newInstance();
					}
					
					// create the container
					ModContainer container = new ModContainer(
							obj.getAsJsonPrimitive("name").getAsString(),
							mod, transformer, obj
					);
					
					mods.add(container);
				} catch (Throwable err) {
					System.out.println("Failed loading a mod.json: " + url);
					err.printStackTrace();
				}
			}
		} catch (Throwable err) {
			throw new RuntimeException(err);
		}
		
		// initialize mods
		for (ModContainer mod : mods) mod.transformerEntry.setup();
		for (ModContainer mod : mods) mod.main.preInit();
		for (ModContainer mod : mods) mod.main.onInit();
		
		try {
			loader.loadClass("example.main.MainClass", true)
					.getDeclaredMethod("main", String[].class)
					.invoke(null, (Object) args);
		} catch (Throwable err) {
			err.printStackTrace();
		}
	}
}
