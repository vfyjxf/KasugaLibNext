// created by glowingstone124 (https://github.com/glowingstone124)

#version 150

in vec3 Position;
in vec3 Normal;
in vec4 Tangent;
// BoneBindingType and BoneIndices are Type.INT in vertex format; must use integer types in GLSL
in int BoneBindingType;
in ivec4 BoneIndices;
in vec4 BoneWeights;
in vec3 sdefR0;
in vec3 sdefR1;
in vec3 sdefC;

uniform samplerBuffer ksg_BoneTransforms;

out vec3 tf_Position;

mat4 ksg_readBoneTransform(int boneIndex, int offset) {
    int base = boneIndex * 9 + offset;
    vec4 col0 = texelFetch(ksg_BoneTransforms, base);
    vec4 col1 = texelFetch(ksg_BoneTransforms, base + 1);
    vec4 col2 = texelFetch(ksg_BoneTransforms, base + 2);
    return mat4(
    vec4(col0.xyz, 0.0),
    vec4(col1.xyz, 0.0),
    vec4(col2.xyz, 0.0),
    vec4(col0.w, col1.w, col2.w, 1.0)
    );
}

mat4 ksg_readBoneAbsTransform(int boneIndex) {
    return ksg_readBoneTransform(boneIndex, 0);
}

mat4 ksg_readBoneInverseTransform(int boneIndex) {
    return ksg_readBoneTransform(boneIndex, 3);
}

mat3 ksg_readBoneNormalTransform(int boneIndex) {
    int base = boneIndex * 9 + 6;
    return mat3(
    texelFetch(ksg_BoneTransforms, base).xyz,
    texelFetch(ksg_BoneTransforms, base + 1).xyz,
    texelFetch(ksg_BoneTransforms, base + 2).xyz
    );
}

vec4 quat_mul(vec4 q1, vec4 q2) {
    return vec4(
    q1.w * q2.x + q1.x * q2.w + q1.y * q2.z - q1.z * q2.y,
    q1.w * q2.y - q1.x * q2.z + q1.y * q2.w + q1.z * q2.x,
    q1.w * q2.z + q1.x * q2.y - q1.y * q2.x + q1.z * q2.w,
    q1.w * q2.w - q1.x * q2.x - q1.y * q2.y - q1.z * q2.z
    );
}

vec4 quat_conj(vec4 q) {
    return vec4(-q.x, -q.y, -q.z, q.w);
}

vec4 quat_from_mat3(mat3 m) {
    float trace = m[0][0] + m[1][1] + m[2][2];
    vec4 q = vec4(0.0);
    if (trace > 0.0) {
        float s = sqrt(trace + 1.0) * 2.0;
        q.w = 0.25 * s;
        q.x = (m[1][2] - m[2][1]) / s;
        q.y = (m[2][0] - m[0][2]) / s;
        q.z = (m[0][1] - m[1][0]) / s;
    } else if ((m[0][0] > m[1][1]) && (m[0][0] > m[2][2])) {
        float s = sqrt(1.0 + m[0][0] - m[1][1] - m[2][2]) * 2.0;
        q.w = (m[1][2] - m[2][1]) / s;
        q.x = 0.25 * s;
        q.y = (m[1][0] + m[0][1]) / s;
        q.z = (m[2][0] + m[0][2]) / s;
    } else if (m[1][1] > m[2][2]) {
        float s = sqrt(1.0 + m[1][1] - m[0][0] - m[2][2]) * 2.0;
        q.w = (m[2][0] - m[0][2]) / s;
        q.x = (m[1][0] + m[0][1]) / s;
        q.y = 0.25 * s;
        q.z = (m[2][1] + m[1][2]) / s;
    } else {
        float s = sqrt(1.0 + m[2][2] - m[0][0] - m[1][1]) * 2.0;
        q.w = (m[0][1] - m[1][0]) / s;
        q.x = (m[2][0] + m[0][2]) / s;
        q.y = (m[2][1] + m[1][2]) / s;
        q.z = 0.25 * s;
    }
    return q;
}

vec3 quat_rotate(vec4 q, vec3 v) {
    vec4 vQuat = vec4(v, 0.0);
    vec4 qConj = vec4(-q.x, -q.y, -q.z, q.w);
    return quat_mul(quat_mul(q, vQuat), qConj).xyz;
}

// Fix (Bug-GPU-1, equivalent to pmx_bugs.md CPU Bug #8):
// BDEF normal must use the normal matrix of the composite skinning deformation
// M = T_anim * T_bind^(-1), not just N(T_anim).
// The boneNormal buffer (offset 6) stores N(T_anim), which is incorrect for skinning.
// We compute composite3x3 = mat3(absTransform) * mat3(invTransform) and
// use its inverse-transpose as the correct normal matrix.
void ksg_applyBdefSkinning(inout vec3 position, inout vec3 normal, inout vec4 tangent) {
    vec4 skinnedPosition = vec4(0.0);
    vec3 skinnedNormal = vec3(0.0);
    vec3 skinnedTangent = vec3(0.0);
    float totalWeight = 0.0;
    for (int i = 0; i < 4; i++) {
        float weight = BoneWeights[i];
        if (weight <= 0.0) {
            continue;
        }
        int boneIndex = BoneIndices[i];
        mat4 invTransform = ksg_readBoneInverseTransform(boneIndex);
        mat4 absTransform = ksg_readBoneAbsTransform(boneIndex);
        vec4 localPos = invTransform * vec4(position, 1.0);
        skinnedPosition += (absTransform * localPos) * weight;

        // Compute composite normal matrix: N(T_anim * T_bind^(-1))
        mat3 composite3x3 = mat3(absTransform) * mat3(invTransform);
        mat3 compositeNormal = transpose(inverse(composite3x3));
        skinnedNormal += (compositeNormal * normal) * weight;
        skinnedTangent += (compositeNormal * tangent.xyz) * weight;
        totalWeight += weight;
    }
    if (totalWeight > 0.0) {
        position = skinnedPosition.xyz / totalWeight;
        normal = normalize(skinnedNormal);
        tangent = vec4(normalize(skinnedTangent), tangent.w);
    }
}

