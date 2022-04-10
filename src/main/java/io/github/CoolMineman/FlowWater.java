package io.github.CoolMineman;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidFillable;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.WaterFluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

public class FlowWater {
    private FlowWater() { }

    public static void flowwater(WorldAccess world, BlockPos fluidPos, FluidState state) {
        if (world.getBlockState(fluidPos).getBlock() instanceof FluidFillable) {
            return;
        }
        if ((world.getBlockState(fluidPos.down()).canBucketPlace(Fluids.WATER)) && (getWaterLevel(fluidPos.down(), world) != 8)) {
            int centerlevel = getWaterLevel(fluidPos, world);
            world.setBlockState(fluidPos, Blocks.AIR.getDefaultState(), 11);
            addWater(centerlevel, fluidPos.down(), world);
        } else {
            ArrayList<BlockPos> blocks = new ArrayList<>(4);
            for (Direction dir : Direction.Type.HORIZONTAL) {
                blocks.add(fluidPos.offset(dir));
            }
            blocks.removeIf(pos -> !world.getBlockState(pos).canBucketPlace(Fluids.WATER));
            Collections.shuffle(blocks);
            equalizeWater(blocks, fluidPos, world);
        }
    }

    public static int getWaterLevel(BlockPos pos, WorldAccess world) {
        BlockState blockstate = world.getBlockState(pos);
        FluidState fluidstate = blockstate.getFluidState();
        int waterlevel = 0;
        if (fluidstate.getFluid() instanceof WaterFluid.Still){
            waterlevel = 8;
        } else if (fluidstate.getFluid() instanceof WaterFluid.Flowing) {
            waterlevel = fluidstate.getLevel();
        }
        return waterlevel;
    }

    public static void setWaterLevel(int level, BlockPos pos, WorldAccess world) {
        if (level == 8) {
            if (!(world.getBlockState(pos).getBlock() instanceof FluidFillable)) { // Don't fill kelp etc
                world.setBlockState(pos, Fluids.WATER.getDefaultState().getBlockState(), 11);
            }
        } else if (level == 0) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);
        } else if (level < 8) {
            world.setBlockState(pos, Fluids.FLOWING_WATER.getFlowing(level, false).getBlockState(), 11);
        } else {
            System.out.println("Can't set water >8 something went very wrong!");
        }
        if (level <8 && level >0) {

            int x = pos.getX();
            System.out.println(x);
            int y = pos.getY() - 1;
            int z = pos.getZ();

            for (int dx = x-1; dx <= x+1; dx++) {
                for (int dz = z-1; dz <= z+1; dz++) {
                    if (dx == 0 && dz == 0)
                        System.out.println("Not Adjacent");
                    else {
                        BlockPos currentPos = new BlockPos(dx,y,dz);
                        int currentLevel = world.getBlockState(currentPos).getFluidState().getLevel();
                        if (world.getBlockState(currentPos).getBlock() == Blocks.AIR){
                            System.out.println("amonger");
                            if (world.getBlockState(pos).getFluidState().getLevel() == 1)
                            {
                                world.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);
                                world.setBlockState(currentPos, Fluids.FLOWING_WATER.getFlowing(currentLevel+1, false).getBlockState(), 11);
                            }
                            /*else
                            {
                                world.setBlockState(pos, Fluids.FLOWING_WATER.getFlowing(level - 1, false).getBlockState(), 11);
                                world.setBlockState(currentPos, Fluids.FLOWING_WATER.getFlowing(currentLevel+level, false).getBlockState(), 11);
                            }*/
                        }
                        else {
                            if (world.getBlockState(currentPos).getBlock() == Blocks.WATER && world.getFluidState(currentPos).getLevel() <8) {

                                if (world.getFluidState(pos).getLevel() == 1)
                                {
                                    System.out.println("sussy imposter caught 1");
                                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);
                                    world.setBlockState(currentPos, Fluids.FLOWING_WATER.getFlowing(currentLevel+1, false).getBlockState(), 11);

                                }
                                /*else // imposter is not here
                                {
                                    world.setBlockState(pos, Fluids.FLOWING_WATER.getFlowing(level-1 , false).getBlockState(), 11);
                                    if (currentLevel+1 < 7) {
                                        world.setBlockState(currentPos, Fluids.FLOWING_WATER.getFlowing(currentLevel+1, false).getBlockState(), 11);
                                    }
                                    else
                                        System.out.println("sussy imposter caught 2");
                                        if (currentLevel+1 == 8) {
                                        world.setBlockState(currentPos, Blocks.WATER., 11);
                                    }
                                }*/

                            }
                        }

                    }
                }
            }
        }
    }


    public static void addWater(int level, BlockPos pos, WorldAccess world) {
        int existingwater = getWaterLevel(pos, world);
        int totalwater = existingwater + level;
        if (totalwater > 8) {
            setWaterLevel(totalwater - 8, pos.up(), world);
            setWaterLevel(8, pos, world);
        } else {
            setWaterLevel(totalwater, pos, world);
        }
    }

    public static void equalizeWater(ArrayList<BlockPos> blocks, BlockPos center, WorldAccess world) {
        int[] waterlevels = new int[4];
        Arrays.fill(waterlevels, -1);
        int centerwaterlevel = getWaterLevel(center, world);
        for (BlockPos block : blocks) {
            waterlevels[blocks.indexOf(block)] = getWaterLevel(block, world);
        }

        int waterlevelsnum = waterlevels.length;
        int didnothings = 0;
        int waterlevel;
        while (didnothings < waterlevelsnum) {
            didnothings = 0;
            for (int i = 0; i < 4; i++) {
                waterlevel = waterlevels[i];
                if (waterlevel != -1) {
                    if ((centerwaterlevel >= (waterlevel + 2))) {
                        waterlevel += 1;
                        waterlevels[i] = waterlevel;
                        centerwaterlevel -= 1;
                    } else {
                        didnothings += 1;
                    }
                } else {
                    didnothings += 1;
                }
            }
        }
        for (BlockPos block : blocks) {
            int newwaterlevel = waterlevels[blocks.indexOf(block)];
            setWaterLevel(newwaterlevel, block, world);
        }
        setWaterLevel(centerwaterlevel, center, world);
    }
}
