package code;

import static org.lwjgl.glfw.GLFW.*;

import code.engine.audio.Sound;
import code.engine.audio.SoundManager;
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
		Texture texture = render.createTexture("res/Paddle.png");
		Texture texture2 = render.createTexture("res/Ball.png");
		Renderable paddle1 = render.createRenderable(shader, new Vector2f(50, 300), 0, new Vector2f(25, 125), texture);
		Renderable paddle2 = render.createRenderable(shader, new Vector2f(750, 300), 0, new Vector2f(25, 125), texture);
		Renderable ball = render.createRenderable(shader, new Vector2f(400, 300), 0, new Vector2f(25, 25), texture2);
		
		Vector4f clearColor = new Vector4f(0.0f, 0.0f, 0.0f, 1);
		
		Sound sound = new Sound("res/test.ogg");
		
		float angle = (float)(Math.random()*2*Math.PI);
		
		Vector2f ballDir = new Vector2f((float)(400*Math.cos(angle)), (float)(400*Math.sin(angle)));
		
		double st = glfwGetTime();
		double ct = st;
		
		double dt = ct - st;
		
		int frames = 0;
		double time = 0;
		int fps = 0;
		
		while(window.isOpen()) {
			window.poll();
			
			ct = glfwGetTime();
			dt = ct - st;
			st = ct;
			
			frames = frames + 1;
			time = time + dt;
			if(time >= 1) {
				fps = frames;
				window.setTitle(title + " | FPS: " + fps);
				frames = 0;
				time = 0;
			}
			
			render.clear(clearColor);
			
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
			
			model.bind();
			shader.bind();
			
			ball.applyUniforms();
			model.draw();
			
			paddle1.applyUniforms();
			model.draw();
			
			paddle2.applyUniforms();
			model.draw();
			
			shader.unbind();
			model.unbind();
			
			render.swap();
		}
		
		SoundManager.destroy();
		
		render.destory();

		window.destroy();
		glfwTerminate();
	}
}
