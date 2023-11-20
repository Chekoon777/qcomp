package qd.qcomp.qcompplugin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.apache.commons.math3.complex.Complex;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.w3c.dom.Text;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public final class Qcomp extends JavaPlugin implements Listener, CommandExecutor {
    public World world;
    public static final int precision = 6;
    private final int[][] dir = {   { +1,  0,  0 }, { -1,  0,  0 }, {  0, +1,  0 },
                                    {  0, -1,  0 }, {  0,  0, +1 }, {  0,  0, -1 },  };
    private final int[] oppdir =    { 1, 0, 3, 2, 5, 4 };
    private MetaManager mm;
    private Player currplayer;
    public Qstate initstate;

    @Override
    public void onEnable() {
        // Plugin startup logic
        world = Bukkit.getWorld("world");
        System.out.println("My first plugin has started!!");
        getServer().getPluginManager().registerEvents(this, this);
        mm = new MetaManager(this);
        initstate = new Qstate(true);
        // Command initializing logic
        Objects.requireNonNull(this.getCommand("qskit")).setExecutor(this);
        Objects.requireNonNull(this.getCommand("qsinit")).setExecutor(this);
        Objects.requireNonNull(this.getCommand("customgate")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String label, @Nonnull String[] args) {
        // command /qskit
        // Set gate A into custom 2x2 matrix
        if (label.equalsIgnoreCase("qskit")) {
            PlayerInventory inventory = ((Player)sender).getInventory();
            inventory.clear();
            Material[] kititems = new Material[] {
                    Material.COAL_BLOCK,
                    Material.BONE_BLOCK,
                    Material.WHITE_STAINED_GLASS,
                    Material.REDSTONE_BLOCK,
                    Material.EMERALD_BLOCK,
                    Material.COPPER_BLOCK,
                    Material.DIAMOND_BLOCK,
                    Material.GOLD_BLOCK,
                    Material.QUARTZ_BLOCK,
            };
            for(Material mat : kititems) {
                ItemStack kitblock = new ItemStack(mat, 64);
                inventory.addItem(kitblock);
            }
            return true;
        }
        // command /customgate
        // Set gate A into custom 2x2 matrix
        else if (label.equalsIgnoreCase("customgate")) {
            if (args.length == 4) {
                try {
                    for(int i=0; i<4; i++)
                        Gate.A[i/2][i%2] = Qubit.StringtoComplex(args[i]);
                    sender.sendMessage(String.format(
                            "Gate A is set to [[%s, %s], [%s, %s]]",
                            Qubit.ComplextoString(Gate.A[0][0]),
                            Qubit.ComplextoString(Gate.A[0][1]),
                            Qubit.ComplextoString(Gate.A[1][0]),
                            Qubit.ComplextoString(Gate.A[1][1])
                    ));
                } catch (NumberFormatException e) {
                    sender.sendMessage("Invalid number. Please enter a complex value.");
                }
            } else {
                sender.sendMessage("Invalid argument type!");
                sender.sendMessage("Usage: /customgate a+bi c+di e+fi g+hi");
            }
            return true;
        }
        // command /qsinit
        // Set starting quantum state into custom state
        else if(label.equalsIgnoreCase("qsinit")) {
            if (args.length == 2) {
                try {
                    initstate.q.coef[0] = Qubit.StringtoComplex(args[0]);
                    initstate.q.coef[1] = Qubit.StringtoComplex(args[1]);
                    initstate.q.normalize();

                    sender.sendMessage(String.format(
                            "Now starting quantum state is 0:%s, 1:%s",
                            Qubit.ComplextoString(initstate.q.coef[0]),
                            Qubit.ComplextoString(initstate.q.coef[1])
                    ));
                } catch (NumberFormatException e) {
                    sender.sendMessage("Invalid number. Please enter a complex value.");
                }
            } else {
                sender.sendMessage("Invalid argument type!");
                sender.sendMessage("Usage: /qsinit a+bi c+di");
            }
            return true;
        }
        return false;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        currplayer = event.getPlayer();
        Block placedBlock = event.getBlockPlaced();

        if(placedBlock.getType() == Material.COAL_BLOCK) {
            // start propagation
            qsProp(placedBlock, -1, initstate);
        }
        else {
            // resume suspended propagation if exists
            Block curr;
            int propfromdir = -1;
            for(int i=0; i<6; i++) {
                curr = placedBlock.getRelative(dir[i][0], dir[i][1], dir[i][2]);
                if(curr.hasMetadata("qstate")) {
                    if(propfromdir == -1)
                        propfromdir = i;
                    else{
                        // More than two inputs: invalid
                        placedBlock.breakNaturally(new ItemStack(Material.AIR));
                        currplayer.sendMessage("Invalid Propagation!");
                        return;
                    }
                }
            }
            if(propfromdir == -1) return;

            curr = placedBlock.getRelative(dir[propfromdir][0], dir[propfromdir][1], dir[propfromdir][2]);
            Qstate q = mm.Get(curr, "qstate", Qstate.class);
            int fromdir = mm.Get(curr, "fromdir", Integer.class);
            switch (curr.getType()) {
                case EMERALD_BLOCK:         // X gate
                case COPPER_BLOCK:          // Y gate
                case DIAMOND_BLOCK:         // Z gate
                case GOLD_BLOCK:            // H gate
                case QUARTZ_BLOCK:          // A gate : Custom gate
                    if(oppdir[propfromdir] == fromdir) qsProp(placedBlock, oppdir[propfromdir], q);
                    break;
                case REDSTONE_BLOCK:
                    qsProp(placedBlock, oppdir[propfromdir], q.setselsig(oppdir[propfromdir] != fromdir));
                    break;
                default:
                    qsProp(placedBlock, oppdir[propfromdir], q);
            }
        }
    }

    private void qsProp(Block curr, int fromdir, Qstate q) {
        Complex[][] gate = null;
        boolean makeselsig = false;

        switch (curr.getType()) {
        /* ======================= Start & End ======================== */
            case COAL_BLOCK:            // start: initialize qubit
                // nothing to do
                break;
            case BONE_BLOCK:            // ends: measure
//                measProp(curr);
                return;
        /* ==================== Propagation Blocks ==================== */
            case WHITE_STAINED_GLASS:   // make new propagation
                if(q.selsig)
                    curr.setType(Material.RED_STAINED_GLASS);
                else
                    curr.setType(Material.YELLOW_STAINED_GLASS);
                break;
            case YELLOW_STAINED_GLASS:  // propagated block         -> occurs error below
            case RED_STAINED_GLASS:     // selection signal block   -> occurs error below
            case BLUE_STAINED_GLASS:    // selection signal block   -> occurs error below
                break;
            case REDSTONE_BLOCK:        // make a selection signal
                makeselsig = true;
                break;
        /* ===================== Quantum Gates ======================== */
            case EMERALD_BLOCK:         // X gate
                gate = Gate.X;
                break;
            case COPPER_BLOCK:          // Y gate
                gate = Gate.Y;
                break;
            case DIAMOND_BLOCK:         // Z gate
                gate = Gate.Z;
                break;
            case GOLD_BLOCK:            // H gate
                gate = Gate.H;
                break;
            case QUARTZ_BLOCK:          // A gate : Custom gate
                gate = Gate.A;
                break;
        /* =================== Irrelevant Blocks ====================== */
            default:
                return;
        }

        Qstate newq = q.copy();

        // Handle operations on quantum gate
        if(gate != null) {
            if(q.selsig) {
                // Selection signal arrived to a gate
                if(curr.hasMetadata("qstate")) {
                    // Data already in the gate: propagate affected
                    Qstate data = mm.Get(curr, "qstate", Qstate.class);
                    controlledApply(curr, data, newq.q, fromdir, gate);

                    newq = data;
                    fromdir = mm.Get(curr, "fromdir", Integer.class);
                }
                else {
                    // Data not in the gate: wait for data
                    return;
                }
            }
            else {
                // Data signal arrived to a gate
                Qubit selq = null;
                int selsigfrom = -1;

                // find selq, selsigfrom
                for(int i=0; i<6; i++) {
                    Qstate tmp = mm.Get(curr.getRelative(dir[i][0], dir[i][1], dir[i][2]), "qstate", Qstate.class);
                    if(tmp != null && tmp.selsig) {
                        if(selq != null) {
                            // If more than one selection signal: break gate
                            curr.breakNaturally(new ItemStack(Material.AIR));
                            breakProp(curr);
                            return;
                        }
                        selq = tmp.q;
                        selsigfrom = oppdir[i];
                    }
                }

                if(selq != null) {
                    // Selsig already in the gate: propagate affected
                    controlledApply(curr, newq, selq, selsigfrom, gate);
                }
                else {
                    // Selsig not in the gate: non-conditional gate operation
                    newq = q.apply(gate);
                }
            }
        }

        // Check invalid propagation
        if(curr.hasMetadata("fromdir")) {
            if(mm.Get(curr, "fromdir", Integer.class) != fromdir) {
                int opp = oppdir[fromdir];
                Block invalidblock = curr.getRelative(dir[opp][0], dir[opp][1], dir[opp][2]);
                TextDisplay invtext = mm.Get(invalidblock, "text", TextDisplay.class);
                if(invtext != null) invtext.remove();
                currplayer.sendMessage("Invalid Propagation!");
                invalidblock.breakNaturally(new ItemStack(Material.AIR));
                mm.DelAll(invalidblock);
                return;
            }
        }

        // Set text
        TextDisplay text = mm.Get(curr, "text", TextDisplay.class);
        if(text == null) {
            // No text display yet: spawn new one
            text = world.spawn(curr.getLocation().add(0.5, 1.1, 0.5), TextDisplay.class);
            text.setBillboard(Display.Billboard.CENTER);
            text.setTransformation(new Transformation(
                    new Vector3f(0f,0f,0f),                // translation
                    new Quaternionf(0f, 0f, 0f, 1f),    // leftRotation
                    new Vector3f(0.5f,0.5f,0.5f),           // scale
                    new Quaternionf(0f, 0f, 0f, 1f)      // rightRotation
            ));
        }
        text.setText(newq.toString());

        // Set metadata
        mm.Set(curr, "fromdir", fromdir);
        mm.Set(curr, "qstate", newq);
        mm.Set(curr, "text", text);

        // Propagate into other directions
        for(int i=0; i<6; i++) {
            if(fromdir != -1 && i == oppdir[fromdir]) continue;
            if(gate != null && i != fromdir) continue;
            if(makeselsig)
                qsProp(curr.getRelative(dir[i][0], dir[i][1], dir[i][2]), i, newq.setselsig(i != fromdir));
            else
                qsProp(curr.getRelative(dir[i][0], dir[i][1], dir[i][2]), i, newq);
        }
    }

    public void controlledApply(Block gateblock, Qstate dataq, Qubit selq, int selsigfrom, Complex[][] gate) {
        Qubit ifzero = dataq.q.copy();
        Qubit ifone  = dataq.q.apply(gate);
        Qubit rev_ifzero = new Qubit (ifzero.coef[0].multiply(selq.coef[0]), ifone.coef[0].multiply(selq.coef[1]));
        Qubit rev_ifone  = new Qubit (ifzero.coef[1].multiply(selq.coef[0]), ifone.coef[1].multiply(selq.coef[1]));

        dataq.q.coef[0] = new Complex(rev_ifzero.normalize());
        dataq.q.coef[1] = new Complex(rev_ifone.normalize());
        dataq.addAffected(rev_ifzero, rev_ifone);

        // Selection signal connected
        int opp = oppdir[selsigfrom];
        Block sigblock = gateblock.getRelative(dir[opp][0], dir[opp][1], dir[opp][2]);
        while(sigblock.getType() != Material.REDSTONE_BLOCK) {
            sigblock.setType(Material.BLUE_STAINED_GLASS);
            int from = oppdir[mm.Get(sigblock, "fromdir", Integer.class)];
            sigblock = sigblock.getRelative(dir[from][0], dir[from][1], dir[from][2]);
        }

        Qstate otherq = mm.Get(sigblock, "qstate", Qstate.class);
        otherq.addAffected(ifzero, ifone);

        TextDisplay text = mm.Get(sigblock, "text", TextDisplay.class);
        int fromdir = mm.Get(sigblock, "fromdir", Integer.class);
        text.setText(otherq.toString());

        qsProp(sigblock.getRelative(dir[fromdir][0], dir[fromdir][1], dir[fromdir][2]), fromdir, otherq);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        currplayer = event.getPlayer();
        Block curr = event.getBlock();

        if(curr.getType() == Material.BLUE_STAINED_GLASS) {
            // Selection signal disconnected
            while(curr.getType() != Material.REDSTONE_BLOCK) {
                curr.setType(Material.RED_STAINED_GLASS);
                int from = mm.Get(curr, "fromdir", Integer.class);
                curr = curr.getRelative(dir[from][0], dir[from][1], dir[from][2]);
            }
        }

        breakProp(curr);
    }

    private void breakProp(Block curr) {
        if(curr.hasMetadata("fromdir")) {
            int fromdir = mm.Get(curr, "fromdir", Integer.class);
            TextDisplay text = mm.Get(curr, "text", TextDisplay.class);
            if(text != null) text.remove();

            Material mat = curr.getType();
            if(mat == Material.YELLOW_STAINED_GLASS
            || mat == Material.RED_STAINED_GLASS || mat == Material.BLUE_STAINED_GLASS)
                curr.setType(Material.WHITE_STAINED_GLASS);
            mm.DelAll(curr);

            for(int i=0; i<6; i++) {
                if(fromdir != -1 && i == oppdir[fromdir]) continue;
                breakProp(curr.getRelative(dir[i][0], dir[i][1], dir[i][2]));
            }
        }
    }

    private void measProp(Block curr) {
        if(curr.hasMetadata("fromdir")) {
            int fromdir = mm.Get(curr, "fromdir", Integer.class);
            Qstate qstate = mm.Get(curr, "qstate", Qstate.class);
            TextDisplay text = mm.Get(curr, "text", TextDisplay.class);

            int randomrange = (int)Math.pow(10, precision);
            int randnum = new Random().nextInt(randomrange) + 1;
            int zeromeasure = (int)(Math.pow(qstate.q.coef[0].abs(), 2) * randomrange);
            int meas_result = randnum <= zeromeasure ? 1 : 0;
            text.setText(String.format("Measured\n%d", meas_result));
            text.setTransformation(new Transformation(
                    new Vector3f(0f,0f,0f),                // translation
                    new Quaternionf(0f, 0f, 0f, 1f),    // leftRotation
                    new Vector3f(1f,1f,1f),           // scale
                    new Quaternionf(0f, 0f, 0f, 1f)      // rightRotation
            ));

            for(int i=0; i<6; i++) {
                if(fromdir != -1 && i == oppdir[fromdir]) continue;
                breakProp(curr.getRelative(dir[i][0], dir[i][1], dir[i][2]));
            }
        }
    }
}
