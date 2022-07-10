package me.roundaround.inventorymanagement.client.gui;

import me.roundaround.inventorymanagement.mixin.HandledScreenAccessor;
import me.roundaround.inventorymanagement.network.AutoStackPacket;
import me.roundaround.roundalib.config.value.Position;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

public class AutoStackButton extends InventoryManagementButton {
  private final boolean fromPlayerInventory;

  public AutoStackButton(
      HandledScreen<?> parent,
      Slot referenceSlot,
      Position offset,
      boolean fromPlayerInventory) {
    super(
        parent,
        (HandledScreenAccessor) parent,
        referenceSlot,
        offset,
        new Position(fromPlayerInventory ? 2 : 1, 0),
        (button) -> {
          AutoStackPacket.sendToServer(fromPlayerInventory);
        });
    this.fromPlayerInventory = fromPlayerInventory;
  }

  @Override
  protected Text getTooltip() {
    String key = fromPlayerInventory
        ? "inventorymanagement.button.autostack_into"
        : "inventorymanagement.button.autostack_from";
    return Text.translatable(key);
  }
}