// Fix (Bug-GPU-2, equivalent to pmx_bugs.md CPU Bug #7):
// Standard DQBS (Dual Quaternion Blend Skinning) formula:
//   v' = DQ_blend(w_i, DQ(T_anim_i * T_bind_i^(-1))) · v
// The DQ must be built from the composite skinning matrix M = T_anim * T_bind^(-1),
// NOT from T_anim alone.
// The previous code built DQ from absTransform only and blended local-space positions
// separately, which does not produce correct results even at bind pose.
void ksg_applyQdefSkinning(inout vec3 position, inout vec3 normal, inout vec4 tangent) {
    vec4 blend_qr = vec4(0.0);
    vec4 blend_qd = vec4(0.0);
    float totalWeight = 0.0;
    for (int i = 0; i < 4; i++) {
        float weight = BoneWeights[i];
        if (weight <= 0.0) continue;
        int boneIndex = BoneIndices[i];
        mat4 invTransform = ksg_readBoneInverseTransform(boneIndex);
        mat4 absTransform = ksg_readBoneAbsTransform(boneIndex);
        // Build skinning composite M = T_anim * T_bind^(-1)
        mat4 composite = absTransform * invTransform;
        vec4 qr = quat_from_mat3(mat3(composite));
        vec3 t = composite[3].xyz;
        vec4 qd = 0.5 * quat_mul(vec4(t, 0.0), qr);
        if (dot(blend_qr, qr) < 0.0) {
            weight = -weight;
        }
        blend_qr += qr * weight;
        blend_qd += qd * weight;
        totalWeight += weight;
    }
    if (totalWeight <= 0.0) {
        return;
    }
    blend_qr = normalize(blend_qr);
    blend_qd -= dot(blend_qr, blend_qd) * blend_qr;
    // Apply blended DQ directly to model-space position
    vec3 rotatedPos = quat_rotate(blend_qr, position);
    vec4 trans4 = 2.0 * quat_mul(blend_qd, quat_conj(blend_qr));
    position = rotatedPos + trans4.xyz;
    normal = normalize(quat_rotate(blend_qr, normal));
    tangent.xyz = normalize(quat_rotate(blend_qr, tangent.xyz));
}

void ksg_applySdefSkinning(inout vec3 position, inout vec3 normal, inout vec4 tangent) {
    vec4 skinnedPos = vec4(0.0);
    vec3 skinnedNormal = vec3(0.0);
    vec3 skinnedTangent = vec3(0.0);
    float totalWeight = 0.0;
    for (int i = 0; i < 4; i++) {
        float weight = BoneWeights[i];
        if (weight <= 0.0) continue;
        int boneIndex = BoneIndices[i];
        mat4 invBind = ksg_readBoneInverseTransform(boneIndex);
        mat4 anim = ksg_readBoneAbsTransform(boneIndex);
        mat3 invRotBind = mat3(invBind);
        vec3 localPos = (invBind * vec4(position, 1.0)).xyz;
        vec3 localC = (invBind * vec4(sdefC, 1.0)).xyz;
        vec3 localR0 = invRotBind * sdefR0;
        vec3 localR1 = invRotBind * sdefR1;
        vec3 localR2 = cross(localR0, localR1);
        vec3 delta = localPos - localC;
        float d0 = dot(delta, localR0);
        float d1 = dot(delta, localR1);
        float d2 = dot(delta, localR2);
        vec3 Cw = (anim * vec4(localC, 1.0)).xyz;
        vec3 R0w = (anim * vec4(localR0, 0.0)).xyz;
        vec3 R1w = (anim * vec4(localR1, 0.0)).xyz;
        vec3 R2w = (anim * vec4(localR2, 0.0)).xyz;
        vec3 deformedPos = Cw + d0 * R0w + d1 * R1w + d2 * R2w;
        skinnedPos += vec4(deformedPos, 1.0) * weight;

        // Fix (Bug-GPU-3, same as BDEF normal fix):
        // Use composite normal matrix N(T_anim * T_bind^(-1)) instead of N(T_anim)
        mat3 composite3x3 = mat3(anim) * mat3(invBind);
        mat3 compositeNormal = transpose(inverse(composite3x3));
        skinnedNormal += (compositeNormal * normal) * weight;
        skinnedTangent += (compositeNormal * tangent.xyz) * weight;
        totalWeight += weight;
    }
    if (totalWeight > 0.0) {
        position = skinnedPos.xyz / totalWeight;
        normal = normalize(skinnedNormal);
        tangent = vec4(normalize(skinnedTangent), tangent.w);
    }
}

void ksg_applyGpuSkinning(inout vec3 position, inout vec3 normal, inout vec4 tangent) {
    if (BoneBindingType == 2) {
        ksg_applyQdefSkinning(position, normal, tangent);
        return;
    } else if (BoneBindingType == 1) {
        ksg_applySdefSkinning(position, normal, tangent);
        return;
    }
    ksg_applyBdefSkinning(position, normal, tangent);
}

void main() {
    vec3 skinnedPosition = Position;
    vec3 skinnedNormal = Normal;
    vec4 skinnedTangent = Tangent;
    ksg_applyGpuSkinning(skinnedPosition, skinnedNormal, skinnedTangent);
    tf_Position = skinnedPosition;
    gl_Position = vec4(0.0);
}