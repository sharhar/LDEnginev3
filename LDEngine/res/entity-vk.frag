#version 450 core

layout ( location = 0 ) out vec4 out_color;

layout ( set = 0, binding = 1 ) uniform sampler2D tex;

layout( std140, binding = 2 ) uniform RenderSettings {
	vec4 color;
	float width;
	float edge;
} RS;

layout ( location = 0 ) in struct fragment_in {
    vec2 uv;
} IN;

void main(void) {
	vec4 tcol = texture(tex, IN.uv);
	
	float dist = 1.0 - tcol.a;
	float alpha = 1.0 - smoothstep(RS.width, RS.width + RS.edge, dist);
	
	out_color = vec4(tcol.r * RS.color.r, tcol.g * RS.color.g, tcol.b * RS.color.b, alpha * RS.color.a);
}