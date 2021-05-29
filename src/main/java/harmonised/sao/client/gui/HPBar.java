package harmonised.sao.client.gui;

import net.minecraft.entity.LivingEntity;

public class HPBar
{
    private long lastUpdate = System.currentTimeMillis();
    private final LivingEntity livingEntity;
    private float hpPos;
    private float xRot;
    private float yRot;

    public HPBar( LivingEntity livingEntity )
    {
        this.livingEntity = livingEntity;
        this.hpPos = livingEntity.getHealth() / livingEntity.getMaxHealth();
        this.xRot = livingEntity.xRot;
        this.yRot = livingEntity.yHeadRot;
    }

    public LivingEntity getLivingEntity()
    {
        return this.livingEntity;
    }

    public void update( float partialTicks )
    {
        //Init
        long ms = System.currentTimeMillis();
        float d = Math.min( 1, ( ms - lastUpdate ) * 0.01f );
        lastUpdate = ms;

        //X Rotation
        xRot += ( livingEntity.xRot - xRot ) * d;

        //Y Rotation
        yRot += ( livingEntity.yHeadRot - yRot ) * d;

        //Hp
        float hpRatio = livingEntity.getHealth() / livingEntity.getMaxHealth();
        float hpDiff = hpPos - hpRatio;
        float crucialMultiplier = 0;
        if( hpPos < hpRatio )
            crucialMultiplier = (float) ( 1/Math.max( 0.25, 1-hpRatio ) );
        else if( hpPos > hpRatio )
            crucialMultiplier = (float) ( 1/Math.max( 0.25, hpRatio ) );

        hpPos -= Math.min( hpDiff*d*0.1523, d*0.1523 ) * crucialMultiplier;
        if( Math.abs( hpRatio - hpPos ) < 0.0001 )
            hpPos = hpRatio;
    }

    public float getHpPos()
    {
        return hpPos;
    }

    public float getXRot()
    {
        return xRot;
    }

    public float getYRot()
    {
        return yRot;
    }
}
