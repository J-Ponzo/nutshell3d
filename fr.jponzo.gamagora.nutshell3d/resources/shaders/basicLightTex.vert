#version 150

in vec3 position;
in vec3 normal;
in vec2 texcoord;

out vec2 Texcoord;
out vec3 Normal;
out vec3 LightDir;
out float Distance;

uniform vec3 lightPosition;
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

	//Compute normal in world base (Rotation Only)
	vec4 worldNormal = mod_RMatrix * vec4(normal, 1.0);
	Normal = normalize(worldNormal.xyz);

	//Compute LightDir and Distance in world base
	vec4 modPosition = vec4(position, 1.0) * mod_WMatrix;
	LightDir =  normalize(lightPosition - modPosition.xyz);
	Distance = distance(lightPosition, modPosition.xyz);

	//Pass UV
	Texcoord = texcoord;
}
