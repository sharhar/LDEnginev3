package code.graphincs.vk;

import static org.lwjgl.glfw.GLFW.*;

import code.graphincs.Model;
import code.graphincs.Renderable;
import code.graphincs.Renderer;
import code.graphincs.Shader;
import code.graphincs.Texture;
import code.math.Vector2f;
import code.math.Vector4f;

public class VLKRenderer extends Renderer{
	VLK.VLKContext context = null;
	VLK.VLKDevice device = null;
	VLK.VLKSwapChain swapChain = null;
	
	public long createWindowandContext(int width, int height, String title) {
		glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
		long window = glfwCreateWindow(800, 600, "LD Game", 0, 0);
		context = VLK.createContext(true);
		device = VLK.createDevice(context);
		swapChain = VLK.createSwapChain(context, device, window);
		
		return window;
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
		return null;
	}

	public Shader createShader(String vertPath, String fragPath) {
		return null;
	}
	
	public Texture createTexture(String path) {
		return null;
	}
	
	public Renderable createRenderable(Vector2f pos, Vector2f size, Texture texture) {
		return null;
	}
}
