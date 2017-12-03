#version 150

uniform sampler2D pst_screenTexture;

in vec2 Texcoord;

out vec4 outColor;

void main()
{
	vec2 invTexcoord = vec2((1.0 - Texcoord.x), Texcoord.y);
	vec4 diffColor = texture2D(pst_screenTexture, invTexcoord);
    outColor = diffColor * vec4(0.3, 0.7, 0.7, 1.0);
}
