package lib.kasuga.rendering.models.mc.source.texture.bake;

import lib.kasuga.rendering.models.uml.typo.miku_miku_dance.data.material.PmxMaterial;

import java.util.Collection;

public record PbrBakeProfile(
        float smoothness,
        int f0Code,
        float sssStrength,
        float normalStrength,
        float emissionStrength
) {
    public static final int VERSION = 1;

    public static PbrBakeProfile from(PmxMaterial material) {
        float normalizedShininess = Math.clamp(material.shininess / 128.0f, 0.0f, 1.0f);
        float smoothness = 0.18f + (float) Math.sqrt(normalizedShininess) * 0.72f;

        // PMX has no reliable metalness, SSS or emission semantics. Material
        // names are deliberately ignored: automatic baking must not depend on
        // author language or naming conventions. The PMX specular colour is
        // only used as a conservative dielectric F0 estimate; values >= 230
        // are reserved for LabPBR's predefined metals and are never generated
        // automatically.
        float specular = (material.specularColor.x + material.specularColor.y + material.specularColor.z) / 3.0f;
        int f0Code = Math.clamp(Math.round(specular * 255.0f), 4, 90);
        return new PbrBakeProfile(smoothness, f0Code, 0.0f, 0.08f, 0.0f);
    }

    public static PbrBakeProfile combine(Collection<PbrBakeProfile> profiles) {
        if (profiles.isEmpty()) throw new IllegalArgumentException("At least one PBR profile is required");
        float smoothness = 0.0f;
        float f0 = 0.0f;
        float sss = 0.0f;
        float normal = 0.0f;
        float emission = 0.0f;
        for (PbrBakeProfile profile : profiles) {
            smoothness += profile.smoothness;
            f0 += profile.f0Code;
            sss += profile.sssStrength;
            normal += profile.normalStrength;
            emission += profile.emissionStrength;
        }
        float divisor = profiles.size();
        return new PbrBakeProfile(
                smoothness / divisor,
                Math.round(f0 / divisor),
                sss / divisor,
                normal / divisor,
                emission / divisor
        );
    }

    public String cacheDescriptor() {
        return VERSION + ":" + smoothness + ":" + f0Code + ":" + sssStrength + ":" + normalStrength + ":" + emissionStrength;
    }
}
