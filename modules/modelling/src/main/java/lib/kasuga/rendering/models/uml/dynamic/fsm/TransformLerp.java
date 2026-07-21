package lib.kasuga.rendering.models.uml.dynamic.fsm;

import lib.kasuga.rendering.models.uml.math.Transform;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Interpolates two {@link Transform}s by TRS decomposition (translation + scale linear, rotation via
 * quaternion slerp). {@link Transform} has no native slerp, so this is the one utility providing it.
 */
public final class TransformLerp {

    private TransformLerp() {}

    public static void lerp(Transform from, Transform to, float alpha, Transform dest) {
        if (alpha <= 0f) {
            dest.set(from);
            return;
        }
        if (alpha >= 1f) {
            dest.set(to);
            return;
        }

        Matrix4f mf = from.transform();
        Matrix4f mt = to.transform();

        Vector3f t = mf.getTranslation(new Vector3f()).lerp(mt.getTranslation(new Vector3f()), alpha, new Vector3f());
        Quaternionf r = mf.getNormalizedRotation(new Quaternionf()).slerp(mt.getNormalizedRotation(new Quaternionf()), alpha, new Quaternionf());
        Vector3f s = scaleOf(mf).lerp(scaleOf(mt), alpha, new Vector3f());

        dest.setIdentity();
        dest.translate(t);
        dest.mul(r);
        dest.scale(s.x(), s.y(), s.z());
    }

    private static Vector3f scaleOf(Matrix4f m) {
        return new Vector3f(len(m.m00(), m.m10(), m.m20()), len(m.m01(), m.m11(), m.m21()), len(m.m02(), m.m12(), m.m22()));
    }

    private static float len(float x, float y, float z) {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }
}
