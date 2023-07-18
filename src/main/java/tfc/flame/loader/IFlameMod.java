package tfc.flame.loader;

public interface IFlameMod {
	/**
	 * Primarily for API setup
	 */
	default void preInit() {
	}
	
	/**
	 * Most mods should only use this
	 */
	void onInit();
}
