package carpettisaddition.mixins.command.lifetime.removal.conversion;

import carpettisaddition.commands.lifetime.interfaces.LifetimeTrackerTarget;
import carpettisaddition.commands.lifetime.removal.MobConversionRemovalReason;
import carpettisaddition.utils.ModIds;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Restriction(require = @Condition(value = ModIds.minecraft, versionPredicates = "<1.16"))
@Mixin(ZombieVillagerEntity.class)
public abstract class ZombieVillagerEntityMixin extends ZombieEntity
{
	public ZombieVillagerEntityMixin(EntityType<? extends ZombieEntity> type, World world)
	{
		super(type, world);
	}

	//#if MC < 11600
	@Inject(
			method = "finishConversion",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/mob/ZombieVillagerEntity;remove()V"
			),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void recordSelfRemoval$LifeTimeTracker(ServerWorld world, CallbackInfo ci, VillagerEntity villager)
	{
		((LifetimeTrackerTarget)this).recordRemoval(new MobConversionRemovalReason(villager.getType()));
	}
	//#endif
}
