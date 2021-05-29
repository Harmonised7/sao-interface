package harmonised.sao.server;

import harmonised.sao.client.gui.ListButton;
import harmonised.sao.client.gui.SaoCraftingContainer;
import harmonised.sao.network.MessageCraft;
import harmonised.sao.network.NetworkHandler;
import net.minecraft.client.gui.recipebook.RecipeList;
import net.minecraft.client.util.ClientRecipeBook;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.CraftingResultSlot;
import net.minecraft.inventory.container.WorkbenchContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.hooks.BasicEventHooks;

public class ServerHandler
{
    public static void craftItem(ServerPlayerEntity player, ResourceLocation recipeResLoc, int amount )
    {
        PlayerInventory inv = player.inventory;
        World world = player.level;
        MinecraftServer server = player.server;

        CraftingInventory craftInv = new CraftingInventory( new WorkbenchContainer( 1523, inv ), 3, 3 );

        RecipeManager recipeManager = server.getRecipeManager();
        IRecipe recipe = recipeManager.byKey( recipeResLoc ).get();

        System.out.println( recipe.getClass().getName() );

//        inv.fillStackedContents( craftInv );

        net.minecraftforge.common.ForgeHooks.setCraftingPlayer( player );
        NonNullList<ItemStack> nonnulllist = recipeManager.getRemainingItemsFor( IRecipeType.CRAFTING, craftInv, world );
        net.minecraftforge.common.ForgeHooks.setCraftingPlayer(null);
        for(int i = 0; i < nonnulllist.size(); ++i) {
            ItemStack itemstack = craftInv.getItem(i);
            ItemStack itemstack1 = nonnulllist.get(i);
            if (!itemstack.isEmpty()) {
                craftInv.removeItem(i, 1);
                itemstack = craftInv.getItem(i);
            }

            if (!itemstack1.isEmpty()) {
                if (itemstack.isEmpty()) {
                    inv.setItem(i, itemstack1);
                } else if (ItemStack.isSame(itemstack, itemstack1) && ItemStack.tagMatches(itemstack, itemstack1)) {
                    itemstack1.grow(itemstack.getCount());
                    craftInv.setItem(i, itemstack1);
                } else if (inv.add(itemstack1)) {
                    player.drop(itemstack1, false);
                }
            }
        }
        System.out.println( recipe.getResultItem().getDisplayName().getString() );
    }
}
