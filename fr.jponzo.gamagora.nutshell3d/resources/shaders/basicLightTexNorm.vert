#version 150

in vec3 position;
in vec3 normal;
in vec2 texcoord;

out vec2 Texcoord;
out vec4 WPosition;
out vec3 Normal;

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
	
	vec4 viewPosition = cam_projMatrix * cam_viewMatrix * mod_WMatrix * vec4(position, 1.0);
	gl_Position = viewPosition;

	//Compute LightDir and Distance in world base
	WPosition = vec4(position, 1.0) * mod_WMatrix;

	//Pass UV & Normal
	Texcoord = texcoord;
	Normal = normal;
}
