#version 330 core

out vec4 out_color;

uniform sampler2D tex;

in vec2 texCoord;

void main(void) {
	vec4 tcol = texture(tex, texCoord);
	
	if(tcol.a < 0.5) {
		discard;
	}
	
	out_color = tcol;
}