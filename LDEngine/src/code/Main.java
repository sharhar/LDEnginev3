package code;

import static org.lwjgl.glfw.GLFW.*;

import code.graphincs.Renderer;
import code.graphincs.gl.GLRenderer;
import code.graphincs.vk.VLKRenderer;
import code.math.Vector4f;

public class Main {
	public static void main(String[] args) {
		glfwInit();
		
		boolean useVulkan = false;
		
		Renderer render = null;
		
		if(useVulkan) {
			render = new VLKRenderer();
		} else {
			render = new GLRenderer();
		}
		
		long window = render.createWindowandContext(800, 600, "LD Game");
		
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
