package edivad.dimstorageconfigurator;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import edivad.dimstorageconfigurator.setup.Registration;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(DimStorageConfigurator.ID)
public class DimStorageConfigurator {

  public static final String ID = "dimstorageconfigurator";
  public static final String MODNAME = "DimStorage Configurator";

  public static final Logger LOGGER = LogUtils.getLogger();

  public DimStorageConfigurator(ModContainer modContainer) {
    var modEventBus = modContainer.getEventBus();
    Registration.init(modEventBus);
    modEventBus.addListener(this::addCreativeTabItems);
  }

  private void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
    if (event.getTabKey().location()
        .equals(ResourceLocation.fromNamespaceAndPath("dimstorage", "tab"))) {
      event.accept(Registration.DIM_CONFIGURATOR.get());
    }
  }
}
