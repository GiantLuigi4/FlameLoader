package mixin.test;

public class MixinTarget {
	public static void main(String[] args) {
		int i = 52;
		System.out.println(i + 16);
		
		for (int e = 0; e < 32; e++) {
			System.out.println(e);
		}
		
		System.out.println(i + 16);
	}
}
