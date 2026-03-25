package lib.kasuga.rendering.models.mc.util;

public enum Direction {

    NORTH, SOUTH, EAST, WEST, UP, DOWN;

    @Override
    public String toString() {
        return switch (this) {
            case UP -> "up";
            case DOWN -> "down";
            case NORTH -> "north";
            case SOUTH -> "south";
            case EAST -> "east";
            case WEST -> "west";
        };
    }

    public static Direction fromString(String str) {
        for (Direction dir : Direction.values()) {
            if (dir.toString().equalsIgnoreCase(str)) {
                return dir;
            }
        }
        throw new IllegalArgumentException("No enum constant for string: " + str);
    }
}
