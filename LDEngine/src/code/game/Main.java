package code.game;

import static org.lwjgl.glfw.GLFW.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

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
	public static int screen = 0;
	public static ArrayList<Laser> lasers = new ArrayList<Laser>();
	
	public static void main(String[] args) {
		glfwInit();
		
		SoundManager.init();
		
		StringBuilder settingsSource = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new FileReader("res/settings.txt"));
			String line;
			while((line = reader.readLine()) != null) {
				settingsSource.append(line).append("\n");
			}
			reader.close();
		} catch (IOException e) {
			System.err.println("Could not read file!");
			e.printStackTrace();
			System.exit(-1);
		}
		
		String[] settings = settingsSource.toString().split("\n");
		
		boolean useVulkan = false;
		
		for(String line : settings) {
			if(line.startsWith("VK")) {
				useVulkan = Boolean.parseBoolean(line.split("=")[1]);
			}
		}
		
		String title = "Planetary Escape | Renderer: ";
		
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
		
		Random rand = new Random();
		
		BufferedImage bgImage = new BufferedImage(800, 800, BufferedImage.TYPE_INT_RGB);
		
		g = bgImage.getGraphics();
		
		for(int i = 0;i < 300;i++) {
			java.awt.Color c = new java.awt.Color(255, 255, rand.nextInt(128) + 128);
			g.setColor(c);
			int size = rand.nextInt(5) + 3;
			g.fillOval(rand.nextInt(800), rand.nextInt(800), size, size);
		}
		
		g.dispose();
		
		Texture fTex = renderer.createTexture("res/Fire.png");	
		Texture rectTex = renderer.createTexture("res/Player.png");
		Texture popoTex = renderer.createTexture("res/PoPo.png");
		Texture planetTex = renderer.createTexture("res/Moon.png");
		Texture circleTex = renderer.createTexture(planetImage);
		Texture titleTex = renderer.createTexture("res/Title.png");
		Texture bgTex = renderer.createTexture(bgImage);
		Texture laserTex = renderer.createTexture(squareImage);
		Texture startMessageTex = renderer.createTexture("res/StartText.png");
		Texture[] tutorialTexs = new Texture[8];
		tutorialTexs[0] = renderer.createTexture("res/Tutorial0.png");
		tutorialTexs[1] = renderer.createTexture("res/Tutorial1.png");
		tutorialTexs[2] = renderer.createTexture("res/Tutorial2.png");
		tutorialTexs[3] = renderer.createTexture("res/Tutorial3.png");
		tutorialTexs[4] = renderer.createTexture("res/Tutorial4.png");
		tutorialTexs[5] = renderer.createTexture("res/Tutorial5.png");
		tutorialTexs[6] = renderer.createTexture("res/Tutorial6.png");
		tutorialTexs[7] = renderer.createTexture("res/Tutorial7.png");
		Texture tutorialSkipTex = renderer.createTexture("res/TutorialSkip.png");
		
		Renderable bg = renderer.createRenderable(model, shader, new Vector2f(400, 400), 0, new Vector2f(800, 800), bgTex);
		
		Renderable tutorial = renderer.createRenderable(model, shader, new Vector2f(400, 650), 0, new Vector2f(250, 200), tutorialTexs[0]);
		
		Renderable tutorialSkip = renderer.createRenderable(model, shader, new Vector2f(400, 150), 0, new Vector2f(250, 200), tutorialSkipTex);
		
		Planet.planetTexture = planetTex;
		Planet.circleTexture = circleTex;
		
		planets = new Planet[5];
		planets[0] = new Planet(new Vector2f(150, 150), 50, 50);
		planets[1] = new Planet(new Vector2f(650, 150), 50, 50);
		planets[2] = new Planet(new Vector2f(650, 650), 50, 50);
		planets[3] = new Planet(new Vector2f(150, 650), 50, 50);
		planets[4] = new Planet(new Vector2f(400, 400), 50, 50);
		
		Vector4f clearColor = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
		
		Sound sound = new Sound("res/test.ogg");
		
		Font arial = new Font(renderer, "res/arial");
		
		Renderable fpsText = renderer.createText(shader, arial, rectTex, "FPS:0", 10, 766, 24);
		
		Renderable titleText = Main.renderer.createRenderable(Main.model, Main.shader, new Vector2f(400, 700), 0, new Vector2f(400, 50), titleTex);
		Renderable startText = Main.renderer.createRenderable(Main.model, Main.shader, new Vector2f(400, 500), 0, new Vector2f(400, 50), startMessageTex);
		
		Player player = new Player(new Vector2f(), new Vector2f(25, 25), rectTex, fTex);
		
		player.setPlanet(planets[4], 0, 10);
		
		player.update();
		
		player.caught = false;

		PoPo.popoTexture = popoTex;
		PoPo.player = player;
		PoPo.fireTexture = fTex;
		
		Laser.laserTexture = laserTex;
		
		ArrayList<PoPo> popos = new ArrayList<PoPo>();
		
		double st = glfwGetTime();
		double ct = st;
		
		double dtd = ct - st;
		
		int frames = 0;
		double time = 0;
		int fps = 0;
		
		float startMessagetime = 0;
		float tutorialTime = 0;
		int currentTutorial = 0;
		
		boolean renderTutorial = true;
		
		while(window.isOpen()) {
			window.poll();
			
			ct = glfwGetTime();
			dtd = ct - st;
			st = ct;

			dt = (float)dtd;
			
			frames = frames + 1;
			time = time + dtd;
			if(time >= 1) {
				fps = frames;
				fpsText.destroy();
				fpsText = renderer.createText(shader, arial, rectTex, "FPS:" + fps, 10, 766, 24);
				frames = 0;
				time = 0;
			}

			if(screen == 0) {
				startMessagetime += dt;
				
				float scg = (float) (Math.sin(startMessagetime*5) + 1)/4 + 0.5f;
				startText.color = new Vector4f(1, 1, 1, scg);
				
				startText.updateSettings();
				
				if(Input.keys[GLFW_KEY_SPACE]) {
					screen = 1;
				}
			} else if (screen == 1) {
				tutorialTime += dt;
				
				if(Input.keys[GLFW_KEY_K] && renderTutorial) {
					renderTutorial = false;
					tutorialTime = 10;
					currentTutorial = tutorialTexs.length + 1;
				}
				
				if(renderTutorial) {
					tutorial.color = new Vector4f(1, 1, 1, (float)(-Math.cos(tutorialTime/5 * Math.PI) + 1)/2.0f);
					
					tutorial.updateSettings();
					
					tutorialSkip.color = new Vector4f(1, 1, 1, (float)(Math.sin(tutorialTime * Math.PI) + 1)/4.0f + 0.5f);
					
					tutorialSkip.updateSettings();
				}
				
				if(tutorialTime >= 10) {
					tutorialTime = 0;
					currentTutorial++;
					
					if(currentTutorial >= tutorialTexs.length) {
						renderTutorial = false;
						popos.add(new PoPo());
					} else {
						tutorial.updateTexture(tutorialTexs[currentTutorial]);
					}
				}
				
				for(PoPo popo : popos) {
					popo.update();
				}
				
				for(Laser laser : lasers) {
					laser.update();
				}
				
				if(Input.keys[GLFW_KEY_SPACE]) {
					player.fly();
				}
				
				player.update();
			}
			
			renderer.clear(clearColor);
			
			shader.bind();
			
			model.bind();
			
			bg.render();
			
			if(screen == 0) {
				startText.render();
				titleText.render();
			} else if (screen == 1) {
				for(Planet p:planets) {
					p.render();
				}
				
				if(renderTutorial) {
					tutorial.render();	
					tutorialSkip.render();
				}
				
				for(PoPo popo : popos) {
					popo.render();
				}
				
				for(Laser laser : lasers) {
					laser.render();
				}
				
				player.render();
			}
			
			model.unbind();
			
			fpsText.model.bind();
			fpsText.render();
			fpsText.model.unbind();
			
			shader.unbind();
			
			renderer.swap();
			
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		sound.destroy();
		SoundManager.destroy();
		
		renderer.destory();

		window.destroy();
		glfwTerminate();
	}
}
