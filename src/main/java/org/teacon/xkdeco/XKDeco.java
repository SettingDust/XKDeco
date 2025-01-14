package org.teacon.xkdeco;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.teacon.xkdeco.client.XKDecoClient;
import org.teacon.xkdeco.data.XKDecoBlockStateProvider;
import org.teacon.xkdeco.data.XKDecoEnUsLangProvider;
import org.teacon.xkdeco.entity.CushionEntity;
import org.teacon.xkdeco.init.XKDecoObjects;
import org.teacon.xkdeco.item.XKDecoCreativeModTab;

import javax.annotation.ParametersAreNonnullByDefault;

@Mod(XKDeco.ID)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class XKDeco {
    public static final String ID = "xkdeco";

    public XKDeco() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        XKDecoObjects.ENTITIES.register(modEventBus);
        XKDecoObjects.BLOCKS.register(modEventBus);
        XKDecoObjects.ITEMS.register(modEventBus);
        XKDecoObjects.BLOCK_ENTITY.register(modEventBus);
        XKDecoCreativeModTab.TABS.register(modEventBus);

        modEventBus.addListener(EventPriority.LOWEST, XKDecoObjects::addSpecialWallBlocks);
        modEventBus.addListener(EventPriority.LOWEST, XKDecoObjects::addSpecialWallItems);
        modEventBus.addListener(EventPriority.LOWEST, XKDecoObjects::addSpecialWallBlockEntity);

        modEventBus.addListener(XKDecoBlockStateProvider::register);
        modEventBus.addListener(XKDecoEnUsLangProvider::register);

        if (FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(XKDecoClient::setItemColors);
            modEventBus.addListener(XKDecoClient::setBlockColors);
            modEventBus.addListener(XKDecoClient::setItemRenderers);
            modEventBus.addListener(XKDecoClient::setEntityRenderers);
            modEventBus.addListener(XKDecoClient::setAdditionalPackFinder);
        }

        var forgeEventBus = MinecraftForge.EVENT_BUS;

        forgeEventBus.addListener(XKDecoObjects::addSpecialWallTags);

        forgeEventBus.addListener(CushionEntity::onRightClickBlock);
        forgeEventBus.addListener(CushionEntity::onBreakBlock);

        if (FMLEnvironment.dist.isClient()) {
            forgeEventBus.addListener(XKDecoClient::addDebugText);
        }
    }
}
