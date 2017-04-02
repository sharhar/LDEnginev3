package code.engine.graphics.gl;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;

import code.engine.graphics.Model;
import code.engine.graphics.Renderable;
import code.engine.graphics.Renderer;
import code.engine.graphics.Shader;
import code.engine.graphics.Texture;
import code.engine.math.Vector2f;

public class GLRenderable extends Renderable{
	
	public GLTexture gtx;
	public GLShader gshd;
	
	public GLRenderable(Renderer renderer, Model model, Shader shader, Vector2f pos, float rot, Vector2f size, Texture texture) {
		super(renderer, model, shader, pos, rot, size, texture);
	}
	
	protected void init() {
		gtx = (GLTexture)texture;
		gshd = (GLShader)shader;
	}
	
	public void applyUniforms() {
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, gtx.tex);
		glUniformMatrix4fv(gshd.modelviewLoc, false, modelview);
	}

	public void destroy() {
		
	}
}
