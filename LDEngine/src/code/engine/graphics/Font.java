package code.engine.graphics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Font {
	public Texture texture;
	public FontChar[] fontChars;
	
	public class FontChar {
		public int x;
		public int y;
		public int width;
		public int height;
		public int xoffset;
		public int yoffset;
		public int xadvance;
		
		public float x1tex;
		public float y1tex;
		public float x2tex;
		public float y2tex;
	}
	
	public Font(Renderer renderer, String path) {
		StringBuilder fontDescSource = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path + ".fnt"));
			String line;
			while((line = reader.readLine()) != null) {
				fontDescSource.append(line).append("\n");
			}
			reader.close();
		} catch (IOException e) {
			System.err.println("Could not read file!");
			e.printStackTrace();
			System.exit(-1);
		}
		
		String fontDesc = new String(fontDescSource);
		texture = renderer.createTexture(path + ".png");
		
		String[] lines = fontDesc.split("\n");
		
		int charNum = Integer.parseInt(lines[3].split("=")[1]);
		
		String[] charLines = new String[charNum];
		
		for(int i = 0; i < charNum;i++) {
			charLines[i] = lines[4+i];
		}
		
		fontChars = new FontChar[128];
		for(int i = 0; i < charNum;i++) {
			String[] charSplit = charLines[i].split("=");
			
			int id = Integer.parseInt(charSplit[1].split(" ")[0]);
			
			fontChars[id] = new FontChar();
			fontChars[id].x = Integer.parseInt(charSplit[2].split(" ")[0]);
			fontChars[id].y = Integer.parseInt(charSplit[3].split(" ")[0]);
			fontChars[id].width = Integer.parseInt(charSplit[4].split(" ")[0]);
			fontChars[id].height = Integer.parseInt(charSplit[5].split(" ")[0]);
			fontChars[id].xoffset = Integer.parseInt(charSplit[6].split(" ")[0]);
			fontChars[id].yoffset = Integer.parseInt(charSplit[7].split(" ")[0]);
			fontChars[id].xadvance = Integer.parseInt(charSplit[8].split(" ")[0]);
			
			fontChars[id].x1tex = ((float)fontChars[id].x)/((float)texture.width);
			fontChars[id].y1tex = ((float)fontChars[id].y)/((float)texture.height);
			
			fontChars[id].x2tex = fontChars[id].x1tex + ((float)fontChars[id].width)/((float)texture.width);
			fontChars[id].y2tex = fontChars[id].y1tex + ((float)fontChars[id].height)/((float)texture.height);
		}
		
	}
}
