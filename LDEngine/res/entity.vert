#version 330 core

in vec2 pos;
in vec2 tex;

uniform mat4 proj;
uniform mat4 modelview;

out vec2 texCoord;

void main(void) {
	gl_Position = proj * modelview * vec4(pos.xy, 0.0, 1.0);
	texCoord = tex;
}