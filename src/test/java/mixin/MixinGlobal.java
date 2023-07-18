package mixin;

import org.spongepowered.asm.service.IGlobalPropertyService;
import org.spongepowered.asm.service.IPropertyKey;

import java.util.HashMap;
import java.util.Objects;

public class MixinGlobal implements IGlobalPropertyService {
	HashMap<String, Key> keys = new HashMap<>();
	HashMap<Key, Object> map = new HashMap<>();
	
	@Override
	public IPropertyKey resolveKey(String name) {
		return keys.computeIfAbsent(name, Key::new);
	}
	
	@Override
	public <T> T getProperty(IPropertyKey key) {
		//noinspection unchecked
		return (T) map.get((Key) key);
	}
	
	@Override
	public void setProperty(IPropertyKey key, Object value) {
		map.put((Key) key, value);
	}
	
	@Override
	public <T> T getProperty(IPropertyKey key, T defaultValue) {
		//noinspection unchecked
		return (T) map.getOrDefault((Key) key, defaultValue);
	}
	
	@Override
	public String getPropertyString(IPropertyKey key, String defaultValue) {
		return map.get((Key) key).toString();
	}
	
	static class Key implements IPropertyKey {
		String name;
		
		public Key(String name) {
			this.name = name;
		}
		
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Key key = (Key) o;
			return Objects.equals(name, key.name);
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(name);
		}
	}
}
