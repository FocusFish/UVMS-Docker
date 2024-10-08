package fish.focus.uvms.docker.validation.asset.assetfilter.test.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AssetFilterListDto implements Serializable {

    private static final long serialVersionUID = 95262131085841494L;

    private Map<String, AssetFilterDto> savedFilters = new HashMap<>();

    public Map<String, AssetFilterDto> getSavedFilters() {
        return savedFilters;
    }

    public void setSavedFilters(Map<String, AssetFilterDto> savedFilters) {
        this.savedFilters = savedFilters;
    }
}
