package qd.qcomp.qcompplugin;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Random;

public final class Qcomp extends JavaPlugin implements Listener {
    public static final int precision = 6;
    private final int[][] dir = {   { +1,  0,  0 }, { -1,  0,  0 }, {  0, +1,  0 },
                                    {  0, -1,  0 }, {  0,  0, +1 }, {  0,  0, -1 },  };
    private final int[] oppdir =    { 1, 0, 3, 2, 5, 4 };
    private MetaManager mm;

    @Override
    public void onEnable() {
        // Plugin startup logic
        System.out.println("My first plugin has started!!");
        getServer().getPluginManager().registerEvents(this, this);
        mm = new MetaManager(this);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block placedBlock = event.getBlockPlaced();
        propagate(placedBlock, -1, new Qubit(false));
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block brokenBlock = event.getBlock();
        mm.DelAll(brokenBlock);
    }

    private void propagate(Block curr, int fromdir, Qubit q) {
        Qubit newq;
        Material mat = curr.getType();

        if(mat == Material.COAL_BLOCK) {
            newq = new Qubit(true);
        }
        else if(!q.valid) {
            // resume suspended propagation if exists
            for(int i=0; i<6; i++) {
                if(fromdir != -1 && i == oppdir[fromdir]) continue;
                propagate(curr.getRelative(dir[i][0], dir[i][1], dir[i][2]), -2, new Qubit(false));
            }
            return;
        }
        else {
            switch (curr.getType()) {
                case BONE_BLOCK:        // propagation ends: measurement
                    measure(curr, fromdir, q);
                    return;
                case IRON_BLOCK:        // propagation continue
                    newq = q;
                    break;
                case EMERALD_BLOCK:     // X gate
                    newq = q.apply(Gate.X);
                    break;
                case COPPER_BLOCK:      // Y gate
                    newq = q.apply(Gate.Y);
                    break;
                case DIAMOND_BLOCK:     // Z gate
                    newq = q.apply(Gate.Z);
                    break;
                case REDSTONE_BLOCK:    // serves as selection signal
                    newq = q;
                    newq.selsig = true;
                    break;
                case GOLD_BLOCK:        // H gate
                    newq = q.apply(Gate.H);
                    break;
                default:
                    return;
            }
        }

        mm.Set(curr, "fromdir", fromdir);
        mm.Set(curr, "qubit", q);

        for(int i=0; i<6; i++) {
            if(fromdir != -1 && i == oppdir[fromdir]) continue;
            propagate(curr.getRelative(dir[i][0], dir[i][1], dir[i][2]), i, newq);
        }
    }

    private void measure(Block curr, int fromdir, Qubit q) {
        Block block = curr.getRelative(dir[fromdir][0], dir[fromdir][1], dir[fromdir][2]).getLocation().getBlock();
        int randomrange = (int)Math.pow(10, precision);
        int randnum = new Random().nextInt(randomrange) + 1;
        int zeromeasure = (int)(Math.pow(q.coef[0].abs(), 2) * randomrange);
        block.setType(randnum <= zeromeasure ? Material.ZOMBIE_HEAD : Material.CREEPER_HEAD);
    }

    private void wait(Block curr, int fromdir, Qubit q) {
        boolean waiting = curr.hasMetadata("fromdir");
        if(!waiting) {
            String state = q.selsig ? "waitval" : "waitsel";
            mm.Set(curr, "state", state);
        }
        else {
            String state = mm.Get(curr, "state", String.class);
        }
    }


}
