package harmonised.mco.temp;

public class RegionPos
{
    private int x, z;

    public RegionPos(int x, int z )
    {
        this.x = x;
        this.z = z;
    }

    public int getX()
    {
        return x;
    }

    public void setX( int x )
    {
        this.x = x;
    }

    public int getZ()
    {
        return z;
    }

    public void setZ( int z )
    {
        this.z = z;
    }

    @Override
    public String toString()
    {
        return "[RegionPos|X:" + getX() + "|Z:" + getZ() + "]";
    }
}
