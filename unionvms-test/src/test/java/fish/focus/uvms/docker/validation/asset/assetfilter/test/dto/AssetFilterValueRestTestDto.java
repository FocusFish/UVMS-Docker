package fish.focus.uvms.docker.validation.asset.assetfilter.test.dto;

public class AssetFilterValueRestTestDto {
    private Double value;
    private String operator;

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }
}
