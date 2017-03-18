#version 330 core

out vec4 out_color;

uniform sampler2D tex;

in vec2 texCoord;

void main(void) {
	out_color = texture(tex, texCoord);
}