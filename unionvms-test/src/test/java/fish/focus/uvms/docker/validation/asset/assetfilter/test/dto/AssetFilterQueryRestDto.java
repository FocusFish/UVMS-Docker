package fish.focus.uvms.docker.validation.asset.assetfilter.test.dto;

import java.util.List;

public class AssetFilterQueryRestDto {
    private List<AssetFilterValueRestTestDto> values;
    private String type;
    private boolean inverse;
    private AssetFilterValueType valueType;

    public List<AssetFilterValueRestTestDto> getValues() {
        return values;
    }

    public void setValues(List<AssetFilterValueRestTestDto> values) {
        this.values = values;
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
}
