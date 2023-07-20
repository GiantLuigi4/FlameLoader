package mixin;

import mixin.extra.dump.BytecodeWriter;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import tfc.flame.loader.FlameLoader;
import tfc.flame.loader.asm.ClassTransformer;
import tfc.flame.loader.asm.Phase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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
					if (node.name.startsWith("mixin/extra")) return null;
					
					if (transformer.transformClass(MixinEnvironment.getDefaultEnvironment(), node.name.replace("/", "."), node)) {
						try {
							File fl = new File(node.name + ".txt");
							if (!fl.getParentFile().exists()) fl.getParentFile().mkdirs();
							BytecodeWriter.write(node, fl);
							
							ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
							node.accept(writer);
							
							FileOutputStream outputStream = new FileOutputStream(node.name + ".class");
							outputStream.write(writer.toByteArray());
							outputStream.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						return node;
					}
					
					
					return null;
				}
			};
			if (theLoader instanceof FlameLoader loader) {
				loader.transformers.add(Phase.FIRST, MIXIN_TRANSFORMER);
			}
		} catch (Throwable err) {
			err.printStackTrace();
			throw new RuntimeException("Failed to setup mixin");
		}
	}
	
	public static void init() {
	}
}
