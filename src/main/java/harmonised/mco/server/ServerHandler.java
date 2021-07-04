package harmonised.mco.server;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.RecipeBookContainer;
import net.minecraft.inventory.container.WorkbenchContainer;
import net.minecraft.item.crafting.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class ServerHandler
{
    public static void craftItem(ServerPlayerEntity player, ResourceLocation recipeResLoc, int amount )
    {
        try
        {

            MinecraftServer server = player.server;
            RecipeManager recipeManager = server.getRecipeManager();
            IRecipe recipe = recipeManager.getRecipe( recipeResLoc ).get();
            if( !recipe.isDynamic() )
            {
                PlayerInventory inv = player.inventory;
                World world = player.world;

//                IntList intList = new IntArrayList();
//                for( ItemStack itemStack : inv.mainInventory )
//                {
//                    intList.add( RecipeItemHelper.pack( itemStack ) );
//                }

//                CraftingInventory craftInv = new CraftingInventory( new WorkbenchContainer( 1523, inv ), 3, 3 );
                RecipeBookContainer<?> recipeBookContainer = new WorkbenchContainer( 1523, inv );
                RecipeItemHelper stackedContents = new RecipeItemHelper();
                stackedContents.clear();
                inv.accountStacks( stackedContents );
                recipeBookContainer.fillStackedContents( stackedContents );
                System.out.println( stackedContents );
//                craftInv.fillStackedContents( stackedContents );
//                System.out.println( stackedContents.canCraft( recipe, intList ) );
            }
        }
        catch( Exception e )
        {
            System.out.println( e );
        }
    }
}
