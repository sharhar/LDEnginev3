package code.graphincs.gl;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;

import code.graphincs.Renderable;
import code.graphincs.Renderer;
import code.graphincs.Shader;
import code.graphincs.Texture;
import code.math.Vector2f;

public class GLRenderable extends Renderable{
	
	public GLTexture gtx;
	public GLShader gshd;
	
	public GLRenderable(Renderer renderer, Shader shader, Vector2f pos, Vector2f size, Texture texture) {
		super(renderer, shader, pos, size, texture);
	}
	
	protected void init() {
		gtx = (GLTexture)texture;
		gshd = (GLShader)shader;
	}
	
	public void applyUniforms() {
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, gtx.tex);
		
		float[] modelview = {
			size.x, 0, 0, 0,
			0, size.y, 0, 0,
			0, 0, 1, 0,
			pos.x, pos.y, 0, 1
		};
		
		glUniformMatrix4fv(gshd.modelviewLoc, false, modelview);
	}
}
