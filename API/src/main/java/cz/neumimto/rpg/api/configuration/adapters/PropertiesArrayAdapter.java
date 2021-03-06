package cz.neumimto.rpg.api.configuration.adapters;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.conversion.Converter;
import cz.neumimto.rpg.api.Rpg;
import cz.neumimto.rpg.api.entity.PropertyService;
import cz.neumimto.rpg.api.logging.Log;

import java.util.Map;

/**
 * Created by NeumimTo on 5.1.2019.
 */
public class PropertiesArrayAdapter implements Converter<float[], Config> {

    @Override
    public float[] convertToField(Config value) {
        int lastId = Rpg.get().getPropertyService().getLastId();
        float[] arr = new float[lastId];
        if (value == null) {
            return arr;
        }

        PropertyService propertyService = Rpg.get().getPropertyService();
        for (Map.Entry<String, Object> objectEntry : value.valueMap().entrySet()) {
            String propertyName = objectEntry.getKey().toLowerCase();
            float f = ((Number) objectEntry.getValue()).floatValue();

            if (propertyService.exists(propertyName)) {
                int idByName = propertyService.getIdByName(propertyName);
                arr[idByName] = f;
            } else {
                Log.error("Unknown property " + propertyName);
            }
        }

        return arr;
    }

    @Override
    public Config convertFromField(float[] floats) {
        Config config = Config.inMemory();
        PropertyService service = Rpg.get().getPropertyService();

        for (int i = 0; i < floats.length; i++) {
            String propertyName = service.getNameById(i);
            if (floats[i] != service.getDefault(i)) {
                config.add(propertyName, floats[i]);
            }
        }

        return Config.inMemory();
    }
}
