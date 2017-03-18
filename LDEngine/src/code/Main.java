package code;

import static org.lwjgl.glfw.GLFW.*;

import code.graphincs.Renderer;
import code.graphincs.vk.VLKRenderer;
import code.math.Vector4f;

public class Main {
	public static void main(String[] args) {
		glfwInit();
		
		glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
		long window = glfwCreateWindow(800, 600, "LD Game", 0, 0);
		
		Renderer render = new VLKRenderer();
		render.init(window);
		
		Vector4f clearColor = new Vector4f(1, 0, 1, 1);
		
		while(!glfwWindowShouldClose(window)) {
			glfwPollEvents();
			
			render.clear(clearColor);
			render.swap();
			
		}
		
		render.destory();

		glfwDestroyWindow(window);
		glfwTerminate();
	}
}
