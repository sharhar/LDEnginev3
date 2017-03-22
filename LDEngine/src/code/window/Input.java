package code.window;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_LAST;

import code.math.Vector2f;

public class Input {
	public static boolean[] keys = new boolean[GLFW_KEY_LAST];
	public static boolean leftMouseDown = false;
	public static boolean rightMouseDown = false;
	public static Vector2f mousePos = new Vector2f();
	
	static {
		for(int i = 0; i < keys.length;i++) {
			keys[i] = false;
		}
	}
}
