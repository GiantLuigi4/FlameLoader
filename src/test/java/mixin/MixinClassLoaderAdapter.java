package mixin;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.service.IClassBytecodeProvider;
import org.spongepowered.asm.service.IClassProvider;
import tfc.flame.loader.IFlameLoader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class MixinClassLoaderAdapter implements IClassProvider, IClassBytecodeProvider {
	IFlameLoader loader;
	
	public MixinClassLoaderAdapter(IFlameLoader loader) {
		this.loader = loader;
	}
	
	@Override
	public URL[] getClassPath() {
		return loader.getClassPath();
	}
	
	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {
		return Class.forName(name, true, (ClassLoader) loader);
	}
	
	@Override
	public Class<?> findClass(String name, boolean initialize) throws ClassNotFoundException {
		return Class.forName(name, initialize, (ClassLoader) loader);
	}
	
	@Override
	public Class<?> findAgentClass(String name, boolean initialize) throws ClassNotFoundException {
		return findClass(name, initialize);
	}
	
	@Override
	public ClassNode getClassNode(String name) throws ClassNotFoundException, IOException {
		return getClassNode(name, true);
	}
	
	@Override
	public ClassNode getClassNode(String name, boolean runTransformers) throws ClassNotFoundException, IOException {
		ClassReader reader = new ClassReader(loader.getBytecode(name, runTransformers, MixinLoader.MIXIN_TRANSFORMER));
		ClassNode node = new ClassNode();
		reader.accept(node, ClassReader.EXPAND_FRAMES);
		return node;
	}
	
	public InputStream getResourceAsStream(String name) {
		return loader.getResourceAsStream(name);
	}
}
