#version 150

uniform vec3 lightColor;
uniform float lightIntensity;
uniform sampler2D mat_diffTexture;

in vec3 Normal;
in vec3 LightDir;
in float Distance;
in vec2 Texcoord;

out vec4 outColor;

void main()
{
	vec4 lightColor = vec4(lightColor, 1.0);
	vec4 diffColor = texture2D(mat_diffTexture, Texcoord);

	vec3 nNormal = normalize(Normal);
	vec3 nLightDir = normalize(LightDir);
	float cosTheta = clamp(dot(nNormal, nLightDir), 0.0, 1.0);

	outColor = lightIntensity * diffColor * lightColor * cosTheta / (Distance * Distance);
}
