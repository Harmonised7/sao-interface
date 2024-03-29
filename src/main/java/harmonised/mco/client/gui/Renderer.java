package harmonised.mco.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import harmonised.mco.confefeg.McoConfefeg;
import harmonised.mco.temp.Region;
import harmonised.mco.temp.RegionPos;
import harmonised.mco.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

public class Renderer
{
    private static final ResourceLocation FORCEFIELD_TEXTURES = new ResourceLocation("textures/misc/forcefield.png");
    public static final Map<LivingEntity, HPBar> hpBars = new HashMap<>();
    public static Map<Integer, List<EffectInstance>> effects = new HashMap<>();
    private static final Minecraft mc = Minecraft.getInstance();

    private static int blitOffset = 0;
    private static float w, h, offset, scale, polyDegRange, degOffset, polyDegStep, livingEntityWidth;

    //HP Bar
    private static final int fullBarWidth = 1024;
    private static final int barHeight = 160;
    private static final int barElements = 4;
    private static final int outsideWallWidth = 2;

    private static float hpBarPitchPlayer;
    private static int hpBarPolyCount;
    private static float hpBarPolyWidth;
    private static int renderDistance;

    //Indicator
    private static int indicatorWidth = 128;
    private static int indicatorHeight = 256;
    public static Set<Integer> attackers = new HashSet<>();
    public static Set<Integer> invisibles = new HashSet<>();

    //Effect Indicator
    private static int effectIndicatorSize = 128;
    private static float indicatorDegs;

    @SubscribeEvent
    public void handleRender(RenderLevelLastEvent event)
    {
        Player player = mc.player;
        float partialTicks = event.getPartialTicks();
        Level world = mc.level;
        Vec3 cameraCenter = mc.getRenderManager().info.getProjectedView();
        BlockPos originBlockPos = mc.getRenderManager().info.getBlockPos();
        PoseStack stack = event.getPoseStack();
        stack.push();
//        IRenderTypeBuffer.Impl buffer = mc.renderBuffers().bufferSource();
        RenderSystem.enableBlend();
        //Align to world
        stack.translate(-cameraCenter.getX() + 0.5, -cameraCenter.getY() + 0.5, -cameraCenter.getZ() + 0.5);
        stack.rotate(Vector3f.XP.rotationDegrees(180.0F));

        drawMcoUi(stack, player, partialTicks);
        int i = 0;
        for(LivingEntity livingEntity : world.getLoadedEntitiesWithinAABB(LivingEntity.class, new AxisAlignedBB(originBlockPos.up(renderDistance).north(renderDistance).east(renderDistance), originBlockPos.down(renderDistance).south(renderDistance).west(renderDistance))))
        {
            if(invisibles.contains(livingEntity.getEntityId()) || livingEntity == player)
                continue;
            drawMcoUi(stack, livingEntity, partialTicks);
            if(i++ > 300)
                break;
        }

        stack.pop();
    }

    public static void renderAntiGriefRegions(RenderLevelLastEvent event)
    {
        PoseStack stack = event.getPoseStack();
        Player player = mc.player;
        RenderSystem.setShaderTexture(0,FORCEFIELD_TEXTURES);
        stack.push();
        Matrix4f matrix = stack.getLast().getMatrix();
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        int color = 0xfffff;

        mirrorBlitColor(stack, -3, 3, -3, 3, 0, indicatorWidth, indicatorHeight, 0, 0, indicatorWidth, indicatorHeight, color);
        stack.rotate(Vector3f.YP.rotationDegrees(90));
        mirrorBlitColor(stack, -3, 3, -3, 3, 0, indicatorWidth, indicatorHeight, 0, 0, indicatorWidth, indicatorHeight, color);
        stack.pop();
    }

