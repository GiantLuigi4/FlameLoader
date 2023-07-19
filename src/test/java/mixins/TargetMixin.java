package mixins;

import mixin.test.MixinTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MixinTarget.class, remap = false)
public class TargetMixin {
	@Inject(at = @At("HEAD"), method = "main", cancellable = true)
	private static void preMain(String[] args, CallbackInfo ci) {
		System.out.println("Hi");
	}
}
