package me.roundaround.inventorymanagement.client.gui.widget.button;

import me.roundaround.inventorymanagement.InventoryManagementMod;
import me.roundaround.inventorymanagement.api.ButtonContext;
import me.roundaround.inventorymanagement.api.PositioningFunction;
import me.roundaround.inventorymanagement.client.network.ClientNetworking;
import me.roundaround.roundalib.config.value.Position;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class AutoStackButton<H extends ScreenHandler, S extends HandledScreen<H>> extends ButtonBase<H, S> {
  private static final Identifier ICON_TO = new Identifier(InventoryManagementMod.MOD_ID, "icon/stack_to");
  private static final Identifier ICON_FROM = new Identifier(InventoryManagementMod.MOD_ID, "icon/stack_from");

  public AutoStackButton(
      Position offset, PositioningFunction<H, S> positioningFunction, ButtonContext<H, S> context
  ) {
    super(positioningFunction.apply(context), offset, positioningFunction, context,
        getAction(context.isPlayerInventory()), getTooltip(context.isPlayerInventory()),
        getIcon(context.isPlayerInventory())
    );
  }

  private static Text getTooltip(boolean isPlayerInventory) {
    String key = isPlayerInventory ?
        "inventorymanagement.button.autostack_into" :
        "inventorymanagement.button.autostack_from";
    return Text.translatable(key);
  }

  private static PressAction getAction(boolean isPlayerInventory) {
    return isPlayerInventory ?
        (button) -> ClientNetworking.sendStackIntoContainerPacket() :
        (button) -> ClientNetworking.sendStackFromContainerPacket();
  }

  private static Identifier getIcon(boolean isPlayerInventory) {
    return isPlayerInventory ? ICON_TO : ICON_FROM;
  }
}
