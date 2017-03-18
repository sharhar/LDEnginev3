package code.graphincs.vk;

import code.graphincs.Renderer;
import code.math.Vector4f;

public class VLKRenderer extends Renderer{
	VLK.VLKContext context = null;
	VLK.VLKDevice device = null;
	VLK.VLKSwapChain swapChain = null;
	
	public void init(long window) {
		context = VLK.createContext(true);
		device = VLK.createDevice(context);
		swapChain = VLK.createSwapChain(context, device, window);
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
}
