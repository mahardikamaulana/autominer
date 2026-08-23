package com.xeraphire.autominer;

import com.xeraphire.autominer.settings.AutoMinerConfig;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.localization.Localization;
import necesse.engine.registries.ContainerRegistry;
import necesse.engine.registries.GlobalIngredientRegistry;
import necesse.engine.util.GameRandom;
import necesse.entity.mobs.PlayerMob;
import necesse.entity.objectEntity.ObjectEntity;
import necesse.entity.particle.Particle;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawOptions.texture.TextureDrawOptionsEnd;
import necesse.gfx.drawables.LevelSortedDrawable;
import necesse.gfx.drawables.OrderableDrawables;
import necesse.gfx.gameTexture.GameTexture;
import necesse.inventory.InventoryItem;
import necesse.inventory.InventoryRange;
import necesse.inventory.container.object.CraftingStationContainer;
import necesse.inventory.item.toolItem.ToolType;
import necesse.inventory.recipe.Recipe;
import necesse.inventory.recipe.Tech;
import necesse.level.gameObject.GameObject;
import necesse.level.maps.Level;
import necesse.level.maps.levelData.settlementData.SettlementRequestOptions;
import necesse.level.maps.levelData.settlementData.SettlementWorkstationObject;
import necesse.level.maps.levelData.settlementData.storage.SettlementStorageGlobalIngredientIDIndex;
import necesse.level.maps.levelData.settlementData.storage.SettlementStorageRecords;
import necesse.level.maps.levelData.settlementData.storage.SettlementStorageRecordsRegionData;
import necesse.level.maps.light.GameLight;

public class AutoMinerObject extends GameObject implements SettlementWorkstationObject {
    public final AutoMinerDefinition definition;
    public final Tech tech;
    public GameTexture texture;
    public Recipe productionRecipe;

    public AutoMinerObject(AutoMinerDefinition definition, Tech tech) {
        super(new Rectangle(2, 6, 28, 20));
        this.definition = definition;
        this.tech = tech;
        this.toolType = ToolType.PICKAXE;
        this.isLightTransparent = false;
    }

    @Override
    public void loadTextures() {
        super.loadTextures();
        this.texture = GameTexture.fromFile(this.definition.texturePath);
    }

    @Override
    public void addDrawables(List<LevelSortedDrawable> list, OrderableDrawables tileList, Level level, int tileX, int tileY, TickManager tickManager, GameCamera camera, PlayerMob perspective) {
        GameLight light = level.getLightLevel(tileX, tileY);
        int drawX = camera.getTileDrawX(tileX);
        int drawY = camera.getTileDrawY(tileY);
        final TextureDrawOptionsEnd options = this.texture.initDraw().light(light).pos(drawX, drawY - this.texture.getHeight() + 32);
        list.add(new LevelSortedDrawable(this, tileX, tileY) {
            @Override
            public int getSortY() {
                return 16;
            }

            @Override
            public void draw(TickManager tickManager) {
                options.draw();
            }
        });
    }

    @Override
    public void tickEffect(Level level, int layerID, int x, int y) {
        super.tickEffect(level, layerID, x, y);
        if (GameRandom.globalRandom.nextInt(10) == 0) {
            AutoMinerObjectEntity entity = this.getAutoMinerObjectEntity(level, x, y);
            if (entity != null && entity.isFuelRunning()) {
                int startHeight = 16 + GameRandom.globalRandom.nextInt(16);
                level.entityManager.addParticle(
                        (float)(x * 32 + GameRandom.globalRandom.getIntBetween(8, 24)),
                        (float)(y * 32 + 32),
                        Particle.GType.COSMETIC
                ).smokeColor().heightMoves((float)startHeight, (float)(startHeight + 20)).lifeTime(1000);
            }
        }
    }

    @Override
    public void drawPreview(Level level, int tileX, int tileY, int rotation, float alpha, PlayerMob player, GameCamera camera) {
        int drawX = camera.getTileDrawX(tileX);
        int drawY = camera.getTileDrawY(tileY);
        this.texture.initDraw().alpha(alpha).draw(drawX, drawY - this.texture.getHeight() + 32);
    }

