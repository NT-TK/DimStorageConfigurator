package edivad.dimstorageconfigurator.setup;

import edivad.dimstorageconfigurator.DimStorageConfigurator;
import edivad.dimstorageconfigurator.items.DimConfigurator;
import edivad.dimstorageconfigurator.items.components.DimConfigComponent;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Registration {

  private static final DeferredRegister.Items ITEMS =
      DeferredRegister.createItems(DimStorageConfigurator.ID);
  public static final DeferredItem<DimConfigurator> DIM_CONFIGURATOR =
      ITEMS.registerItem("dimensional_configurator", properties ->
          new DimConfigurator(properties.stacksTo(1)));

  private static final DeferredRegister.DataComponents COMPONENTS =
      DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, DimStorageConfigurator.ID);
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<DimConfigComponent>> DIM_CONFIG =
      COMPONENTS.registerComponentType("dim_config", builder ->
          builder
              .persistent(DimConfigComponent.CODEC)
              .networkSynchronized(DimConfigComponent.STREAM_CODEC));

  private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
      DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DimStorageConfigurator.ID);
  public static final DeferredHolder<CreativeModeTab, CreativeModeTab> DIMSTORAGE_CONFIGURATOR_TAB =
      CREATIVE_MODE_TABS.register("tab", () -> CreativeModeTab.builder()
          .withTabsAfter(CreativeModeTabs.TOOLS_AND_UTILITIES)
          .title(Component.translatable("itemGroup.dimstorageconfigurator"))
          .icon(() -> new ItemStack(DIM_CONFIGURATOR.get()))
          .displayItems((parameters, output) -> output.accept(DIM_CONFIGURATOR.get()))
          .build());

  public static void init(IEventBus modEventBus) {
    ITEMS.register(modEventBus);
    COMPONENTS.register(modEventBus);
    CREATIVE_MODE_TABS.register(modEventBus);
  }
}
