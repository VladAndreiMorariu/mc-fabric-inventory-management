package me.roundaround.inventorymanagement.api;

import me.roundaround.inventorymanagement.inventory.InventoryHelper;
import me.roundaround.inventorymanagement.mixin.HandledScreenAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class ButtonContext<H extends ScreenHandler, S extends HandledScreen<H>> {
  private static final MinecraftClient MINECRAFT = MinecraftClient.getInstance();

  private final S parentScreen;
  private final HandledScreenAccessor accessor;
  private final boolean isPlayerInventory;

  private final H screenHandler;
  private final Slot referenceSlot;
  private final Inventory playerInventory;
  private final Inventory containerInventory;

  public ButtonContext(
      S parentScreen,
      HandledScreenAccessor accessor,
      H screenHandler,
      Slot referenceSlot,
      boolean isPlayerInventory,
      Inventory playerInventory,
      Inventory containerInventory) {
    this.parentScreen = parentScreen;
    this.accessor = accessor;
    this.screenHandler = screenHandler;
    this.referenceSlot = referenceSlot;
    this.isPlayerInventory = isPlayerInventory;
    this.playerInventory = playerInventory;
    this.containerInventory = containerInventory;
  }

  public ButtonContext(
      S parentScreen, boolean isPlayerInventory) {
    this.parentScreen = parentScreen;
    this.accessor = (HandledScreenAccessor) parentScreen;
    this.screenHandler = parentScreen.getScreenHandler();
    this.referenceSlot = PositioningFunction.getReferenceSlot(parentScreen, isPlayerInventory);
    this.isPlayerInventory = isPlayerInventory;

    ClientPlayerEntity player = MINECRAFT.player;
    if (player != null) {
      this.playerInventory = player.getInventory();
      this.containerInventory = InventoryHelper.getContainerInventory(player);
    } else {
      this.playerInventory = null;
      this.containerInventory = null;
    }
  }

  public boolean hasParentScreen() {
    return parentScreen != null;
  }

  public HandledScreen<?> getParentScreen() {
    return parentScreen;
  }

  public HandledScreenAccessor getAccessor() {
    return accessor;
  }

  public boolean isPlayerInventory() {
    return isPlayerInventory;
  }

  public boolean hasScreenHandler() {
    return screenHandler != null;
  }

  public ScreenHandler getScreenHandler() {
    return screenHandler;
  }

  public boolean hasReferenceSlot() {
    return referenceSlot != null;
  }

  public Slot getReferenceSlot() {
    return referenceSlot;
  }

  public boolean hasPlayerInventory() {
    return playerInventory != null;
  }

  public Inventory getPlayerInventory() {
    return playerInventory;
  }

  public boolean hasContainerInventory() {
    return containerInventory != null;
  }

  public Inventory getContainerInventory() {
    return containerInventory;
  }
}
