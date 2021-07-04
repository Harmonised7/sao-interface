package harmonised.mco.client.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;

import javax.annotation.Nullable;

public class SaoCraftingContainer extends Container
{
    public SaoCraftingContainer(@Nullable ContainerType<?> type, int id)
    {
        super( type, id );
    }

    @Override
    public boolean canInteractWith(PlayerEntity p_75145_1_)
    {
        return false;
    }
}
