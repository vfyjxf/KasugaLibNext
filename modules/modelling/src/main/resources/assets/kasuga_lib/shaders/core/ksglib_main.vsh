#version 150

#moj_import <light.glsl>
#moj_import <fog.glsl>

in vec3 Position;
in vec4 Color;
in vec3 Normal;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec4 Tangent;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;
uniform sampler2D Sampler1;
uniform sampler2D Sampler2;
uniform mat4 ModelPoseMat;
uniform mat3 ModelNormalMat;

uniform vec3 Light0_Direction;
uniform vec3 Light1_Direction;

out float vertexDistance;
out vec4 vertexColor;
out vec4 lightMapColor;
out vec4 overlayColor;
out vec2 texCoord0;
out vec3 viewPos;
out vec3 viewNormal;
out mat3 TBN;

void main() {
    vec4 posWorld = (ModelPoseMat * vec4(Position, 1.0));
    vertexColor = Color;
    lightMapColor = texelFetch(Sampler2, UV2 / 16, 0);
    overlayColor = texelFetch(Sampler1, UV1, 0);

    texCoord0 = UV0;
    vec4 viewPos4 = ModelViewMat * posWorld;
    viewPos = viewPos4.xyz;
    vertexDistance = fog_distance(viewPos, FogShape);
    mat3 normalMatrix = mat3(ModelViewMat) * ModelNormalMat;
    viewNormal = normalize(normalMatrix * Normal);

    vec3 T = normalize(normalMatrix * Tangent.xyz);
    vec3 B = cross(viewNormal, T) * Tangent.w;
    TBN = mat3(T, B, viewNormal);

    gl_Position = ProjMat * viewPos4;
}