package mixins;

import mixin.test.MixinTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MixinTarget.class, remap = false)
public class TargetMixin {
	@Inject(at = @At("HEAD"), method = "main")
	private static void preMain(String[] args, CallbackInfo ci) {
		System.out.println("Start of Main");
	}
	
	// How do I inject it between the "i" variable declaration and the first system out println?
	// @Inject(at = @At(opcode = ), method = "main")
//	private static void beforePrint(String[] args, CallbackInfo ci) {
//		System.out.println("PRINT");
//	}
	
	@Inject(at = @At("RETURN"), method = "main")
	private static void postMain(String[] args, CallbackInfo ci) {
		System.out.println("End of Main");
	}
}
