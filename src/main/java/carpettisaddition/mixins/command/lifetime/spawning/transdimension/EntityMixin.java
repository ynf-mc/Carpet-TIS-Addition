package carpettisaddition.mixins.command.lifetime.spawning.transdimension;

import carpettisaddition.commands.lifetime.interfaces.LifetimeTrackerTarget;
import carpettisaddition.commands.lifetime.spawning.TransDimensionSpawningReason;
import carpettisaddition.utils.compat.DimensionWrapper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin
{
	@Shadow public World world;

	@Shadow public abstract EntityType<?> getType();

	@Inject(
			//#if MC >= 11600
			//$$ method = "moveToWorld",
			//#else
			method = "changeDimension",
			//#endif
			at = @At("RETURN")
	)
	private void onEntityTransDimensionSpawnedLifeTimeTracker(CallbackInfoReturnable<Entity> cir)
	{
		Entity entity = cir.getReturnValue();
		if (entity != null)
		{
			((LifetimeTrackerTarget)entity).recordSpawning(new TransDimensionSpawningReason(DimensionWrapper.of(this.world)));
		}
	}
}
