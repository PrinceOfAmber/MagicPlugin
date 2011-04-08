package com.elmakers.mine.bukkit.spells;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.elmakers.mine.bukkit.magic.Spell;
import com.elmakers.mine.bukkit.persistence.dao.BlockList;
import com.elmakers.mine.bukkit.persistence.dao.MaterialList;
import com.elmakers.mine.bukkit.persistence.dao.ParameterMap;

public class Tunnel extends Spell
{
    private int          defaultDepth          = 8;
    private int          defaultHeight         = 3;
    private int          defaultSearchDistance = 32;
    private int          defaultWidth          = 3;
    //private int          torchFrequency        = 4;
    private MaterialList destructibleMaterials = null;

    @Override
    public String getDescription()
    {
        return "Create a tunnel through common materials";
    }

    @Override
    public String getName()
    {
        return "tunnel";
    }

    public boolean isDestructible(Block block)
    {
        if (block.getType() == Material.AIR)
        {
            return false;
        }

        return destructibleMaterials.contains(block.getType());
    }

    @Override
    public boolean onCast(ParameterMap parameters)
    {
        Block playerBlock = targeting.getPlayerBlock();
        if (playerBlock == null)
        {
            // no spot found to tunnel
            player.sendMessage("You need to be standing on something");
            return false;
        }

        BlockFace direction = targeting.getPlayerFacing();
        Block searchBlock = playerBlock.getFace(BlockFace.UP).getFace(BlockFace.UP);

        int searchDistance = 0;
        while (searchBlock.getType() == Material.AIR && searchDistance < defaultSearchDistance)
        {
            searchBlock = searchBlock.getFace(direction);
            searchDistance++;
        }

        int depth = defaultDepth;
        int height = defaultHeight;
        int width = defaultWidth;

        BlockList tunneledBlocks = new BlockList();

        BlockFace toTheLeft = targeting.goLeft(direction);
        BlockFace toTheRight = targeting.goRight(direction);
        Block bottomBlock = searchBlock.getFace(BlockFace.DOWN);
        Block bottomLeftBlock = bottomBlock;
        for (int i = 0; i < width / 2; i++)
        {
            bottomLeftBlock = bottomLeftBlock.getFace(toTheLeft);
        }

        Block targetBlock = bottomLeftBlock;

        for (int d = 0; d < depth; d++)
        {
            bottomBlock = bottomLeftBlock;
            for (int w = 0; w < width; w++)
            {
                targetBlock = bottomBlock;
                for (int h = 0; h < height; h++)
                {
                    if (isDestructible(targetBlock))
                    {
                        // Put torches on the left and right wall
                        /*
                         * boolean useTorch = ( torchFrequency > 0 && (w == 0 ||
                         * w == width - 1) && (h == 1) && (d % torchFrequency ==
                         * 0) );
                         */
                        boolean useTorch = false; // TODO!
                        tunneledBlocks.add(targetBlock);
                        if (useTorch)
                        {
                            // First check to see if the torch will stick to the
                            // wall
                            // TODO: Check for glass, other non-sticky types.
                            Block checkBlock = null;
                            if (w == 0)
                            {
                                checkBlock = targetBlock.getFace(toTheLeft);
                            }
                            else
                            {
                                checkBlock = targetBlock.getFace(toTheRight);
                            }
                            if (checkBlock.getType() == Material.AIR)
                            {
                                targetBlock.setType(Material.AIR);
                            }
                            else
                            {
                                targetBlock.setType(Material.TORCH);
                            }
                        }
                        else
                        {
                            targetBlock.setType(Material.AIR);
                        }
                    }
                    targetBlock = targetBlock.getFace(BlockFace.UP);
                }
                bottomBlock = bottomBlock.getFace(toTheRight);
            }
            bottomLeftBlock = bottomLeftBlock.getFace(direction);
        }

        magic.addToUndoQueue(player, tunneledBlocks);
        castMessage(player, "Tunneled through " + tunneledBlocks.size() + "blocks");

        return true;
    }

    @Override
    public void onLoad()
    {
        destructibleMaterials = getMaterialList("common");
        //defaultDepth = properties.getInteger("spells-tunnel-depth", defaultDepth);
        //defaultWidth = properties.getInteger("spells-tunnel-width", defaultWidth);
        //defaultHeight = properties.getInteger("spells-tunnel-height", defaultHeight);
        //defaultSearchDistance = properties.getInteger("spells-tunnel-search-distance", defaultSearchDistance);
        //torchFrequency = properties.getInteger("spells-tunnel-torch-frequency", torchFrequency);
    }

}