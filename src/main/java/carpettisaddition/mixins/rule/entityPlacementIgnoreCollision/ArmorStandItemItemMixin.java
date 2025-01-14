package carpettisaddition.mixins.rule.entityPlacementIgnoreCollision;

import carpettisaddition.CarpetTISAdditionSettings;
import net.minecraft.entity.Entity;
import net.minecraft.item.ArmorStandItem;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

//#if MC >= 11600
//$$ import net.minecraft.util.math.Box;
//$$ import java.util.function.Predicate;
//#else
import carpettisaddition.utils.GameUtil;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
//#endif

@Mixin(ArmorStandItem.class)
public abstract class ArmorStandItemItemMixin
{
	//#if MC >= 11600
	//$$ @Redirect(
	//$$ 		method = "useOnBlock",
	//$$ 		at = @At(
	//$$ 				value = "INVOKE",
	//$$ 				target = "Lnet/minecraft/world/World;isSpaceEmpty(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;)Z"
	//$$ 		),
	//$$ 		require = 0
	//$$ )
	//$$ private boolean entityPlacementIgnoreCollision_makeIfCondition1true(World world, Entity entity, Box box, Predicate<Entity> predicate)
	//$$ {
	//$$ 	if (CarpetTISAdditionSettings.entityPlacementIgnoreCollision)
	//$$ 	{
	//$$ 		return true;
	//$$ 	}
	//$$ 	// vanilla
	//$$ 	return world.isSpaceEmpty(
	//$$  			entity, box
	 			//#if MC < 11800
	//$$  			, predicate
	 			//#endif
	//$$ 	);
	//$$ }
 //$$
	//$$ @Redirect(
	//$$ 		method = "useOnBlock",
	//$$ 		at = @At(
	//$$ 				value = "INVOKE",
	//$$ 				target = "Ljava/util/List;isEmpty()Z",
	//$$ 				remap = false
	//$$ 		),
	//$$ 		require = 0
	//$$ )
	//$$ private boolean entityPlacementIgnoreCollision_makeIfCondition2true(List<Entity> entityList)
	//$$ {
	//$$ 	if (CarpetTISAdditionSettings.entityPlacementIgnoreCollision)
	//$$ 	{
	//$$ 		return true;
	//$$ 	}
	//$$ 	// vanilla
	//$$ 	return entityList.isEmpty();
	//$$ }
 //$$
	//#else

	private static final ThreadLocal<ItemPlacementContext> currentContext$TISCM = ThreadLocal.withInitial(() -> null);

	/**
	 * Make the first segment of the if statement below always returns true
	 * if (itemPlacementContext.canPlace() && world.getBlockState(blockPos2).canReplace(itemPlacementContext))
	 *           ^ handled here
	 */
	@ModifyVariable(
			method = "useOnBlock",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/item/ItemPlacementContext;canPlace()Z"
			)
	)
	private ItemPlacementContext entityPlacementIgnoreCollision_makeIfCondition1true(ItemPlacementContext itemPlacementContext)
	{
		if (CarpetTISAdditionSettings.entityPlacementIgnoreCollision)
		{
			((ItemPlacementContextAccessor)itemPlacementContext).setCanReplaceExisting(true);
			currentContext$TISCM.set(itemPlacementContext);
		}
		return itemPlacementContext;
	}

	/**
	 * Make the first segment of the if statement below always returns true
	 * if (itemPlacementContext.canPlace() && world.getBlockState(blockPos2).canReplace(itemPlacementContext))
	 *                                                                         ^ handled here
	 */
	@Redirect(
			method = "useOnBlock",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/block/BlockState;canReplace(Lnet/minecraft/item/ItemPlacementContext;)Z"
			),
			require = 0
	)
	private boolean entityPlacementIgnoreCollision_makeIfCondition2true(BlockState blockState, ItemPlacementContext ctx)
	{
		if (CarpetTISAdditionSettings.entityPlacementIgnoreCollision)
		{
			return true;
		}
		// vanilla
		return blockState.canReplace(ctx);
	}

	@ModifyVariable(
			method = "useOnBlock",
			at = @At(
					value = "INVOKE_ASSIGN",
					target = "Lnet/minecraft/world/World;getEntities(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;)Ljava/util/List;",
					shift = At.Shift.AFTER
			)
	)
	private List<Entity> entityPlacementIgnoreCollision_skipEntityCheck(List<Entity> entities)
	{
		if (CarpetTISAdditionSettings.entityPlacementIgnoreCollision)
		{
			entities.clear();
		}
		return entities;
	}

	@ModifyArg(
			method = "useOnBlock",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/World;removeBlock(Lnet/minecraft/util/math/BlockPos;Z)Z"
			),
			require = 2
	)
	private BlockPos entityPlacementIgnoreCollision_dontRemoveBlockIfNotReplaceable(BlockPos pos)
	{
		if (CarpetTISAdditionSettings.entityPlacementIgnoreCollision)
		{
			ItemPlacementContext ctx = currentContext$TISCM.get();
			World world = ctx.getWorld();
			if (!world.getBlockState(pos).canReplace(ctx))
			{
				pos = GameUtil.getInvalidBlockPos();
			}
		}
		return pos;
	}
	//#endif
}
