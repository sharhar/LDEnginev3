package code.window;

import static org.lwjgl.glfw.GLFW.*;

public class Window {
	public long window;
	
	public Window(long window) {
		this.window = window;
	}
	
	public boolean isOpen() {
		return !glfwWindowShouldClose(window);
	}
	
	public void setTitle(String title) {
		glfwSetWindowTitle(window, title);
	}
	
	public void poll() {
		glfwPollEvents();
	}
	
	public void destroy() {
		glfwDestroyWindow(window);
	}
}
