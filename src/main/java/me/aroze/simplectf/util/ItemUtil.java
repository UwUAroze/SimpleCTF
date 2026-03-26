package me.aroze.simplectf.util;

import lombok.experimental.UtilityClass;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

/**
 * A collection of item manipulation utilities
 */
@UtilityClass
public class ItemUtil {

    /**
     * Sets basic custom flags for a game item, making it unbreakable and hiding attribute modifiers.
     *
     * @param item the {@link ItemStack} to modify
     * @return the same {@link ItemStack}
     */
    public ItemStack setBasicFlags(ItemStack item) {
        item.editMeta(meta -> {
            meta.setUnbreakable(true);
            meta.setAttributeModifiers(item.getType().getDefaultAttributeModifiers());
            meta.addItemFlags(ItemFlag.values());
        });

        return item;
    }

}
