package com.jaquadro.minecraft.storagedrawers.client.renderer;

import com.jaquadro.minecraft.storagedrawers.StorageDrawers;
import com.jaquadro.minecraft.storagedrawers.block.BlockCompDrawers;
import com.jaquadro.minecraft.storagedrawers.block.BlockDrawers;
import com.jaquadro.minecraft.storagedrawers.block.tile.TileEntityDrawers;
import com.jaquadro.minecraft.storagedrawers.block.tile.TileEntityDrawersBase;
import com.jaquadro.minecraft.storagedrawers.core.ClientProxy;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;

public class DrawersRenderer implements ISimpleBlockRenderingHandler
{
    private static final double unit = .0625f;

    private ModularBoxRenderer boxRenderer = new ModularBoxRenderer();

    @Override
    public void renderInventoryBlock (Block block, int metadata, int modelId, RenderBlocks renderer) {
        if (!(block instanceof BlockDrawers))
            return;

        renderInventoryBlock((BlockDrawers) block, metadata, modelId, renderer);
    }

    private void renderInventoryBlock (BlockDrawers block, int metadata, int modelId, RenderBlocks renderer) {
        int side = 4;

        boxRenderer.setUnit(unit);
        boxRenderer.setColor(ModularBoxRenderer.COLOR_WHITE);
        for (int i = 0; i < 6; i++)
            boxRenderer.setIcon(block.getIcon(i, metadata), i);

        GL11.glRotatef(90, 0, 1, 0);
        GL11.glTranslatef(-.5f, -.5f, -.5f);

        renderExterior(block, 0, 0, 0,side, renderer);

        boxRenderer.setUnit(0);
        boxRenderer.setInteriorIcon(block.getIcon(side, metadata), ForgeDirection.OPPOSITES[side]);

        renderInterior(block, 0, 0, 0, side, renderer);

        GL11.glTranslatef(.5f, .5f, .5f);
    }

    @Override
    public boolean renderWorldBlock (IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
        if (!(block instanceof BlockDrawers))
            return false;

        return renderWorldBlock(world, x, y, z, (BlockDrawers) block, modelId, renderer);
    }

    private boolean renderWorldBlock (IBlockAccess world, int x, int y, int z, BlockDrawers block, int modelId, RenderBlocks renderer) {
        TileEntityDrawersBase tile = block.getTileEntity(world, x, y, z);
        if (tile == null)
            return false;

        int side = tile.getDirection();
        int meta = world.getBlockMetadata(x, y, z);

        double unit = .0625;
        boxRenderer.setUnit(unit);
        boxRenderer.setColor(ModularBoxRenderer.COLOR_WHITE);
        for (int i = 0; i < 6; i++)
            boxRenderer.setExteriorIcon(block.getIcon(world, x, y, z, i), i);

        boxRenderer.setCutIcon(block.getIconTrim(meta));
        boxRenderer.setInteriorIcon(block.getIconTrim(meta));

        renderExterior(block, x, y, z, side, renderer);

        if (tile.getLevel() > 1 && StorageDrawers.config.cache.renderStorageUpgrades) {
            for (int i = 0; i < 6; i++)
                boxRenderer.setExteriorIcon(block.getOverlayIcon(world, x, y, z, i, tile.getLevel()), i);

            boxRenderer.setCutIcon(block.getOverlayIconTrim(tile.getLevel()));
            boxRenderer.setInteriorIcon(block.getOverlayIconTrim(tile.getLevel()));

            renderExterior(block, x, y, z, side, renderer);
        }

        boxRenderer.setUnit(0);
        boxRenderer.setInteriorIcon(block.getIcon(world, x, y, z, side), ForgeDirection.OPPOSITES[side]);

        renderInterior(block, x, y, z, side, renderer);

        boxRenderer.setIcon(block.getIndicatorIcon(false));

        if (StorageDrawers.config.cache.enableIndicatorUpgrades)
            renderHandleIndicator(block, x, y, z, side, renderer, tile.getStatusLevel());

        return true;
    }

    private static final int[] cut = new int[] {
        ModularBoxRenderer.CUT_YPOS | ModularBoxRenderer.CUT_YNEG | ModularBoxRenderer.CUT_XPOS | ModularBoxRenderer.CUT_XNEG | ModularBoxRenderer.CUT_ZPOS,
        ModularBoxRenderer.CUT_YPOS | ModularBoxRenderer.CUT_YNEG | ModularBoxRenderer.CUT_XPOS | ModularBoxRenderer.CUT_XNEG | ModularBoxRenderer.CUT_ZNEG,
        ModularBoxRenderer.CUT_YPOS | ModularBoxRenderer.CUT_YNEG | ModularBoxRenderer.CUT_XPOS | ModularBoxRenderer.CUT_ZNEG | ModularBoxRenderer.CUT_ZPOS,
        ModularBoxRenderer.CUT_YPOS | ModularBoxRenderer.CUT_YNEG | ModularBoxRenderer.CUT_XNEG | ModularBoxRenderer.CUT_ZNEG | ModularBoxRenderer.CUT_ZPOS,
    };

