package example.main;

public class MainClass {
	public static void main(String[] args) {
		System.out.println("Hello!");
		System.out.println("Game loaded from " + MainClass.class.getClassLoader());
	}
}
