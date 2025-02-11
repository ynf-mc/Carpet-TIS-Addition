package carpettisaddition.mixins.command.lifetime;

import carpettisaddition.commands.lifetime.interfaces.ServerWorldWithLifeTimeTracker;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerChunkManager.class)
public abstract class ServerChunkManagerMixin
{
	@Shadow @Final
	//#if MC < 11700
	private
	//#endif
	ServerWorld world;

	@Inject(
			method = "tickChunks",
			at = @At(
					value = "INVOKE",
					//#if MC >= 11800
					//$$ target = "Lnet/minecraft/server/world/ChunkTicketManager;getTickedChunkCount()I"
					//#elseif MC >= 11600
					//$$ target = "Lnet/minecraft/server/world/ChunkTicketManager;getSpawningChunkCount()I"
					//#else
					target = "Lnet/minecraft/server/world/ServerWorld;getMobCountsByCategory()Lit/unimi/dsi/fastutil/objects/Object2IntMap;"
					//#endif
			)
	)
	private void onCountingMobcapLifeTimeTracker(CallbackInfo ci)
	{
		((ServerWorldWithLifeTimeTracker)this.world).getLifeTimeWorldTracker().increaseSpawnStageCounter();
	}
}
