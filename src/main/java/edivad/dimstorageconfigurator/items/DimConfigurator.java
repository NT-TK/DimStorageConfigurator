package edivad.dimstorageconfigurator.items;

import java.util.List;
import java.util.Optional;
import edivad.dimstorage.api.Frequency;
import edivad.dimstorage.blockentities.BlockEntityDimTank;
import edivad.dimstorage.blockentities.BlockEntityFrequencyOwner;
import edivad.dimstorage.blocks.DimBlockBase;
import edivad.dimstorage.setup.Config;
import edivad.dimstorageconfigurator.items.components.DimConfigComponent;
import edivad.dimstorageconfigurator.setup.Registration;
import edivad.dimstorageconfigurator.tools.Translations;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Copies the settings of a DimChest or DimTank and applies them to another one.
 * <p>
 * Right-click a DimStorage block to copy its configuration, Shift + right-click a DimStorage
 * block to apply the stored configuration. Shift + right-click the air to clear it.
 */
public class DimConfigurator extends Item {

  private static final double CLEAR_RAYCAST_DISTANCE = 8.0D;

  public DimConfigurator(Properties properties) {
    super(properties);
  }

  @Override
  public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
    var level = context.getLevel();
    var player = context.getPlayer();
    var pos = context.getClickedPos();

    if (level.isClientSide || player == null) {
      return InteractionResult.PASS;
    }
    if (!(level.getBlockEntity(pos) instanceof BlockEntityFrequencyOwner block)) {
      return InteractionResult.PASS;
    }

    if (!Config.DimBlock.ALLOW_CONFIG.get()) {
      player.displayClientMessage(
          Component.translatable(Translations.CONFIG_DISABLED).withStyle(ChatFormatting.RED),
          true);
      return InteractionResult.FAIL;
    }

    if (player.isCrouching()) {
      return applyConfiguration(stack, player, level, pos, block);
    }
    return copyConfiguration(stack, player, block);
  }

  private InteractionResult copyConfiguration(ItemStack stack, Player player,
      BlockEntityFrequencyOwner block) {
    if (!block.canAccess(player)) {
      player.displayClientMessage(
          Component.translatable(Translations.CONFIG_ACCESS_DENIED).withStyle(ChatFormatting.RED),
          true);
      return InteractionResult.SUCCESS;
    }

    var frequency = block.getFrequency();
    if (!Config.DimBlock.ALLOW_PRIVATE_NETWORK.get()) {
      frequency = frequency.setPublic();
    }
    Optional<Boolean> autoEject = block instanceof BlockEntityDimTank tank
        ? Optional.of(tank.autoEject)
        : Optional.empty();

    stack.set(Registration.DIM_CONFIG,
        new DimConfigComponent(frequency, block.isLocked(), autoEject));
    player.displayClientMessage(
        Component.translatable(Translations.CONFIG_COPIED).withStyle(ChatFormatting.GREEN), true);
    return InteractionResult.SUCCESS;
  }

  private InteractionResult applyConfiguration(ItemStack stack, Player player, Level level,
      BlockPos pos, BlockEntityFrequencyOwner block) {
    var config = stack.get(Registration.DIM_CONFIG);
    if (config == null) {
      player.displayClientMessage(
          Component.translatable(Translations.CONFIG_EMPTY).withStyle(ChatFormatting.RED), true);
      return InteractionResult.SUCCESS;
    }

    if (!block.canAccess(player) || !config.frequency().canAccess(player)) {
      player.displayClientMessage(
          Component.translatable(Translations.CONFIG_ACCESS_DENIED).withStyle(ChatFormatting.RED),
          true);
      return InteractionResult.SUCCESS;
    }

    block.setFrequency(config.frequency());
    block.setLocked(config.locked());
    if (block instanceof BlockEntityDimTank tank) {
      config.autoEject().ifPresent(value -> {
        tank.autoEject = value;
        tank.setChanged();
      });
    }

    var state = level.getBlockState(pos);
    level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
    player.displayClientMessage(
        Component.translatable(Translations.CONFIG_APPLIED).withStyle(ChatFormatting.GREEN), true);
    return InteractionResult.SUCCESS;
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    var stack = player.getItemInHand(hand);
    if (player.isCrouching() && stack.has(Registration.DIM_CONFIG)) {
      // Do not clear the configuration when the player is aiming at a DimStorage block.
      // This avoids losing the copy when the block is just out of interaction range.
      if (isLookingAtDimStorage(player)) {
        return super.use(level, player, hand);
      }
      if (!level.isClientSide) {
        stack.remove(Registration.DIM_CONFIG);
        player.displayClientMessage(
            Component.translatable(Translations.CONFIG_CLEARED).withStyle(ChatFormatting.YELLOW),
            true);
      }
      return new InteractionResultHolder<>(InteractionResult.sidedSuccess(level.isClientSide()),
          stack);
    }
    return super.use(level, player, hand);
  }

  private static boolean isLookingAtDimStorage(Player player) {
    var hitResult = player.pick(CLEAR_RAYCAST_DISTANCE, 0.0F, false);
    if (hitResult.getType() != HitResult.Type.BLOCK) {
      return false;
    }
    var blockPos = ((BlockHitResult) hitResult).getBlockPos();
    return player.level().getBlockState(blockPos).getBlock() instanceof DimBlockBase;
  }

  @Override
  public boolean isFoil(ItemStack stack) {
    return stack.has(Registration.DIM_CONFIG);
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip,
      TooltipFlag tooltipFlag) {
    var config = stack.get(Registration.DIM_CONFIG);
    if (config == null) {
      tooltip.add(
          Component.translatable(Translations.CONFIG_COPY_HINT).withStyle(ChatFormatting.DARK_GRAY));
      return;
    }

    config.addToTooltip(tooltip::add);
    tooltip.add(
        Component.translatable(Translations.CONFIG_APPLY_HINT).withStyle(ChatFormatting.DARK_GRAY));
    tooltip.add(
        Component.translatable(Translations.CONFIG_CLEAR_HINT).withStyle(ChatFormatting.DARK_GRAY));
  }
}
