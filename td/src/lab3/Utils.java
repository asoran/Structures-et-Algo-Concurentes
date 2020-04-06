package lab3;

import java.nio.file.Path;

public class Utils {

	// Question 3

//	private static volatile Path HOME;
//	private final static Object lock = new Object();
//
//	public static Path getHome() {
//		if (HOME == null) {
//			synchronized (lock) {
//				if (HOME == null)
//					HOME = Path.of(System.getenv("HOME"));
//			}
//		}
//
//		return HOME;
//	}

	// Question 4

//	@SuppressWarnings("unused")
//	private static Path HOME;
//	private final static VarHandle HANDLE;
//	static {
//		try {
//			HANDLE = MethodHandles.lookup().findStaticVarHandle(Utils.class, "HOME", Path.class);
//		} catch (NoSuchFieldException | IllegalAccessException e) {
//		"	throw new AssertionError("Should not happen");
//		}
//	}
//
//	public static Path getHome() {
//		var home = (Path) HANDLE.getAcquire();
//		if (home == null) {
//			synchronized (Utils.class) {
//				home = (Path) HANDLE.getAcquire();
//				if (home == null) {
//					HANDLE.setRelease(Path.of(System.getenv("HOME")));
//					return (Path) HANDLE.getAcquire();
//				}
//			}
//		}
//		return home;
//	}

	// Question 5

	private static class HomeHolder {
		static final Path HOME = Path.of(System.getenv("HOME"));
	}

	public static Path getHome() {
		return HomeHolder.HOME;
	}

	// Testing main

	public static void main(String[] args) {
		System.out.println(Utils.getHome());
	}

}
