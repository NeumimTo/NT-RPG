package cz.neumimto.rpg.sponge;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import cz.neumimto.rpg.api.ResourceLoader;
import cz.neumimto.rpg.api.damage.DamageService;
import cz.neumimto.rpg.api.effects.EffectService;
import cz.neumimto.rpg.api.entity.EntityService;
import cz.neumimto.rpg.api.entity.players.CharacterService;
import cz.neumimto.rpg.api.entity.players.IActiveCharacter;
import cz.neumimto.rpg.api.entity.players.parties.PartyService;
import cz.neumimto.rpg.api.events.EventFactoryService;
import cz.neumimto.rpg.api.exp.ExperienceService;
import cz.neumimto.rpg.api.gui.IPlayerMessage;
import cz.neumimto.rpg.api.inventory.CharacterInventoryInteractionHandler;
import cz.neumimto.rpg.api.inventory.InventoryService;
import cz.neumimto.rpg.api.items.ItemService;
import cz.neumimto.rpg.api.permissions.PermissionService;
import cz.neumimto.rpg.api.skills.SkillService;
import cz.neumimto.rpg.common.AbstractRpgGuiceModule;
import cz.neumimto.rpg.common.ResourceManagerImpl;
import cz.neumimto.rpg.common.assets.AssetService;
import cz.neumimto.rpg.common.bytecode.ClassGenerator;
import cz.neumimto.rpg.common.entity.configuration.MobSettingsDao;
import cz.neumimto.rpg.common.inventory.InventoryHandler;
import cz.neumimto.rpg.common.inventory.crafting.runewords.RWDao;
import cz.neumimto.rpg.common.skills.scripting.CustomSkillGenerator;
import cz.neumimto.rpg.sponge.assets.SpongeAssetService;
import cz.neumimto.rpg.sponge.commands.SpongeCommandService;
import cz.neumimto.rpg.sponge.damage.SpongeDamageService;
import cz.neumimto.rpg.sponge.effects.SpongeEffectService;
import cz.neumimto.rpg.sponge.entities.SpongeEntityService;
import cz.neumimto.rpg.sponge.entities.configuration.SpongeMobSettingsDao;
import cz.neumimto.rpg.sponge.entities.players.SpongeCharacterService;
import cz.neumimto.rpg.sponge.entities.players.party.SpongePartyService;
import cz.neumimto.rpg.sponge.events.SpongeEventFactory;
import cz.neumimto.rpg.sponge.exp.SpongeExperienceService;
import cz.neumimto.rpg.sponge.gui.ItemLoreBuilderService;
import cz.neumimto.rpg.sponge.gui.ParticleDecorator;
import cz.neumimto.rpg.sponge.gui.VanillaMessaging;
import cz.neumimto.rpg.sponge.inventory.SpongeInventoryService;
import cz.neumimto.rpg.sponge.inventory.SpongeItemService;
import cz.neumimto.rpg.sponge.inventory.runewords.RWService;
import cz.neumimto.rpg.sponge.permission.SpongePermissionService;
import cz.neumimto.rpg.sponge.scripting.SpongeClassGenerator;
import cz.neumimto.rpg.sponge.scripting.SpongeCustomSkillGenerator;
import cz.neumimto.rpg.sponge.skills.SpongeSkillService;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.CauseStackManager;

import java.util.Map;

public class SpongeGuiceModule extends AbstractRpgGuiceModule {

    private final SpongeRpgPlugin ntRpgPlugin;
    private final Logger logger;
    private final Game game;
    private CauseStackManager causeStackManager;
    private Map extraBindings;

    public SpongeGuiceModule(SpongeRpgPlugin ntRpgPlugin, Logger logger, Game game, CauseStackManager causeStackManager, Map extraBindings) {
        this.ntRpgPlugin = ntRpgPlugin;
        this.logger = logger;
        this.game = game;
        this.causeStackManager = causeStackManager;
        this.extraBindings = extraBindings;
    }

    @Override
    protected Map getBindings() {
        Map map = super.getBindings();
        map.put(SkillService.class, SpongeSkillService.class);
        map.put(PartyService.class, SpongePartyService.class);
        map.put(IPlayerMessage.class, VanillaMessaging.class);
        map.put(ClassGenerator.class, SpongeClassGenerator.class);
        map.put(DamageService.class, SpongeDamageService.class);
        map.put(EffectService.class, SpongeEffectService.class);
        map.put(EntityService.class, SpongeEntityService.class);
        map.put(MobSettingsDao.class, SpongeMobSettingsDao.class);
        map.put(ExperienceService.class, SpongeExperienceService.class);
        map.put(ItemService.class, SpongeItemService.class);
        map.put(InventoryService.class, SpongeInventoryService.class);
        map.put(AssetService.class, SpongeAssetService.class);
        map.put(PermissionService.class, SpongePermissionService.class);
        map.put(EventFactoryService.class, SpongeEventFactory.class);
        map.put(CharacterInventoryInteractionHandler.class, InventoryHandler.class);
        map.put(ResourceLoader.class, ResourceManagerImpl.class);
        map.put(SpongeCommandService.class, null);
        map.put(ParticleDecorator.class, null);
        map.put(ItemLoreBuilderService.class, null);
        map.put(RWDao.class, null);
        map.put(RWService.class, null);
        //map.put(ICharacterClassDao.class).to(JPACharacterClassDao.class);
        //map.put(IPlayerDao.class).to(JPAPlayerDao.class);
        map.put(CustomSkillGenerator.class, SpongeCustomSkillGenerator.class);
        map.putAll(extraBindings);
        return map;
    }

    @Override
    protected void configure() {
        super.configure();

        bind(new TypeLiteral<CharacterService>() {
        }).toProvider(SpongeCharacterServiceProvider.class);
        bind(new TypeLiteral<CharacterService<? extends IActiveCharacter>>() {
        }).to(SpongeCharacterService.class);
        bind(new TypeLiteral<CharacterService<? super IActiveCharacter>>() {
        }).toProvider(SpongeCharacterServiceProvider1.class);

        bind(Game.class).toProvider(() -> game);
        bind(SpongeRpgPlugin.class).toProvider(() -> ntRpgPlugin);
        bind(Logger.class).toProvider(() -> logger);
        bind(CauseStackManager.class).toProvider(() -> causeStackManager);
    }


    private static SpongeCharacterService scs;

    public static class SpongeCharacterServiceProvider implements Provider<CharacterService<IActiveCharacter>> {

        @Inject
        private Injector injector;

        @Override
        public CharacterService get() {
            if (scs == null) {
                scs = injector.getInstance(SpongeCharacterService.class);
            }
            return scs;
        }
    }

    public static class SpongeCharacterServiceProvider1 implements Provider<CharacterService<? super IActiveCharacter>> {

        @Inject
        private Injector injector;

        @Override
        public CharacterService<? super IActiveCharacter> get() {
            if (scs == null) {
                scs = injector.getInstance(SpongeCharacterService.class);
            }
            return (CharacterService) scs;
        }
    }
}
