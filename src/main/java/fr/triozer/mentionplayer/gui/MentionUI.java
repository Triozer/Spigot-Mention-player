package fr.triozer.mentionplayer.gui;

import fr.triozer.mentionplayer.MentionPlayer;
import fr.triozer.mentionplayer.api.player.MPlayer;
import fr.triozer.mentionplayer.api.ui.ClickableItem;
import fr.triozer.mentionplayer.api.ui.builder.InventoryBuilder;
import fr.triozer.mentionplayer.api.ui.builder.ItemBuilder;
import fr.triozer.mentionplayer.api.ui.color.ColorData;
import fr.triozer.mentionplayer.misc.I18N;
import fr.triozer.mentionplayer.misc.Settings;
import fr.triozer.mentionplayer.misc.Utils;
import fr.triozer.mentionplayer.misc.xseries.XMaterial;
import fr.triozer.mentionplayer.misc.xseries.XSound;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * @author Cédric / Triozer
 */
public class MentionUI {
    public static void open(Player player) {
        MPlayer          mPlayer  = MPlayer.get(player.getUniqueId());
        InventoryBuilder contents = new InventoryBuilder(I18N.get("messages.gui.settings.title"), 36, true).fill(ClickableItem.EMPTY);
        ItemStack        sounds   = new ItemBuilder(XMaterial.NOTE_BLOCK.parseMaterial()).name(I18N.get("messages.gui.settings.categories.sounds.item.title")).lore(I18N.getStringList("messages.gui.settings.categories.sounds.item.lore")).build();
        ItemStack        ignored  = new ItemBuilder(XMaterial.BARRIER.parseMaterial()).name(I18N.get("messages.gui.settings.categories.ignored-players.item.title")).lore(I18N.getStringList("messages.gui.settings.categories.ignored-players.item.lore")).build();

        if (mPlayer.canUseTag()) {
            contents.setItem(20, ClickableItem.of(sounds, (event) -> openSound(mPlayer, true)));
            contents.setItem(21, ClickableItem.of(ignored, (event) -> openIgnored(mPlayer, true)));
            if (mPlayer.getColor() != ColorData.RAINBOW && !mPlayer.getColor().isCustom()) {
                ItemStack wool = XMaterial.matchXMaterial(
                        "WOOL",
                        mPlayer.getColor().getDyeColor()[0].getWoolData()
                ).get().parseItem();
                ItemMeta itemMeta = wool.getItemMeta();
                itemMeta.setDisplayName(I18N.get("messages.gui.settings.categories.color.item.title"));
                itemMeta.setLore(Arrays.asList(I18N.getStringList("messages.gui.settings.categories.color.item.lore")));
                wool.setItemMeta(itemMeta);

                contents.setItem(19, ClickableItem.of(
                        wool,
                        (event) -> openColor(mPlayer, true)
                ));
            } else {
                final int[] tries = {0};
                new BukkitRunnable() {
                    Random random = new Random();

                    @Override
                    public void run() {
                        if (player.getPlayer() == null || !player.getPlayer().isOnline()) cancel();

                        if (player.getPlayer().getOpenInventory().getTopInventory().equals(contents.build())) {
                            DyeColor color;
                            if (mPlayer.getColor().isCustom()) {
                                color = mPlayer.getColor().getDyeColor()[tries[0]++];
                            } else {
                                color = DyeColor.values()[this.random.nextInt(16)];
                            }

                            ItemStack wool = XMaterial.matchXMaterial(
                                    "WOOL",
                                    color.getWoolData()
                            ).get().parseItem();
                            ItemMeta itemMeta = wool.getItemMeta();
                            itemMeta.setDisplayName(I18N.get("messages.gui.settings.categories.color.item.title"));
                            itemMeta.setLore(Arrays.asList(I18N.getStringList("messages.gui.settings.categories.color.item.lore")));
                            wool.setItemMeta(itemMeta);

                            contents.setItem(19, ClickableItem
                                    .of(wool, (event) -> {
                                        this.cancel();
                                        MentionUI.openColor(mPlayer, true);
                                    }));
                            if (tries[0] == mPlayer.getColor().getDyeColor().length) {
                                tries[0] = 0;
                            }
                        } else {
                            this.cancel();
                        }
                    }
                }.runTaskTimerAsynchronously(MentionPlayer.getInstance(), 5L, 5L);
            }
        } else {
            contents.setItem(19, ClickableItem.of(sounds, (event) -> openSound(mPlayer, true)));
            contents.setItem(20, ClickableItem.of(ignored, (event) -> openIgnored(mPlayer, true)));
        }

        setContents(contents, mPlayer, 10);
        player.openInventory(contents.build());
    }


