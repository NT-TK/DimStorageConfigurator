package edivad.dimstorageconfigurator.items.components;

import java.util.Optional;
import java.util.function.Consumer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import edivad.dimstorage.api.Frequency;
import edivad.dimstorageconfigurator.tools.Translations;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * A snapshot of the settings of a DimChest or DimTank.
 *
 * @param frequency the frequency of the block
 * @param locked whether the block's automation is locked
 * @param autoEject the auto-eject setting, only present for DimTanks
 */
public record DimConfigComponent(Frequency frequency, boolean locked,
                                 Optional<Boolean> autoEject) {

  public static final Codec<DimConfigComponent> CODEC =
      RecordCodecBuilder.create(instance -> instance.group(
          Frequency.CODEC.fieldOf("frequency").forGetter(DimConfigComponent::frequency),
          Codec.BOOL.fieldOf("locked").forGetter(DimConfigComponent::locked),
          Codec.BOOL.optionalFieldOf("auto_eject").forGetter(DimConfigComponent::autoEject)
      ).apply(instance, DimConfigComponent::new));

  public static final StreamCodec<RegistryFriendlyByteBuf, DimConfigComponent> STREAM_CODEC =
      StreamCodec.composite(
          Frequency.STREAM_CODEC, DimConfigComponent::frequency,
          ByteBufCodecs.BOOL, DimConfigComponent::locked,
          ByteBufCodecs.optional(ByteBufCodecs.BOOL), DimConfigComponent::autoEject,
          DimConfigComponent::new);

  public void addToTooltip(Consumer<Component> consumer) {
    consumer.accept(Component.translatable(Translations.CONFIG_STORED)
        .withStyle(ChatFormatting.GOLD));
    consumer.accept(Component.translatable(Translations.FREQUENCY)
        .append(" " + this.frequency.channel())
        .withStyle(ChatFormatting.GRAY));
    if (this.frequency.hasOwner()) {
      consumer.accept(Component.translatable(Translations.OWNER)
          .append(" " + this.frequency.getOwner())
          .withStyle(ChatFormatting.GRAY));
    }
    consumer.accept(Component.translatable(Translations.LOCKED)
        .append(" ")
        .append(Component.translatable(this.locked ? Translations.YES : Translations.NO))
        .withStyle(ChatFormatting.GRAY));
    this.autoEject.ifPresent(value -> consumer.accept(Component.translatable(Translations.AUTO_EJECT)
        .append(" ")
        .append(Component.translatable(value ? Translations.YES : Translations.NO))
        .withStyle(ChatFormatting.GRAY)));
  }
}
