#version 150

uniform vec3 lgt_position[32];
uniform vec3 lgt_albedo[32];
uniform float lgt_intensity[32];

uniform sampler2D mat_diffTexture;
uniform sampler2D mat_normTexture;
uniform mat4 mod_RMatrix;

in vec3 Normal;
in vec4 WPosition;
in vec2 Texcoord;

out vec4 outColor;

vec4 computeColor(vec3 lgt_position, vec3 lgt_albedo, float lgt_intensity) {
	vec4 albedo = vec4(lgt_albedo, 1.0);
	vec4 diffColor = texture2D(mat_diffTexture, Texcoord);

	vec3 LightDir = normalize(WPosition.xyz - lgt_position);
	float Distance = distance(lgt_position, WPosition.xyz);

	vec4 localNormal = ((texture2D(mat_normTexture, Texcoord) * 2 - 1) / 2);
	localNormal.w = 1.0;
	vec4 normal = normalize(vec4(-Normal, 1.0));
	vec3 nNormal = normalize((mod_RMatrix * normal + localNormal).xyz);
	vec3 nLightDir = normalize(LightDir);
	float cosTheta = clamp(dot(nNormal, nLightDir), 0.0, 1.0);

	return lgt_intensity * diffColor * albedo * cosTheta / (Distance * Distance);
}

void main()
{	vec4 finalColor = vec4(0f, 0f, 0f, 0f);
	for (int i = 0; i < 32; i++) {
		finalColor += computeColor(lgt_position[i], lgt_albedo[i], lgt_intensity[i]);
	}
	outColor = finalColor;
}
