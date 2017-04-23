package code.game;

import code.engine.graphics.Renderable;
import code.engine.graphics.Texture;
import code.engine.math.Vector2f;
import code.engine.math.Vector4f;

public class Laser {
	public static Texture laserTexture;
	
	public Renderable renderable;
	
	public Laser(Vector2f pos, float angle) {
		renderable = Main.renderer.createRenderable(Main.model, Main.shader, pos, angle, new Vector2f(20, 3), laserTexture);
		
		renderable.color = new Vector4f(1, 0, 0, 1);
		
		renderable.updateSettings();
	}
	
	public void update() {
		renderable.pos = renderable.pos.add(new Vector2f(
				(float)Math.cos(renderable.rot - (float)Math.PI/2) * 300 * Main.dt, 
				(float)Math.sin(renderable.rot - (float)Math.PI/2) * 300 * Main.dt));
	}
	
	public void render() {
		renderable.render();
	}
}
