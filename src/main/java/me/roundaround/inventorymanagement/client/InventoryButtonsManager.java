package me.roundaround.inventorymanagement.client;

import me.roundaround.inventorymanagement.api.ButtonContext;
import me.roundaround.inventorymanagement.api.InventoryButtonsRegistry;
import me.roundaround.inventorymanagement.api.PositioningFunction;
import me.roundaround.inventorymanagement.client.gui.widget.button.AutoStackButton;
import me.roundaround.inventorymanagement.client.gui.widget.button.ButtonBase;
import me.roundaround.inventorymanagement.client.gui.widget.button.SortInventoryButton;
import me.roundaround.inventorymanagement.client.gui.widget.button.TransferAllButton;
import me.roundaround.inventorymanagement.config.InventoryManagementConfig;
import me.roundaround.inventorymanagement.config.value.ButtonVisibility;
import me.roundaround.roundalib.config.value.Position;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.HorseScreenHandler;
import net.minecraft.screen.ScreenHandler;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class InventoryButtonsManager {
  public static final InventoryButtonsManager INSTANCE = new InventoryButtonsManager();
  public static final int BUTTON_WIDTH = ButtonBase.WIDTH;
  public static final int BUTTON_HEIGHT = ButtonBase.HEIGHT;
  public static final int BUTTON_SPACING = 1;

  private static final int BUTTON_SHIFT_X = 0;
  private static final int BUTTON_SHIFT_Y = 1;

  private final LinkedHashSet<ButtonBase<?, ?>> playerButtons = new LinkedHashSet<>();
  private final LinkedHashSet<ButtonBase<?, ?>> containerButtons = new LinkedHashSet<>();

  public void init() {
    ScreenEvents.AFTER_INIT.register(this::onScreenAfterInit);
  }

  private void onScreenAfterInit(
      MinecraftClient client, Screen screen, float scaledWidth, float scaledHeight
  ) {
    if (!(screen instanceof HandledScreen<?> handledScreen)) {
      return;
    }

    // Container side
    this.containerButtons.clear();
    ButtonContext<?, ?> containerContext = new ButtonContext<>(handledScreen, false);
    generateSortButton(containerContext);
    generateAutoStackButton(containerContext);
    generateTransferAllButton(containerContext);

    // Player side
    this.playerButtons.clear();
    ButtonContext<?, ?> playerContext = new ButtonContext<>(handledScreen, true);
    generateSortButton(playerContext);
    generateAutoStackButton(playerContext);
    generateTransferAllButton(playerContext);
  }

  private boolean shouldTryGeneratingSortButton(ButtonContext<?, ?> context) {
    if (!InventoryManagementConfig.getInstance().modEnabled.getValue()) {
      return false;
    }

    if (getNumberOfBulkInventorySlots(context) < 3) {
      return false;
    }

    if (!InventoryManagementConfig.getInstance().showSort.getValue()) {
      return false;
    }

    LinkedList<ButtonVisibility> visibilitySettings = new LinkedList<>();

    // Per screen config
    Optional.ofNullable(
        InventoryManagementConfig.getInstance().perScreenConfigs.getSortVisibility(context.getParentScreen(),
            context.isPlayerInventory()
        )).ifPresent(visibilitySettings::add);

    // Screen class match
    Optional.ofNullable(InventoryButtonsRegistry.HANDLED_SCREENS.get(context.getParentScreen().getClass()))
        .map((options) -> options.getSortVisibility(context.isPlayerInventory()))
        .ifPresent(visibilitySettings::add);

    // ScreenHandler class match
    Optional.ofNullable(InventoryButtonsRegistry.SCREEN_HANDLERS.get(context.getScreenHandler().getClass()))
        .map((options) -> options.getSortVisibility(context.isPlayerInventory()))
        .ifPresent(visibilitySettings::add);

    // Inventory class match
    Inventory inventory = context.isPlayerInventory() ? context.getPlayerInventory() : context.getContainerInventory();
    if (inventory != null) {
      Optional.ofNullable(InventoryButtonsRegistry.INVENTORIES.get(inventory.getClass()))
          .map((options) -> options.getSortVisibility(context.isPlayerInventory()))
          .ifPresent(visibilitySettings::add);
    }

    for (ButtonVisibility visibility : visibilitySettings) {
      if (ButtonVisibility.SHOW.equals(visibility)) {
        return true;
      } else if (ButtonVisibility.HIDE.equals(visibility)) {
        return false;
      }
    }

    return false;
  }

  private <H extends ScreenHandler, S extends HandledScreen<H>> void generateSortButton(
      ButtonContext<H, S> context
  ) {
    if (!this.shouldTryGeneratingSortButton(context)) {
      return;
    }

    Position offset = getButtonOffset(context);
    PositioningFunction<H, S> positioningFunction = getPositioningFunction(context);
    SortInventoryButton<H, S> button = new SortInventoryButton<>(offset, positioningFunction, context);
    addButton(context.getParentScreen(), button, context.isPlayerInventory());
  }

  private boolean shouldTryGeneratingStackButton(ButtonContext<?, ?> context) {
    if (!InventoryManagementConfig.getInstance().modEnabled.getValue()) {
      return false;
    }

    if (getNumberOfBulkInventorySlots(context) < 3) {
      return false;
    }

    if (!context.hasPlayerInventory() || !context.hasContainerInventory() ||
        context.getPlayerInventory() == context.getContainerInventory()) {
      return false;
    }

    if (!InventoryManagementConfig.getInstance().showStack.getValue()) {
      return false;
    }

    LinkedList<ButtonVisibility> visibilitySettings = new LinkedList<>();

    // Per screen config
    Optional.ofNullable(
        InventoryManagementConfig.getInstance().perScreenConfigs.getStackVisibility(context.getParentScreen(),
            context.isPlayerInventory()
        )).ifPresent(visibilitySettings::add);

    // Screen class match
    Optional.ofNullable(InventoryButtonsRegistry.HANDLED_SCREENS.get(context.getParentScreen().getClass()))
        .map((options) -> options.getStackVisibility(context.isPlayerInventory()))
        .ifPresent(visibilitySettings::add);

    // ScreenHandler class match
    Optional.ofNullable(InventoryButtonsRegistry.SCREEN_HANDLERS.get(context.getScreenHandler().getClass()))
        .map((options) -> options.getStackVisibility(context.isPlayerInventory()))
        .ifPresent(visibilitySettings::add);

    // Inventory class match
    Inventory inventory = context.isPlayerInventory() ? context.getPlayerInventory() : context.getContainerInventory();
    if (inventory != null) {
      Optional.ofNullable(InventoryButtonsRegistry.INVENTORIES.get(inventory.getClass()))
          .map((options) -> options.getStackVisibility(context.isPlayerInventory()))
          .ifPresent(visibilitySettings::add);
    }

    for (ButtonVisibility visibility : visibilitySettings) {
      if (ButtonVisibility.SHOW.equals(visibility)) {
        return true;
      } else if (ButtonVisibility.HIDE.equals(visibility)) {
        return false;
      }
    }

    return false;
  }

  private <H extends ScreenHandler, S extends HandledScreen<H>> void generateAutoStackButton(
      ButtonContext<H, S> context
  ) {
    if (!this.shouldTryGeneratingStackButton(context)) {
      return;
    }

    Position offset = getButtonOffset(context);
    PositioningFunction<H, S> positioningFunction = getPositioningFunction(context);
    AutoStackButton<H, S> button = new AutoStackButton<>(offset, positioningFunction, context);
    addButton(context.getParentScreen(), button, context.isPlayerInventory());
  }

  private boolean shouldTryGeneratingTransferButton(ButtonContext<?, ?> context) {
    if (!InventoryManagementConfig.getInstance().modEnabled.getValue()) {
      return false;
    }

    if (getNumberOfBulkInventorySlots(context) < 3) {
      return false;
    }

    if (!context.hasPlayerInventory() || !context.hasContainerInventory() ||
        context.getPlayerInventory() == context.getContainerInventory()) {
      return false;
    }

    if (!InventoryManagementConfig.getInstance().showTransfer.getValue()) {
      return false;
    }

    LinkedList<ButtonVisibility> visibilitySettings = new LinkedList<>();

    // Per screen config
    Optional.ofNullable(
        InventoryManagementConfig.getInstance().perScreenConfigs.getTransferVisibility(context.getParentScreen(),
            context.isPlayerInventory()
        )).ifPresent(visibilitySettings::add);

    // Screen class match
    Optional.ofNullable(InventoryButtonsRegistry.HANDLED_SCREENS.get(context.getParentScreen().getClass()))
        .map((options) -> options.getTransferVisibility(context.isPlayerInventory()))
        .ifPresent(visibilitySettings::add);

    // ScreenHandler class match
    Optional.ofNullable(InventoryButtonsRegistry.SCREEN_HANDLERS.get(context.getScreenHandler().getClass()))
        .map((options) -> options.getTransferVisibility(context.isPlayerInventory()))
        .ifPresent(visibilitySettings::add);

    // Inventory class match
    Inventory inventory = context.isPlayerInventory() ? context.getPlayerInventory() : context.getContainerInventory();
    if (inventory != null) {
      Optional.ofNullable(InventoryButtonsRegistry.INVENTORIES.get(inventory.getClass()))
          .map((options) -> options.getTransferVisibility(context.isPlayerInventory()))
          .ifPresent(visibilitySettings::add);
    }

    for (ButtonVisibility visibility : visibilitySettings) {
      if (ButtonVisibility.SHOW.equals(visibility)) {
        return true;
      } else if (ButtonVisibility.HIDE.equals(visibility)) {
        return false;
      }
    }

    return false;
  }

  private <H extends ScreenHandler, S extends HandledScreen<H>> void generateTransferAllButton(
      ButtonContext<H, S> context
  ) {
    if (!this.shouldTryGeneratingTransferButton(context)) {
      return;
    }

    Position offset = getButtonOffset(context);
    PositioningFunction<H, S> positioningFunction = getPositioningFunction(context);
    TransferAllButton<H, S> button = new TransferAllButton<>(offset, positioningFunction, context);
    addButton(context.getParentScreen(), button, context.isPlayerInventory());
  }

  private void addButton(
      HandledScreen<?> screen, ButtonBase<?, ?> button, boolean isPlayerInventory
  ) {
    Screens.getButtons(screen).add(button);
    (isPlayerInventory ? playerButtons : containerButtons).add(button);
  }

  private int getNumberOfBulkInventorySlots(ButtonContext<?, ?> context) {
    return context.getScreenHandler().slots.stream()
        .filter(slot -> context.isPlayerInventory() == (slot.inventory instanceof PlayerInventory))
        .filter(slot -> !(context.getScreenHandler() instanceof HorseScreenHandler) || slot.getIndex() >= 2)
        .mapToInt(slot -> 1)
        .sum();
  }

  private int getNumberOfNonPlayerBulkInventorySlots(ButtonContext<?, ?> context) {
    return context.getScreenHandler().slots.stream()
        .filter(slot -> !(slot.inventory instanceof PlayerInventory))
        .filter(slot -> !(context.getScreenHandler() instanceof HorseScreenHandler) || slot.getIndex() >= 2)
        .mapToInt(slot -> 1)
        .sum();
  }

  private Position getButtonOffset(ButtonContext<?, ?> context) {
    Position offset = InventoryManagementConfig.getInstance().perScreenConfigs.getPosition(context.getParentScreen(),
        context.isPlayerInventory()
    );
    if (offset == null) {
      offset = InventoryManagementConfig.getInstance().defaultPosition.getValue();
    }

    return getButtonOffset((context.isPlayerInventory() ? playerButtons : containerButtons).size(), offset);
  }

  @SuppressWarnings("unchecked")
  private <H extends ScreenHandler, S extends HandledScreen<H>> PositioningFunction<H, S> getPositioningFunction(
      ButtonContext<H, S> context
  ) {
    PositioningFunction<?, ?> positioningFunction = Optional.ofNullable(
            InventoryButtonsRegistry.SCREEN_HANDLERS.get(context.getScreenHandler().getClass()))
        .map(InventoryButtonsRegistry.DefaultOptions::getPositioningFunction)
        .orElse(null);

    if (positioningFunction == null) {
      return PositioningFunction.getDefault();
    }

    // This is safe because the positioning function is registered with the correct types
    return (PositioningFunction<H, S>) positioningFunction;
  }

  public Position getButtonOffset(int index, Position offset) {
    int x = offset.x() + BUTTON_SHIFT_X * (ButtonBase.WIDTH + BUTTON_SPACING) * index;
    int y = offset.y() + BUTTON_SHIFT_Y * (ButtonBase.HEIGHT + BUTTON_SPACING) * index;

    return new Position(x, y);
  }

  public LinkedList<ButtonBase<?, ?>> getPlayerButtons() {
    return new LinkedList<>(playerButtons);
  }

  public LinkedList<ButtonBase<?, ?>> getContainerButtons() {
    return new LinkedList<>(containerButtons);
  }
}
