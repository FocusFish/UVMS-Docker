package fish.focus.uvms.docker.validation.asset.assetfilter.test.dto;

import javax.json.JsonObject;
import java.util.List;

public class AssetFilterDto {
    private String name;
    private String id;
    private List<JsonObject> filter;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<JsonObject> getFilter() {
        return filter;
    }

    public void setFilter(List<JsonObject> filter) {
        this.filter = filter;
    }

}