    public static void renderBorders(PoseStack stack)
    {
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
//        WorldBorder worldborder = mc.level.getWorldBorder();
        Region region = new Region(mc.level.getDimensionKey().getRegistryName(), "potato", new RegionPos(-25, -5), new RegionPos(5, 5));
        double d0 = (double)(mc.gameSettings.renderDistanceChunks * 16);
        ActiveRenderInfo activeRenderInfo = mc.gameRenderer.getActiveRenderInfo();
        Vec3 cameraCenter = activeRenderInfo.getProjectedView();
        if (!(cameraCenter.x < region.getX2() - d0) || !(cameraCenter.x > region.getX1() + d0) || !(cameraCenter.z < region.getZ2() - d0) || !(cameraCenter.z > region.getZ1() + d0))
        {
            double d1 = 1.0D - getClosestDistance(region, cameraCenter.x, cameraCenter.z) / d0;
            d1 = Math.pow(d1, 4.0D);
            double d2 = cameraCenter.x;
            double d3 = cameraCenter.y;
            double d4 = cameraCenter.z;
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            RenderSystem.setShaderTexture(0,FORCEFIELD_TEXTURES);
            RenderSystem.depthMask(Minecraft.isFabulousGraphicsEnabled());
            RenderSystem.pushMatrix();
            stack.scale(1, -1, -1);
            RenderSystem.multMatrix(stack.getLast().getMatrix());
//            int i = BorderStatus.STATIONARY.getColor();
            int i = 0xff00ff;
            float red = (float)(i >> 16 & 255) / 255.0F;
            float green = (float)(i >> 8 & 255) / 255.0F;
            float blue = (float)(i & 255) / 255.0F;
            RenderSystem.color4f(red, green, blue, (float)d1);
            RenderSystem.polygonOffset(-3.0F, -3.0F);
            RenderSystem.enablePolygonOffset();
            RenderSystem.defaultAlphaFunc();
            RenderSystem.enableAlphaTest();
            RenderSystem.disableCull();
            float f3 = (float)(net.minecraft.util.Util.milliTime() % 3000L) / 3000.0F;
            float f4 = 0.0F;
            float f5 = 0.0F;
            float f6 = 128.0F;
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
            double d5 = Math.max((double) MathHelper.floor(d4 - d0), region.getZ1());
            double d6 = Math.min((double)MathHelper.ceil(d4 + d0), region.getZ2());
            float f11;
            double d11;
            double d14;
            float f14;
            if (d2 > region.getX2() - d0)
            {
                f11 = 0.0F;

                for(d11 = d5; d11 < d6; f11 += 0.5F)
                {
                    d14 = Math.min(1.0D, d6 - d11);
                    f14 = (float)d14 * 0.5F;
                    addVertex(bufferbuilder, d2, d3, d4, region.getX2(), 256, d11, f3 + f11, f3 + 0.0F);
                    addVertex(bufferbuilder, d2, d3, d4, region.getX2(), 256, d11 + d14, f3 + f14 + f11, f3 + 0.0F);
                    addVertex(bufferbuilder, d2, d3, d4, region.getX2(), 0, d11 + d14, f3 + f14 + f11, f3 + 128.0F);
                    addVertex(bufferbuilder, d2, d3, d4, region.getX2(), 0, d11, f3 + f11, f3 + 128.0F);
                    ++d11;
                }
            }

            if (d2 < region.getX1() + d0)
            {
                f11 = 0.0F;

                for(d11 = d5; d11 < d6; f11 += 0.5F)
                {
                    d14 = Math.min(1.0D, d6 - d11);
                    f14 = (float)d14 * 0.5F;
                    addVertex(bufferbuilder, d2, d3, d4, region.getX1(), 256, d11, f3 + f11, f3 + 0.0F);
                    addVertex(bufferbuilder, d2, d3, d4, region.getX1(), 256, d11 + d14, f3 + f14 + f11, f3 + 0.0F);
                    addVertex(bufferbuilder, d2, d3, d4, region.getX1(), 0, d11 + d14, f3 + f14 + f11, f3 + 128.0F);
                    addVertex(bufferbuilder, d2, d3, d4, region.getX1(), 0, d11, f3 + f11, f3 + 128.0F);
                    ++d11;
                }
            }

            d5 = Math.max((double)MathHelper.floor(d2 - d0), region.getX1());
            d6 = Math.min((double)MathHelper.ceil(d2 + d0), region.getX2());
            if (d4 > region.getZ2() - d0)
            {
                f11 = 0.0F;

                for(d11 = d5; d11 < d6; f11 += 0.5F)
                {
                    d14 = Math.min(1.0D, d6 - d11);
                    f14 = (float)d14 * 0.5F;
                    addVertex(bufferbuilder, d2, d3, d4, d11, 256, region.getZ2(), f3 + f11, f3 + 0.0F);
                    addVertex(bufferbuilder, d2, d3, d4, d11 + d14, 256, region.getZ2(), f3 + f14 + f11, f3 + 0.0F);
                    addVertex(bufferbuilder, d2, d3, d4, d11 + d14, 0, region.getZ2(), f3 + f14 + f11, f3 + 128.0F);
                    addVertex(bufferbuilder, d2, d3, d4, d11, 0, region.getZ2(), f3 + f11, f3 + 128.0F);
                    ++d11;
                }
            }

            if (d4 < region.getZ1() + d0)
            {
                f11 = 0.0F;

                for(d11 = d5; d11 < d6; f11 += 0.5F)
                {
                    d14 = Math.min(1.0D, d6 - d11);
                    f14 = (float)d14 * 0.5F;
                    addVertex(bufferbuilder, d2, d3, d4, d11, 256, region.getZ1(), f3 + f11, f3 + 0.0F);
                    addVertex(bufferbuilder, d2, d3, d4, d11 + d14, 256, region.getZ1(), f3 + f14 + f11, f3 + 0.0F);
                    addVertex(bufferbuilder, d2, d3, d4, d11 + d14, 0, region.getZ1(), f3 + f14 + f11, f3 + 128.0F);
                    addVertex(bufferbuilder, d2, d3, d4, d11, 0, region.getZ1(), f3 + f11, f3 + 128.0F);
                    ++d11;
                }
            }

            bufferbuilder.finishDrawing();
            WorldVertexBufferUploader.draw(bufferbuilder);
            RenderSystem.enableCull();
            RenderSystem.disableAlphaTest();
            RenderSystem.polygonOffset(0.0F, 0.0F);
            RenderSystem.disablePolygonOffset();
            RenderSystem.enableAlphaTest();
            RenderSystem.disableBlend();
            RenderSystem.popMatrix();
            RenderSystem.depthMask(true);
        }
    }

    public static void addVertex(BufferBuilder bufferIn, double camX, double camY, double camZ, double xIn, int yIn, double zIn, float texU, float texV)
    {
        bufferIn.pos(xIn - camX, (double)yIn - camY, zIn - camZ).tex(texU, texV).endVertex();
    }

    public static double getClosestDistance(Region region, double x, double z)
    {
        double d0 = z - region.getZ1();
        double d1 = region.getZ2() - z;
        double d2 = x - region.getX1();
        double d3 = region.getX2() - x;
        double d4 = Math.min(d2, d3);
        d4 = Math.min(d4, d0);
        return Math.min(d4, d1);
    }

