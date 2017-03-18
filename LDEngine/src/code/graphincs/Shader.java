package code.graphincs;

abstract public class Shader {
	protected String vertPath, fragPath;
	protected Renderer renderer;
	
	public Shader(Renderer renderer, String vertPath, String fragPath) {
		this.vertPath = vertPath;
		this.fragPath = fragPath;
		this.renderer = renderer;
		
		init();
	}
	
	abstract protected void init();
	abstract public void bind();
	abstract public void unbind();
}
