#version 150

uniform sampler2D pst_screenTexture;
uniform vec3 mat_filter;
uniform vec3 mat_diffColor;

in vec2 Texcoord;

out vec4 outColor;

void main()
{
	vec4 diffColor = vec4(mat_diffColor, 1f);

	vec2 invTexcoord = vec2((1.0 - Texcoord.x), Texcoord.y);
	vec4 reflectColor = texture2D(pst_screenTexture, invTexcoord);
    outColor = (0.4 * diffColor + 0.6 * reflectColor) * vec4(mat_filter, 1.0);
}
