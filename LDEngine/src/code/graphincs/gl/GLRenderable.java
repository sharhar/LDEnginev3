package code.graphincs.gl;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;

import code.graphincs.Renderable;
import code.graphincs.Shader;
import code.graphincs.Texture;
import code.math.Vector2f;

public class GLRenderable extends Renderable{
	public GLRenderable(Vector2f pos, Vector2f size, Texture texture) {
		super(pos, size, texture);
	}

	public void update(Shader shader) {
		GLShader glShader = (GLShader)shader;
		GLTexture glTexture = (GLTexture)texture;
		
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, glTexture.tex);
		
		float[] modelview = {
			size.x, 0, 0, 0,
			0, size.y, 0, 0,
			0, 0, 1, 0,
			pos.x, pos.y, 0, 1
		};
		
		glUniformMatrix4fv(glShader.modelviewLoc, false, modelview);
	}
}
