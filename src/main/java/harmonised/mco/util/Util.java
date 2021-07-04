package harmonised.mco.util;

import harmonised.mco.network.MessageSwapItems;
import harmonised.mco.network.NetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class Util
{
    public static ResourceLocation getDimensionResLoc(World world )
    {
        return world.getDimensionKey().getRegistryName();
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

    public static double mapCapped( double input, double inLow, double inHigh, double outLow, double outHigh )
    {
        if( input < inLow )
            input = inLow;
        if( input > inHigh )
            input = inHigh;

        return map( input, inLow, inHigh, outLow, outHigh );
    }

    public static double getDistance( int x1, int y1, int x2, int y2 )
    {
        return Math.sqrt( Math.pow( x2 - x1, 2 ) + Math.pow( y2 - y1, 2 ) );
    }

    public static double getDistance( double x1, double y1, double x2, double y2 )
    {
        return Math.sqrt( Math.pow( x2 - x1, 2 ) + Math.pow( y2 - y1, 2 ) );
    }

    public static double getDistance( Vector3d pos1, Vector3d pos2 )
    {
        return Math.sqrt( Math.pow( pos2.x - pos1.x, 2 ) + Math.pow( pos2.y - pos1.y, 2 )+ Math.pow( pos2.z - pos1.z, 2 ) );
    }

    public static double getDistance( double x1, double y1, double z1, double x2, double y2, double z2 )
    {
        return Math.sqrt( Math.pow( x2 - x1, 2 ) + Math.pow( y2 - y1, 2 )+ Math.pow( z2 - z1, 2 ) );
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
        int invSize = inv.mainInventory.size();
        for( int i = 0; i < invSize && i < 36; i++ )
        {
            if( inv.getStackInSlot( i ).isEmpty() )
                return i;
        }
        return emptySlot;
    }

    public static boolean swapItems( PlayerEntity player, int a, int b )
    {
        {
            PlayerInventory inv = player.inventory;
            ItemStack itemA = inv.getStackInSlot( a );
            ItemStack itemB = inv.getStackInSlot( b );
            if( inv.isItemValidForSlot( a, itemB ) && inv.isItemValidForSlot( b, itemA ) )
            {
                inv.setInventorySlotContents( a, itemB );
                inv.setInventorySlotContents( b, itemA );
                if( player.getEntityWorld().isRemote() )
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
        for( ItemStack itemStack : inv.mainInventory )
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

    public static double cap( double input, double min, double max )
    {
        return Math.max( min, Math.min( max, input ) );
    }

    public static CompoundNBT entityEffectInstanceMapToNBT( Map<Integer, List<EffectInstance>> effects )
    {
        CompoundNBT nbt = new CompoundNBT();

        for( Map.Entry<Integer, List<EffectInstance>> entry : effects.entrySet() )
        {
            CompoundNBT effectsNBT = new CompoundNBT();
            for( EffectInstance effect : entry.getValue() )
            {
                CompoundNBT effectNBT = new CompoundNBT();

                effectNBT.putInt( "amp", effect.getAmplifier() );
                effectNBT.putInt( "time", effect.getDuration() );

                effectsNBT.put( effect.getPotion().getRegistryName().toString(), effectNBT );
            }

            nbt.put( "" + entry.getKey(), effectsNBT );
        }

        return nbt;
    }

    public static Map<Integer, List<EffectInstance>> nbtToEntityEffectInstanceMap( CompoundNBT nbt )
    {
        Map<Integer, List<EffectInstance>> effects = new HashMap<>();

        for( String mobId : nbt.keySet() )
        {
            List<EffectInstance> effectInstances = new ArrayList<>();

            CompoundNBT effectInstancesNBT = nbt.getCompound( mobId );
            for( String effectKey : effectInstancesNBT.keySet() )
            {
                CompoundNBT effectInstanceNBT = effectInstancesNBT.getCompound( effectKey );
                ResourceLocation effectResLoc = new ResourceLocation( effectKey );
                if( ForgeRegistries.POTIONS.containsKey( effectResLoc ) )
                    effectInstances.add( new EffectInstance( ForgeRegistries.POTIONS.getValue( effectResLoc ), effectInstanceNBT.getInt( "time" ), effectInstanceNBT.getInt( "amp" ) ) );
            }

            effects.put( Integer.parseInt( mobId ), effectInstances );
        }

        return effects;
    }

    public static boolean isReleased()
    {
        return FMLEnvironment.production;
    }

    public static String toStamp( double input )
    {
        int seconds = (int) input % 60;
        return (int) ( input / 60 ) + ":" + ( seconds > 9 ? "" : 0 ) + seconds;
    }

    public static int multiplyAlphaColor( int alpha, int color )
    {
        int output = (int) ( (alpha/255f) * ( ( getAlphaFromColor( color ) ) ) );
        return output;
    }

    public static int getAlphaFromColor( int color )
    {
        return ( color & 0xff000000 ) >> 24 & 0xff;
    }

    public static String intToHexString( int number )
    {
        return String.format("0x%08X", number );
    }

    public static boolean canCraft( PlayerInventory inputInv, IRecipe recipe )
    {
        PlayerInventory tempInv = new PlayerInventory( inputInv.player );
        Map<Item, Integer> items = new HashMap<>();

        //Sort Inventory
        for( ItemStack itemStack : inputInv.mainInventory )
        {
            Item item = itemStack.getItem();
            int count = itemStack.getCount();
            items.put( item, items.containsKey( item ) ? items.get( item ) + count : count );
        }

        for( Map.Entry<Item, Integer> item : items.entrySet() )
        {
            tempInv.addItemStackToInventory( new ItemStack( item.getKey(), item.getValue() ) );
        }

        for( Object obj : recipe.getIngredients() )
        {
            Ingredient ingredient = (Ingredient) obj;
            for( ItemStack itemStack : tempInv.mainInventory )
            {
                if( ingredient.test( itemStack ) )
                    break;
                return false;
            }
        }

        return true;
    }
}