    private static void setContents(InventoryBuilder inventoryBuilder, MPlayer mPlayer, int firstSlot) {
        ItemStack sound = new ItemBuilder(mPlayer.allowSound() ? XMaterial.LIME_DYE.parseItem() : XMaterial.GRAY_DYE.parseItem())
                .name(I18N.get("messages.gui.settings.actions.sounds.title"))
                .lore(Utils.set(
                        I18N.getStringList("messages.gui.settings.actions.sound.lore"),
                        new String[]{"state"},
                        !mPlayer.allowSound() ? I18N.get("messages.gui.state.enable") : I18N.get("messages.gui.state.disable"))
                )
                .build();
        ItemStack mention = new ItemBuilder(mPlayer.allowMention() ? XMaterial.LIME_DYE.parseItem() : XMaterial.GRAY_DYE.parseItem())
                .name(I18N.get("messages.gui.settings.actions.mention.title"))
                .lore(Utils.set(
                        I18N.getStringList("messages.gui.settings.actions.mention.lore"),
                        new String[]{"state"},
                        !mPlayer.allowMention() ? I18N.get("messages.gui.state.enable") : I18N.get("messages.gui.state.disable"))
                )
                .build();
        ItemStack visible = new ItemBuilder(mPlayer.isMentionPublic() ? XMaterial.LIME_DYE.parseItem() : XMaterial.GRAY_DYE.parseItem())
                .name(I18N.get("messages.gui.settings.actions.public.title"))
                .lore(Utils.set(
                        I18N.getStringList("messages.gui.settings.actions.public.lore"),
                        new String[]{"state"},
                        !mPlayer.isMentionPublic() ? I18N.get("messages.gui.state.enable") : I18N.get("messages.gui.state.disable"))
                )
                .build();

        inventoryBuilder.setItem(firstSlot++, ClickableItem.of(sound, (event) -> {
            mPlayer.setSound(!mPlayer.allowSound());
            ItemMeta meta = event.getCurrentItem().getItemMeta();
            meta.setLore(Arrays.asList(Utils.set(
                    I18N.getStringList("messages.gui.settings.actions.sound.lore"),
                    new String[]{"state"},
                    !mPlayer.allowSound() ? I18N.get("messages.gui.state.enable") : I18N.get("messages.gui.state.disable"))));
            event.getCurrentItem().setItemMeta(meta);
            open(mPlayer.getPlayer());
        })).setItem(firstSlot++, ClickableItem.of(mention, (event) -> {
            mPlayer.setMention(!mPlayer.allowMention());
            ItemMeta meta = event.getCurrentItem().getItemMeta();
            meta.setLore(Arrays.asList(Utils.set(
                    I18N.getStringList("messages.gui.settings.actions.mention.lore"),
                    new String[]{"state"},
                    !mPlayer.allowMention() ? I18N.get("messages.gui.state.enable") : I18N.get("messages.gui.state.disable"))));
            event.getCurrentItem().setItemMeta(meta);
            open(mPlayer.getPlayer());
        })).setItem(firstSlot++, ClickableItem.of(visible, (event) -> {
            mPlayer.setVisible(!mPlayer.isMentionPublic());
            ItemMeta meta = event.getCurrentItem().getItemMeta();
            meta.setLore(Arrays.asList(Utils.set(
                    I18N.getStringList("messages.gui.settings.actions.public.lore"),
                    new String[]{"state"},
                    !mPlayer.isMentionPublic() ? I18N.get("messages.gui.state.enable") : I18N.get("messages.gui.state.disable"))));
            event.getCurrentItem().setItemMeta(meta);
            open(mPlayer.getPlayer());
        }));
        if (Settings.canActionBar()) {
            ItemStack action = new ItemBuilder(mPlayer.allowActionbar() ? XMaterial.LIME_DYE.parseItem() : XMaterial.GRAY_DYE.parseItem())
                    .name(I18N.get("messages.gui.settings.actions.action-bar.title"))
                    .lore(Utils.set(
                            I18N.getStringList("messages.gui.settings.actions.action-bar.lore"),
                            new String[]{"state"},
                            !mPlayer.allowActionbar() ? I18N.get("messages.gui.state.enable") : I18N.get("messages.gui.state.disable"))
                    )
                    .build();
            inventoryBuilder.setItem(firstSlot++, ClickableItem.of(action, (event) -> {
                mPlayer.setActionbar(!mPlayer.allowActionbar());
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                meta.setLore(Arrays.asList(Utils.set(
                        I18N.getStringList("messages.gui.settings.actions.action-bar.lore"),
                        new String[]{"state"},
                        !mPlayer.allowActionbar() ? I18N.get("messages.gui.state.enable") : I18N.get("messages.gui.state.disable"))));
                event.getCurrentItem().setItemMeta(meta);
                open(mPlayer.getPlayer());
            }));
        }
        if (Settings.canPopup()) {
            ItemStack popup = (new ItemBuilder(mPlayer.allowPopup() ? XMaterial.LIME_DYE.parseItem() : XMaterial.GRAY_DYE.parseItem()))
                    .name(I18N.get("messages.gui.settings.actions.popup.title"))
                    .lore(Utils.set(
                            I18N.getStringList("messages.gui.settings.actions.popup.lore"),
                            new String[]{"state"},
                            !mPlayer.allowPopup() ? I18N.get("messages.gui.state.enable") : I18N.get("messages.gui.state.disable"))
                    )
                    .build();
            inventoryBuilder.setItem(firstSlot, ClickableItem.of(popup, (event) -> {
                mPlayer.setPopup(!mPlayer.allowPopup());
                ItemMeta meta = event.getCurrentItem().getItemMeta();
                meta.setLore(Arrays.asList(Utils.set(
                        I18N.getStringList("messages.gui.settings.actions.popup.lore"),
                        new String[]{"state"},
                        !mPlayer.allowPopup() ? I18N.get("messages.gui.state.enable") : I18N.get("messages.gui.state.disable"))
                ));
                event.getCurrentItem().setItemMeta(meta);
                open(mPlayer.getPlayer());
            }));
        }
    }

