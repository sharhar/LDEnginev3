package code.graphincs;

import code.math.Vector2f;

abstract public class Renderable {
	public Vector2f pos;
	public Vector2f size;
	public Texture texture;
	public Renderer renderer;
	public Shader shader;
	
	public Renderable(Renderer renderer, Shader shader, Vector2f pos, Vector2f size, Texture texture) {
		this.pos = pos;
		this.size = size;
		this.texture = texture;
		this.renderer = renderer;
		this.shader = shader;
		
		init();
	}
	
	abstract protected void init();
	abstract public void applyUniforms();
}
