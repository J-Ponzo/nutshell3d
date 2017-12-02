#version 150

uniform vec3 lightPosition;
uniform vec3 lightColor;
uniform float lightIntensity;
uniform sampler2D mat_diffTexture;
uniform sampler2D mat_normTexture;
uniform mat4 mod_RMatrix;

in vec3 Normal;
in vec4 WPosition;
in vec2 Texcoord;

out vec4 outColor;

void main()
{
	vec4 lightColor = vec4(lightColor, 1.0);
	vec4 diffColor = texture2D(mat_diffTexture, Texcoord);

	vec3 LightDir = normalize(lightPosition - WPosition.xyz);
	float Distance = distance(lightPosition, WPosition.xyz);

	vec4 localNormal = texture2D(mat_normTexture, Texcoord);
	localNormal.w = 1.0;
	vec4 normal = vec4(Normal, 1.0);
	vec3 nNormal = normalize((mod_RMatrix * normal + localNormal).xyz);
	vec3 nLightDir = normalize(LightDir);
	float cosTheta = clamp(dot(nNormal, nLightDir), 0.0, 1.0);

	outColor = lightIntensity * diffColor * lightColor * cosTheta / (Distance * Distance);
}