    public static void drawMcoUi(PoseStack stack, LivingEntity livingEntity, float partialTicks)
    {
        stack.push();
        Vec3 livingEntityEyePos = livingEntity.getEyePosition(partialTicks);
        stack.translate(livingEntityEyePos.getX() - 0.5, -livingEntityEyePos.getY() + 0.5, -livingEntityEyePos.getZ() + 0.5);

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        if(McoConfefeg.hpBarEnabled.get())
            drawHpBar(stack, livingEntity, partialTicks);
        if(McoConfefeg.npcIndicatorEnabled.get())
            drawNPCIndicator(stack, livingEntity);

        stack.pop();
    }

    public static void drawHpBar(PoseStack stack, LivingEntity livingEntity, float partialTicks)
    {
        stack.push();
        Player player = mc.player;

        hpBarPolyCount = McoConfefeg.hpBarPolyCount.get();
        hpBarPitchPlayer = McoConfefeg.hpBarPitchPlayer.get();
        renderDistance = McoConfefeg.renderDistance.get();
        hpBarPolyWidth = fullBarWidth/(float) hpBarPolyCount;

        if(!hpBars.containsKey(livingEntity) || hpBars.get(livingEntity).getLivingEntity() != livingEntity)
            hpBars.put(livingEntity, new HPBar(livingEntity));
        HPBar hpBar = hpBars.get(livingEntity);
        if(!mc.isGamePaused())
            hpBar.update(partialTicks);

        boolean isPlayer = livingEntity == player;
        if(isPlayer)
        {
//            return;
        }

        if(isPlayer)
        {
//            RenderSystem.disableDepthTest();
            scale = McoConfefeg.hpBarScalePlayer.get();
        }
        else
        {
            scale = McoConfefeg.hpBarScaleOthers.get();
        }

        stack.rotate(Vector3f.YP.rotationDegrees(hpBar.getYRot()));    //Vertical Rotation
        stack.rotate(Vector3f.XP.rotationDegrees(hpBar.getXRot() + (isPlayer ? hpBarPitchPlayer : 0))); //Horizontal Rotation
        float maxHp = livingEntity.getMaxHealth();
        float currHp = livingEntity.getHealth();
        float hpRatio = Math.min(1, currHp / maxHp);
        float hpBarHpRatio = hpBar.getHpPos();

        polyDegRange      = (Math.max(20, Math.min(270, 60 * maxHp * 0.1f))) * scale;
        degOffset         = 180 - polyDegRange/2 + McoConfefeg.hpBarOffsetDeg.get();
        polyDegStep       = polyDegRange / hpBarPolyCount;
        livingEntityWidth = livingEntity.getBbWidth();
        offset            = livingEntityWidth * McoConfefeg.hpBarOffset.get();
        indicatorDegs = (float) Math.atan(256/offset) * 4f * McoConfefeg.effectIndicatorBaseSize.get();
        w = (float) (2*offset*Math.tan(Math.toRadians(polyDegStep/2)));
        h = livingEntity.getHeight() * 0.1f * scale;

        ITextComponent livingEntityNameComp = livingEntity.getName();
        String livingEntityName = livingEntityNameComp.getString();


        //Name
        if(isPlayer)
        {
            {
                float nameTextScale = 100 / livingEntityWidth / (isPlayer ? 0.7f : 3);
                Vector3f nameTextPos = new Vector3f(-w/2f, -h/2f - 0.1f, offset+0.02f);
//                drawCurvedText(stack, livingEntityName, textPos, textScale, offset, degOffset, isPlayer);
                drawCurvedText(stack, livingEntityName, nameTextPos, nameTextScale, degOffset + livingEntityWidth, isPlayer);

                if(player.isCreative())
                    return;

                float xpTextScale = 100 / livingEntityWidth / 0.3f;
                Vector3f levelTextPos = new Vector3f(-w/2f, -h/2f - 0.04f, offset+0.02f);

                drawCurvedText(stack, (int) (player.xpBarCap() * player.experience) + "/" + player.xpBarCap() + " LV: " + player.experienceLevel, levelTextPos, xpTextScale, degOffset + 17 + (livingEntity.getMaxHealth() - 20)*1, isPlayer);
            }
        }

        RenderSystem.setShaderTexture(0,Icons.HP_BAR);

        //Draw bar outside
        for(int i = 0; i < hpBarPolyCount; i++)
        {
            stack.push();
            stack.rotate(Vector3f.YP.rotationDegrees(polyDegStep*i + degOffset));
            stack.translate(-w/2f, -h/2f - 0.1f, offset);
            mirrorBlitColor(stack, 0, w, 0, h, 0, hpBarPolyWidth, barHeight, hpBarPolyWidth *i, isPlayer ? barHeight : 0, fullBarWidth, barHeight*barElements, McoConfefeg.hpBarOutsideColor.get());
            stack.pop();
        }

        float drawnRatio, nextDrawRatio, minPolyU = 0, maxPolyU = 1;

        boolean hpLoss = hpRatio < hpBarHpRatio;

        float tempHpBarHpRatio = hpLoss ? hpRatio : hpBarHpRatio;

        //Draw bar inside
        for(int i = 0; i < hpBarPolyCount; i++)
        {
            drawnRatio = i / (float) hpBarPolyCount;
            nextDrawRatio = (i+1) / (float) hpBarPolyCount;
            if(tempHpBarHpRatio < nextDrawRatio)
                maxPolyU = (float) Util.map(tempHpBarHpRatio, drawnRatio, nextDrawRatio, 0, 1);
            stack.push();
            stack.rotate(Vector3f.YP.rotationDegrees(polyDegStep*i + degOffset));
            stack.translate(-w/2f, -h/2f - 0.1f, offset);
            mirrorBlitColor(stack, w*minPolyU, w*maxPolyU, 0, h, 0, hpBarPolyWidth *maxPolyU - hpBarPolyWidth *minPolyU, barHeight, hpBarPolyWidth *i + hpBarPolyWidth *minPolyU, barHeight*2, fullBarWidth, barHeight*barElements, Util.hueToRGB((float) Util.map(hpBarHpRatio, 0, 1, McoConfefeg.hpBarEndHue.get(), McoConfefeg.hpBarStartHue.get()), 1, 1));
            stack.pop();
            if(maxPolyU < 1)
                break;
        }

        minPolyU = maxPolyU;
        maxPolyU = 1;

        //Draw Gain/Loss Indicator
        if(hpBarHpRatio < hpRatio)
        {
            int firstIndicatorPoly = (int) (hpBarPolyCount *hpBarHpRatio);
            int lastIndicatorPoly = (int) (hpBarPolyCount *hpRatio)+1;
            //Gain
            for(int i = firstIndicatorPoly; i < lastIndicatorPoly; i++)
            {
                drawnRatio = i / (float) hpBarPolyCount;
                nextDrawRatio = (i+1) / (float) hpBarPolyCount;

                if(i+1 == lastIndicatorPoly)
                    maxPolyU = (float) Util.map(hpRatio, drawnRatio, nextDrawRatio, 0, 1);

                stack.push();
                stack.rotate(Vector3f.YP.rotationDegrees(polyDegStep*i + degOffset));
                stack.translate(-w/2f, -h/2f - 0.1f, offset);
                mirrorBlitColor(stack, w*minPolyU, w*maxPolyU, 0, h, 0, hpBarPolyWidth *maxPolyU - hpBarPolyWidth *minPolyU, barHeight, hpBarPolyWidth *i + hpBarPolyWidth *minPolyU, barHeight*2, fullBarWidth, barHeight*barElements, McoConfefeg.hpBarGainIndicatorColor.get());
                minPolyU = 0;
                stack.pop();
            }
        }
        else if(hpRatio < hpBarHpRatio)
        {
            int firstIndicatorPoly = (int) (hpBarPolyCount *hpRatio);
            int lastIndicatorPoly = (int) (hpBarPolyCount *hpBarHpRatio)+1;
            //Loss
            for(int i = firstIndicatorPoly; i < lastIndicatorPoly; i++)
            {
                drawnRatio = i / (float) hpBarPolyCount;
                nextDrawRatio = (i+1) / (float) hpBarPolyCount;

                if(i+1 == lastIndicatorPoly)
                    maxPolyU = (float) Util.map(hpBarHpRatio, drawnRatio, nextDrawRatio, 0, 1);

                stack.push();
                stack.rotate(Vector3f.YP.rotationDegrees(polyDegStep*i + degOffset));
                stack.translate(-w/2f, -h/2f - 0.1f, offset);
                mirrorBlitColor(stack, w*minPolyU, w*maxPolyU, 0, h, 0, hpBarPolyWidth *maxPolyU - hpBarPolyWidth *minPolyU, barHeight, hpBarPolyWidth *i + hpBarPolyWidth *minPolyU, barHeight*2, fullBarWidth, barHeight*barElements, McoConfefeg.hpBarLossIndicatorColor.get());
                minPolyU = 0;
                stack.pop();
            }
        }

        //Hunger
        if(isPlayer)
        {
            float hungerRatio = hpBar.getHungerPos();
            float saturationRatio = hpBar.getSaturationPos();

            Item mainitem = player.getHeldItemMainhand().getItem();
            Food food = mainitem.getFood();
            if(food != null)
            {
                hungerRatio = (float) Math.min(0.5, hungerRatio + food.getHealing() / 40f);
                saturationRatio = (float) Math.min(0.5, saturationRatio + food.getSaturation() / 4f);
            }
            else
                saturationRatio = Math.min(hungerRatio, saturationRatio);

            //Draw Warning
            if(hungerRatio <= 0.1 && (System.currentTimeMillis() / 1000) % 2 == 0)
            {
                for(int i = 0; i < hpBarPolyCount; i++)
                {
                    stack.push();
                    stack.rotate(Vector3f.YP.rotationDegrees(polyDegStep*i + degOffset));
                    stack.translate(-w/2f, -h/2f - 0.1f, offset);
                    mirrorBlitColor(stack, 0, w, 0, h, 0, hpBarPolyWidth, barHeight, hpBarPolyWidth *i, barHeight*3, fullBarWidth, barHeight*barElements, McoConfefeg.HungerBarWarningColor.get());
                    stack.pop();
                }
            }

            minPolyU = 0;
            maxPolyU = 1;

            //Draw Saturation
            for(int i = 0; i < hpBarPolyCount; i++)
            {
                drawnRatio = i / (float) hpBarPolyCount;
                nextDrawRatio = (i+1) / (float) hpBarPolyCount;
                if(saturationRatio < nextDrawRatio)
                    maxPolyU = (float) Util.map(saturationRatio, drawnRatio, nextDrawRatio, 0, 1);
                stack.push();
                stack.rotate(Vector3f.YP.rotationDegrees(polyDegStep*i + degOffset));
                stack.translate(-w/2f, -h/2f - 0.1f, offset);
                mirrorBlitColor(stack, w*minPolyU, w*maxPolyU, 0, h, 0, hpBarPolyWidth *maxPolyU - hpBarPolyWidth *minPolyU, barHeight, hpBarPolyWidth *i + hpBarPolyWidth *minPolyU, barHeight*3, fullBarWidth, barHeight*barElements, McoConfefeg.HungerBarSaturationColor.get());
                stack.pop();
                if(maxPolyU < 1)
                    break;
            }

            minPolyU = maxPolyU;
            maxPolyU = 1;

            //Draw Hunger

            int firstIndicatorPoly = (int) (hpBarPolyCount *saturationRatio);
            int lastIndicatorPoly = (int) (hpBarPolyCount *hungerRatio)+1;
            for(int i = firstIndicatorPoly; i < lastIndicatorPoly; i++)
            {
                drawnRatio = i / (float) hpBarPolyCount;
                nextDrawRatio = (i+1) / (float) hpBarPolyCount;

                if(i+1 == lastIndicatorPoly)
                    maxPolyU = (float) Util.map(hungerRatio, drawnRatio, nextDrawRatio, 0, 1);

                stack.push();
                stack.rotate(Vector3f.YP.rotationDegrees(polyDegStep*i + degOffset));
                stack.translate(-w/2f, -h/2f - 0.1f, offset);
                mirrorBlitColor(stack, w*minPolyU, w*maxPolyU, 0, h, 0, hpBarPolyWidth *maxPolyU - hpBarPolyWidth *minPolyU, barHeight, hpBarPolyWidth *i + hpBarPolyWidth *minPolyU, barHeight*3, fullBarWidth, barHeight*barElements, Util.hueToRGB((float) Util.map(hungerRatio, 0, 0.5, McoConfefeg.HungerBarHungerEndHue.get(), McoConfefeg.HungerBarHungerStartHue.get()), 1f, 1f));
                minPolyU = 0;
                stack.pop();
            }
        }

        //Status effects
        {
            effects.put(player.getEntityId(), new ArrayList<>(player.getActivePotionEffects()));
            List<EffectInstance> entityEffects = effects.get(livingEntity.getEntityId());
            if(entityEffects != null)
            {
                drawEffectBase(stack, entityEffects);
                drawEffectIcons(stack, entityEffects);
                drawEffectTimers(stack, entityEffects);
            }
        }

        stack.pop();
    }

