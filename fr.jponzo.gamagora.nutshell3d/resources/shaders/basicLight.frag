#version 150

uniform vec3 mat_diffuseColor;
uniform vec3 lgt_position[32];
uniform vec3 lgt_albedo[32];
uniform float lgt_intensity[32];

in vec3 Normal;
in vec4 WPosition;

out vec4 outColor;

vec4 computeColor(vec3 lgt_position, vec3 lgt_albedo, float lgt_intensity) {
	vec4 lightColor4f = vec4(lgt_albedo, 1.0);
	vec4 diffColor4f = vec4(mat_diffuseColor, 1.0);

	vec3 LightDir = normalize(lgt_position - WPosition.xyz);
	float Distance = distance(lgt_position, WPosition.xyz);

	vec3 nNormal = normalize(Normal);
	vec3 nLightDir = normalize(LightDir);
	float cosTheta = clamp(dot(nNormal, nLightDir), 0.0, 1.0);

	return lgt_intensity * diffColor4f * lightColor4f * cosTheta / (Distance * Distance);
}

void main()
{
	vec4 finalColor = vec4(0f, 0f, 0f, 0f);
	for (int i = 0; i < 32; i++) {
		finalColor += computeColor(lgt_position[i], lgt_albedo[i], lgt_intensity[i]);
	}
	outColor = finalColor;
}
