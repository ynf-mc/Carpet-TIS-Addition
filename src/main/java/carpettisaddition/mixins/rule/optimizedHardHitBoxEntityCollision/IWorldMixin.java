package carpettisaddition.mixins.rule.optimizedHardHitBoxEntityCollision;

import carpettisaddition.CarpetTISAdditionSettings;
import carpettisaddition.helpers.rule.optimizedHardHitBoxEntityCollision.OptimizedHardHitBoxEntityCollisionHelper;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.EntityView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

//#if MC >= 11800
//$$ import org.jetbrains.annotations.Nullable;
//$$ import java.util.List;
//#else
import java.util.stream.Stream;
//#endif

//#if MC >= 11600
//$$ import net.minecraft.world.RegistryWorldView;
//#else
import net.minecraft.world.IWorld;
//#endif

//#if MC < 11600
import java.util.Set;
//#endif

//#if MC >= 11600 && MC < 11800
//$$ import java.util.function.Predicate;
//#endif

@Mixin(
		//#if MC >= 11600
		//$$ value = RegistryWorldView.class,
		//#else
		value = IWorld.class,
		//#endif
		priority = 2000
)
public interface IWorldMixin extends EntityView
{
	/**
	 * @reason Interface injection is not supported by Mixin yet
	 * So here comes the @Overwrite
	 *
	 * @author Fallen_Breath
	 */
	@Overwrite
	//#if MC >= 11800
	//$$ default List<VoxelShape> getEntityCollisions(@Nullable Entity entity, Box box)
	//#elseif MC >= 11600
	//$$ default Stream<VoxelShape> getEntityCollisions(Entity entity, Box box, Predicate<Entity> predicate)
	//#elseif MC >= 11500
	default Stream<VoxelShape> getEntityCollisions(Entity entity, Box box, Set<Entity> excluded)
	//#else
	//$$ default Stream<VoxelShape> method_20743(Entity entity, Box box, Set<Entity> excluded)
	//#endif
	{
		try
		{
			if (CarpetTISAdditionSettings.optimizedHardHitBoxEntityCollision)
			{
				if (!OptimizedHardHitBoxEntityCollisionHelper.treatsGeneralEntityAsHardHitBox(entity))
				{
					OptimizedHardHitBoxEntityCollisionHelper.checkHardHitBoxEntityOnly.set(true);
				}
			}

			// vanilla copy
			return EntityView.super.
					//#if MC >= 11800
					//$$ getEntityCollisions(entity, box);
					//#elseif MC >= 11600
					//$$ getEntityCollisions(entity, box, predicate);
					//#elseif MC >= 11500
					getEntityCollisions(entity, box, excluded);
					//#else
					//$$ method_20743(entity, box, excluded);
					//#endif

		}
		finally
		{
			OptimizedHardHitBoxEntityCollisionHelper.checkHardHitBoxEntityOnly.set(false);
		}
	}
}
