package code.graphincs.gl;

import code.graphincs.Model;
import code.graphincs.Renderer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class GLModel extends Model{
	protected int vao;
	protected int vbo;
	protected int vertCount;
	
	public GLModel(Renderer renderer, float[] data) {
		super(renderer, data);
	}

	protected void init() {
		vao = glGenVertexArrays();
		glBindVertexArray(vao);
		
		vbo = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);
		
		int sizeOfFloat = Float.SIZE / Byte.SIZE;
		
		glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * sizeOfFloat, 0);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * sizeOfFloat, 2 * sizeOfFloat);
		
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
		
		vertCount = data.length/4;
	}
	
	public void bind() {
		glBindVertexArray(vao);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
	}
	
	public void unbind() {
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glBindVertexArray(0);
	}
	
	public void draw() {
		glDrawArrays(GL_TRIANGLES, 0, vertCount);
	}
}