    @Override
    public ObjectEntity getNewObjectEntity(Level level, int x, int y) {
        return new AutoMinerObjectEntity(level, this.definition, x, y, this.tech);
    }

    @Override
    public String getInteractTip(Level level, int x, int y, PlayerMob perspective, boolean debug) {
        return Localization.translate("controls", "opentip");
    }

    @Override
    public boolean canInteract(Level level, int x, int y, PlayerMob player) {
        return true;
    }

    @Override
    public void interact(Level level, int x, int y, PlayerMob player) {
        if (level.isServer()) {
            CraftingStationContainer.openAndSendContainer(ContainerRegistry.FUELED_PROCESSING_STATION_CONTAINER, player.getServerClient(), level, x, y);
        }
    }

    public AutoMinerObjectEntity getAutoMinerObjectEntity(Level level, int tileX, int tileY) {
        ObjectEntity objectEntity = level.entityManager.getObjectEntity(tileX, tileY);
        if (objectEntity instanceof AutoMinerObjectEntity) {
            return (AutoMinerObjectEntity)objectEntity;
        }
        return null;
    }

    @Override
    public Stream<Recipe> streamSettlementRecipes(Level level, int tileX, int tileY) {
        return this.productionRecipe != null ? Stream.of(this.productionRecipe) : Stream.empty();
    }

    @Override
    public boolean isProcessingInventory(Level level, int tileX, int tileY) {
        return true;
    }

    @Override
    public boolean canCurrentlyCraft(Level level, int tileX, int tileY, Recipe recipe) {
        AutoMinerObjectEntity autoObj = this.getAutoMinerObjectEntity(level, tileX, tileY);
        if (autoObj != null) {
            return autoObj.getExpectedResults().crafts < 10 && (!AutoMinerConfig.isRequireFuel() || autoObj.isFuelRunning() || autoObj.canUseFuel());
        }
        return false;
    }

    private static final SettlementRequestOptions FUEL_REQUEST_OPTIONS = new SettlementRequestOptions(5, 50) {
        @Override
        public SettlementStorageRecordsRegionData getRequestStorageData(SettlementStorageRecords records) {
            return ((SettlementStorageGlobalIngredientIDIndex)records.getIndex(SettlementStorageGlobalIngredientIDIndex.class))
                    .getGlobalIngredient(GlobalIngredientRegistry.getGlobalIngredientID("anylog"));
        }
    };

    @Override
    public int getMaxCraftsAtOnce(Level level, int tileX, int tileY, Recipe recipe) {
        return 5;
    }

    @Override
    public InventoryRange getProcessingInputRange(Level level, int tileX, int tileY) {
        AutoMinerObjectEntity autoObj = this.getAutoMinerObjectEntity(level, tileX, tileY);
        if (autoObj != null) {
            return autoObj.getInputInventoryRange();
        }
        return null;
    }

    @Override
    public InventoryRange getProcessingOutputRange(Level level, int tileX, int tileY) {
        AutoMinerObjectEntity autoObj = this.getAutoMinerObjectEntity(level, tileX, tileY);
        if (autoObj != null) {
            return autoObj.getOutputInventoryRange();
        }
        return null;
    }

    @Override
    public ArrayList<InventoryItem> getCurrentAndFutureProcessingOutputs(Level level, int tileX, int tileY) {
        AutoMinerObjectEntity autoObj = this.getAutoMinerObjectEntity(level, tileX, tileY);
        if (autoObj != null) {
            return autoObj.getCurrentAndExpectedResults().items;
        }
        return new ArrayList<>(0);
    }

    @Override
    public SettlementRequestOptions getFuelRequestOptions(Level level, int tileX, int tileY) {
        if (!AutoMinerConfig.isRequireFuel()) {
            return null;
        }
        return FUEL_REQUEST_OPTIONS;
    }

    @Override
    public InventoryRange getFuelInventoryRange(Level level, int tileX, int tileY) {
        AutoMinerObjectEntity autoObj = this.getAutoMinerObjectEntity(level, tileX, tileY);
        if (autoObj != null && autoObj.getInventory() != null && autoObj.fuelSlots > 0) {
            return new InventoryRange(autoObj.getInventory(), 0, autoObj.fuelSlots - 1);
        }
        return null;
    }
}
