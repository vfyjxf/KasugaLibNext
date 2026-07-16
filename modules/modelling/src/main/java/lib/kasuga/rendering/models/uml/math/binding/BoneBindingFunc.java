package lib.kasuga.rendering.models.uml.math.binding;

import lib.kasuga.rendering.models.uml.math.BoneContext;
import lib.kasuga.rendering.models.uml.math.DualQuaternion;
import lib.kasuga.rendering.models.uml.math.Transform;
import lib.kasuga.rendering.models.uml.math.binding.SDEFData;
import lib.kasuga.rendering.models.uml.structure.basic.Mesh;
import lib.kasuga.rendering.models.uml.structure.basic.Vertex;
import lib.kasuga.rendering.models.uml.structure.basic.data.vertex.SDEFBoneBindingData;
import lib.kasuga.rendering.models.uml.structure.skeleton.data.BoneData;
import lib.kasuga.structure.Pair;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface BoneBindingFunc {

    Vertex apply(Vertex vertex, List<BoneContext> context);

    BoneBindingFunc IDENTITY = (vertex, context) -> vertex;

    BoneBindingFunc BDEF = (vertex, context) -> {
        if (context.isEmpty()) {
            return IDENTITY.apply(vertex, context);
        }
        Vector3f posFinal = new Vector3f();
        HashMap<Mesh, Vector3f> normalFinals = new HashMap<>();
        for (BoneContext<BoneData> boneContext : context) {
            Vector3f posLocal = new Vector3f(vertex.getPosition());
            posLocal = boneContext.invTransform().apply(posLocal);
            Vector3f posTransformed = boneContext.absTransform().apply(posLocal);
            posTransformed.mul(boneContext.weight());
            posFinal.add(posTransformed);
            for (Map.Entry<Mesh, Vector3f> meshAndNormal : vertex.getNormals().entrySet()) {
                Vector3f normalTransformed = new Vector3f(meshAndNormal.getValue());
                boneContext.invTransform().normal().transform(normalTransformed);
                boneContext.absTransform().normal().transform(normalTransformed);
                normalTransformed.mul(boneContext.weight());
                if (normalFinals.containsKey(meshAndNormal.getKey())) {
                    normalFinals.get(meshAndNormal.getKey()).add(normalTransformed);
                } else {
                    normalFinals.put(meshAndNormal.getKey(), normalTransformed);
                }
            }
        }
        normalFinals.forEach((k, v) -> v.normalize());
        return new Vertex(vertex, posFinal, normalFinals);
    };

    // Fix: SDEF 使用 C(球心)+R0/R1(轴) 的球面分解模型，3×3 基矩阵逆求解
    BoneBindingFunc SDEF = (vertex, context) -> {
        if (context.size() < 2) {
            return BDEF.apply(vertex, context);
        }

        BoneContext boneCtx0 = context.get(0);
        BoneContext boneCtx1 = context.get(1);

        // 从 binding 读取 SDEF 参数 (bone0 局部空间)
        Object bd = vertex.getBinding().getData();
        if (!(bd instanceof SDEFBoneBindingData sdefData) || sdefData.getSDEFData() == null) {
            return BDEF.apply(vertex, context);
        }
        SDEFData sdef = sdefData.getSDEFData();
        Vector3f C_b0  = new Vector3f(sdef.c());
        Vector3f R0_b0 = new Vector3f(sdef.r0());
        Vector3f R1_b0 = new Vector3f(sdef.r1());

        // bone0 local → MODEL space
        Vector3f C_m  = new Vector3f(C_b0);
        boneCtx0.bindTransform().apply(C_m);
        Vector3f R0_m = new Vector3f(R0_b0);
        boneCtx0.bindTransform().applyDirection(R0_m);
        Vector3f R1_m = new Vector3f(R1_b0);
        boneCtx0.bindTransform().applyDirection(R1_m);

        BoneContext[] ctxs = {boneCtx0, boneCtx1};
        Vector3f[] deformed = new Vector3f[2];

        for (int bi = 0; bi < 2; bi++) {
            BoneContext ctx = ctxs[bi];

            // MODEL → bone-local
            Vector3f lC  = new Vector3f(C_m);
            ctx.invTransform().apply(lC);
            Vector3f lR0 = new Vector3f(R0_m);
            ctx.invTransform().normal().transform(lR0);
            Vector3f lR1 = new Vector3f(R1_m);
            ctx.invTransform().normal().transform(lR1);
            Vector3f lR2 = new Vector3f(lR0).cross(lR1);

            // 顶点在 bone-local 空间
            Vector3f lPos = new Vector3f(vertex.getPosition());
            ctx.invTransform().apply(lPos);

            // 3×3 基矩阵逆: E=[R0 R1 R2], coeff = E⁻¹·delta
            float a=lR0.x(), b=lR1.x(), c=lR2.x();
            float d=lR0.y(), e=lR1.y(), f=lR2.y();
            float g=lR0.z(), h=lR1.z(), i=lR2.z();
            float det = a*(e*i-f*h) - b*(d*i-f*g) + c*(d*h-e*g);
            float dx=lPos.x()-lC.x(), dy=lPos.y()-lC.y(), dz=lPos.z()-lC.z();
            float d0,d1,d2;
            if (Math.abs(det) > 1e-10f) {
                float inv = 1f/det;
                d0 = inv*((e*i-f*h)*dx + (c*h-b*i)*dy + (b*f-c*e)*dz);
                d1 = inv*((f*g-d*i)*dx + (a*i-c*g)*dy + (c*d-a*f)*dz);
                d2 = inv*((d*h-e*g)*dx + (b*g-a*h)*dy + (a*e-b*d)*dz);
            } else {
                d0=(lPos.x()-lC.x())*lR0.x()+(lPos.y()-lC.y())*lR0.y()+(lPos.z()-lC.z())*lR0.z();
                d1=(lPos.x()-lC.x())*lR1.x()+(lPos.y()-lC.y())*lR1.y()+(lPos.z()-lC.z())*lR1.z();
                d2=(lPos.x()-lC.x())*lR2.x()+(lPos.y()-lC.y())*lR2.y()+(lPos.z()-lC.z())*lR2.z();
            }

            // bone-local → world (animated)
            Vector3f Cw  = new Vector3f(lC);
            ctx.absTransform().apply(Cw);
            Vector3f R0w = new Vector3f(lR0);
            ctx.absTransform().applyDirection(R0w);
            Vector3f R1w = new Vector3f(lR1);
            ctx.absTransform().applyDirection(R1w);
            Vector3f R2w = new Vector3f(lR2);
            ctx.absTransform().applyDirection(R2w);

            Vector3f p = new Vector3f(Cw);
            p.x += d0*R0w.x() + d1*R1w.x() + d2*R2w.x();
            p.y += d0*R0w.y() + d1*R1w.y() + d2*R2w.y();
            p.z += d0*R0w.z() + d1*R1w.z() + d2*R2w.z();
            deformed[bi] = p;
        }

        float w0 = boneCtx0.weight(), w1 = boneCtx1.weight();
        float s = w0 + w1;
        Vector3f posFinal = new Vector3f(deformed[0]).mul(w0/s)
                .add(new Vector3f(deformed[1]).mul(w1/s));

        HashMap<Mesh, Vector3f> normalFinals = new HashMap<>();
        for (var e : vertex.getNormals().entrySet()) {
            Vector3f n = e.getValue();
            Vector3f n0 = new Vector3f(n);
            boneCtx0.invTransform().normal().transform(n0);
            boneCtx0.absTransform().normal().transform(n0);
            Vector3f n1 = new Vector3f(n);
            boneCtx1.invTransform().normal().transform(n1);
            boneCtx1.absTransform().normal().transform(n1);
            Vector3f nf = new Vector3f(n0).mul(w0/s).add(new Vector3f(n1).mul(w1/s));
            nf.normalize();
            normalFinals.put(e.getKey(), nf);
        }
        return new Vertex(vertex, posFinal, normalFinals);
    };

    BoneBindingFunc QDEF = (vertex, context) -> {
        if (context.isEmpty()) {
            return IDENTITY.apply(vertex, context);
        }
        Vector3f posOrg = vertex.getPosition();
        List<Pair<DualQuaternion, Float>> dualQuaternions = new ArrayList<>();
        Transform skinningMatrix = new Transform();
        for (BoneContext<BoneData> boneContext : context) {
            skinningMatrix.set(boneContext.absTransform()).mul(boneContext.invTransform());
            DualQuaternion dq = skinningMatrix.toDualQuaternion();
            dualQuaternions.add(Pair.of(dq, boneContext.weight()));
        }
        DualQuaternion result = DualQuaternion.blend(dualQuaternions);
        Vector3f posFinal = result.transformPoint(new Vector3f(posOrg));
        HashMap<Mesh, Vector3f> normalFinals = new HashMap<>();
        for (Map.Entry<Mesh, Vector3f> meshAndNormal : vertex.getNormals().entrySet()) {
            Vector3f normal = new Vector3f(meshAndNormal.getValue());
            normal = result.getReal().transform(normal);
            normal.normalize();
            normalFinals.put(meshAndNormal.getKey(), normal);
        }
        return new Vertex(vertex, posFinal, normalFinals);
    };
}
