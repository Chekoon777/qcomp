package qd.qcomp.qcompplugin;

import org.bukkit.block.Block;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class MetaManager {
    Plugin owningplugin;
    public final String[] metanames = {
            "fromdir",
            "qstate",
            "text"
    };

    public MetaManager(Plugin plugin) {
        owningplugin = plugin;
    }

    public <T> void Set(Block curr, String name, T data) {
        MetadataValue metadataValue = new FixedMetadataValue(owningplugin, data);
        curr.setMetadata(name, metadataValue);
    }

    public <T> T Get(Block curr, String name, Class<T> expectedType) {
        List<MetadataValue> values = curr.getMetadata(name);
        for (MetadataValue value : values) {
            if (value.getOwningPlugin() == owningplugin) {
                Object storedValue = value.value();

                if (expectedType.isInstance(storedValue)) {
                    return expectedType.cast(storedValue);
                } else {
                    throw new IllegalArgumentException("Unexpected metadata type");
                }
            }
        }
        return null;
    }

    public void DelAll(Block curr) {
        for (String name : metanames) {
            if(curr.hasMetadata(name))
                curr.removeMetadata(name, owningplugin);
        }
    }
}
