package code;

import static org.lwjgl.glfw.GLFW.*;

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
	
	public static void main(String[] args) {
		glfwInit();
		
		SoundManager.init();
		
		boolean useVulkan = false;
		
		Renderer render = null;
		
		String title = "LD Game | Renderer: ";
		
		if(useVulkan) {
			title += "Vulkan";
			render = new VLKRenderer();
		} else {
			title += "OpenGL";
			render = new GLRenderer();
		}
		
		Window window = render.createWindowandContext(800, 600, title, true, false);
		
		float verts[] = {
				 0.5f, -0.5f, 1, 1,
				-0.5f,  0.5f, 0, 0,
				-0.5f, -0.5f, 0, 1,
				 
				-0.5f,  0.5f, 0, 0,
				 0.5f, -0.5f, 1, 1,
				 0.5f,  0.5f, 1, 0
		};
		
		Model model = render.createModel(verts);
		Shader shader = render.createShader("res/entity", "res/entity");
		Texture paddleTex = render.createTexture("res/Paddle.png");
		Texture ballTex = render.createTexture("res/Ball.png");
		Renderable paddle1 = render.createRenderable(model, shader, new Vector2f(50, 300), 0, new Vector2f(25, 125), paddleTex);
		Renderable paddle2 = render.createRenderable(model, shader, new Vector2f(750, 300), 0, new Vector2f(25, 125), paddleTex);
		Renderable ball = render.createRenderable(model, shader, new Vector2f(400, 300), 0, new Vector2f(25, 25), ballTex);
		
		Vector4f clearColor = new Vector4f(0.0f, 0.0f, 0.0f, 1);
		
		Sound sound = new Sound("res/test.ogg");
		
		Random rand = new Random();
		
		Vector2f ballDir = new Vector2f(
				(rand.nextInt(2)*2 - 1)*(rand.nextInt(100)+300), 
				(rand.nextInt(2)*2 - 1)*(rand.nextInt(100)+300));
		
		String scoreTextString = "0 | 0";
		
		Font arial = new Font(render, "res/arial");
		
		float textWidth = getTextWidth(arial, scoreTextString, 24);
		
		
		
		Renderable fpsText = render.createText(shader, arial, paddleTex, "FPS:0", 10, 566, 24);
		Renderable scoreText = render.createText(shader, arial, paddleTex, scoreTextString, 400-(textWidth/2.0f), 566, 24);
		
		double st = glfwGetTime();
		double ct = st;
		
		double dt = ct - st;
		
		int frames = 0;
		double time = 0;
		int fps = 0;
		
		int p1Score = 0;
		int p2Score = 0;
		
		while(window.isOpen()) {
			window.poll();
			
			ct = glfwGetTime();
			dt = ct - st;
			st = ct;
			
			frames = frames + 1;
			time = time + dt;
			if(time >= 1) {
				fps = frames;
				fpsText.destroy();
				fpsText = render.createText(shader, arial, paddleTex, "FPS:" + fps, 10, 566, 24);
				frames = 0;
				time = 0;
			}
			
			ball.pos.x += ballDir.x * dt;
			ball.pos.y += ballDir.y * dt;
			
			if(ball.pos.y > 600 - ball.size.y/2) {
				ballDir.y = -Math.abs(ballDir.y);
			}
			
			if(ball.pos.y < ball.size.y/2) {
				ballDir.y = Math.abs(ballDir.y);
			}
			
			if(isHitting(paddle1, ball)) {
				ballDir.x = Math.abs(ballDir.x);
			}
			
			if(isHitting(paddle2, ball)) {
				ballDir.x = -Math.abs(ballDir.x);
			}
			
			if(ball.pos.x < 0) {
				p2Score++;
				
				ball.pos = new Vector2f(400, 300);
				
				ballDir = new Vector2f(
						(rand.nextInt(2)*2 - 1)*(rand.nextInt(100)+300), 
						(rand.nextInt(2)*2 - 1)*(rand.nextInt(100)+300));
				
				scoreTextString = p1Score + " | " + p2Score;
				textWidth = getTextWidth(arial, scoreTextString, 24);
				scoreText = render.createText(shader, arial, paddleTex, scoreTextString, 400-(textWidth/2.0f), 566, 24);
			}
			
			if(ball.pos.x > 800) {
				p1Score++;
				
				ball.pos = new Vector2f(400, 300);
				
				ballDir = new Vector2f(
						(rand.nextInt(2)*2 - 1)*(rand.nextInt(100)+300), 
						(rand.nextInt(2)*2 - 1)*(rand.nextInt(100)+300));
				
				scoreTextString = p1Score + " | " + p2Score;
				textWidth = getTextWidth(arial, scoreTextString, 24);
				scoreText = render.createText(shader, arial, paddleTex, scoreTextString, 400-(textWidth/2.0f), 566, 24);
			}
			
			if(Input.keys[GLFW_KEY_W]) {
				paddle1.pos.y += 1200 * dt;
				
				if(paddle1.pos.y > 600 - paddle1.size.y/2) {
					paddle1.pos.y = 600 - paddle1.size.y/2;
				}
			}
			
			if(Input.keys[GLFW_KEY_S]) {
				paddle1.pos.y -= 1200 * dt;
				
				if(paddle1.pos.y < paddle1.size.y/2) {
					paddle1.pos.y = paddle1.size.y/2;
				}
			}
			
			if(Input.keys[GLFW_KEY_UP]) {
				paddle2.pos.y += 1200 * dt;
				
				if(paddle2.pos.y > 600 - paddle2.size.y/2) {
					paddle2.pos.y = 600 - paddle2.size.y/2;
				}
			}
			
			if(Input.keys[GLFW_KEY_DOWN]) {
				paddle2.pos.y -= 1200 * dt;
				
				if(paddle2.pos.y < paddle2.size.y/2) {
					paddle2.pos.y = paddle2.size.y/2;
				}
			}
			
			if(Input.keys[GLFW_KEY_SPACE]) {
				sound.play();
			}
			
			render.clear(clearColor);
			
			shader.bind();
			
			model.bind();
			
			ball.render();
			paddle1.render();
			paddle2.render();
			
			model.unbind();
			
			fpsText.model.bind();
			fpsText.render();
			fpsText.model.unbind();
			
			scoreText.model.bind();
			scoreText.render();
			scoreText.model.unbind();
			
			shader.unbind();
			
			render.swap();
		}
		
		SoundManager.destroy();
		
		render.destory();

		window.destroy();
		glfwTerminate();
	}
}
