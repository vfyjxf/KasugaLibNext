package lib.kasuga.rendering.models.mc.util;

import lib.kasuga.rendering.models.uml.math.Transform;
import org.joml.Vector3f;

public class RotHelper {

    public static final Vector3f ZERO = new Vector3f(0, 0, 0);

    public static void rotation(Transform transform, Vector3f rotation) {
        if (rotation.equals(ZERO)) return;
        transform.rotate(0, 0, rotation.z, true);
        transform.rotate(0, - rotation.y, 0, true);
        transform.rotate(- rotation.x, 0, 0, true);
    }
}
