package dev.david.tntrun.utils;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.NumberConversions;

public class PlayerPosition {

    private final double x;
    private final int y;
    private final double z;

    public PlayerPosition(double x, int y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Block getBlock(World world, double addx, double addz) {
        return world.getBlockAt(NumberConversions.floor(x + addx), y, NumberConversions.floor(z + addz));
    }
}