    public static void drawEffectBase(PoseStack stack, List<EffectInstance> entityEffects)
    {
        int i = 0;
        float baseSize = McoConfefeg.effectIndicatorBaseSize.get()*0.09f;
        for(EffectInstance effectInstance : entityEffects)
        {
            int col = i % 10;
            int row = i / 10;

            RenderSystem.setShaderTexture(0,Icons.BUFF_BASE);

            stack.push();
            stack.rotate(Vector3f.YP.rotationDegrees(degOffset + polyDegRange + col * indicatorDegs - row*2));
            stack.translate(-w/2f, -h/2f - 0.11f + row*McoConfefeg.effectIndicatorBaseSize.get()*0.08f, offset);
            int color;
            switch(effectInstance.getPotion().getEffectType())
            {
                case HARMFUL:
                    color = McoConfefeg.effectIndicatorHarmfulColor.get();
                    break;

                case BENEFICIAL:
                    color = McoConfefeg.effectIndicatorBeneficialColor.get();
                    break;

                case NEUTRAL:
                default:
                    color = McoConfefeg.effectIndicatorNeutralColor.get();
                    break;
            }
            mirrorBlitColor(stack, 0, baseSize, 0, baseSize, 0, effectIndicatorSize, effectIndicatorSize, 0, 0, effectIndicatorSize, effectIndicatorSize, color);
            stack.pop();

            i++;
        }
    }

