package me.aroze.simplectf.util;

import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public final class ItemUtil {

    public static ItemStack setBasicFlags(ItemStack item) {
        item.editMeta(meta -> {
            meta.setUnbreakable(true);
            meta.setAttributeModifiers(item.getType().getDefaultAttributeModifiers());
            meta.addItemFlags(ItemFlag.values());
        });

        return item;
    }

}
