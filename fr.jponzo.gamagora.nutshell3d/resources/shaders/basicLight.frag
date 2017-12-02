#version 150

uniform vec3 mat_diffuseColor;
uniform vec3 lightColor;
uniform float lightIntensity;

in vec3 Normal;
in vec3 LightDir;
in float Distance;

out vec4 outColor;

void main()
{
	vec4 lightColor4f = vec4(lightColor, 1.0);
	vec4 diffColor4f = vec4(mat_diffuseColor, 1.0);

	vec3 nNormal = normalize(Normal);
	vec3 nLightDir = normalize(LightDir);
	float cosTheta = clamp(dot(nNormal, nLightDir), 0.0, 1.0);

	outColor = lightIntensity * diffColor4f * lightColor4f * cosTheta / (Distance * Distance);
}
