#version 150

uniform vec3 lgt_position;
uniform float lgt_intensity;
uniform vec3 lgt_albedo;

uniform vec3 mat_specular;
uniform vec3 mat_ambient;
uniform float mat_shininess;
uniform sampler2D mat_diffTexture;
uniform sampler2D mat_normTexture;
uniform mat4 mod_RMatrix;

in vec3 Normal;
in vec4 WPosition;
in vec2 Texcoord;

out vec4 outColor;

void main()
{
	vec4 albedo = vec4(lgt_albedo, 1.0);

	// Compute diffuse
	vec4 diffColor = texture2D(mat_diffTexture, Texcoord);

	vec3 LightDir = normalize(WPosition.xyz - lgt_position);
	float Distance = distance(lgt_position, WPosition.xyz);

	vec4 localNormal = ((texture2D(mat_normTexture, Texcoord) * 2 - 1) / 2);
	localNormal.w = 1.0;
	vec4 normal = normalize(vec4(-Normal, 1.0));
	vec3 nNormal = normalize((mod_RMatrix * normal + localNormal).xyz);
	vec3 nLightDir = normalize(LightDir);
	float diff = clamp(dot(nNormal, nLightDir), 0.0, 1.0);
	diffColor = diffColor * albedo * diff;

	//Compute ambient
	vec4 ambient = diffColor;
	vec4 ambientColor = albedo * ambient;

	//Compute specular
	vec4 specular = vec4(1, 0, 0, 1);
	vec3 ViewDir = normalize(WPosition.xyz);
	vec3 relfetcDir = reflect(-nLightDir, nNormal);
	float spec = pow(clamp(dot(relfetcDir, relfetcDir), 0.0, 1.0), 10);
	vec4 specColor = specular * albedo * spec;

	outColor = lgt_intensity * (ambientColor + specColor + diffColor) / (Distance * Distance);
}
