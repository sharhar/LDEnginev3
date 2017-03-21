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
		
		String title = "LD Game | Renderer: ";
		
		if(useVulkan) {
			title += "Vulkan";
			render = new VLKRenderer();
		} else {
			title += "OpenGL";
			render = new GLRenderer();
		}
		
		long window = render.createWindowandContext(800, 600, title, true);
		
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
		Texture texture = render.createTexture("res/TestImage.png");
		Texture texture2 = render.createTexture("res/TestImage2.png");
		Renderable renderable = render.createRenderable(shader, new Vector2f(100, 100), new Vector2f(50, 50), texture);
		Renderable renderable2 = render.createRenderable(shader, new Vector2f(100, 300), new Vector2f(50, 50), texture2);
		
		Vector4f clearColor = new Vector4f(0.2f, 0.3f, 0.8f, 1);
		
		double st = glfwGetTime();
		double ct = st;
		
		double dt = ct - st;
		
		int frames = 0;
		double time = 0;
		int fps = 0;
		
		while(!glfwWindowShouldClose(window)) {
			glfwPollEvents();
			
			ct = glfwGetTime();
			dt = ct - st;
			st = ct;
			
			frames = frames + 1;
			time = time + dt;
			if(time >= 1) {
				fps = frames;
				glfwSetWindowTitle(window, title + " | FPS: " + fps);
				frames = 0;
				time = 0;
			}
			
			render.clear(clearColor);
			
			float xoff = (float) Math.cos(glfwGetTime()*3.14159) * 100;
			float yoff = (float) Math.sin(glfwGetTime()*2) * 100;
			
			renderable.pos.x = xoff + 400;
			renderable2.pos.x = xoff + 400;
			
			renderable.pos.y = -yoff + 200;
			renderable2.pos.y = yoff + 400;
			
			model.bind();
			shader.bind();
			
			renderable.applyUniforms();
			model.draw();
			
			renderable2.applyUniforms();
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
