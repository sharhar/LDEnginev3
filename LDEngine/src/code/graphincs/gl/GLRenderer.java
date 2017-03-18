package code.graphincs.gl;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.opengl.GL;

import code.graphincs.Model;
import code.graphincs.Renderable;
import code.graphincs.Renderer;
import code.graphincs.Shader;
import code.graphincs.Texture;
import code.math.Vector2f;
import code.math.Vector4f;

public class GLRenderer extends Renderer{
	long window = 0;
	
	public long createWindowandContext(int width, int height, String title) {
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);

		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

		window = glfwCreateWindow(800, 600, "LD Game", 0, 0);
		glfwMakeContextCurrent(window);
		GL.createCapabilities();
		
		//glEnable(GL_CULL_FACE);
		//glCullFace(GL_FRONT_FACE);
		
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
	
	public Model createModel(float[] data) {
		return new GLModel(this, data);
	}
	
	public Shader createShader(String vertPath, String fragPath) {
		return new GLShader(this, vertPath + ".vert", fragPath + ".frag");
	}
	
	public Texture createTexture(String path) {
		return new GLTexture(this, path);
	}
	
	public Renderable createRenderable(Vector2f pos, Vector2f size, Texture texture) {
		return new GLRenderable(pos, size, texture);
	}
}
