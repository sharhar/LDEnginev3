package code.engine.graphics.gl;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import code.engine.graphics.Renderer;
import code.engine.graphics.Shader;
import code.engine.graphics.Texture;

public class GLTexture extends Texture{

	public int tex;
	
	public GLTexture(Renderer renderer, String path) {
		super(renderer, path);
	}
	
	public GLTexture(Renderer renderer, BufferedImage img) {
		super(renderer, img);
	}

	protected void init() {
		tex = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, tex);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		IntBuffer buffer = ByteBuffer.allocateDirect(pixels.length << 2).order(ByteOrder.nativeOrder()).asIntBuffer();
		buffer.put(pixels).flip();
		glTexImage2D(GL_TEXTURE_2D,0,GL_RGBA,width,height,0,GL_RGBA,GL_UNSIGNED_BYTE, buffer);
		glBindTexture(GL_TEXTURE_2D, 0);
	}

	public void use(Shader shader) {
		
	}

	public void bind() {
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, tex);
	}

	public void unbind() {
		glBindTexture(GL_TEXTURE_2D, 0);
	}

}
