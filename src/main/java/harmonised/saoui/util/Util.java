package harmonised.saoui.util;

import harmonised.saoui.network.MessageSwapItems;
import harmonised.saoui.network.NetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.*;

public class Util
{
    public static ResourceLocation getDimensionResLoc(World world )
    {
        return world.dimension().getRegistryName();
    }

    public static ResourceLocation getResLoc( String regKey )
    {
        try
        {
            return new ResourceLocation( regKey );
        }
        catch( Exception e )
        {
            return new ResourceLocation( "" );
        }
    }

    public static ResourceLocation getResLoc( String firstPart, String secondPart )
    {
        try
        {
            return new ResourceLocation( firstPart, secondPart );
        }
        catch( Exception e )
        {
            return null;
        }
    }

    public static int hueToRGB( float hue, float saturation, float brightness )
    {
        float r = 0, g = 0, b = 0;

        float chroma = brightness * saturation;
        float hue1 = hue/60F;
        float x = chroma * (1- Math.abs((hue1 % 2) - 1));
        switch( (int) hue1 )
        {
            case 0:
                r = chroma;
                g = x;
                b = 0;
                break;

            case 1:
                r = x;
                g = chroma;
                b = 0;
                break;

            case 2:
                r = 0;
                g = chroma;
                b = x;
                break;

            case 3:
                r = 0;
                g = x;
                b = chroma;
                break;

            case 4:
                r = x;
                g = 0;
                b = chroma;
                break;

            case 5:
                r = chroma;
                g = 0;
                b = x;
                break;
        }

        float m = brightness - chroma;
        int r1 = (int) ((r + m) * 255);
        int g1 = (int) ((g + m) * 255);
        int b1 = (int) ((b + m) * 255);
        return r1 << 16 | b1 << 8 | g1;
    }

    public static double map( double input, double inLow, double inHigh, double outLow, double outHigh )
    {
        return ( (input - inLow) / (inHigh - inLow) ) * (outHigh - outLow) + outLow;
    }

    public static double getDistance( int x1, int y1, int x2, int y2 )
    {
        return Math.sqrt( Math.pow( x2 - x1, 2 ) + Math.pow( y2 - y1, 2 ) );
    }

    public static double getDistance( double x1, double y1, double x2, double y2 )
    {
        return Math.sqrt( Math.pow( x2 - x1, 2 ) + Math.pow( y2 - y1, 2 ) );
    }

    public static int getEquipmentSlotInvIndex( EquipmentSlotType type )
    {
        switch ( type )
        {
            case HEAD:
                return 39;

            case CHEST:
                return 38;

            case LEGS:
                return 37;

            case FEET:
                return 36;

            default:
                return 0;
        }
    }

    public static int findEmptyInvSlot( PlayerInventory inv )
    {
        int emptySlot = -1;
        int invSize = inv.items.size();
        for( int i = 0; i < invSize && i < 36; i++ )
        {
            if( inv.getItem( i ).isEmpty() )
                return i;
        }
        return emptySlot;
    }

    public static boolean swapItems( PlayerEntity player, int a, int b )
    {
        {
            PlayerInventory inv = player.inventory;
            ItemStack itemA = inv.getItem( a );
            ItemStack itemB = inv.getItem( b );
            if( inv.canPlaceItem( a, itemB ) && inv.canPlaceItem( b, itemA ) )
            {
                inv.setItem( a, itemB );
                inv.setItem( b, itemA );
                if( player.getCommandSenderWorld().isClientSide() )
                {
                    NetworkHandler.sendToServer( new MessageSwapItems( a, b ) );
                }
                return true;
            }
        }
        return false;
    }

    public static boolean unequipItem( PlayerEntity player, int a )
    {
        int b = findEmptyInvSlot( player.inventory );
        if( b >= 0 )
            return swapItems( player, a, b );
        else
            return false;
    }

    public static int canCraftX( PlayerInventory inv, ItemStack[] ingredients )
    {
        if( ingredients.length == 0 )
            return 0;
        Map<Item, Integer> ingredientsPerCraftMap = new HashMap<>();
        Map<Item, Integer> suppliesMap = new HashMap<>();

        //Init Maps
        for( ItemStack ingredient : ingredients )
        {
            ingredientsPerCraftMap.put( ingredient.getItem(), ingredient.getCount() );
        }

        //Count Inventory Ingredients
        for( ItemStack itemStack : inv.items )
        {
            Item item = itemStack.getItem();
            if( ingredientsPerCraftMap.containsKey( item ) )
            {
                if( !suppliesMap.containsKey( item ) )
                    suppliesMap.put( item, 0 );
                suppliesMap.replace( item, suppliesMap.get( item ) + itemStack.getCount() );
            }
        }

        //Missing Ingredients
        if( suppliesMap.size() < ingredientsPerCraftMap.size() )
            return 0;
        Integer lowestCraftX = null;

        for( Map.Entry<Item, Integer> supply : suppliesMap.entrySet() )
        {
            int canCraftX = supply.getValue() / ingredientsPerCraftMap.get( supply.getKey() );
            if( lowestCraftX == null || canCraftX < lowestCraftX )
                lowestCraftX = canCraftX;
        }

        return lowestCraftX == null ? 0 : lowestCraftX;
    }

    public static float getDeltaChange( float original, float diff, float d )
    {
        if( Math.abs( diff ) < 0.001 )
            return original + diff;
        else
            return original + diff * d;
    }
}