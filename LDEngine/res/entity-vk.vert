#version 450 core

layout ( location = 0 ) in vec2 pos;
layout ( location = 1 ) in vec2 uv;

layout( std140, binding = 0 ) uniform UniformBufferObject {
	mat4 view;
	mat4 proj;
} UBO;

layout( location = 0 ) out struct vert_out {
    vec2 uv;
} OUT;

void main(void) {
	gl_Position = UBO.proj * UBO.view * vec4(pos.xy, 0.0, 1.0);
	OUT.uv = uv;
}