    public static void openColor(MPlayer player, boolean fromGui) {
        ItemStack back = null;
        if (fromGui)
            back = new ItemBuilder(Material.ARROW)
                    .name(I18N.get("messages.gui.back.title"))
                    .lore(I18N.getStringList("messages.gui.back.lore"))
                    .build();

        int size = Math.round((float) MentionPlayer.getInstance().getColorData().size() / 9.0F) * 9 + 9;
        InventoryBuilder color = new InventoryBuilder(I18N.get("messages.gui.settings.categories.color.gui.title"), size, true)
                .fill(ClickableItem.EMPTY);
        if (fromGui) color.setItem(size - 1, ClickableItem.of(back, (event) -> open(player.getPlayer())));
        int slot = 0;
        for (ColorData colorData : MentionPlayer.getInstance().getColorData().values()) {
            if (!player.canUseTag(colorData)) continue;

            if (colorData == ColorData.RAINBOW || colorData.isCustom()) {
                int finalI = slot;
                // hack this shit x)
                final int[] tries = {0};
                new BukkitRunnable() {
                    Random random = new Random();

                    @Override
                    public void run() {
                        if (player.getPlayer() == null || !player.getPlayer().isOnline()) cancel();

                        if (player.getPlayer().getOpenInventory().getTopInventory().equals(color.build())) {
                            ItemStack item = XMaterial.matchXMaterial(
                                    player.getColor() == colorData ? "STAINED_GLASS_PANE" : "WOOL",
                                    (colorData.isCustom() ?
                                            colorData.getDyeColor()[tries[0]++]
                                            : DyeColor.values()[random.nextInt(16)]).getWoolData()
                            ).get().parseItem();
                            ItemMeta meta = item.getItemMeta();
                            meta.setDisplayName(colorData.parse(colorData.getName()));
                            item.setItemMeta(meta);

                            color.setItem(finalI, ClickableItem
                                    .of(item,
                                            event -> {
                                                player.setColor(colorData);
                                                openColor(player, fromGui);
                                                cancel();
                                            }));
                            if (tries[0] == colorData.getDyeColor().length) tries[0] = 0;
                        } else cancel();
                    }
                }.runTaskTimerAsynchronously(MentionPlayer.getInstance(), 1L, 5L);

            } else if (player.getColor() == colorData) {
                ItemStack item = XMaterial.matchXMaterial(
                        "STAINED_GLASS_PANE",
                        colorData.getDyeColor()[0].getWoolData()
                ).get().parseItem();
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(colorData.parse(colorData.getName()));
                meta.setLore(Arrays.asList(I18N.getStringList("messages.gui.settings.categories.color.gui.current-color-lore")));
                item.setItemMeta(meta);

                color.setItem(slot, ClickableItem.empty(item));
            } else {
                ItemStack item = XMaterial.matchXMaterial(
                        "WOOL",
                        colorData.getDyeColor()[0].getWoolData()
                ).get().parseItem();
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(colorData.parse(colorData.getName()));
                item.setItemMeta(meta);

                color.setItem(slot, ClickableItem.of(
                        item,
                        event -> {
                            player.setColor(colorData);
                            openColor(player, fromGui);
                        }
                ));
            }
            slot++;
        }

        player.getPlayer().openInventory(color.build());

    }

