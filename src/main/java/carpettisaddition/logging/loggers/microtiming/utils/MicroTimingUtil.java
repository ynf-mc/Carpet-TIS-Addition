package carpettisaddition.logging.loggers.microtiming.utils;

import carpet.logging.LoggerRegistry;
import carpet.utils.WoolTool;
import carpettisaddition.CarpetTISAdditionServer;
import carpettisaddition.CarpetTISAdditionSettings;
import carpettisaddition.logging.loggers.microtiming.MicroTimingLogger;
import carpettisaddition.logging.loggers.microtiming.MicroTimingLoggerManager;
import carpettisaddition.logging.loggers.microtiming.enums.MicroTimingTarget;
import carpettisaddition.logging.loggers.microtiming.marker.MicroTimingMarkerManager;
import carpettisaddition.logging.loggers.microtiming.marker.MicroTimingMarkerType;
import carpettisaddition.translations.Translator;
import carpettisaddition.utils.Messenger;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.*;
import net.minecraft.block.enums.WallMountLocation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.BaseText;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MicroTimingUtil
{
	public static final Direction[] DIRECTION_VALUES = Direction.values();
	private static final Translator TRANSLATOR = MicroTimingLoggerManager.TRANSLATOR.getDerivedTranslator("util");
	private static final ImmutableMap<DyeColor, String> COLOR_STYLE = new ImmutableMap.Builder<DyeColor, String>().
			put(DyeColor.WHITE, "w").

			//#if MC >= 11600
			//$$ put(DyeColor.ORANGE, "#F9801D").  // DyeColor.ORANGE.color
			//#else
			put(DyeColor.ORANGE, "d").
			//#endif

			put(DyeColor.MAGENTA, "m").
			put(DyeColor.LIGHT_BLUE, "c").
			put(DyeColor.YELLOW, "y").
			put(DyeColor.LIME, "l").

			//#if MC >= 11600
			//$$ put(DyeColor.PINK, "#F38BAA").  // DyeColor.PINK.color
			//#else
			put(DyeColor.PINK, "r").
			//#endif

			put(DyeColor.GRAY, "f").
			put(DyeColor.LIGHT_GRAY, "g").
			put(DyeColor.CYAN, "q").
			put(DyeColor.PURPLE, "p").
			put(DyeColor.BLUE, "v").

			//#if MC >= 11600
			//$$ put(DyeColor.BROWN, "#835432").  // DyeColor.BROWN.color
			//#else
			put(DyeColor.BROWN, "n").
			//#endif

			put(DyeColor.GREEN, "e").
			put(DyeColor.RED, "r").
			put(DyeColor.BLACK, "k").
			build();

	public static String getColorStyle(DyeColor color)
	{
		return COLOR_STYLE.getOrDefault(color, "w");
	}

	private static BaseText tr(String key, Object... args)
	{
		return TRANSLATOR.tr(key, args);
	}

	public static BaseText getColoredValue(Object value)
	{
		BaseText text = Messenger.s(value.toString());
		Formatting color = null;
		if (Boolean.TRUE.equals(value))
		{
			color = Formatting.GREEN;
		}
		else if (Boolean.FALSE.equals(value))
		{
			color = Formatting.RED;
		}
		if (value instanceof Number)
		{
			color = Formatting.GOLD;
		}
		if (color != null)
		{
			Messenger.formatting(text, color);
		}
		return text;
	}

	public static BaseText getSuccessText(boolean value, boolean showReturnValue, BaseText hoverExtra)
	{
		BaseText hintText = value ?
				Messenger.formatting(tr("successful"), "e") :
				Messenger.formatting(tr("failed"), "r");
		if (hoverExtra != null)
		{
			hintText.append(Messenger.c("w \n", hoverExtra));
		}
		if (showReturnValue)
		{
			hintText.append(Messenger.c(
					"w \n", tr("return_value"), "w : ",
					getColoredValue(value)
			));
		}
		return value ?
				Messenger.fancy("e", Messenger.s("√"), hintText, null) :
				Messenger.fancy("r", Messenger.s("×"), hintText, null);
	}
	public static BaseText getSuccessText(boolean bool, boolean showReturnValue)
	{
		return getSuccessText(bool, showReturnValue, null);
	}

	private static boolean isPositionAvailable(World world, BlockPos pos)
	{
		return world instanceof ServerWorld &&
				//#if MC >= 11700
				//$$ ((ServerWorld)world).method_37117(pos);
				//#else
				world.getChunkManager().shouldTickBlock(pos);
				//#endif
	}

	private static Optional<DyeColor> getWoolColor(World world, BlockPos pos)
	{
		if (!MicroTimingLoggerManager.isLoggerActivated() || !isPositionAvailable(world, pos))
		{
			return Optional.empty();
		}
		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		BlockPos woolPos;

		if (block instanceof ObserverBlock || block instanceof EndRodBlock ||
				block instanceof PistonBlock || block instanceof PistonExtensionBlock)
		{
			woolPos = pos.offset(state.get(Properties.FACING).getOpposite());
		}
		else if (block instanceof AbstractButtonBlock || block instanceof LeverBlock)
		{
			Direction facing;
			if (state.get(Properties.WALL_MOUNT_LOCATION) == WallMountLocation.FLOOR)
			{
				facing = Direction.UP;
			}
			else if (state.get(Properties.WALL_MOUNT_LOCATION) == WallMountLocation.CEILING)
			{
				facing = Direction.DOWN;
			}
			else
			{
				facing = state.get(Properties.HORIZONTAL_FACING);
			}
			woolPos = pos.offset(facing.getOpposite());
		}
		else if (block instanceof WallRedstoneTorchBlock || block instanceof TripwireHookBlock)
		{
			woolPos = pos.offset(state.get(Properties.HORIZONTAL_FACING).getOpposite());
		}
		else if (
				block instanceof AbstractRailBlock ||
				block instanceof AbstractRedstoneGateBlock ||
				block instanceof RedstoneTorchBlock ||
				block instanceof RedstoneWireBlock ||
				block instanceof AbstractPressurePlateBlock
		)  // on block
		{
			woolPos = pos.down();
		}
		else
		{
			return Optional.empty();
		}

		return Optional.ofNullable(WoolTool.getWoolColorAtPosition(world, woolPos));
	}

	private static Optional<DyeColor> getEndRodWoolColor(World world, BlockPos pos)
	{
		if (!MicroTimingLoggerManager.isLoggerActivated() || !isPositionAvailable(world, pos))
		{
			return Optional.empty();
		}
		for (Direction facing: DIRECTION_VALUES)
		{
			BlockPos blockEndRodPos = pos.offset(facing);
			BlockState iBlockState = world.getBlockState(blockEndRodPos);
			if (iBlockState.getBlock() == Blocks.END_ROD && iBlockState.get(FacingBlock.FACING).getOpposite() == facing)
			{
				BlockPos woolPos = blockEndRodPos.offset(facing);
				DyeColor color = WoolTool.getWoolColorAtPosition(world, woolPos);
				if (color != null)
				{
					return Optional.of(color);
				}
			}
		}
		return Optional.empty();
	}

	public static Optional<DyeColor> blockUpdateColorGetter(World world, BlockPos pos)
	{
		Optional<DyeColor> optionalDyeColor = Optional.empty();
		if (CarpetTISAdditionSettings.microTimingTarget != MicroTimingTarget.MARKER_ONLY)
		{
			optionalDyeColor = getEndRodWoolColor(world, pos);
		}
		if (!optionalDyeColor.isPresent())
		{
			optionalDyeColor = MicroTimingMarkerManager.getInstance().getColor(world, pos, MicroTimingMarkerType.END_ROD);
		}
		return optionalDyeColor;
	}

	public static Optional<DyeColor> defaultColorGetter(World world, BlockPos pos)
	{
		Optional<DyeColor> optionalDyeColor = Optional.empty();
		boolean usingFallbackColor = false;
		if (CarpetTISAdditionSettings.microTimingTarget != MicroTimingTarget.MARKER_ONLY)
		{
			optionalDyeColor = getWoolColor(world, pos);
			if (!optionalDyeColor.isPresent())
			{
				optionalDyeColor = getEndRodWoolColor(world, pos);
			}
			if (!optionalDyeColor.isPresent())
			{
				switch (CarpetTISAdditionSettings.microTimingTarget)
				{
					case IN_RANGE:
						usingFallbackColor = world.getClosestPlayer(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, MicroTimingTarget.IN_RANGE_RADIUS, player -> true) != null;
						break;
					case ALL:
						usingFallbackColor = true;
						break;
					default:
						break;
				}
				if (usingFallbackColor)
				{
					optionalDyeColor = Optional.of(DyeColor.LIGHT_GRAY);
				}
			}
		}
		if (!optionalDyeColor.isPresent() || usingFallbackColor)
		{
			Optional<DyeColor> markerColor = MicroTimingMarkerManager.getInstance().getColor(world, pos, MicroTimingMarkerType.REGULAR);
			if (markerColor.isPresent())
			{
				optionalDyeColor = markerColor;
			}
		}
		return optionalDyeColor;
	}

	public static BaseText getFormattedDirectionText(Direction direction)
	{
		BaseText translatedName = tr("direction." + direction.toString());
		char sign = direction.getDirection().offset() > 0 ? '+' : '-';
		return Messenger.c(translatedName, String.format("w (%c%s)", sign, direction.getAxis()));
	}

	public static boolean isMarkerEnabled()
	{
		return MicroTimingLoggerManager.isLoggerActivated() && CarpetTISAdditionSettings.microTimingDyeMarker.equals("true");
	}

	public static boolean isPlayerSubscribed(PlayerEntity playerEntity)
	{
		Map<String, String> map = LoggerRegistry.getPlayerSubscriptions(playerEntity.getEntityName());
		return map != null && map.containsKey(MicroTimingLogger.NAME);
	}

	public static List<ServerPlayerEntity> getSubscribedPlayers()
	{
		return CarpetTISAdditionServer.minecraft_server == null ?
				Collections.emptyList() :
				CarpetTISAdditionServer.minecraft_server.getPlayerManager().getPlayerList().stream().
						filter(MicroTimingUtil::isPlayerSubscribed).
						collect(Collectors.toList());
	}
}
