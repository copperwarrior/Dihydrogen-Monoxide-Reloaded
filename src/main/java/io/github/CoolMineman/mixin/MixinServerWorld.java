package io.github.CoolMineman.mixin;

import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.*;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.block.FluidFillable;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@SuppressWarnings("deprecation")
@Mixin(ServerWorld.class)
public abstract class MixinServerWorld extends World {

    protected MixinServerWorld(MutableWorldProperties properties, RegistryKey<World> registryRef, RegistryEntry<DimensionType> registryEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed) {
        super(properties, registryRef, registryEntry, profiler, isClient, debugWorld, seed);
    }

    @Inject(at = @At("TAIL"),method = "tickChunk")
    public void tickChunk(WorldChunk chunk, int randomTickSpeed, CallbackInfo ci) {
        ChunkPos chunkPos = chunk.getPos();
        boolean rain = this.isRaining();
		boolean day = this.isDay();
        int x = chunkPos.getStartX();
        int z = chunkPos.getStartZ();
        BlockPos pos = this.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, getRandomPosInChunk(x, 0, z, 15));
		BlockState downstate = getBlockState(pos.down());

		if (rain && random.nextInt(100) == 0) {
			if (this.getBlockState(pos).getBlock() == Blocks.SNOW && downstate.isSideSolidFullSquare(this, pos, Direction.UP)) {
				int layer = getBlockState(pos).get(Properties.LAYERS);
				if (layer < 5) {
					setBlockState(pos, Blocks.SNOW.getDefaultState().with(Properties.LAYERS, layer + 1));
				}
			}
			if (this.getBlockState(pos).getBlock() == Blocks.WATER && downstate.isSideSolidFullSquare(this, pos, Direction.UP) && downstate.getBlock() != Blocks.WATER) {
				int layer = getFluidState(pos).getLevel();
				if (layer < 8) {
					setBlockState(pos, Fluids.FLOWING_WATER.getFlowing(layer + 1, false).getBlockState(), 11);
				}
			}
			if (this.hasRain(pos) && downstate.isSideSolidFullSquare(this, pos, Direction.UP) && downstate.getBlock() != Blocks.WATER) {
				setBlockState(pos, Fluids.FLOWING_WATER.getFlowing(1, false).getBlockState(), 11);
			}
		}
		if (day && rain == false && random.nextInt(50) == 0) {
			if (this.getBlockState(pos).getBlock() == Blocks.SNOW) {
				int layer = getBlockState(pos).get(Properties.LAYERS);
				setBlockState(pos, Fluids.FLOWING_WATER.getFlowing(layer, false).getBlockState(), 11);
			}
			if (this.getBlockState(pos).getBlock() == Blocks.WATER && downstate.getBlock() != Blocks.WATER) {
				int layer = getFluidState(pos).getLevel();
				if (layer < 8 && layer > 1) {
					setBlockState(pos, Fluids.FLOWING_WATER.getFlowing(layer - 1, false).getBlockState(), 11);
				}
				if (layer == 1) {
					setBlockState(pos, Blocks.AIR.getDefaultState(), 11);
				}
			}
		}

    }
}