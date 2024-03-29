package harmonised.mco.client.gui;

import harmonised.mco.util.Util;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class HPBar
{
    private long lastUpdate = System.currentTimeMillis();
    private final boolean isPlayer;
    private final LivingEntity livingEntity;
    private float hpPos;
    private float hungerPos, saturationPos;
    private float xRot;
    private float yRot;

    public HPBar(LivingEntity livingEntity)
    {
        this.livingEntity = livingEntity;
        this.hpPos = livingEntity.getHealth() / livingEntity.getMaxHealth();
        if(livingEntity instanceof Player)
        {
            isPlayer = true;
            Player player = (Player) livingEntity;
            this.hungerPos = player.getFoodStats().getFoodLevel() / 40f;
            this.saturationPos = player.getFoodStats().getSaturationLevel() / 40f;
        }
        else
            isPlayer = false;
        this.xRot = livingEntity.rotationPitch;
        this.yRot = livingEntity.rotationYawHead;
    }

    public LivingEntity getLivingEntity()
    {
        return this.livingEntity;
    }

    public void update(float partialTicks)
    {
        //Init
        long ms = System.currentTimeMillis();
        float d = Math.min(1, (ms - lastUpdate) * 0.005f);
        lastUpdate = ms;

        //X Rotation
        xRot = Util.getDeltaChange(xRot, livingEntity.rotationPitch - xRot, d);

        //Y Rotation
        yRot = Util.getDeltaChange(yRot, livingEntity.rotationYawHead - yRot, d);

        //Hunger
        if(isPlayer)
        {
            float hungerRatio = ((Player) livingEntity).getFoodStats().getFoodLevel() / 40f;
            float hungerDiff = hungerRatio - hungerPos;
            hungerPos = Util.getDeltaChange(hungerPos, hungerDiff, d*0.1f);

            float saturationRatio = ((Player) livingEntity).getFoodStats().getSaturationLevel() / 40f;
            float saturationDiff = saturationRatio - saturationPos;
            saturationPos = Util.getDeltaChange(saturationPos, saturationDiff, d*0.1f);
        }

        //Hp
        float hpRatio = Math.min(1, livingEntity.getHealth() / livingEntity.getMaxHealth());
        float hpDiff = hpPos - hpRatio;
        float crucialMultiplier = 0;
        if(hpPos < hpRatio)
            crucialMultiplier = (float) (1/Math.max(0.25, 1-hpRatio));
        else if(hpPos > hpRatio)
            crucialMultiplier = (float) (1/Math.max(0.25, hpRatio));

        hpPos -= Math.min(hpDiff*d*0.1523, d*0.1523) * crucialMultiplier;
        if(Math.abs(hpRatio - hpPos) < 0.0001)
            hpPos = hpRatio;
    }

    public float getHpPos()
    {
        return hpPos;
    }

    public float getHungerPos()
    {
        return hungerPos;
    }

    public float getSaturationPos()
    {
        return saturationPos;
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
