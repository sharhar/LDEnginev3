package code.game;

import code.engine.graphics.Renderable;
import code.engine.graphics.Texture;
import code.engine.math.Vector2f;

public class PoPo {
	Renderable ship;
	
	public PoPo(Vector2f pos, Vector2f size, Texture texture) {
		ship = Main.renderer.createRenderable(Main.model, Main.shader, pos, 0, size, texture);
	}
	
	public void render() {
		ship.render();
	}
}
