package code.game;

import static org.lwjgl.glfw.GLFW.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import code.engine.audio.Sound;
import code.engine.audio.SoundManager;
import code.engine.graphics.Font;
import code.engine.graphics.Model;
import code.engine.graphics.Renderable;
import code.engine.graphics.Renderer;
import code.engine.graphics.Shader;
import code.engine.graphics.Texture;
import code.engine.graphics.gl.GLRenderer;
import code.engine.graphics.vk.VLKRenderer;
import code.engine.math.Vector2f;
import code.engine.math.Vector4f;
import code.engine.window.Input;
import code.engine.window.Window;

public class Main {
	
	public static boolean isHitting(Renderable paddle, Renderable ball) {
		Vector2f p1 = new Vector2f(paddle.pos.x - paddle.size.x/2, paddle.pos.y - paddle.size.y/2);
		Vector2f p2 = new Vector2f(ball.pos.x - ball.size.x/2, ball.pos.y - ball.size.y/2);
		Vector2f s1 = paddle.size;
		Vector2f s2 = ball.size;
		
		if(p1.x + s1.x< p2.x || p2.x + s2.x < p1.x) {
			return false;
		}
		
		if(p1.y + s1.y < p2.y || p2.y + s2.y < p1.y) {
			return false;
		}
		
		return true;
	}
	
	public static float getTextWidth(Font font, String text, float size) {
		float result = 0;
		
		for (int i = 0; i < text.length(); i++) {
			int chid = (int)text.charAt(i);

			if(chid != 32) {
				float sc = size / font.fontChars[chid].height;
				
				result += font.fontChars[chid].width*sc + font.fontChars[chid].xadvance*sc/2;
			} else {
				result += size;
			}
		}
		
		return result;
	}
	
	public static Renderer renderer;
	public static Model model;
	public static Shader shader;
	public static float dt;
	public static Planet[] planets;
	
	public static void main(String[] args) {
		glfwInit();
		
		SoundManager.init();
		
		boolean useVulkan = true;
		
		String title = "LD Game | Renderer: ";
		
		if(useVulkan) {
			title += "Vulkan";
			renderer = new VLKRenderer();
		} else {
			title += "OpenGL";
			renderer = new GLRenderer();
		}
		
		Window window = renderer.createWindowandContext(800, 800, title, true, false);
		
		float verts[] = {
				 0.5f, -0.5f, 1, 1,
				-0.5f,  0.5f, 0, 0,
				-0.5f, -0.5f, 0, 1,
				 
				-0.5f,  0.5f, 0, 0,
				 0.5f, -0.5f, 1, 1,
				 0.5f,  0.5f, 1, 0
		};
		
		model = renderer.createModel(verts);
		shader = renderer.createShader("res/entity", "res/entity");
		
		BufferedImage planetImage = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_ARGB);
		
		Graphics g = planetImage.getGraphics();
		
		g.setColor(new Color(0x00000000, true));
		g.fillRect(0, 0, 1024, 1024);
		g.setColor(new Color(0xffffffff, true));
		g.fillOval(0, 0, 1024, 1024);
		
		g.dispose();
		
		BufferedImage squareImage = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_ARGB);
		
		g = squareImage.getGraphics();
		
		g.setColor(new Color(0xffffffff, true));
		g.fillRect(0, 0, 1024, 1024);
		
		g.dispose();
		
		Texture fTex = renderer.createTexture("res/Fire.png");	
		Texture rectTex = renderer.createTexture("res/Player.png");		
		Texture planetTex = renderer.createTexture("res/Moon.png");
		Texture circleTex = renderer.createTexture(planetImage);
		
		Planet.planetTexture = planetTex;
		Planet.circleTexture = circleTex;
		
		planets = new Planet[5];
		planets[0] = new Planet(new Vector2f(100, 100), 50, 50);
		planets[1] = new Planet(new Vector2f(700, 100), 50, 50);
		planets[2] = new Planet(new Vector2f(700, 700), 50, 50);
		planets[3] = new Planet(new Vector2f(100, 700), 50, 50);
		planets[4] = new Planet(new Vector2f(400, 400), 50, 50);
		
		Vector4f clearColor = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
		
		Sound sound = new Sound("res/test.ogg");
		
		Font arial = new Font(renderer, "res/arial");
		
		Renderable fpsText = renderer.createText(shader, arial, rectTex, "FPS:0", 10, 566, 24);
		
		Player player = new Player(new Vector2f(), new Vector2f(25, 25), rectTex, fTex);
		
		player.setPlanet(planets[4], 0, 10);
		
		double st = glfwGetTime();
		double ct = st;
		
		double dtd = ct - st;
		
		int frames = 0;
		double time = 0;
		int fps = 0;
		
		while(window.isOpen()) {
			window.poll();
			
			ct = glfwGetTime();
			dtd = ct - st;
			dt = (float)dtd;
			st = ct;
			
			frames = frames + 1;
			time = time + dtd;
			if(time >= 1) {
				fps = frames;
				fpsText.destroy();
				fpsText = renderer.createText(shader, arial, rectTex, "FPS:" + fps, 10, 566, 24);
				frames = 0;
				time = 0;
			}

			player.update();
			
			if(Input.keys[GLFW_KEY_SPACE]) {
				player.fly();
			}
			
			renderer.clear(clearColor);
			
			shader.bind();
			
			model.bind();
			
			for(Planet p:planets) {
				p.render();
			}
			
			player.render();
			
			model.unbind();
			
			fpsText.model.bind();
			fpsText.render();
			fpsText.model.unbind();
			
			shader.unbind();
			
			renderer.swap();
		}
		
		sound.destroy();
		SoundManager.destroy();
		
		renderer.destory();

		window.destroy();
		glfwTerminate();
	}
}
