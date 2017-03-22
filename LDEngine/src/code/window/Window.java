package code.window;

import static org.lwjgl.glfw.GLFW.*;

import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

public class Window {
	public long window;
	
	public Window(long window) {
		this.window = window;
		
		glfwSetCursorPosCallback(window, new GLFWCursorPosCallback() {
			public void invoke(long window, double xpos, double ypos) {
				Input.mousePos.x = (float)xpos;
				Input.mousePos.y = (float)ypos;
			}
		});
		
		glfwSetMouseButtonCallback(window, new GLFWMouseButtonCallback() {
			public void invoke(long window, int button, int action, int mods) {
				if(action == GLFW_PRESS && button == GLFW_MOUSE_BUTTON_LEFT) {
					Input.leftMouseDown = true;
				} else if(action == GLFW_RELEASE && button == GLFW_MOUSE_BUTTON_LEFT) {
					Input.leftMouseDown = false;
				} else if(action == GLFW_PRESS && button == GLFW_MOUSE_BUTTON_RIGHT) {
					Input.rightMouseDown = true;
				} else if(action == GLFW_RELEASE && button == GLFW_MOUSE_BUTTON_RIGHT) {
					Input.rightMouseDown = false;
				}
			} 
		});
		
		glfwSetKeyCallback(window, new GLFWKeyCallback() {
			public void invoke(long window, int key, int scancode, int action, int mods) {
				if(action == GLFW_PRESS) {
					Input.keys[key] = true;
				} else if (action == GLFW_RELEASE) {
					Input.keys[key] = false;
				}
			}
		});
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