    private static final float[][][] indicatorsX2 = new float[][][] {
        { { 6, 6 }, { 6, 6 } },
        { { 6, 6 }, { 6, 6 } },
        { { .95f, .95f }, { 8.95f, 8.95f } },
        { { 15, 15 }, { 7, 7 } },
    };
    private static final float[][][] indicatorsX4 = new float[][][] {
        { { 11, 11, 3, 3 }, { 11, 11, 3, 3 } },
        { { 3, 3, 11, 11 }, { 3, 3, 11, 11 } },
        { { .95f, .95f, .95f, .95f }, { 8.95f, 8.95f, 8.95f, 8.95f } },
        { { 15, 15, 15, 15 }, { 7, 7, 7, 7 } },
    };

    private static final float[][] indicatorsX2Len = new float[][] {
        { 4, 4 }, { 4, 4 }, { .05f, .05f }, { .05f, .05f },
    };
    private static final float[][] indicatorsX4Len = new float[][] {
        { 2, 2, 2, 2 }, { 2, 2, 2, 2 }, {.05f, .05f, .05f, .05f }, { .05f, .05f, .05f, .05f },
    };

    private static final float[] indicatorsY2 = new float[]  { 14, 6 };
    private static final float[] indicatorsY4 = new float[] { 14, 6, 14, 6 };

    private void renderHandleIndicator (BlockDrawers block, int x, int y, int z, int side, RenderBlocks renderer, int level) {
        if (level <= 0 || side < 2 || side > 5)
            return;

        TileEntityDrawersBase tile = block.getTileEntity(renderer.blockAccess, x, y, z);

        int d = block.halfDepth ? 1 : 0;
        int xi = side - 2;
        int zi = (xi + 2) % 4;

        int count;
        float[][][] indX = null;
        float[][] indXLen = null;
        float[] indY = null;

        if (block.drawerCount == 2 || block instanceof BlockCompDrawers) {
            count = (block instanceof BlockCompDrawers) ? 1 : 2;
            indX = indicatorsX2;
            indXLen = indicatorsX2Len;
            indY = indicatorsY2;
        }
        else if (block.drawerCount == 4) {
            count = 4;
            indX = indicatorsX4;
            indXLen = indicatorsX4Len;
            indY = indicatorsY4;
        }
        else
            return;

        for (int i = 0; i < count; i++) {
            int iconIndex = 0;
            if (level == 2)
                iconIndex = (tile.getItemCapacity(i) > 0 && (float)tile.getItemCount(i) / tile.getItemCapacity(i) > .75) ? 1 : iconIndex;
            if (level >= 1)
                iconIndex = (tile.getItemCapacity(i) > 0 && tile.getItemCount(i) == tile.getItemCapacity(i)) ? 2 : iconIndex;

            boxRenderer.setExteriorIcon(block.getIndicatorIcon(iconIndex));
            boxRenderer.renderExterior(renderer, block, x, y, z, indX[xi][d][i] * unit, indY[i] * unit, indX[zi][d][i] * unit,
                (indX[xi][d][i] + indXLen[xi][i]) * unit, (indY[i] + 1) * unit, (indX[zi][d][i] + indXLen[zi][i]) * unit, 0, cut[xi]);
        }
    }

    private void renderExterior (BlockDrawers block, int x, int y, int z, int side, RenderBlocks renderer) {
        double depth = block.halfDepth ? .5 : 1;
        double xMin = 0, xMax = 0, zMin = 0, zMax = 0;

        switch (side) {
            case 2:
                xMin = 0; xMax = 1;
                zMin = 1 - depth; zMax = 1;
                break;
            case 3:
                xMin = 0; xMax = 1;
                zMin = 0; zMax = depth;
                break;
            case 4:
                xMin = 1 - depth; xMax = 1;
                zMin = 0; zMax = 1;
                break;
            case 5:
                xMin = 0; xMax = depth;
                zMin = 0; zMax = 1;
                break;
        }

        boxRenderer.renderExterior(renderer, block, x, y, z, xMin, 0, zMin, xMax, 1, zMax, 0, ModularBoxRenderer.sideCut[side]);
    }

    private void renderInterior (BlockDrawers block, int x, int y, int z, int side, RenderBlocks renderer) {
        double unit = .0625;
        double depth = block.halfDepth ? .5 : 1;
        double xMin = 0, xMax = 0, zMin = 0, zMax = 0;

        switch (side) {
            case 2:
                xMin = unit; xMax = 1 - unit;
                zMin = 1 - depth; zMax = 1 - depth + unit;
                break;
            case 3:
                xMin = unit; xMax = 1 - unit;
                zMin = depth - unit; zMax = depth;
                break;
            case 4:
                xMin = 1 - depth; xMax = 1 - depth + unit;
                zMin = unit; zMax = 1 - unit;
                break;
            case 5:
                xMin = depth - unit; xMax = depth;
                zMin = unit; zMax = 1 - unit;
                break;
        }

        boxRenderer.renderInterior(renderer, block, x, y, z, xMin, unit, zMin, xMax, 1 - unit, zMax, 0, ModularBoxRenderer.sideCut[side]);
    }

    @Override
    public boolean shouldRender3DInInventory (int modelId) {
        return true;
    }

    @Override
    public int getRenderId () {
        return StorageDrawers.proxy.drawersRenderID;
    }
}
