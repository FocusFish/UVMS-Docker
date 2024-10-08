package fish.focus.uvms.docker.validation.asset;

import fish.focus.uvms.docker.validation.asset.assetfilter.test.dto.*;
import fish.focus.uvms.docker.validation.common.AbstractRest;

import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class AssetFilterTestHelper extends AbstractRest {

    /* AssetFilterDto */
    public static final String ASSET_FILTER_BASE_URL = "asset/rest/filter";

    public static AssetFilterDto createBasicAssetFilter() {
        String user = "test User";
        AssetFilterDto assetFilter = new AssetFilterDto();
        assetFilter.setName(user);
        assetFilter.setFilter(new ArrayList<>());
        return assetFilter;
    }

    public static AssetFilterQueryDto createBasicAssetFilterQuery(AssetFilterDto assetFilterDto) {
        AssetFilterQueryDto assetFilterQuery = new AssetFilterQueryDto();
        assetFilterQuery.setAssetFilter(assetFilterDto);
        assetFilterQuery.setInverse(false);
        assetFilterQuery.setValueType(AssetFilterValueType.STRING);
        assetFilterQuery.setType("GUID");
        return assetFilterQuery;
    }

    public static AssetFilterValueDto createBasicAssetFilterValue(AssetFilterQueryDto assetFilterQueryDto) {
        AssetFilterValueDto assetFilterValue = new AssetFilterValueDto();
        assetFilterValue.setAssetFilterQuery(assetFilterQueryDto);
        assetFilterValue.setOperator("<=");
        return assetFilterValue;
    }

    public static AssetFilterDto createAssetFilterWithQueryAndValuesDefault() {
        AssetFilterDto createdAssetFilter = createBasicAssetFilter();
        AssetFilterQueryDto createdAssetFilterQuery = createBasicAssetFilterQuery(createdAssetFilter);
        AssetFilterValueDto createdAssetFilterValue = createBasicAssetFilterValue(createdAssetFilterQuery);

        Set<AssetFilterValueDto> setOfAssetFilteVAlues = new HashSet<AssetFilterValueDto>();
        setOfAssetFilteVAlues.add(createdAssetFilterValue);
        createdAssetFilterQuery.setValues(setOfAssetFilteVAlues);
        Set<AssetFilterQueryDto> setOfAssetFilteQueries = new HashSet<AssetFilterQueryDto>();
        setOfAssetFilteQueries.add(createdAssetFilterQuery);

        createdAssetFilter = createAssetFilter(createdAssetFilter);
        return createdAssetFilter;
    }

    public static AssetFilterDto createAssetFilterDefault() {
        AssetFilterDto createdAssetFilter = createBasicAssetFilter();

        createdAssetFilter = createAssetFilter(createdAssetFilter);
        return createdAssetFilter;
    }

    public static AssetFilterQueryRestDto deserializeFilter(JsonObject filter) {
        return JSONB.fromJson(filter.toString(), AssetFilterQueryRestDto.class);
    }

    /* AssetFilter Integration */

    public static String getAssetFilterByGuid(String filterId) {
        return getWebTarget().path(ASSET_FILTER_BASE_URL).path(filterId).request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getValidJwtToken()).get(String.class);
    }

    public static AssetFilterDto createAssetFilter(AssetFilterDto assetFilter) {
        return getWebTarget().path(ASSET_FILTER_BASE_URL).path("createFilter").request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getValidJwtToken())
                .post(Entity.json(assetFilter), AssetFilterDto.class);
    }

    public static AssetFilterQueryDto createAssetFilterQuery(AssetFilterDto assetFilterforQuery,
                                                             AssetFilterQueryDto assetFilterQuery) {
        return getWebTarget().path(ASSET_FILTER_BASE_URL).path(assetFilterforQuery.getId().toString()).path("query")
                .request(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, getValidJwtToken())
                .post(Entity.json(assetFilterQuery), AssetFilterQueryDto.class);
    }

    public static AssetFilterValueDto createAssetFilterValue(AssetFilterQueryDto assetFilterQueryForValue,
                                                             AssetFilterValueDto assetFilterValue) {
        return getWebTarget().path(ASSET_FILTER_BASE_URL).path(assetFilterQueryForValue.getId().toString())
                .path("value").request(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, getValidJwtToken())
                .post(Entity.json(assetFilterValue), AssetFilterValueDto.class);
    }

    public static String updateAssetFilter(String assetFilterDto) {
        return getWebTarget().path(ASSET_FILTER_BASE_URL).request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getValidJwtToken()).put(Entity.json(assetFilterDto), String.class);
    }

    public static AssetFilterDto deleteAssetFilter(String filterId) {
        return getWebTarget().path(ASSET_FILTER_BASE_URL).path(filterId).request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getValidJwtToken()).delete(AssetFilterDto.class);
    }

    public static AssetFilterListDto getAssetFilterList() {
        return getWebTarget().path(ASSET_FILTER_BASE_URL).path("list").request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getValidJwtToken()).get(AssetFilterListDto.class);
    }

    public static String createAssetFilterFromJson(String json) {
        return getWebTarget().path(ASSET_FILTER_BASE_URL).request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getValidJwtToken()).post(Entity.json(json), String.class);
    }
}
