#version 150

in vec3 position;

uniform mat4 cam_projMatrix;
uniform mat4 cam_viewMatrix;
uniform mat4 mod_TMatrix;
uniform mat4 mod_RMatrix;
uniform mat4 mod_SMatrix;

void main()
{
	//Compute cam position
	//mat4 mod_WMatrix = mod_SMatrix * mod_RMatrix * mod_TMatrix;
	mat4 mod_WMatrix = mod_TMatrix * mod_RMatrix * mod_SMatrix;
	
	//vec4 viewPosition = vec4(position, 1.0) * mod_WMatrix * cam_viewMatrix * cam_projMatrix;
	vec4 viewPosition = cam_projMatrix * cam_viewMatrix * mod_WMatrix * vec4(position, 1.0);
	
    gl_Position = viewPosition;
}