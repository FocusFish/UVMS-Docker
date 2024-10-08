package fish.focus.uvms.docker.validation.movement.model;

import fish.focus.schema.movement.v1.MovementPoint;
import fish.focus.schema.movement.v1.MovementSourceType;

import java.time.Instant;

public class MicroMovement {

    private MovementPoint location;

    private Double heading;

    private String guid;

    private Instant timestamp;

    private Double speed;

    private MovementSourceType source;

    public MicroMovement() {
    }

    public MovementPoint getLocation() {
        return location;
    }

    public void setLocation(MovementPoint location) {
        this.location = location;
    }

    public Double getHeading() {
        return heading;
    }

    public void setHeading(Double heading) {
        this.heading = heading;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public MovementSourceType getSource() {
        return source;
    }

    public void setSource(MovementSourceType source) {
        this.source = source;
    }
}
