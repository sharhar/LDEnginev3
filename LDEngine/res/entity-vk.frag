#version 450 core

layout ( location = 0 ) out vec4 out_color;

layout ( set = 0, binding = 1 ) uniform sampler2D tex;

layout ( location = 0 ) in struct fragment_in {
    vec2 uv;
} IN;

void main(void) {
	vec4 tcol = texture(tex, IN.uv);
	
	if(tcol.a < 0.5) {
		discard;
	}

	out_color = tcol;
}