    public static void drawEffectIcons(PoseStack stack, List<EffectInstance> entityEffects)
    {
        float baseSize = McoConfefeg.effectIndicatorBaseSize.get()*0.09f;
        float iconSize = baseSize*McoConfefeg.effectIndicatorIconSize.get();
        float xy1 = baseSize - iconSize;
        int i = 0;
        for(EffectInstance effectInstance : entityEffects)
        {
            int col = i % 10;
            int row = i / 10;

            stack.push();
            stack.rotate(Vector3f.YP.rotationDegrees(degOffset + polyDegRange + col * indicatorDegs - row*2));
            Effect effect = effectInstance.getPotion();
            PotionSpriteUploader potionspriteuploader = mc.getPotionSpriteUploader();
            TextureAtlasSprite texAtlasSprite = potionspriteuploader.getSprite(effect);
            RenderSystem.setShaderTexture(0,texAtlasSprite.getAtlasTexture().getTextureLocation());
            stack.translate(-w/2f, -h/2f - 0.11f + row*McoConfefeg.effectIndicatorBaseSize.get()*0.08f, offset);
            stack.translate(0, 0, -0.001f);
            mirrorBlit(stack.getLast().getMatrix(), xy1, iconSize, xy1, iconSize, 0, texAtlasSprite.getMinU(), texAtlasSprite.getMaxU(), texAtlasSprite.getMinV(), texAtlasSprite.getMaxV());
            stack.translate(0, 0, 0.002f);
            mirrorBlit(stack.getLast().getMatrix(), xy1, iconSize, xy1, iconSize, 0, texAtlasSprite.getMinU(), texAtlasSprite.getMaxU(), texAtlasSprite.getMinV(), texAtlasSprite.getMaxV());
            stack.pop();

            i++;
        }
    }

    public static void drawEffectTimers(PoseStack stack, List<EffectInstance> entityEffects)
    {
        int i = 0;
        for(EffectInstance effectInstance : entityEffects)
        {
            int col = i % 10;
            int row = i / 10;

            stack.push();
            stack.rotate(Vector3f.YP.rotationDegrees(degOffset + polyDegRange + (col * indicatorDegs + indicatorDegs*0.4f) - row*2));
            int color = i%2 == 0 ? 0xffffff : 0xcccccc;
            stack.translate(-w/2f, -h/2f - 0.075f + (row+1)*McoConfefeg.effectIndicatorBaseSize.get()*0.065f, offset + 0.01f);
            stack.translate(0, 0, -0.001f);
            stack.scale(1/400f, 1/400f, 1/400f);
            Renderer.drawCenteredString(stack, mc.fontRenderer, new StringTextComponent("" + (Util.toStamp(effectInstance.getDuration() / 20f))), 0, 0, color);
//            stack.translate(0, 0, 0.002f);
//            stack.rotate(Vector3f.YP.rotationDegrees(90f));
//            Renderer.drawCenteredString(stack, mc.fontRenderer, new StringTextComponent("" + (Util.toStamp(effectInstance.getDuration() / 20f))), 0, 0, color);
            stack.pop();

            i++;
        }
        RenderSystem.enableBlend();
    }

