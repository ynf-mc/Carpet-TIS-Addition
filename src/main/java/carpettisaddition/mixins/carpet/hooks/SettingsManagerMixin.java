package carpettisaddition.mixins.carpet.hooks;

import carpet.settings.SettingsManager;
import carpet.utils.Translations;
import carpettisaddition.CarpetTISAdditionMod;
import carpettisaddition.utils.Messenger;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static carpettisaddition.CarpetTISAdditionServer.fancyName;

@Mixin(SettingsManager.class)
public class SettingsManagerMixin
{
	@Inject(
			method = "listAllSettings",

			//#if MC < 11901
			slice = @Slice(
					from = @At(
							value = "CONSTANT",
							// after printed fabric-carpet version

							//#if MC >= 11500
							args = "stringValue=ui.version",
							//#else
							//$$ args = "stringValue= version: ",
							//#endif
							ordinal = 0
					)
			),
			//#endif

			at = @At(
					value = "INVOKE",
					//#if MC >= 11901
					//$$ target = "Lcarpet/api/settings/SettingsManager;getCategories()Ljava/lang/Iterable;"
					//#elseif MC >= 11600
					//$$ target = "Lcarpet/settings/SettingsManager;getCategories()Ljava/lang/Iterable;",
					//$$ ordinal = 0
					//#else
					target = "Lnet/minecraft/server/command/ServerCommandSource;getPlayer()Lnet/minecraft/server/network/ServerPlayerEntity;",
					ordinal = 0,
					remap = true
					//#endif
			),
			remap = false
	)
	private void printAdditionVersion(ServerCommandSource source, CallbackInfoReturnable<Integer> cir) {
		Messenger.tell(
				source,
				Messenger.c(
						String.format("g %s ", fancyName),
						String.format("g %s: ", Translations.tr("ui.version",  "version")),
						String.format("g %s", CarpetTISAdditionMod.getVersion())
				)
		);
	}
}
