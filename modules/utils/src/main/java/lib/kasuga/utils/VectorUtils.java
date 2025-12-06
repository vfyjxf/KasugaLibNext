package lib.kasuga.utils;

import net.minecraft.core.BlockPos;

public class VectorUtils {
    public static String blockPosAsUnderlineFormat(BlockPos localPos) {
        return localPos.getX() + "_" + localPos.getY() + "_" + localPos.getZ();
    }
}