    public static void drawNPCIndicator(PoseStack stack, LivingEntity livingEntity)
    {
        RenderSystem.setShaderTexture(0,Icons.INDICATOR);
        stack.push();
        int color = 0xfffff;
        if(livingEntity instanceof AnimalEntity)
            color = McoConfefeg.npcIndicatorPassiveColor.get();
        else if(livingEntity instanceof MobEntity)
        {

            if(attackers.contains(livingEntity.getEntityId()) && livingEntity.canAttack(mc.player))
                color = McoConfefeg.npcIndicatorHostileColor.get();
            else
                color = McoConfefeg.npcIndicatorAggresiveColor.get();
        }
        else if(livingEntity instanceof Player)
            color = McoConfefeg.npcIndicatorPlayerColor.get();
        float scale = livingEntity.getRenderScale()*0.1523f;
        float height = scale*2;
        stack.translate(0, -livingEntity.getHeight()*0.3251f - height, 0);
        mirrorBlitColor(stack, -scale, scale, -height, height, 0, indicatorWidth, indicatorHeight, 0, 0, indicatorWidth, indicatorHeight, color);
        stack.rotate(Vector3f.YP.rotationDegrees(90));
        mirrorBlitColor(stack, -scale, scale, -height, height, 0, indicatorWidth, indicatorHeight, 0, 0, indicatorWidth, indicatorHeight, color);
        stack.pop();
    }

    public static void drawCurvedText(PoseStack stack, String text, Vector3f pos, float scale, float degOffset, boolean isPlayer)
    {
        stack.push();

        stack.scale(1/scale, 1/scale, 1/scale);
        float x = pos.getX()*scale, y = pos.getY()*scale, z = pos.getZ()*scale;
        stack.translate(x, y, 0);
        float compound = 0;
        for(int i = 0; i < text.length(); i++)
        {
            char c = text.charAt(i);
            stack.push();
            stack.rotate(Vector3f.YP.rotationDegrees(3 + degOffset + compound*(float) Math.atan(1/z)*60));
            stack.translate(0, 0, z);
            drawString(stack, mc.fontRenderer, new StringTextComponent("" + c), 0, isPlayer ? -5 : -7, 0xffffff);
            compound += mc.fontRenderer.getStringWidth("" + c);
            stack.pop();
        }
        stack.pop();
    }

    public static void mirrorBlit(PoseStack PoseStack, float x1, float x2, float y1, float y2, float blitOffset, float uWidth, float vHeight, float uOffset, float vOffset, float textureWidth, float textureHeight)
    {
        mirrorBlit(PoseStack.getLast().getMatrix(), x1, x2, y1, y2, blitOffset, (uOffset + 0.0F) / textureWidth, (uOffset + uWidth) / textureWidth, (vOffset + 0.0F) / textureHeight, (vOffset + vHeight) / textureHeight);
    }

    public static void mirrorBlit(Matrix4f matrix, float x1, float x2, float y1, float y2, float blitOffset, float minU, float maxU, float minV, float maxV)
    {
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();

        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(matrix, x1, y2, blitOffset).tex(minU, maxV).endVertex();
        bufferbuilder.pos(matrix, x2, y2, blitOffset).tex(maxU, maxV).endVertex();
        bufferbuilder.pos(matrix, x2, y1, blitOffset).tex(maxU, minV).endVertex();
        bufferbuilder.pos(matrix, x1, y1, blitOffset).tex(minU, minV).endVertex();
        bufferbuilder.finishDrawing();
        WorldVertexBufferUploader.draw(bufferbuilder);

        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(matrix, x1, y1, blitOffset).tex(minU, minV).endVertex();
        bufferbuilder.pos(matrix, x2, y1, blitOffset).tex(maxU, minV).endVertex();
        bufferbuilder.pos(matrix, x2, y2, blitOffset).tex(maxU, maxV).endVertex();
        bufferbuilder.pos(matrix, x1, y2, blitOffset).tex(minU, maxV).endVertex();
        bufferbuilder.finishDrawing();

        RenderSystem.enableAlphaTest();
        WorldVertexBufferUploader.draw(bufferbuilder);
    }

    public static void mirrorBlitColor(PoseStack PoseStack, float x1, float x2, float y1, float y2, float blitOffset, float uWidth, float vHeight, float uOffset, float vOffset, float textureWidth, float textureHeight, int color)
    {
        mirrorBlitColor(PoseStack.getLast().getMatrix(), x1, x2, y1, y2, blitOffset, (uOffset + 0.0F) / textureWidth, (uOffset + uWidth) / textureWidth, (vOffset + 0.0F) / textureHeight, (vOffset + vHeight) / textureHeight, color);
    }

