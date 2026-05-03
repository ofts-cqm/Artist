package net.ofts.artist.client.comtroller;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.ofts.artist.client.Config;
import net.ofts.artist.client.menu.MenuHandler;

import java.util.HashSet;

public class InventoryUtils {
    public static void sleep(){
        try {
            Thread.sleep(Config.MENU_WAIT_TIME);
        } catch (InterruptedException ignored) {}
    }

    public static int getFromChest(AbstractContainerScreen<?> screen, Item required, int amount, int from, int to, Container toContainer){
        int clicked = 0;

        NonNullList<Slot> slots = screen.getMenu().slots;
        for (int i = from; i < to; i++) {
            Slot slot = slots.get(i);
            if (slot.getItem().is(required) && slot.getItem().getCount() == 64){
                MenuHandler.sendClick(screen.getMenu(), i, ClickType.QUICK_MOVE);
                clicked++;
                sleep();
                if (clicked >= amount) break;
            }
        }

        return Math.min(clicked, countFreeSlots(toContainer));
    }

    // free slots, occurred type count
    public static int countFreeSlots(){
        assert Minecraft.getInstance().player != null;
        Inventory inventory = Minecraft.getInstance().player.getInventory();
        return countFreeSlots(inventory);
    }

    public static int countType(){
        assert Minecraft.getInstance().player != null;
        Inventory inventory = Minecraft.getInstance().player.getInventory();
        HashSet<Item> occurred = new HashSet<>();
        for (int i = 0; i < 36; i++){
            Item item = inventory.getItem(i).getItem();
            occurred.add(item);
        }
        return occurred.size();
    }

    public static int countFreeSlots(Container inventory){
        return countSlotOf(inventory, null);
    }

    public static int countSlotOf(Container inventory, Item target){
        int freeSlots = 0;
        for (int i = 0; i < 36; i++){
            ItemStack item = inventory.getItem(i);
            if ((target == null && item.isEmpty()) || item.is(target)) freeSlots++;
        }
        return freeSlots;
    }
}
