package mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import tfc.flame.asm.ClassTransformer;
import tfc.flame.asm.Phase;
import tfc.flame.loader.FlameLoader;

import java.lang.reflect.Constructor;

public class MixinLoader {
	public static final ClassTransformer MIXIN_TRANSFORMER;
	
	static {
		try {
			MixinBootstrap.init();
			
			Mixins.addConfiguration("test.mixins.json");
			
			IMixinTransformer transformer = MixinServiceTest.getInstance().getTransformer();
			
			ClassLoader theLoader = MixinLoader.class.getClassLoader();
			MIXIN_TRANSFORMER = new ClassTransformer() {
				@Override
				public ClassNode accept(ClassNode node) {
					if (node.name.startsWith("org/spongepowered/asm")) return null;
					
					if (transformer.transformClass(MixinEnvironment.getDefaultEnvironment(), node.name.replace("/", "."), node))
						return node;
					return null;
				}
			};
			if (theLoader instanceof FlameLoader) {
				((FlameLoader) theLoader).transformers.add(Phase.FIRST, MIXIN_TRANSFORMER);
			}
		} catch (Throwable err) {
			err.printStackTrace();
			throw new RuntimeException("Failed to setup mixin");
		}
	}
	
	public static void init() {
	}
}
