package code.graphincs.gl;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.opengl.GL;

import code.graphincs.Renderer;
import code.math.Vector4f;

public class GLRenderer extends Renderer{
	long window = 0;
	
	public long createWindowandContext(int width, int height, String title) {
		window = glfwCreateWindow(800, 600, "LD Game", 0, 0);
		glfwMakeContextCurrent(window);
		GL.createCapabilities();
		return window;
	}

	public void clear(Vector4f color) {
		glClearColor(color.x, color.y, color.z, color.w);
		glClear(GL_COLOR_BUFFER_BIT);
	}

	public void swap() {
		glfwSwapBuffers(window);
	}

	public void destory() {
		
	}
}
