package fish.focus.uvms.docker.validation.movement.model;

import fish.focus.schema.movement.asset.v1.VesselType;

public class ManualMovementDto {

    private MicroMovement movement;

    private VesselType asset;

    public MicroMovement getMovement() {
        return movement;
    }

    public void setMovement(MicroMovement movement) {
        this.movement = movement;
    }

    public VesselType getAsset() {
        return asset;
    }

    public void setAsset(VesselType asset) {
        this.asset = asset;
    }
}
