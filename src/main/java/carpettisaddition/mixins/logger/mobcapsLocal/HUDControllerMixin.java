package carpettisaddition.mixins.logger.mobcapsLocal;

import carpet.logging.HUDController;
import carpettisaddition.utils.ModIds;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import org.spongepowered.asm.mixin.Mixin;

//#if MC >= 11800
//$$ import carpettisaddition.logging.TISAdditionHUDController;
//$$ import carpettisaddition.logging.TISAdditionLoggerRegistry;
//$$ import carpettisaddition.logging.loggers.mobcapsLocal.MobcapsLocalLogger;
//$$ import net.minecraft.server.MinecraftServer;
//$$ import net.minecraft.server.network.ServerPlayerEntity;
//$$ import org.spongepowered.asm.mixin.injection.At;
//$$ import org.spongepowered.asm.mixin.injection.Inject;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//$$ import java.util.List;
//#endif

@Restriction(require = @Condition(value = ModIds.minecraft, versionPredicates = ">=1.18"))
@Mixin(HUDController.class)
public abstract class HUDControllerMixin
{
	//#if MC >= 11800
	//$$ @Inject(
	//$$ 		method = "update_hud",
	//$$ 		at = @At(
	//$$ 				value = "FIELD",
	//$$ 				target = "Lcarpet/logging/LoggerRegistry;__counter:Z",
	//$$ 				remap = false
	//$$ 		),
	//$$ 		remap = false
	//$$ )
	//$$ private static void updateMobCapsLocalLogger(MinecraftServer packetData, List<ServerPlayerEntity> packet, CallbackInfo ci)
	//$$ {
	//$$ 	TISAdditionHUDController.doHudLogging(TISAdditionLoggerRegistry.__mobcapsLocal, MobcapsLocalLogger.NAME, MobcapsLocalLogger.getInstance());
	//$$ }
	//#endif
}