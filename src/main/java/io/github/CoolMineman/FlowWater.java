package io.github.CoolMineman;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.block.FluidFillable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.WaterFluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.BlockView;
import org.lwjgl.system.CallbackI;


public class FlowWater {
    private FlowWater() {
    }
	
	protected static void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
        Block.dropStacks(state, world, pos, blockEntity);
    }
	
    public static void flowwater(WorldAccess world, BlockPos fluidPos, FluidState state) {
        if (world.getBlockState(fluidPos).getBlock() instanceof FluidFillable) {
            return;
        }
        if ((world.getBlockState(fluidPos.down()).canBucketPlace(Fluids.WATER)) && (getWaterLevel(fluidPos.down(), world) != 8)) {
            int centerlevel = getWaterLevel(fluidPos, world);
			beforeBreakingBlock(world, fluidPos, world.getBlockState(fluidPos));
            world.setBlockState(fluidPos, Blocks.AIR.getDefaultState(), 11);
            addWater(centerlevel, fluidPos.down(), world);
        } else {
            ArrayList<BlockPos> blocks = new ArrayList<>(4);
            for (Direction dir : Direction.Type.HORIZONTAL) {
                blocks.add(fluidPos.offset(dir));
            }
            blocks.removeIf(pos -> !world.getBlockState(pos).canBucketPlace(Fluids.WATER));
            Collections.shuffle(blocks);
            equalizeWater(blocks, fluidPos, world, state);
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
        }  else {
            System.out.println("Can't set water >8 something went very wrong!");
        }
        if (level == 1 && world.getBlockState(pos.down()).getBlock() != Blocks.AIR ) {

            int maxRadius = 4;
            int maxDia = (maxRadius * 2) + 1;
            //int maxArea = maxDia * 2;
            int currentRadius = 1;
            int currentDiameter = (2 * currentRadius) + 1;
            int previousRadius = currentRadius - 1;
            int x = pos.getX();
            int y = pos.getY();
            int uy = pos.getY() - 1;
            int z = pos.getZ();
            int count = 0;
            boolean didJump = false;
            int dx;
            int dz;
            boolean addZ = false;
            Boolean doHop = false;
            int perim = 4*(currentDiameter-1);
            int totalCount =  maxDia*maxDia;


            //puddle feature start

            //System.out.println("loop start");
            for (dx = x - currentRadius, dz = z - currentRadius; didJump == false && dx <= x + maxRadius && dz <= z + maxRadius; ) {

                /*System.out.println("original pos: " + pos);
                System.out.println("loop restart");
                System.out.println("initial count " + count);
                System.out.println("didjump 1 " + didJump);*/
                if (didJump == false) {
                    //System.out.println("didjump 2 " + didJump);
                    if (!(((dx > x + previousRadius || dx < x - previousRadius) || (dz > z + previousRadius || dz < z - previousRadius)) || ((dx > x + previousRadius || dx < x - previousRadius) && (dz > z + previousRadius || dz < z - previousRadius)))) {

                        dz = z + currentRadius;
                    } else {
                        addZ = true;
                    }


                    //code start

                    int currDiameter = (currentRadius * 2) + 1;


                    //System.out.println("current radius: " + currentRadius);
                    //System.out.println("dx: " + dx);
                    //System.out.println("dz: " + dz);

                    if (world.getBlockState(pos.down()).getBlock() != Blocks.AIR) {
                        //System.out.println("catch 1");
                        BlockPos currentPos = new BlockPos(dx, y, dz);
                        BlockPos checkBelow = currentPos.down();
                        BlockPos newWaterPos = new BlockPos(0, 0, 0);
                        String direction = "";
                        //Boolean doHop = false;

                        if (checkBelow != pos.down() && currentPos != pos) {
                            //BlockState below =
                            if ((world.getBlockState(checkBelow).isAir() == true || (world.getBlockState(checkBelow).getBlock() == Blocks.WATER) && world.getFluidState(checkBelow).getLevel() != 8)) {
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
                                    //System.out.println("dir: " + direction);
                                    //System.out.println("jumping");
									BlockState oldWaterState = world.getBlockState(pos);
                                    world.setBlockState(newWaterPos, Fluids.FLOWING_WATER.getFlowing(1, false).getBlockState(), 11);
                                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);
                                    didJump = true;
                                    doHop = false;
                                    direction = "";
									doErosion(world, pos, newWaterPos, oldWaterState, world.getBlockState(newWaterPos));
                                    //System.out.println("dir2: " + direction);
                                } else {
                                    doHop = false;
                                }
                            }
                        }
                       //System.out.println("dir3: " + direction);
                        //code end

                        if (dz == z + currentRadius) {
                            dz = z - currentRadius;
                            dx += 1;
                            addZ = false;
                        }
                        if (addZ == true) {
                            dz += 1;
                            addZ = false;
                        }

                        //radius stuff
                        count += 1;
                        /*System.out.println("count2: " + count);
                        System.out.println("count: " + count);
                        System.out.println("perim: " + perim);*/
                        if (count == perim && (currentRadius + 1 <= maxRadius)) {
                            //System.out.println("expanded radius");
                            currentRadius += 1;
                            count = 0;
                            //System.out.println("reset count: " + count);
                            dx = x - currentRadius;
                            dz = z - currentRadius;
                        }
                        currentDiameter = (2 * currentRadius) + 1;
                        perim = 4 * (currentDiameter - 1);
                        //System.out.println("perim: " + perim);
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


    public static void equalizeWater(ArrayList<BlockPos> blocks, BlockPos center, WorldAccess world, FluidState fluidState ) {
        int[] waterlevels = new int[4];
        Arrays.fill(waterlevels, -1);
        int centerwaterlevel = getWaterLevel(center, world);
		int i = 0;
        for (BlockPos block : blocks) {
            waterlevels[i] = getWaterLevel(block, world);
			i++;
        }
/*        int waterlevelsnum = waterlevels.length;
        int didnothings = 0;
        int waterlevel;*/
        List<Integer> matrixLevels = new ArrayList<>(Arrays.asList());

        int x = center.getX();
        int y = center.getY();
        int z = center.getZ();
        int radius = 1;
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

                            method1(blocks, center, world, fluidState, range);
                            matrixLevels.clear();
                            counter = 0;
                        }

                    }
                }
            }






    public static void method1(ArrayList<BlockPos> blocks, BlockPos center, WorldAccess world, FluidState fluidState, int range) {

        int[] waterlevels = new int[4];
        Arrays.fill(waterlevels, -1);
        int centerwaterlevel = getWaterLevel(center, world);
		int ind = 0;
		int mod = 1;
		if (range == 1) {
			mod = 2;
		}
        for (BlockPos block : blocks) {
            waterlevels[ind] = getWaterLevel(block, world);
			ind++;
        }
		ind = 0;
        int waterlevelsnum = waterlevels.length;
        int didnothings = 0;
        int waterlevel;

        while (didnothings < waterlevelsnum) {
            for (int i = 0; i < 4; i++) {
                waterlevel = waterlevels[i];
                if (waterlevel != -1) {
                    if ((centerwaterlevel >= (waterlevel + mod))) {
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
			BlockState state = world.getBlockState(block);
			int layer = 0;
			if (state.getBlock() == Blocks.SNOW) {
				layer = world.getBlockState(block).get(Properties.LAYERS);
				world.setBlockState(block, Fluids.FLOWING_WATER.getFlowing(layer, false).getBlockState(), 11);
			}
			beforeBreakingBlock(world, block, state);
			int newwaterlevel = Math.max(waterlevels[ind], layer);
			setWaterLevel(newwaterlevel, block, world);
			ind++;
        }
        setWaterLevel(centerwaterlevel, center, world);
    }
	
	public static void doErosion(WorldAccess world, BlockPos frompos, BlockPos topos, BlockState waterfrom, BlockState waterto) {
		BlockPos erodetarg;
		switch (world.getRandom().nextInt(4)) {
            case 1:  erodetarg = frompos.west();
                     break;
            case 2:  erodetarg = frompos.east();
                     break;
            case 3:  erodetarg = frompos.south();
                     break;
            case 4:  erodetarg = frompos.north();
                     break;
            default: erodetarg = frompos.down();
                     break;
        }
		BlockState blockStateBelow = world.getBlockState(erodetarg);
		Block erodeto = null;
		if (world.getRandom().nextInt(15) == 0) {
			switch (blockStateBelow.getBlock().getTranslationKey()) {
				case "block.minecraft.clay":  erodeto = Blocks.AIR;
						break;
				case "block.minecraft.sand":  erodeto = Blocks.CLAY;
						 break;
				case "block.minecraft.coarse_dirt":  erodeto = Blocks.SAND;
						 break;
				case "block.minecraft.dirt":  erodeto = Blocks.COARSE_DIRT;
						 break;
				case "block.minecraft.grass_block":  erodeto = Blocks.DIRT;
						 break;
				case "block.minecraft.gravel":  erodeto = Blocks.COARSE_DIRT;
						 break;
				case "block.minecraft.stone":  erodeto = Blocks.GRAVEL;
						 break;
				default: break;
			}
			if (erodeto != null) {
				world.setBlockState(erodetarg, erodeto.getDefaultState(),11);
			}
		}
	}
}
