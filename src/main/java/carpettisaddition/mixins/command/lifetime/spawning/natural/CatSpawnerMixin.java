package carpettisaddition.mixins.command.lifetime.spawning.natural;

import carpettisaddition.commands.lifetime.interfaces.LifetimeTrackerTarget;
import carpettisaddition.commands.lifetime.spawning.LiteralSpawningReason;
import net.minecraft.entity.Entity;
import net.minecraft.world.gen.CatSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(CatSpawner.class)
public abstract class CatSpawnerMixin
{
	@ModifyArg(
			//#if MC >= 11600
			//$$ method = "spawn(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/server/world/ServerWorld;)I",
			//#else
			method = "spawn(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/World;)I",
			//#endif
			at = @At(
					value = "INVOKE",
					//#if MC >= 11600
					//$$ target = "Lnet/minecraft/server/world/ServerWorld;spawnEntityAndPassengers(Lnet/minecraft/entity/Entity;)V"
					//#else
					target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"
					//#endif
			)
	)
	private Entity onCatSpawnLifeTimeTracker(Entity cat)
	{
		((LifetimeTrackerTarget)cat).recordSpawning(LiteralSpawningReason.NATURAL);
		return cat;
	}
}
