package dev.animecosmetics;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;
import dev.animecosmetics.fx.RiftFx;
import dev.animecosmetics.fx.SkyFx;
import dev.animecosmetics.look.AnimeLookLayer;
import dev.animecosmetics.look.CustomDress;
import dev.animecosmetics.look.CustomVeil;
import dev.animecosmetics.look.EyeFeathers;
import dev.animecosmetics.look.SkirtPhysics;
import dev.animecosmetics.look.WingPhysics;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.Disconnect;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping.Category;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

public final class AnimeCosmeticsClient implements ClientModInitializer {
   private static final Category CATEGORY = Category.register(AnimeCosmetics.id("main"));
   private static final int UNBOUND = InputConstants.UNKNOWN.getValue();
   private static final KeyMapping KEY_SKIN = new KeyMapping("key.animecosmetics.skin", Type.KEYBOARD, UNBOUND, CATEGORY);
   private static final KeyMapping KEY_WINGS = new KeyMapping("key.animecosmetics.wings", Type.KEYBOARD, UNBOUND, CATEGORY);
   private static final KeyMapping KEY_CAPE = new KeyMapping("key.animecosmetics.cape", Type.KEYBOARD, UNBOUND, CATEGORY);
   private static final KeyMapping KEY_FEATHERS = new KeyMapping("key.animecosmetics.feathers", Type.KEYBOARD, UNBOUND, CATEGORY);
   private static final KeyMapping KEY_SKY = new KeyMapping("key.animecosmetics.sky", Type.KEYBOARD, UNBOUND, CATEGORY);
   private static final KeyMapping KEY_SUN_EYE = new KeyMapping("key.animecosmetics.sun_eye", Type.KEYBOARD, UNBOUND, CATEGORY);
   private static final KeyMapping KEY_RIFT = new KeyMapping("key.animecosmetics.rift", Type.KEYBOARD, UNBOUND, CATEGORY);
   private static final KeyMapping KEY_RIFT_SKY = new KeyMapping("key.animecosmetics.rift_sky", Type.KEYBOARD, UNBOUND, CATEGORY);
   private static final long MESSAGE_MS = 2000L;
   private static Component message;
   private static long messageAt;

   public void onInitializeClient() {
      KeyMappingHelper.registerKeyMapping(KEY_SKIN);
      KeyMappingHelper.registerKeyMapping(KEY_WINGS);
      KeyMappingHelper.registerKeyMapping(KEY_CAPE);
      KeyMappingHelper.registerKeyMapping(KEY_FEATHERS);
      KeyMappingHelper.registerKeyMapping(KEY_SKY);
      KeyMappingHelper.registerKeyMapping(KEY_SUN_EYE);
      KeyMappingHelper.registerKeyMapping(KEY_RIFT);
      KeyMappingHelper.registerKeyMapping(KEY_RIFT_SKY);
      SkyFx.register();
      RiftFx.register();
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> {
         handle(client, KEY_SKIN, Toggles.Feature.SKIN);
         handle(client, KEY_WINGS, Toggles.Feature.WINGS);
         handle(client, KEY_CAPE, Toggles.Feature.CAPE);
         handle(client, KEY_FEATHERS, Toggles.Feature.FEATHERS);
         handle(client, KEY_SKY, Toggles.Feature.SKY);
         handle(client, KEY_SUN_EYE, Toggles.Feature.SUN_EYE);
         handle(client, KEY_RIFT, Toggles.Feature.RIFT);
         handle(client, KEY_RIFT_SKY, Toggles.Feature.RIFT_SKY);
         RiftFx.tick(client);
      });
      LivingEntityRenderLayerRegistrationCallback.EVENT.register((LivingEntityRenderLayerRegistrationCallback)(entityType, renderer, helper, context) -> {
         if (renderer instanceof AvatarRenderer<?> avatarRenderer) {
            helper.register(cast(new AnimeLookLayer(avatarRenderer)));
         }
      });
      ClientPlayConnectionEvents.DISCONNECT.register((Disconnect)(handler, client) -> {
         WingPhysics.clear();
         SkirtPhysics.clear();
         CustomDress.clear();
         CustomVeil.clear();
         EyeFeathers.clear();
         RiftFx.clear();
      });
      HudElementRegistry.addLast(AnimeCosmetics.id("toggle_message"), AnimeCosmeticsClient::renderMessage);
   }

   private static void handle(Minecraft client, KeyMapping key, Toggles.Feature feature) {
      while (key.consumeClick()) {
         if (client.player != null) {
            boolean on = Toggles.toggle(feature);
            message = Component.translatable("animecosmetics.feature." + feature.key)
               .append(": ")
               .append(Component.translatable(on ? "animecosmetics.state.on" : "animecosmetics.state.off"));
            messageAt = System.currentTimeMillis();
         }
      }
   }

   private static void renderMessage(GuiGraphicsExtractor g, DeltaTracker delta) {
      if (message != null && System.currentTimeMillis() - messageAt <= 2000L) {
         Minecraft mc = Minecraft.getInstance();
         g.centeredText(mc.font, message, g.guiWidth() / 2, g.guiHeight() - 68, ARGB.color(255, 255, 255, 255));
      }
   }

   private static <L> L cast(Object layer) {
      return (L)layer;
   }
}
