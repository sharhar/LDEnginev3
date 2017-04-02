package code.engine.graphics;

import code.engine.graphics.Font.FontChar;
import code.engine.math.Vector2f;
import code.engine.math.Vector4f;
import code.engine.window.Window;

public abstract class Renderer {
	public long window;
	public int height;
	public int width;
	
	abstract public Window createWindowandContext(int width, int height, String title, boolean debug, boolean vSync);
	abstract public void clear(Vector4f color);
	abstract public void swap();
	abstract public void destory();
	
	abstract public Model createModel(float[] data);
	abstract public Shader createShader(String vertPath, String fragPath);
	abstract public Texture createTexture(String path);
	abstract public Renderable createRenderable(Model model, Shader shader, Vector2f pos, float rot, Vector2f size, Texture texture);
	
	public Renderable createText(Shader shader, Font font, Texture texture, String text, float xOff, float yOff, float size) {
		return createText(shader, font, texture, text, xOff, yOff, size, 0.3f, 0.3f, new Vector4f(1, 1, 1, 1));
	}
	
	public Renderable createText(Shader shader, Font font, Texture texture, String text, float xOff, float yOff, float size, float width, float edge) {
		return createText(shader, font, texture, text, xOff, yOff, size, width, edge, new Vector4f(1, 1, 1, 1));
	}
	
	public Renderable createText(Shader shader, Font font, Texture texture, String text, float xOff, float yOff, float size, Vector4f color) {
		return createText(shader, font, texture, text, xOff, yOff, size, 0.3f, 0.3f, color);
	}
	
	public Renderable createText(Shader shader, Font font, Texture texture, String text, float xOff, float yOff, float size, float width, float edge, Vector4f color) {
		int tsz = text.length();
		float[] verts = new float[tsz * 4 * 6];
		
		float cursorPos = 0;
		
		for (int i = 0; i < tsz; i++) {
			int chid = (int)text.charAt(i);
			FontChar fontChar = font.fontChars[chid];

			if(chid == 32) {
				cursorPos += size;
				continue;
			}
			
			float sc = size / font.fontChars[chid].height;
			
			float x1 = xOff + cursorPos;
			float y1 = yOff + font.fontChars[chid].height*sc;
			float x2 = xOff + cursorPos + font.fontChars[chid].width*sc;
			float y2 = yOff;

			float tx1 = fontChar.x1tex;
			float ty1 = fontChar.y1tex;
			float tx2 = fontChar.x2tex;
			float ty2 = fontChar.y2tex;
			
			cursorPos += font.fontChars[chid].width*sc + font.fontChars[chid].xadvance*sc/2;

			verts[i * 24 + 0] = x1;
			verts[i * 24 + 1] = y1;

			verts[i * 24 + 2] = tx1;
			verts[i * 24 + 3] = ty1;

			verts[i * 24 + 4] = x1;
			verts[i * 24 + 5] = y2;

			verts[i * 24 + 6] = tx1;
			verts[i * 24 + 7] = ty2;

			verts[i * 24 + 8] = x2;
			verts[i * 24 + 9] = y1;

			verts[i * 24 + 10] = tx2;
			verts[i * 24 + 11] = ty1;

			verts[i * 24 + 12] = x1;
			verts[i * 24 + 13] = y2;

			verts[i * 24 + 14] = tx1;
			verts[i * 24 + 15] = ty2;

			verts[i * 24 + 16] = x2;
			verts[i * 24 + 17] = y1;

			verts[i * 24 + 18] = tx2;
			verts[i * 24 + 19] = ty1;

			verts[i * 24 + 20] = x2;
			verts[i * 24 + 21] = y2;

			verts[i * 24 + 22] = tx2;
			verts[i * 24 + 23] = ty2;
		}
		
		Model model = createModel(verts);
		
		Renderable renderable = createRenderable(model, shader, new Vector2f(0, 0), 0, new Vector2f(1, 1), font.texture);
		renderable.alphaEdge = edge;
		renderable.alphaWidth = width;
		renderable.color = color;
		renderable.updateSettings();
		
		return renderable;
	}
}
