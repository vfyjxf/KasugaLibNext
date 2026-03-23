package lib.kasuga.rendering.models.uml.math.binding;

import org.joml.Vector3f;

public record SDEFData(Vector3f r0, Vector3f r1) {
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof SDEFData(Vector3f r2, Vector3f r3))) return false;
        return r0.equals(r2) && r1.equals(r3);
    }
}
