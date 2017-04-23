package code.game;

import java.util.Random;

import code.engine.graphics.Renderable;
import code.engine.graphics.Texture;
import code.engine.math.Vector2f;

public class PoPo {
	public static Texture popoTexture;
	public static Player player;
	public static Texture fireTexture;
	
	public Renderable ship;
	public Renderable fire;
	public float fireTime = 0;
	
	public PoPo() {
		Random rand = new Random();
		
		Vector2f pos = new Vector2f();
		
		int side = rand.nextInt(4);
		
		if(side == 0) {
			pos.x = 0;
			pos.y = rand.nextFloat()*800;
		} else if(side == 1) {
			pos.x = 800;
			pos.y = rand.nextFloat()*800;
		} else if(side == 2) {
			pos.x = rand.nextFloat()*800;
			pos.y = 0;
		} else if(side == 3) {
			pos.x = rand.nextFloat()*800;
			pos.y = 800;
		}
		
		ship = Main.renderer.createRenderable(Main.model, Main.shader, pos, 0, new Vector2f(25, 25), popoTexture);
		fire = Main.renderer.createRenderable(Main.model, Main.shader, pos, 0, new Vector2f(10, 10), fireTexture);
	}
	
	public void update() {
		float dx = ship.pos.x - player.renderable.pos.x;
		float dy = ship.pos.y - player.renderable.pos.y;
		
		float newAngle = (float)Math.atan(dy/dx);
		newAngle = (float) (dx < 0 ? Math.PI-newAngle : -newAngle);
		
		ship.rot = -newAngle + (float)Math.PI/2;
		
		ship.pos = ship.pos.add(new Vector2f((float)-Math.cos(-newAngle) * 100 * Main.dt, 
				 							 (float)-Math.sin(-newAngle) * 100 * Main.dt));
		
		fireTime += Main.dt;
		
		float fireDist = 15 + (float)Math.sin(fireTime*100)*1;
		
		fire.pos.x = ship.pos.x + fireDist*(float)Math.sin(ship.rot);
		fire.pos.y = ship.pos.y - fireDist*(float)Math.cos(ship.rot);

		fire.rot = ship.rot + (float)Math.PI;
		
		if(Main.isHitting(ship, player.renderable)) {
			Main.screen = 2;
		}
	}
	
	public void render() {
		ship.render();
		fire.render();
	}
}
