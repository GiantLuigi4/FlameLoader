package mixin;

import org.spongepowered.asm.launch.platform.container.ContainerHandleVirtual;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.logging.LoggerAdapterJava;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.mixin.transformer.IMixinTransformerFactory;
import org.spongepowered.asm.service.*;
import tfc.flame.loader.IFlameLoader;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;

public class MixinServiceTest extends MixinServiceAbstract {
    private static MixinServiceTest INSTANCE;

    public static MixinServiceTest getInstance() {
        return INSTANCE;
    }

    MixinClassLoaderAdapter adapter;
	IMixinTransformer transformer;

	public MixinServiceTest() {
        if (INSTANCE != null)
            throw new RuntimeException("Cannot create multiple mixin services");

		try {
			IFlameLoader loader = (IFlameLoader) MixinServiceTest.class.getClassLoader();

			adapter = new MixinClassLoaderAdapter(loader);
		} catch (Throwable err) {
			err.printStackTrace();
			throw new RuntimeException(err);
		}

        INSTANCE = this;
	}

	public IMixinTransformer getTransformer() {
		if (transformer == null)
    		transformer = getInternal(IMixinTransformerFactory.class).createTransformer();
		return transformer;
	}

	@Override
	public String getName() {
		return "FlameMixin";
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public IClassProvider getClassProvider() {
		return adapter;
	}

	@Override
	public IClassBytecodeProvider getBytecodeProvider() {
		return adapter;
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		return adapter.getResourceAsStream(name);
	}

	@Override
	public Collection<String> getPlatformAgents() {
		return Collections.singletonList(
				"mixin.extra.PlatformAgent"
		);
	}

	@Override
	public IContainerHandle getPrimaryContainer() {
		return new ContainerHandleVirtual(getName());
	}

	@Override
	public ILogger getLogger(String name) {
		return new LoggerAdapterJava(name);
	}

	@Override
	public IClassTracker getClassTracker() {
		return null;
	}

	@Override
	public IMixinAuditTrail getAuditTrail() {
		return null;
	}

	@Override
	public ITransformerProvider getTransformerProvider() {
		throw new RuntimeException("This shouldn't happen");
	}
}