    public static void mirrorBlitColor(Matrix4f matrix, float x1, float x2, float y1, float y2, float blitOffset, float minU, float maxU, float minV, float maxV, int color)
    {
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();

        int alpha = Util.getAlphaFromColor(color);
        if(alpha == 0)
            alpha = 255;
        int red = (color & 0x00ff0000) >> 16;
        int green = (color & 0x0000ff00) >> 8;
        int blue = color & 0x000000ff;

        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
        bufferbuilder.pos(matrix, x1, y2, blitOffset).color(red, green, blue, alpha).tex(minU, maxV).endVertex();
        bufferbuilder.pos(matrix, x2, y2, blitOffset).color(red, green, blue, alpha).tex(maxU, maxV).endVertex();
        bufferbuilder.pos(matrix, x2, y1, blitOffset).color(red, green, blue, alpha).tex(maxU, minV).endVertex();
        bufferbuilder.pos(matrix, x1, y1, blitOffset).color(red, green, blue, alpha).tex(minU, minV).endVertex();
        bufferbuilder.finishDrawing();
        WorldVertexBufferUploader.draw(bufferbuilder);

        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
        bufferbuilder.pos(matrix, x1, y1, blitOffset).color(red, green, blue, alpha).tex(minU, minV).endVertex();
        bufferbuilder.pos(matrix, x2, y1, blitOffset).color(red, green, blue, alpha).tex(maxU, minV).endVertex();
        bufferbuilder.pos(matrix, x2, y2, blitOffset).color(red, green, blue, alpha).tex(maxU, maxV).endVertex();
        bufferbuilder.pos(matrix, x1, y2, blitOffset).color(red, green, blue, alpha).tex(minU, maxV).endVertex();
        bufferbuilder.finishDrawing();

        RenderSystem.enableAlphaTest();
        WorldVertexBufferUploader.draw(bufferbuilder);
    }

    public static void blitColor(PoseStack PoseStack, float x1, float x2, float y1, float y2, float blitOffset, float uWidth, float vHeight, float uOffset, float vOffset, float textureWidth, float textureHeight, int color, int alpha)
    {
        blitColor(PoseStack.getLast().getMatrix(), x1, x2, y1, y2, blitOffset, (uOffset + 0.0F) / textureWidth, (uOffset + uWidth) / textureWidth, (vOffset + 0.0F) / textureHeight, (vOffset + vHeight) / textureHeight, color, alpha);
    }

    public static void blitColor(Matrix4f matrix, float x1, float x2, float y1, float y2, float blitOffset, float minU, float maxU, float minV, float maxV, int color, int alpha)
    {
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();

        int red = color >> 16;
        int green = (color & 0x00ff00) >> 8;
        int blue = color & 0x0000ff;

        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
        bufferbuilder.pos(matrix, x1, y2, blitOffset).color(red, green, blue, alpha).tex(minU, maxV).endVertex();
        bufferbuilder.pos(matrix, x2, y2, blitOffset).color(red, green, blue, alpha).tex(maxU, maxV).endVertex();
        bufferbuilder.pos(matrix, x2, y1, blitOffset).color(red, green, blue, alpha).tex(maxU, minV).endVertex();
        bufferbuilder.pos(matrix, x1, y1, blitOffset).color(red, green, blue, alpha).tex(minU, minV).endVertex();
        bufferbuilder.finishDrawing();
        WorldVertexBufferUploader.draw(bufferbuilder);

//        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
//        bufferbuilder.pos(matrix, x1, y1, blitOffset).color(red, green, blue, alpha).tex(minU, minV).endVertex();
//        bufferbuilder.pos(matrix, x2, y1, blitOffset).color(red, green, blue, alpha).tex(maxU, minV).endVertex();
//        bufferbuilder.pos(matrix, x2, y2, blitOffset).color(red, green, blue, alpha).tex(maxU, maxV).endVertex();
//        bufferbuilder.pos(matrix, x1, y2, blitOffset).color(red, green, blue, alpha).tex(minU, maxV).endVertex();
//        bufferbuilder.finishDrawing();
//
//        RenderSystem.enableAlphaTest();
//        WorldVertexBufferUploader.draw(bufferbuilder);
    }

    public static void mirrorBlitColorHorizontalFlip(PoseStack PoseStack, float x1, float x2, float y1, float y2, float blitOffset, float uWidth, float vHeight, float uOffset, float vOffset, float textureWidth, float textureHeight, int color)
    {
        mirrorBlitColor(PoseStack.getLast().getMatrix(), x1, x2, y1, y2, blitOffset, 1 - ((uOffset + 0.0F) / textureWidth), 1 - ((uOffset + uWidth) / textureWidth), (vOffset + 0.0F) / textureHeight, (vOffset + vHeight) / textureHeight, color);
    }

    public static void drawCenteredString(PoseStack stack, FontRenderer font, ITextComponent msg, float x, float y, int color)
    {
        drawString(stack, font, msg, x - font.getStringPropertyWidth(msg)/2f, y, color);
    }

    public static void drawString(PoseStack stack, FontRenderer font, String msg, float x, float y, int color)
    {
        font.drawString(stack, msg, x, y, color);
    }

    public static void drawString(PoseStack stack, FontRenderer font, ITextComponent msg, float x, float y, int color)
    {
        font.func_243246_a(stack, msg, x, y, color);
    }

    public static void renderTooltip(PoseStack stack, ItemStack itemStack, int x, int y)
    {
        net.minecraftforge.fml.client.gui.GuiUtils.preItemToolTip(itemStack);
        renderWrappedToolTip(stack, itemStack.getTooltip(mc.player, mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL), x, y);
        net.minecraftforge.fml.client.gui.GuiUtils.postItemToolTip();
    }

    public static void renderWrappedToolTip(PoseStack PoseStack, List<? extends ITextProperties> tooltips, int mouseX, int mouseY)
    {
        GuiUtils.drawHoveringText(PoseStack, tooltips, mouseX, mouseY, getScaledWidth(), getScaledHeight(), -1, mc.fontRenderer);
    }

    public static float getTextCompWidth(ITextComponent comp)
    {
        return mc.fontRenderer.getCharacterManager().func_238356_a_(comp);
    }

