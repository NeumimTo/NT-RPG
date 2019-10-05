package cz.neumimto.rpg.api.configuration.adapters;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.conversion.Converter;
import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.core.conversion.Path;
import cz.neumimto.rpg.api.Rpg;
import cz.neumimto.rpg.api.effects.EffectParams;
import cz.neumimto.rpg.api.effects.IGlobalEffect;
import cz.neumimto.rpg.api.logging.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EffectsAdapter implements Converter<Map<IGlobalEffect, EffectParams>, List<Config>> {

    @Override
    public Map<IGlobalEffect, EffectParams> convertToField(List<Config> value) {
        Map<IGlobalEffect, EffectParams> params = new HashMap<>();

        for (Config config : value) {

            EffectConfigModel model = new ObjectConverter().toObject(config, EffectConfigModel::new);

            if (model.type == null) {
                Log.warn("Cannot read effects section - Missing node Id");
                continue;
            }
            IGlobalEffect globalEffect = Rpg.get().getEffectService().getGlobalEffect(model.type);
            if (globalEffect == null) {
                Log.error("Unknown Effect " + model.type);
                continue;
            }
            if (model.settings == null) {
                model.settings = new HashMap<>();
            }
            params.put(globalEffect, new EffectParams(model.settings));

        }


        return params;
    }

    @Override
    public List<Config> convertFromField(Map<IGlobalEffect, EffectParams> value) {
        List<EffectConfigModel> list = new ArrayList<>();
        for (Map.Entry<IGlobalEffect, EffectParams> entry : value.entrySet()) {
            EffectConfigModel model = new EffectConfigModel();
            model.settings = entry.getValue();
            model.type = entry.getKey().getName();
        }
        Config config = Config.inMemory();
        config.set("effects", list);
        return new ArrayList<>();
    }

    protected static class EffectConfigModel {

        @Path("Id")
        private String type;

        @Path("Settings")
        private Map<String, String> settings = new HashMap<>();
    }

}
