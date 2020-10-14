package cz.neumimto.rpg.sponge.inventory.data;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;

import java.util.Optional;

public class AttributeRefMenuData extends AbstractSingleData<String, AttributeRefMenuData, AttributeRefMenuData.Immutable> {

    public AttributeRefMenuData(String value) {
        super(value, NKeys.MENU_COMMAND);
    }

    @Override
    public Optional<AttributeRefMenuData> fill(DataHolder dataHolder, MergeFunction overlap) {
        Optional<AttributeRefMenuData> otherData_ = dataHolder.get(AttributeRefMenuData.class);
        if (otherData_.isPresent()) {
            AttributeRefMenuData otherData = otherData_.get();
            AttributeRefMenuData finalData = overlap.merge(this, otherData);
            finalData.setValue(otherData.getValue());
        }
        return Optional.of(this);
    }

    @Override
    public Optional<AttributeRefMenuData> from(DataContainer container) {
        return from((DataView) container);
    }

    public Optional<AttributeRefMenuData> from(DataView view) {
        if (view.contains(NKeys.MENU_COMMAND.getQuery())) {
            setValue(view.getString(NKeys.MENU_COMMAND.getQuery()).get());
            return Optional.of(this);
        }
        return Optional.empty();

    }

    @Override
    public AttributeRefMenuData copy() {
        return new AttributeRefMenuData(getValue());
    }

    @Override
    protected Value<?> getValueGetter() {
        return Sponge.getRegistry().getValueFactory().createValue(NKeys.MENU_COMMAND, getValue());
    }

    @Override
    public Immutable asImmutable() {
        return new AttributeRefMenuData.Immutable(getValue());
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(NKeys.MENU_COMMAND.getQuery(), getValue());
    }

    public static class Immutable extends AbstractImmutableSingleData<String, Immutable, AttributeRefMenuData> {


        public Immutable(String value) {
            super(value, NKeys.MENU_COMMAND);
        }

        @Override
        protected ImmutableValue<?> getValueGetter() {
            return Sponge.getRegistry().getValueFactory().createValue(NKeys.MENU_COMMAND, getValue()).asImmutable();
        }

        @Override
        public AttributeRefMenuData asMutable() {
            return new AttributeRefMenuData(getValue());
        }

        @Override
        public int getContentVersion() {
            return 1;
        }

        @Override
        public DataContainer toContainer() {
            return super.toContainer().set(NKeys.MENU_COMMAND.getQuery(), getValue());
        }
    }

    public static class Builder extends AbstractDataBuilder<AttributeRefMenuData>
            implements DataManipulatorBuilder<AttributeRefMenuData, Immutable> {

        public Builder() {
            super(AttributeRefMenuData.class, 1);
        }

        @Override
        public AttributeRefMenuData create() {
            return new AttributeRefMenuData("");
        }

        @Override
        public Optional<AttributeRefMenuData> createFrom(DataHolder dataHolder) {
            return create().fill(dataHolder);
        }

        @Override
        protected Optional<AttributeRefMenuData> buildContent(DataView container) throws InvalidDataException {
            return create().from(container);
        }
    }
}
