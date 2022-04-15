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
import org.lwjgl.system.CallbackI;


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
        }
            if (level == 1) {

                int maxRadius = 5;
                int maxDia = (maxRadius * 2) + 1;
                int maxArea = maxDia * 2;
                int currentRadius = 1;
                int x = pos.getX();
                int y = pos.getY();
                int uy = pos.getY() - 1;
                int z = pos.getZ();
                int count = 0;
                boolean didJump = false;


                //puddle feature start


                    for (int dx = x - currentRadius; dx <= x + currentRadius; dx++) {
                        for (int dz = z - currentRadius; dz <= z + currentRadius; dz++) {
                            int currDiameter = (currentRadius * 2) + 1;
                            int currArea = currDiameter * currDiameter;

                            if (didJump == false && world.getBlockState(pos.down()).getBlock() != Blocks.AIR) {
                                //System.out.println("catch 1");
                                BlockPos currentPos = new BlockPos(dx, y, dz);
                                BlockPos checkBelow = currentPos.down();
                                BlockPos newWaterPos = new BlockPos(0, 0, 0);
                                String direction = "";
                                Boolean doHop = false;

                                if (checkBelow != pos.down() && currentPos != pos) {
                                    if (world.getBlockState(checkBelow).canBucketPlace(Fluids.WATER) && (world.getFluidState(checkBelow).getLevel() != 8)) {
                                        //System.out.println("catch 2");
                                        doHop = true;
                                    }
                                    if (doHop == true) {
                                        if (currentPos.getX() > pos.getX()) {
                                            direction = "east";
                                        } else {
                                            if (currentPos.getX() < pos.getX()) {
                                                direction = "west";
                                            } else {
                                                if (currentPos.getZ() > pos.getZ()) {
                                                    direction = "south";
                                                } else {
                                                    if (currentPos.getZ() < pos.getZ()) {
                                                        direction = "north";
                                                    }
                                                }
                                            }
                                        }
                                        if (direction.equals("north")) {
                                            newWaterPos = pos.north();
                                        }
                                        if (direction.equals("south")) {
                                            newWaterPos = pos.south();
                                        }
                                        if (direction.equals("east")) {
                                            newWaterPos = pos.east();
                                        }
                                        if (direction.equals("west")) {
                                            newWaterPos = pos.west();
                                        }
                                        //System.out.println("catch 3");
                                        if (world.getBlockState(pos).getBlock() == Blocks.WATER && newWaterPos.getY() == pos.getY() && world.getBlockState(newWaterPos).getBlock() == Blocks.AIR) {
                                            //System.out.println("jumping");
                                            world.setBlockState(newWaterPos, Fluids.FLOWING_WATER.getFlowing(1, false).getBlockState(), 11);
                                            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);
                                            didJump = true;
                                        }
                                        else {
                                            doHop = false;
                                        }
                                    }
                                }
                                count += 1;
                                //System.out.println("count " + count);
                                if (count == currArea && currentRadius <= maxRadius) {
                                    //System.out.println("expanded radius");
                                    currentRadius += 1;
                                }
                                //puddle feature end
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


    public static void equalizeWater(ArrayList<BlockPos> blocks, BlockPos center, WorldAccess world ) {
        int[] waterlevels = new int[4];
        Arrays.fill(waterlevels, -1);
        int centerwaterlevel = getWaterLevel(center, world);
        for (BlockPos block : blocks) {
            waterlevels[blocks.indexOf(block)] = getWaterLevel(block, world);
        }
/*        int waterlevelsnum = waterlevels.length;
        int didnothings = 0;
        int waterlevel;*/
        List<Integer> matrixLevels = new ArrayList<>(Arrays.asList());

        int x = center.getX();
        int y = center.getY();
        int z = center.getZ();
        int radius = 2;
        int diameter = (radius*2)+1;
        int counter = 0;
        int countEnd = diameter*diameter;

                for (int dx = x - radius; dx <= x + radius; dx++) {
                    for (int dz = z - radius; dz <= z + radius; dz++) {
                        BlockPos internalPos = new BlockPos(dx, y, dz);
                        counter += 1;
                        if (world.getBlockState(internalPos).getBlock() == Blocks.WATER || world.getBlockState(internalPos).getBlock() == Blocks.AIR) {
                            int ilevel = world.getFluidState(internalPos).getLevel();
                            matrixLevels.add(ilevel);
                        }
                        if (counter == countEnd && matrixLevels.size() > 0) {
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
                            counter = 0;
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
