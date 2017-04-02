#version 330 core

out vec4 out_color;

uniform sampler2D tex;

uniform vec4 color;
uniform float width;
uniform float edge;

in vec2 texCoord;

void main(void) {
	vec4 tcol = texture(tex, texCoord);
	
	float dist = 1.0 - tcol.a;
	float alpha = 1.0 - smoothstep(width, width + edge, dist);
	
	out_color = vec4(tcol.r * color.r, tcol.g * color.g, tcol.b * color.b, alpha * color.a);
}