    public static int getScaledWidth()
    {
        return mc.getMainWindow().getScaledWidth();
    }

    public static int getScaledHeight()
    {
        return mc.getMainWindow().getScaledHeight();
    }

    public static void renderGuiItem(ItemStack itemStack, float x, float y)
    {
        Minecraft mc = Minecraft.getInstance();
        TextureManager textureManager = mc.getTextureManager();
        ItemRenderer itemRenderer = mc.getItemRenderer();
        IBakedModel bakedModel = itemRenderer.getItemModelWithOverrides(itemStack, null, null);
        RenderSystem.pushMatrix();
        textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        textureManager.getTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).setBlurMipmapDirect(false, false);
        RenderSystem.enableRescaleNormal();
        RenderSystem.enableAlphaTest();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.translatef(x, y, 100.0F + itemRenderer.zLevel);
        RenderSystem.translatef(8.0F, 8.0F, 0.0F);
        RenderSystem.scalef(1.0F, -1.0F, 1.0F);
        RenderSystem.scalef(16.0F, 16.0F, 16.0F);
        PoseStack PoseStack = new PoseStack();
        IRenderTypeBuffer.Impl irendertypebuffer$impl = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        boolean flag = !bakedModel.isSideLit();
        if (flag) {
            RenderHelper.setupGuiFlatDiffuseLighting();
        }

        itemRenderer.renderItem(itemStack, ItemCameraTransforms.TransformType.GUI, false, PoseStack, irendertypebuffer$impl, 15728880, OverlayTexture.NO_OVERLAY, bakedModel);
        irendertypebuffer$impl.finish();
        RenderSystem.enableDepthTest();
        if (flag) {
            RenderHelper.setupGui3DDiffuseLighting();
        }

        RenderSystem.disableAlphaTest();
        RenderSystem.disableRescaleNormal();
        RenderSystem.popMatrix();
    }

    public static void fillGradient(PoseStack PoseStack, float x1, float y1, float x2, float y2, int colorFrom, int colorTo)
    {
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        fillGradient(PoseStack.getLast().getMatrix(), bufferbuilder, x1, y1, x2, y2, blitOffset, colorFrom, colorTo);
        tessellator.draw();
        RenderSystem.shadeModel(7424);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
    }

    public static void fillGradient(Matrix4f matrix, BufferBuilder builder, float x1, float y1, float x2, float y2, float z, int colorA, int colorB)
    {
        float f = (float)(colorA >> 24 & 255) / 255.0F;
        float f1 = (float)(colorA >> 16 & 255) / 255.0F;
        float f2 = (float)(colorA >> 8 & 255) / 255.0F;
        float f3 = (float)(colorA & 255) / 255.0F;
        float f4 = (float)(colorB >> 24 & 255) / 255.0F;
        float f5 = (float)(colorB >> 16 & 255) / 255.0F;
        float f6 = (float)(colorB >> 8 & 255) / 255.0F;
        float f7 = (float)(colorB & 255) / 255.0F;
        builder.pos(matrix, x2, y1, z).color(f1, f2, f3, f).endVertex();
        builder.pos(matrix, x1, y1, z).color(f1, f2, f3, f).endVertex();
        builder.pos(matrix, x1, y2, z).color(f5, f6, f7, f4).endVertex();
        builder.pos(matrix, x2, y2, z).color(f5, f6, f7, f4).endVertex();
    }

    public static void drawEntityOnScreen(PoseStack stack, float posX, float posY, int scale, float mouseX, float mouseY, LivingEntity livingEntity)
    {
        float f = (float) ((System.currentTimeMillis() / 25D) % 360);
        float f1 = 0;
        RenderSystem.pushMatrix();
        RenderSystem.translatef((float)posX, (float)posY, 1050.0F);
        RenderSystem.scalef(1.0F, 1.0F, -1.0F);
        PoseStack PoseStack = new PoseStack();
        PoseStack.translate(0.0D, 0.0D, 1000.0D);
        PoseStack.scale((float)scale, (float)scale, (float)scale);
        Quaternion quaternion = Vector3f.ZP.rotationDegrees(180.0F);
        Quaternion quaternion1 = Vector3f.XP.rotationDegrees(f1 * 20.0F);
        quaternion.multiply(quaternion1);
        PoseStack.rotate(quaternion);
        float f2 = livingEntity.renderYawOffset;
        float f3 = livingEntity.rotationYaw;
        float f4 = livingEntity.rotationPitch;
        float f5 = livingEntity.prevRotationYawHead;
        float f6 = livingEntity.rotationYawHead;
        livingEntity.renderYawOffset = f;
        livingEntity.rotationYaw = f;
        livingEntity.rotationPitch = -f1 * 20.0F;
        livingEntity.rotationYawHead = f;
        livingEntity.prevRotationYawHead = 0;
        EntityRendererManager entityrenderermanager = Minecraft.getInstance().getRenderManager();
        quaternion1.conjugate();
        entityrenderermanager.setCameraOrientation(quaternion1);
        entityrenderermanager.setRenderShadow(false);
        IRenderTypeBuffer.Impl irendertypebuffer$impl = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        entityrenderermanager.renderEntityStatic(livingEntity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, PoseStack, irendertypebuffer$impl, 15728880);
        irendertypebuffer$impl.finish();
        entityrenderermanager.setRenderShadow(true);
        livingEntity.renderYawOffset = f2;
        livingEntity.rotationYaw = f3;
        livingEntity.rotationPitch = f4;
        livingEntity.prevRotationYawHead = f5;
        livingEntity.rotationYawHead = f6;
        RenderSystem.popMatrix();
    }
}
