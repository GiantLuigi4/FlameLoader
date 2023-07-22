package mixins;

import mixin.extra.dump.Insn;
import mixin.test.MixinTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MixinTarget.class, remap = false)
public class TargetMixin {
	@Inject(at = @At("HEAD"), method = "main")
	private static void preMain(String[] args, CallbackInfo ci) {
		System.out.println("Start of Main");
	}
	
	//Not working very much
//	@ModifyVariable(method = "main", at = @At(value = "STORE", target = "Ljava/lang/Integer;i"))
//	private static int beforePrint(int var) {
//		int newVar = var + 1;
//		System.out.printf("Modifying variable i: %d -> %d%n", var, newVar);
//		return newVar;
//	}
	
	@Inject(method = "main", at = @At(value = "INVOKE", target = "Ljava/io/PrintStream;println(I)V", ordinal = 0, shift = At.Shift.AFTER))
	private static void afterPrint(String[] args, CallbackInfo ci) {
		System.out.println("The number was printed");
	}
	
	@ModifyConstant(method = "main", constant = @Constant(intValue = 0))
	private static int loopStartChange(int var) {
		int newVar = var + 10;
		System.out.printf("Modifying start variable e: %d -> %d%n", var, newVar);
		return newVar;
	}
	
	@ModifyConstant(method = "main", constant = @Constant(intValue = 32))
	private static int loopEndChange(int var) {
		int newVar = var + 10;
		System.out.printf("Modifying end variable e: %d -> %d%n", var, newVar);
		return newVar;
	}
	
	@Inject(method = "main", at = @At(value = "INVOKE", target = "Ljava/io/PrintStream;println(I)V", ordinal = 1, shift = At.Shift.AFTER))
	private static void printInLoopChange(String[] args, CallbackInfo ci) {
		System.out.println("It's me");
	}
	
	@Inject(method = "main", at = @At(value = "FIELD", target = "Ljava/lang/System;out:Ljava/io/PrintStream;", ordinal = 2, shift = At.Shift.BEFORE))
	private static void printAfterLoop(String[] args, CallbackInfo ci) {
		System.out.println("Loop ended");
	}
	@Inject(at = @At("RETURN"), method = "main")
	private static void postMain(String[] args, CallbackInfo ci) {
		System.out.println("End of Main\n");
	}
}
