#version 150

uniform sampler2D pst_screenTexture;
uniform vec3 mat_filter;
uniform float mat_time;
uniform float mat_periode;
uniform float mat_amplitude;

in vec2 Texcoord;

out vec4 outColor;

void main()
{
	float newTexcoordY = Texcoord.y;
	float newTexcoordX = Texcoord.x + sin(newTexcoordY * mat_amplitude + mat_time * 5) / mat_periode;

	vec4 diffColor = texture(pst_screenTexture, vec2(newTexcoordX, newTexcoordY));
	outColor = diffColor * vec4(mat_filter, 1.0) * (sin(mat_time * 3) * 3 + 4);
}
