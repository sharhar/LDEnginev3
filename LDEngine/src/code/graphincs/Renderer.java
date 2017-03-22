package code.graphincs;

import code.math.Vector2f;
import code.math.Vector4f;
import code.window.Window;

public abstract class Renderer {
	public long window;
	
	abstract public Window createWindowandContext(int width, int height, String title, boolean debug);
	abstract public void clear(Vector4f color);
	abstract public void swap();
	abstract public void destory();
	
	abstract public Model createModel(float[] data);
	abstract public Shader createShader(String vertPath, String fragPath);
	abstract public Texture createTexture(String path);
	abstract public Renderable createRenderable(Shader shader, Vector2f pos, Vector2f size, Texture texture);
}
