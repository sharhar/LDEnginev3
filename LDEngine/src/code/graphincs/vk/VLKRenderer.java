package code.graphincs.vk;

import static org.lwjgl.glfw.GLFW.*;

import code.graphincs.Model;
import code.graphincs.Renderable;
import code.graphincs.Renderer;
import code.graphincs.Shader;
import code.graphincs.Texture;
import code.math.Vector2f;
import code.math.Vector4f;
import code.window.Window;

public class VLKRenderer extends Renderer{
	VLK.VLKContext context = null;
	VLK.VLKDevice device = null;
	VLK.VLKSwapChain swapChain = null;
	
	public Window createWindowandContext(int width, int height, String title, boolean debug) {
		glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
		window = glfwCreateWindow(width, height, title, 0, 0);
		context = VLK.createContext(debug);
		device = VLK.createDevice(context);
		swapChain = VLK.createSwapChain(context, device, window);
		
		return new Window(window);
	}

	public void clear(Vector4f color) {
		VLK.clear(device, swapChain, color);
	}

	public void swap() {
		VLK.swap(device, swapChain);
	}
	
	public void destory() {
		VLK.destroyContext(context);
	}
	
	public Model createModel(float[] data) {
		return new VLKModel(this, data);
	}

	public Shader createShader(String vertPath, String fragPath) {
		return new VLKShader(this, vertPath + "-vert.spv", fragPath + "-frag.spv");
	}
	
	public Texture createTexture(String path) {
		return new VLKTexture(this, path);
	}
	
	public Renderable createRenderable(Shader shader, Vector2f pos, Vector2f size, Texture texture) {
		return new VLKRenderable(this, shader, pos, size, texture);
	}
}
