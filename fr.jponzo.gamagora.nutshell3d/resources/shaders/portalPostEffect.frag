#version 150

uniform sampler2D pst_screenTexture;
uniform vec3 mat_filter;

in vec2 Texcoord;

out vec4 outColor;

void main()
{
	vec4 diffColor = texture2D(pst_screenTexture, Texcoord);
    outColor = diffColor * vec4(mat_filter, 1.0);
}
