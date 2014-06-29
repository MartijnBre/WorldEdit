/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.command.tool;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;

import java.util.HashSet;
import java.util.Set;

/**
 * A tool that flood fills blocks.
 */
public class FloodFillTool implements BlockTool {

    private int range;
    private Pattern pattern;

    public FloodFillTool(int range, Pattern pattern) {
        this.range = range;
        this.pattern = pattern;
    }

    @Override
    public boolean canUse(Actor player) {
        return player.hasPermission("worldedit.tool.flood-fill");
    }

    @Override
    public boolean actPrimary(Platform server, LocalConfiguration config, Player player, LocalSession session, Location clicked) {
        World world = clicked.getWorld();

        int initialType = world.getBlockType(clicked.toVector());

        if (initialType == BlockID.AIR) {
            return true;
        }

        if (initialType == BlockID.BEDROCK && !player.canDestroyBedrock()) {
            return true;
        }

        EditSession editSession = session.createEditSession(player);

        try {
            recurse(server, editSession, world, clicked.toVector().toBlockVector(),
                    clicked.toVector(), range, initialType, new HashSet<BlockVector>());
        } catch (MaxChangedBlocksException e) {
            player.printError("Max blocks change limit reached.");
        } finally {
            session.remember(editSession);
        }

        return true;
    }

    private void recurse(Platform server, EditSession editSession, World world, BlockVector pos, Vector origin, int size, int initialType,
            Set<BlockVector> visited) throws MaxChangedBlocksException {

        if (origin.distance(pos) > size || visited.contains(pos)) {
            return;
        }

        visited.add(pos);

        if (editSession.getBlock(pos).getType() == initialType) {
            editSession.setBlock(pos, pattern.next(pos));
        } else {
            return;
        }

        recurse(server, editSession, world, pos.add(1, 0, 0).toBlockVector(),
                origin, size, initialType, visited);
        recurse(server, editSession, world, pos.add(-1, 0, 0).toBlockVector(),
                origin, size, initialType, visited);
        recurse(server, editSession, world, pos.add(0, 0, 1).toBlockVector(),
                origin, size, initialType, visited);
        recurse(server, editSession, world, pos.add(0, 0, -1).toBlockVector(),
                origin, size, initialType, visited);
        recurse(server, editSession, world, pos.add(0, 1, 0).toBlockVector(),
                origin, size, initialType, visited);
        recurse(server, editSession, world, pos.add(0, -1, 0).toBlockVector(),
                origin, size, initialType, visited);
    }

}
