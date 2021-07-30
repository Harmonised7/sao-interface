package harmonised.mco.temp;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class Region
{
    private final ResourceLocation dimResLoc;
    private final String name;
    private final RegionPos pos1, pos2;

    public Region( ResourceLocation dimResLoc, String name, RegionPos pos1, RegionPos pos2 )
    {
        this.dimResLoc = dimResLoc;
        this.name = name;
        this.pos1 = pos1;
        this.pos2 = pos2;
        if( getX2() < getX1() )
        {
            int temp = getX1();
            setX1( getX2() );
            setX2( temp );
        }
        if( getZ2() < getZ1() )
        {
            int temp = getZ1();
            setZ1( getZ2() );
            setZ2( temp );
        }
    }

    public RegionPos getPos1()
    {
        return pos1;
    }

    public RegionPos getPos2()
    {
        return pos2;
    }

    public int getX1()
    {
        return pos1.getX();
    }

    public int getX2()
    {
        return pos2.getX();
    }

    public int getZ1()
    {
        return pos1.getZ();
    }

    public int getZ2()
    {
        return pos2.getZ();
    }

    public void setX1( int x )
    {
        pos1.setX( x );
    }

    public void setX2( int x )
    {
        pos2.setX( x );
    }

    public void setZ1( int z )
    {
        pos1.setZ( z );
    }

    public void setZ2( int z )
    {
        pos2.setZ( z );
    }

    public void setPos1( RegionPos pos )
    {
        setX1( pos.getX() );
        setZ1( pos.getZ() );
    }

    public void setPos2( RegionPos pos )
    {
        setX2( pos.getX() );
        setZ2( pos.getZ() );
    }

    public boolean isInside( BlockPos pos )
    {
        return pos.getX() >= getX1() && pos.getX() <= getX2() && pos.getZ() >= getZ1() && pos.getZ() <= getZ2();
    }

    public boolean isInside( Vector3d pos )
    {
        return isInside( new BlockPos( pos ) );
    }

    public String getName()
    {
        return name;
    }

    public ResourceLocation getDimResLoc()
    {
        return dimResLoc;
    }

    @Override
    public String toString()
    {
        return "[Region|" + getDimResLoc().toString() + "|" + getName() + "|pos1:" + getPos1() + "|pos2:" + getPos2() + "]";
    }
}
