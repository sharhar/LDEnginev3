package code.game;

import static org.lwjgl.glfw.GLFW.*;

import code.engine.graphics.Renderable;
import code.engine.graphics.Texture;
import code.engine.math.Vector2f;
import code.engine.window.Input;

public class Player {
	
	public Renderable renderable;
	public Renderable fire;
	public Vector2f prevPos = new Vector2f();
	
	public Vector2f planetPos;
	public float planetR;
	public float planetAngle;
	public float planetHeight;
	public float rotOffset = -(float)Math.PI/2;
	public float move = 1;
	public float fireTime = 0;
	
	public boolean caught = true;
	public boolean fly = false;
	public boolean space = false;
	
	public Player(Vector2f pos, Vector2f size, Texture texture, Texture fireTexture) {
		renderable = Main.renderer.createRenderable(Main.model, Main.shader, pos, 0, size, texture);
		fire = Main.renderer.createRenderable(Main.model, Main.shader, pos, 0, new Vector2f(10, 10), fireTexture);
	}
	
	public void update() {
		if(Input.keys[GLFW_KEY_A] && !fly) {
			move = -1;
		}
		
		if(Input.keys[GLFW_KEY_D] && !fly) {
			move = 1;
		}
		
		if(fly) {
			renderable.pos = renderable.pos.add(new Vector2f((float)Math.sin(planetAngle + move * (float)Math.PI/2) * 200 * Main.dt, 
															 (float)Math.cos(planetAngle + move * (float)Math.PI/2) * 200 * Main.dt));
			
			renderable.rot = rotOffset * move - planetAngle;
			
			boolean found = false;
			
			for(Planet planet : Main.planets) {
				float dx = renderable.pos.x - planet.pos.x;
				float dy = renderable.pos.y - planet.pos.y;
				float dist = (float) Math.sqrt(dx*dx + dy*dy);
				
				if(dist <= planetR + renderable.size.y /2) {
					Main.screen = 2;
					return;
				}
					
				if(dist <= planetR + planet.gravityRad + renderable.size.y /2) {
					found = true;
					
					if(space && Input.keys[GLFW_KEY_SPACE]) {
						float dxp = prevPos.x - planet.pos.x;
						float dyp = prevPos.y - planet.pos.y;
						
						float newAnglep = (float)Math.atan(dyp/dxp);
						newAnglep = (float) (dx < 0 ? Math.PI-newAnglep : -newAnglep);
						
						float newAngle = (float)Math.atan(dy/dx);
						newAngle = (float) (dx < 0 ? Math.PI-newAngle : -newAngle);
							
						setPlanet(planet, newAngle + (float)Math.PI/2, dist - planetR - renderable.size.y /2);
						
						move = Math.signum(newAngle - newAnglep);
							
						float dist2 = planetR + renderable.size.y /2;
							
						Vector2f offSet = new Vector2f((float)Math.sin(planetAngle)*dist2, (float)Math.cos(planetAngle)*dist2);
							
						renderable.pos = planetPos.add(offSet);
						renderable.rot = rotOffset * move - planetAngle;
						
						space = false;
						
						caught = false;
						
						return;
					}
				}
			}
			
			if(!found) {
				space = true;
			}
			
		} else {
			planetAngle += Main.dt * move * 2;
			
			float dist = planetR + planetHeight + renderable.size.y /2;
			
			Vector2f offSet = new Vector2f((float)Math.sin(planetAngle)*dist, (float)Math.cos(planetAngle)*dist);
			
			renderable.pos = planetPos.add(offSet);
			renderable.rot = rotOffset * move - planetAngle;
		}
		
		if(!Input.keys[GLFW_KEY_SPACE]) {
			caught = true;
		}
		
		if(renderable.pos.x > 800) {
			renderable.pos.x = 0;
		}
		
		if(renderable.pos.x < 0) {
			renderable.pos.x = 800;
		}
		
		if(renderable.pos.y > 800) {
			renderable.pos.y = 0;
		}
		
		if(renderable.pos.y < 0) {
			renderable.pos.y = 800;
		}
		
		fireTime += Main.dt;
		
		float fireDist = 15 + (float)Math.sin(fireTime*100)*1;
		
		fire.pos.x = renderable.pos.x + fireDist*(float)Math.sin(renderable.rot);
		fire.pos.y = renderable.pos.y - fireDist*(float)Math.cos(renderable.rot);

		fire.rot = renderable.rot + (float)Math.PI;
	}
	
	public void fly() {
		if(caught) {
			fly = true;
		}
	}
	
	public void setPlanet(Planet planet, float angle, float height) {
		planetPos = planet.pos;
		planetR = planet.r;
		planetAngle = angle;
		planetHeight = height;
		
		fly = false;
	}
	
	public void render() {
		renderable.render();
		fire.render();
		
		prevPos.x = renderable.pos.x;
		prevPos.y = renderable.pos.y;
	}
}
