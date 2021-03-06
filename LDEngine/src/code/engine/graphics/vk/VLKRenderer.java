package code.engine.graphics.vk;

import static org.lwjgl.glfw.GLFW.*;

import java.awt.image.BufferedImage;

import code.engine.graphics.Model;
import code.engine.graphics.Renderable;
import code.engine.graphics.Renderer;
import code.engine.graphics.Shader;
import code.engine.graphics.Texture;
import code.engine.math.Vector2f;
import code.engine.math.Vector4f;
import code.engine.window.Window;

public class VLKRenderer extends Renderer{
	VLK.VLKContext context = null;
	VLK.VLKDevice device = null;
	VLK.VLKSwapChain swapChain = null;
	boolean debug;
	
	public Window createWindowandContext(int width, int height, String title, boolean debug, boolean vSync) {
		glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
		window = glfwCreateWindow(width, height, title, 0, 0);
		context = VLK.createContext(debug);
		device = VLK.createDevice(context);
		swapChain = VLK.createSwapChain(context, device, window, vSync);
		
		this.debug = debug;
		
		this.width = width;
		this.height = height;
		
		return new Window(window);
	}

	public void clear(Vector4f color) {
		VLK.clear(device, swapChain, color);
	}

	public void swap() {
		VLK.swap(device, swapChain);
	}
	
	public void destory() {
		if(debug) {
			VLK.destroyContext(context);
		}	
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
	
	public Renderable createRenderable(Model model, Shader shader, Vector2f pos, float rot, Vector2f size, Texture texture) {
		return new VLKRenderable(this, model, shader, pos, rot, size, texture);
	}
	
	public Texture createTexture(BufferedImage image) {
		return new VLKTexture(this, image);
	}
}
