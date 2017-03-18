package code.graphincs.gl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import static org.lwjgl.glfw.GLFW.*;

import code.graphincs.Renderer;
import code.graphincs.Shader;

public class GLShader extends Shader{
	public int program;
	public int vertShader;
	public int fragShader;
	
	public int texLoc;
	public int projLoc;
	public int modelviewLoc;
	
	public GLShader(Renderer renderer, String vertPath, String fragPath) {
		super(renderer, vertPath, fragPath);
	}
	
	private int loadShader(String file, int type) {
		StringBuilder shaderSource = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			while((line = reader.readLine()) != null) {
				shaderSource.append(line).append("\n");
			}
			reader.close();
		} catch (IOException e) {
			System.err.println("Could not read file!");
			e.printStackTrace();
			System.exit(-1);
		}
		
		int shaderID = glCreateShader(type);
		glShaderSource(shaderID, new String(shaderSource));
		glCompileShader(shaderID);
		if(glGetShaderi(shaderID, GL_COMPILE_STATUS) == GL_FALSE) {
			System.out.println(glGetShaderInfoLog(shaderID, 500));
			System.err.println("Could not compile shader!");
			System.exit(-1);
		}
		return shaderID;
	}

	protected void init() {
		vertShader = loadShader(vertPath, GL_VERTEX_SHADER);
		fragShader = loadShader(fragPath, GL_FRAGMENT_SHADER);
		program = glCreateProgram();
		glAttachShader(program, vertShader);
		glAttachShader(program, fragShader);
		
		glBindAttribLocation(program, 0, "pos");
		glBindAttribLocation(program, 1, "tex");
		
		glLinkProgram(program);
		glValidateProgram(program);
		
		texLoc = glGetUniformLocation(program, "tex");
		projLoc = glGetUniformLocation(program, "proj");
		modelviewLoc = glGetUniformLocation(program, "modelview");
		
		long window = glfwGetCurrentContext();
		
		int[] widthArr = new int[1];
		int[] heightArr = new int[1];
		glfwGetWindowSize(window, widthArr, heightArr);
		
		float r = widthArr[0];
		float l = 0;
		float t = heightArr[0];
		float b = 0;
		float f = 1;
		float n = -1;
		
		float[] proj = {
			2/(r - l), 0, 0, 0,
			0, 2/(t - b), 0, 0,
			0, 0, -2/(f - n), 0,
			-(r + l) / (r - l), -(t + b) / (t - b), -(f + n) / (f - n), 1
		};
		
		bind();
		glUniform1i(texLoc, 0);
		
		glUniformMatrix4fv(projLoc, false, proj);
		
		unbind();
	}

	public void bind() {
		glUseProgram(program);
	}

	public void unbind() {
		glUseProgram(0);
	}
}
