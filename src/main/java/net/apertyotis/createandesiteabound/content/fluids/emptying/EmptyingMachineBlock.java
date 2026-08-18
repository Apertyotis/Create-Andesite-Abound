package net.apertyotis.createandesiteabound.content.fluids.emptying;

import net.apertyotis.createandesiteabound.AllBlockEntityType;
import net.apertyotis.createandesiteabound.content.fluids.AbstractFluidMachineBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class EmptyingMachineBlock extends AbstractFluidMachineBlock<EmptyingMachineBlockEntity> {
    public EmptyingMachineBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public Class<EmptyingMachineBlockEntity> getBlockEntityClass() {
        return EmptyingMachineBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends EmptyingMachineBlockEntity> getBlockEntityType() {
        return AllBlockEntityType.EMPTYING_MACHINE.get();
    }
}