    public static void openSound(MPlayer player, boolean fromGui) {
        ItemStack back = null;
        if (fromGui)
            back = new ItemBuilder(Material.ARROW)
                    .name(I18N.get("messages.gui.back.title"))
                    .lore(I18N.getStringList("messages.gui.back.lore"))
                    .build();

        List<Sound> listSound = new LinkedList<>();

        listSound.add(XSound.BLOCK_NOTE_BLOCK_BASEDRUM.parseSound());
        listSound.add(XSound.BLOCK_NOTE_BLOCK_BASS.parseSound());
        listSound.add(XSound.BLOCK_NOTE_BLOCK_BELL.parseSound());
        listSound.add(XSound.BLOCK_NOTE_BLOCK_CHIME.parseSound());
        listSound.add(XSound.BLOCK_NOTE_BLOCK_FLUTE.parseSound());
        listSound.add(XSound.BLOCK_NOTE_BLOCK_GUITAR.parseSound());
        listSound.add(XSound.BLOCK_NOTE_BLOCK_HARP.parseSound());
        listSound.add(XSound.BLOCK_NOTE_BLOCK_HAT.parseSound());
        listSound.add(XSound.BLOCK_NOTE_BLOCK_PLING.parseSound());
        listSound.add(XSound.BLOCK_NOTE_BLOCK_SNARE.parseSound());
        if (XSound.BLOCK_NOTE_BLOCK_XYLOPHONE.isSupported())
            listSound.add(XSound.BLOCK_NOTE_BLOCK_XYLOPHONE.parseSound());
        listSound.add(XSound.ENTITY_PLAYER_HURT.parseSound());
        listSound.add(XSound.ENTITY_VILLAGER_HURT.parseSound());
        listSound.add(XSound.ENTITY_VILLAGER_NO.parseSound());
        listSound.add(XSound.ENTITY_VILLAGER_TRADE.parseSound());
        listSound.add(XSound.ENTITY_VILLAGER_YES.parseSound());
        listSound.add(XSound.ENTITY_EXPERIENCE_ORB_PICKUP.parseSound());


        int size = Math.round((float) listSound.size() / 9.0F) * 9 + 9;
        InventoryBuilder sound = new InventoryBuilder(I18N.get("messages.gui.settings.categories.sounds.gui.title"), size, true)
                .fill(ClickableItem.EMPTY);
        if (fromGui) sound.setItem(size - 1, ClickableItem.of(back, (event) -> open(player.getPlayer())));

        final Sound[]     choose  = {null};
        final ItemStack[] choosed = {null};
        int               pos     = 0;
        for (Sound _sound : listSound) {
            if (_sound == null) continue;
            String name = I18N.get("messages.gui.settings.categories.sounds.gui.notes." + _sound.name(), _sound.name());
            if (player.getSound() == _sound) {
                sound.setItem(pos, ClickableItem.of(new ItemBuilder(Material.NOTE_BLOCK)
                                .name(I18N.get("messages.gui.settings.categories.sounds.gui.current-sound-item.name").replace("{sound-name}", name))
                                .lore(I18N.getStringList("messages.gui.settings.categories.sounds.gui.current-sound-item.lore")),
                        (event) -> player.getPlayer().playSound(player.getPlayer().getLocation(), _sound, 1f, 1f)));
            } else {
                sound.setItem(pos, ClickableItem.of(new ItemBuilder(Material.NOTE_BLOCK)
                                .name(I18N.get("messages.gui.settings.categories.sounds.gui.block-note-item.name").replace("{sound-name}", name))
                                .lore(I18N.getStringList("messages.gui.settings.categories.sounds.gui.block-note-item.lore")),
                        (event) -> {
                            if (choose[0] != _sound) {
                                ItemMeta meta;
                                if (choosed[0] != null &&
                                        !choosed[0].getItemMeta().getDisplayName().equalsIgnoreCase(event.getCurrentItem().getItemMeta().getDisplayName())) {
                                    meta = choosed[0].getItemMeta();
                                    meta.setLore(Arrays.asList(I18N.getStringList("messages.gui.settings.categories.sounds.gui.block-note-item.lore")));
                                    choosed[0].setItemMeta(meta);
                                }
                                meta = event.getCurrentItem().getItemMeta();
                                meta.setLore(Arrays.asList(I18N.getStringList("messages.gui.settings.categories.sounds.gui.block-note-confirmation-lore")));
                                event.getCurrentItem().setItemMeta(meta);
                                choose[0] = _sound;
                                choosed[0] = event.getCurrentItem();
                            } else {
                                player.setSound(_sound);
                                openSound(player, fromGui);
                            }
                            player.getPlayer().playSound(player.getPlayer().getLocation(), _sound, 1f, 1f);
                        }));
            }
            pos++;
        }

        player.getPlayer().openInventory(sound.build());
    }

