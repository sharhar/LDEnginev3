package code.engine.graphics;

abstract public class Model {
	public float[] data;
	protected Renderer renderer;
	
	public Model(Renderer renderer, float[] data) {
		this.data = data;
		this.renderer = renderer;
		
		init();
	}
	
	abstract protected void init();
	abstract public void bind();
	abstract public void unbind();
	abstract public void draw();
}
