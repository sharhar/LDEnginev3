package code.graphincs;

import code.math.Vector2f;

abstract public class Renderable {
	public Vector2f pos;
	public Vector2f size;
	public Texture texture;
	
	public Renderable(Vector2f pos, Vector2f size, Texture texture) {
		this.pos = pos;
		this.size = size;
		this.texture = texture;
	}
	
	abstract public void update(Shader shader);
}
