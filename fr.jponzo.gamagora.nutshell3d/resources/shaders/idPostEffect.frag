#version 150

uniform sampler2D pst_screenTexture;

in vec2 Texcoord;

out vec4 outColor;

void main()
{
	vec4 diffColor = texture2D(pst_screenTexture, Texcoord);
    outColor = diffColor;
}