    public static void openIgnored(MPlayer player, boolean fromGui) {
        ItemStack back = null;
        if (fromGui)
            back = new ItemBuilder(Material.ARROW)
                    .name(I18N.get("messages.gui.back.title"))
                    .lore(I18N.getStringList("messages.gui.back.lore"))
                    .build();

        final List<UUID> uuids = new ArrayList<>(player.getIgnoredPlayers());
        int              size  = Math.round((float) uuids.size() / 9.0F) * 9 + 9;
        if (size < 18) size += 9;
        InventoryBuilder ignore = new InventoryBuilder(I18N.get("messages.gui.settings.categories.ignored-players.gui.title"), size, true)
                .fill(ClickableItem.EMPTY);
        if (fromGui) ignore.setItem(size - 1, ClickableItem.of(back, (event) -> open(player.getPlayer())));

        for (int i = 0; i < uuids.size(); i++) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(uuids.get(i));
            if (target == null) continue;
            String name = target.getName();

            ignore.setItem(i, ClickableItem.of(new ItemBuilder.Skull(target.getUniqueId())
                            .name(I18N.get("messages.gui.settings.categories.ignored-players.gui.item-name").replace("{player-name}", name))
                            .lore(I18N.getStringList("messages.gui.settings.categories.ignored-players.gui.item-lore")),
                    (event) -> {
                        player.ignore(target);
                        openIgnored(player, fromGui);
                    }));
        }

        player.getPlayer().openInventory(ignore.build());
    }

}