package cz.neumimto.rpg.players;

import org.spongepowered.api.CatalogType;
import org.spongepowered.api.util.annotation.CatalogedBy;

/**
 * Created by NeumimTo on 16.10.2015.
 */
@CatalogedBy(ExperienceSources.class)
public class ExperienceSource implements CatalogType {

	private final String id;

	public ExperienceSource(String id) {
		this.id = id;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return id;
	}
}
