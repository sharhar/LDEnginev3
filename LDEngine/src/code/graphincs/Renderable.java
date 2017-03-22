package code.graphincs;

import code.math.Vector2f;

abstract public class Renderable {
	public Vector2f pos;
	public float rot;
	public Vector2f size;
	public Texture texture;
	public Renderer renderer;
	public Shader shader;
	public float[] modelview;
	
	public Renderable(Renderer renderer, Shader shader, Vector2f pos, float rot, Vector2f size, Texture texture) {
		this.pos = pos;
		this.rot = rot;
		this.size = size;
		this.texture = texture;
		this.renderer = renderer;
		this.shader = shader;
		
		this.modelview = new float[16];
		
		init();
	}
	
	public void applyUniforms() {
		float c = (float)Math.cos(Math.toRadians(rot));
		float s = (float)Math.sin(Math.toRadians(rot));
		
		modelview[0] = size.x * c;
		modelview[1] = size.x * s;
		modelview[2] = 0;
		modelview[3] = 0;
		
		modelview[4] = -size.y * s;
		modelview[5] = size.y * c;
		modelview[6] = 0;
		modelview[7] = 0;
		
		modelview[8] = 0;
		modelview[9] = 0;
		modelview[10] = 1;
		modelview[11] = 0;
		
		modelview[12] = pos.x;
		modelview[13] = pos.y;
		modelview[14] = 0;
		modelview[15] = 1;
		
		applyUniformsCustom();
	}
	
	abstract protected void init();
	abstract public void applyUniformsCustom();
}
