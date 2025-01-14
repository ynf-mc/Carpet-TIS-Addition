package carpettisaddition.helpers.rule.optimizedFastEntityMovement;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.CollisionView;

import java.util.stream.Stream;

@FunctionalInterface
public interface CollisionBoxGetter
{
	//#if MC >= 11800
	//$$ Iterable<VoxelShape>
	//#else
	Stream<VoxelShape>
	//#endif
	get(CollisionView world, Entity entity, Box box);
}