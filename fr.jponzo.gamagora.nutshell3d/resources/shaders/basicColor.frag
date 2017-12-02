#version 150

uniform vec3 mat_color;

out vec4 outColor;

void main()
{
    outColor = vec4(mat_color, 1.0);
}