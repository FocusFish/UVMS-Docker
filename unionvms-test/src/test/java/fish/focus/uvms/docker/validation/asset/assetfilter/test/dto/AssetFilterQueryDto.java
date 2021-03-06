package fish.focus.uvms.docker.validation.asset.assetfilter.test.dto;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

public class AssetFilterQueryDto implements Serializable {

    private static final long serialVersionUID = 4014705294222745778L;

    private UUID id;
    private String type;
    private boolean inverse;
    private AssetFilterValueType valueType;
    private Set<AssetFilterValueDto> values;
    private AssetFilterDto assetFilter;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean getInverse() {
        return inverse;
    }

    public void setInverse(boolean inverse) {
        this.inverse = inverse;
    }

    public AssetFilterValueType getValueType() {
        return valueType;
    }

    public void setValueType(AssetFilterValueType valueType) {
        this.valueType = valueType;
    }

    public Set<AssetFilterValueDto> getValues() {
        return values;
    }

    public void setValues(Set<AssetFilterValueDto> values) {
        this.values = values;
    }

    public AssetFilterDto getAssetFilter() {
        return assetFilter;
    }

    public void setAssetFilter(AssetFilterDto assetFilter) {
        this.assetFilter = assetFilter;
    }
}
