package example;

import com.google.gson.JsonObject;
import tfc.flame.loader.IFlameMod;
import tfc.flame.loader.asm.ITransformerEntry;

public class ModContainer {
	String name;
	IFlameMod main;
	ITransformerEntry transformerEntry;
	
	JsonObject data;
	
	public ModContainer(String name, IFlameMod main, ITransformerEntry transformerEntry, JsonObject data) {
		this.name = name;
		this.main = main;
		this.transformerEntry = transformerEntry;
		this.data = data;
	}
}
