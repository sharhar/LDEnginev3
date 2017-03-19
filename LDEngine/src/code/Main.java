package code;

import static org.lwjgl.glfw.GLFW.*;

import code.graphincs.Model;
import code.graphincs.Renderable;
import code.graphincs.Renderer;
import code.graphincs.Shader;
import code.graphincs.Texture;
import code.graphincs.gl.GLRenderer;
import code.graphincs.vk.VLKRenderer;
import code.math.Vector2f;
import code.math.Vector4f;

public class Main {
	public static void main(String[] args) {
		glfwInit();
		
		boolean useVulkan = true;
		
		Renderer render = null;
		
		if(useVulkan) {
			render = new VLKRenderer();
		} else {
			render = new GLRenderer();
		}
		
		long window = render.createWindowandContext(800, 600, "LD Game");
		
		float verts[] = {
				 1, -1, 1, 1,
				-1,  1, 0, 0,
				-1, -1, 0, 1,
				 
				-1,  1, 0, 0,
				 1, -1, 1, 1,
				 1,  1, 1, 0
		};
		
		Model model = render.createModel(verts);
		Shader shader = render.createShader("res/entity", "res/entity");
		//Texture texture = render.createTexture("/TestImage.png");
		//Renderable renderable = render.createRenderable(new Vector2f(100, 100), new Vector2f(50, 50), texture);
		
		Vector4f clearColor = new Vector4f(0.2f, 0.3f, 0.8f, 1);
		
		while(!glfwWindowShouldClose(window)) {
			glfwPollEvents();
			
			render.clear(clearColor);
			
			//renderable.pos.x += 1;
			
			model.bind();
			shader.bind();
			//renderable.update(shader);
			model.draw();
			shader.unbind();
			model.unbind();
			
			render.swap();
		}
		
		render.destory();

		glfwDestroyWindow(window);
		glfwTerminate();
	}
}
