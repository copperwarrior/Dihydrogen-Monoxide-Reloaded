package io.github.CoolMineman;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    private FlowWater() {
    }


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
        if (fluidstate.getFluid() instanceof WaterFluid.Still) {
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
        if (level < 8 && level > 0) {

            int radius = 1;
            int x = pos.getX();
            int y = pos.getY() - 1;
            int uy = pos.getY();
            int z = pos.getZ();


            for (int dx = x - radius; dx <= x + radius; dx++) {
                for (int dz = z - radius; dz <= z + radius; dz++) {
                    if (dx != 0 && dz != 0) {

                        BlockPos checkAbove = new BlockPos(dx, uy, dz);
                        if (world.getBlockState(pos.north()) == Blocks.AIR.getDefaultState() &&
                                world.getBlockState(pos.east()) == Blocks.AIR.getDefaultState() &&
                                world.getBlockState(pos.west()) == Blocks.AIR.getDefaultState() &&
                                world.getBlockState(pos.south()) == Blocks.AIR.getDefaultState()) {

                            BlockPos currentPos = new BlockPos(dx, y, dz);
                            int currentLevel = world.getBlockState(currentPos).getFluidState().getLevel();
                            if (world.getBlockState(currentPos).getBlock() == Blocks.AIR) {

                                if (world.getBlockState(pos).getFluidState().getLevel() == 1) {
                                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);
                                    world.setBlockState(currentPos, Fluids.FLOWING_WATER.getFlowing(currentLevel + 1, false).getBlockState(), 11);
                                }

                            } else {
                                if (world.getBlockState(currentPos).getBlock() == Blocks.WATER && world.getFluidState(currentPos).getLevel() < 8) {

                                    if (world.getFluidState(pos).getLevel() == 1) {
                                        world.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);
                                        world.setBlockState(currentPos, Fluids.FLOWING_WATER.getFlowing(currentLevel + 1, false).getBlockState(), 11);

                                    }

                                }
                            }
                        }

                    }
                }
            }
        }
    }

    public static void l2Fixer(BlockPos pos, int level, WorldAccess world) {

        int x = pos.getX();
        int y = pos.getY() - 1;
        int uy = pos.getY();
        int z = pos.getZ();
        int radiusCheck = 2;
        int adjustX = 0;
        int adjustZ = 0;
        int count = 0;
        int level7count = 0;


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


    public static void equalizeWater(ArrayList<BlockPos> blocks, BlockPos center, WorldAccess world ) {
        int[] waterlevels = new int[4];
        Arrays.fill(waterlevels, -1);
        int centerwaterlevel = getWaterLevel(center, world);
        for (BlockPos block : blocks) {
            waterlevels[blocks.indexOf(block)] = getWaterLevel(block, world);
        }
        int waterlevelsnum = waterlevels.length;
        int didnothings = 0;
        int waterlevel;
        List<Integer> matrixLevels = new ArrayList<>(Arrays.asList());


        int x = center.getX();
        int y = center.getY();
        int z = center.getZ();
        int radius = 1;
        int counter = 0;

        //start
        int x2 = center.getX();
        int y2 = center.getY() - 1;
        int z2 = center.getZ();
        int radiusCheck = 3;
        int adjustX = 0;
        int adjustZ = 0;
        int count = 0;
        int level7count = 0;

        for (int dx2 = x2 - radiusCheck; dx2 <= x2 + radiusCheck; dx2++) {
            for (int dz2 = z2 - radiusCheck; dz2 <= z2 + radiusCheck; dz2++) {
                BlockPos checkPos = new BlockPos(dx2, y2, dz2);
                if (world.getFluidState(checkPos).getLevel() < 8) {
                    if (world.getBlockState(checkPos).getBlock() == Blocks.WATER) {
                        //System.out.println("amoger");
                        level7count += 1;
                    }

                }
                if (level7count > 0) {
                    //System.out.println("amoger 1");
                    method1(blocks, center, world);
                    level7count = 0;
                }
            }
        }

                //end

                for (int dx = x - radius; dx <= x + radius; dx++) {
                    for (int dz = z - radius; dz <= z + radius; dz++) {
                        BlockPos internalPos = new BlockPos(dx, y, dz);
                        counter += 1;
                        //if (world.g)
                        if (world.getBlockState(internalPos).getBlock() == Blocks.WATER || world.getBlockState(internalPos).getBlock() == Blocks.AIR) {
                            int ilevel = world.getFluidState(internalPos).getLevel();
                            matrixLevels.add(ilevel);
                        }
                        if (counter == 9 && matrixLevels.size() > 0) {
                            //System.out.println(matrixLevels);
                            int maxLevel = Collections.max(matrixLevels);
                            int minLevel = Collections.min(matrixLevels);
                            int range = maxLevel - minLevel;


                            if (range == 1) {
                                method2(blocks, center, world);
                            }
                            if (range > 1) {
                                method1(blocks, center, world);
                            }
                            matrixLevels.clear();
                            //counter = 0;
                        }
                    }
                }
            }






    public static void method1(ArrayList<BlockPos> blocks, BlockPos center, WorldAccess world) {

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
            for (int i = 0; i < 4; i++) {
                waterlevel = waterlevels[i];
                if (waterlevel != -1) {
                    if ((centerwaterlevel >= (waterlevel + 1))) {
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
    public static void method2(ArrayList<BlockPos> blocks, BlockPos center, WorldAccess world) {

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
