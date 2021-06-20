package harmonised.saoui.server;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.WorkbenchContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class ServerHandler
{
    public static void craftItem(ServerPlayerEntity player, ResourceLocation recipeResLoc, int amount )
    {
        PlayerInventory inv = player.inventory;
        World world = player.world;
        MinecraftServer server = player.server;

        CraftingInventory craftInv = new CraftingInventory( new WorkbenchContainer( 1523, inv ), 3, 3 );

        RecipeManager recipeManager = server.getRecipeManager();
        IRecipe recipe = recipeManager.getRecipe( recipeResLoc ).get();
        RecipeItemHelper stackedContents = new RecipeItemHelper();

        System.out.println( recipe.getClass().getName() );
    }
}
