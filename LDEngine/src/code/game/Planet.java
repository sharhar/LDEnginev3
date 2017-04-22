package code.game;

import code.engine.graphics.Renderable;
import code.engine.graphics.Texture;
import code.engine.math.Vector2f;
import code.engine.math.Vector4f;

public class Planet {
	
	public static Texture planetTexture;
	public static Texture circleTexture;
	
	public Renderable renderable;
	public Renderable gravRender;
	
	public Vector2f pos;
	public float r;
	public float gravityRad;
	
	public Planet(Vector2f pos, float r, float gravityRad) {
		this.pos = pos;
		this.r = r;
		this.gravityRad = gravityRad;
		
		renderable = Main.renderer.createRenderable(Main.model, Main.shader, pos, 0, new Vector2f(r*2, r*2), planetTexture);
		gravRender = Main.renderer.createRenderable(Main.model, Main.shader, pos, 0, new Vector2f((r + gravityRad)*2, (r + gravityRad)*2), circleTexture);
		
		gravRender.color = new Vector4f(1, 1, 0, 0.25f);
		
		gravRender.updateSettings();
	}
	
	public void render() {
		gravRender.render();
		renderable.render();
	}
	
} 
