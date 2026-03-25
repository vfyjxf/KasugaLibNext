#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform sampler2D Sampler2;
uniform sampler2D NormalMap;
uniform sampler2D MetallicRoughnessMap;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

uniform vec3 Light0_Direction;
uniform vec3 Light1_Direction;
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

in float vertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec4 overlayColor;
in vec2 texCoord0;
in vec3 viewPos;
in vec3 viewNormal;
in mat3 TBN;

out vec4 fragColor;

float DistributionGGX(float NdotH, float roughness) {
    float a = roughness * roughness;
    float a2 = a * a;
    float NdotH2 = NdotH * NdotH;

    float nom   = a2;
    float denom = (NdotH2 * (a2 - 1.0) + 1.0);
    denom = 3.14159265 * denom * denom;
    return nom / denom;
}

float GeometrySchlickGGX(float NdotV, float roughness) {
    float r = (roughness + 1.0);
    float k = (r * r) / 8.0;

    float nom   = NdotV;
    float denom = NdotV * (1.0 - k) + k;
    return nom / denom;
}

float GeometrySmith(float NdotV, float NdotL, float roughness) {
    float ggx2 = GeometrySchlickGGX(NdotV, roughness);
    float ggx1 = GeometrySchlickGGX(NdotL, roughness);
    return ggx1 * ggx2;
}

vec3 FresnelSchlick(vec3 F0, float VdotH) {
    return F0 + (1.0 - F0) * pow(1.0 - VdotH, 5.0);
}

void main() {
//    vec3 albedo = texture(Sampler0, texCoord0).rgb;
//
//    mat3 normalMat = mat3(ModelViewMat);
//    vec3 L0 = normalize(normalMat * Light0_Direction);
//    vec3 L1 = normalize(normalMat * Light1_Direction);
//
//    vec3 normalTexture = texture(NormalMap, texCoord0).rgb * 2.0 - 1.0;
//    vec3 V = normalize(- viewPos);
//    vec3 N = normalize(TBN * normalTexture);
//    vec3 color = vec3(0.0);
//
//    float NdotL0 = max(dot(N, L0), 0.0);
//    float NdotL1 = max(dot(N, L1), 0.0);
//    color += albedo * NdotL0;
//    color += albedo * NdotL1;
//
//    color += albedo * 0.1;  // ambient
//
//    color *= vertexColor.rgb;
//    color *= lightMapColor.rgb * ColorModulator.rgb;
//    fragColor = vec4(color, 1.0);

//        fragColor = overlayColor;  // 疑似有问题, 全红

//    vec3 metallic = texture(MetallicRoughnessMap,  texCoord0).rgb;  // 没问题, 非全黑
//    fragColor = vec4(metallic, 1.0);

//    vec4 albedo = texture(Sampler0, texCoord0);
//    vec3 mra = texture(MetallicRoughnessMap, texCoord0).rgb;
//    float metallic = mra.r;
//    metallic = clamp(metallic, 0.0, 1.0);
//    vec3 F0 = mix(vec3(0.04), albedo.rgb, metallic);
//    vec3 kD = (1.0 - metallic) * (1.0 - F0);
//    vec3 diffuse = kD * albedo.rgb; // / 3.14159265;
//    fragColor = vec4(diffuse, 1.0);
//
//    metallic = clamp(metallic, 0.0, 1.0);
//    vec3 F0 = mix(vec3(0.04), albedo.rgb, metallic);
//    vec3 kD = (1.0 - metallic) * (1.0 - F0);
//    vec3 diffuse = kD * albedo.rgb / 3.14159265;
//    fragColor = vec4(diffuse, 1.0);
//
//    vec4 color = texture(Sampler0, texCoord0);
//    fragColor = color;

    vec4 albedo = texture(Sampler0, texCoord0);
    vec3 normalTexture = texture(NormalMap, texCoord0).rgb * 2.0 - 1.0;
    vec4 mra = texture(MetallicRoughnessMap, texCoord0);
    float metallic = mra.r;
    float roughness = mra.g;
    float ao = mra.b;

    metallic = clamp(metallic, 0.0, 1.0);
    roughness = clamp(roughness, 0.0, 1.0);
    ao = clamp(ao, 0.0, 1.0);

    vec3 N = normalize(TBN * normalTexture);
    vec3 V = normalize(-viewPos);

    vec3 F0 = mix(vec3(0.04), albedo.rgb, metallic);
    vec3 kD = (1.0 - metallic) * (1.0 - F0);
    vec3 diffuse = kD * albedo.rgb / 3.14159265;

    mat3 viewMatrix = mat3(ModelViewMat);
    vec3 L0 = normalize(viewMatrix * Light0_Direction);
    vec3 L1 = normalize(viewMatrix * Light1_Direction);

    vec3 lightColor = vec3(1.0);
    vec3 color = vec3(0.0);

    {
        vec3 L = L0;
        vec3 H = normalize(V + L);
        float NdotL = max(dot(N, L), 0.0);
        float NdotV = max(dot(N, V), 0.0);
        float VdotH = max(dot(V, H), 0.0);
        float NdotH = max(dot(N, H), 0.0);

        float D = DistributionGGX(NdotH, roughness);
        float G = GeometrySmith(NdotV, NdotL, roughness);
        vec3 F = FresnelSchlick(F0, VdotH);
        vec3 specular = (D * G * F) / (4.0 * NdotV * NdotL + 0.001);
        color += (diffuse + specular) * lightColor * NdotL;
    }
    {
        vec3 L = L1;
        vec3 H = normalize(V + L);
        float NdotL = max(dot(N, L), 0.0);
        float NdotV = max(dot(N, V), 0.0);
        float VdotH = max(dot(V, H), 0.0);
        float NdotH = max(dot(N, H), 0.0);

        float D = DistributionGGX(NdotH, roughness);
        float G = GeometrySmith(NdotV, NdotL, roughness);
        vec3 F = FresnelSchlick(F0, VdotH);
        vec3 specular = (D * G * F) / (4.0 * NdotV * NdotL + 0.001);
        color += (diffuse + specular) * lightColor * NdotL;
    }

    vec3 ambient = vec3(0.75) * albedo.rgb * ao;
    color += ambient;
    color *= vertexColor.rgb;
//    color = mix(overlayColor.rgb, color, overlayColor.a);
    color *= lightMapColor.rgb * ColorModulator.rgb;
    float alpha = albedo.a * ColorModulator.a;
    vec4 finalColor = vec4(color, alpha);

    fragColor = linear_fog(finalColor, vertexDistance, FogStart, FogEnd, FogColor);
}