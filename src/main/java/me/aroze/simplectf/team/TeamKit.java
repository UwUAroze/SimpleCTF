package me.aroze.simplectf.team;

import me.aroze.simplectf.util.ItemUtil;
import me.aroze.simplectf.util.text.CtfMiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class TeamKit {

    private static final ItemStack[] SHARED_ITEMS = new ItemStack[] {
        ItemUtil.setBasicFlags(new ItemStack(Material.STONE_SWORD)),
        ItemUtil.setBasicFlags(new ItemStack(Material.BOW)),
        ItemUtil.setBasicFlags(new ItemStack(Material.ARROW, 32))
    };

    private final TeamColor teamColor;

    private final ItemStack flagInventoryItem;
    private final ItemStack chestplate;
    private final ItemStack leggings;
    private final ItemStack boots;

    public TeamKit(TeamColor teamColor) {
        this.teamColor = teamColor;

        this.flagInventoryItem = createFlagItem();
        this.chestplate = createDyedLeather(Material.LEATHER_CHESTPLATE);
        this.leggings = createDyedLeather(Material.LEATHER_LEGGINGS);
        this.boots = createDyedLeather(Material.LEATHER_BOOTS);
    }

    public void applyKit(final Player player) {
        player.getInventory().clear();

        for (final ItemStack sharedItem : SHARED_ITEMS) {
            player.getInventory().addItem(sharedItem.clone());
        }

        player.getInventory().setArmorContents(new ItemStack[] {boots.clone(), leggings.clone(), chestplate.clone()});
    }

    public ItemStack retrieveFlagItem() {
        return flagInventoryItem.clone();
    }

    private ItemStack createFlagItem() {
        final ItemStack item = new ItemStack(teamColor.flagType());

        item.editMeta(meta -> meta.displayName(CtfMiniMessage.getInstance().deserialize(
            "<i:false><color><team>'s Flag</color>",
            Placeholder.styling("color", teamColor.color()),
            Placeholder.component("team", teamColor.formattedDisplayName())
        )));

        return item;
    }

    private ItemStack createDyedLeather(final Material material) {
        final ItemStack item = new ItemStack(material);

        item.editMeta(meta -> {
            if (meta instanceof LeatherArmorMeta leatherMeta) {
                leatherMeta.setColor(teamColor.bukkitColor());
            }
        });

        return item;
    }

}
