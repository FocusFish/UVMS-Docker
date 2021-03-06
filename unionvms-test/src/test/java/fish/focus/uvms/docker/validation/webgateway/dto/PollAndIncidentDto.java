package fish.focus.uvms.docker.validation.webgateway.dto;

import fish.focus.uvms.incident.model.dto.IncidentDto;

public class PollAndIncidentDto {
    String pollId;

    IncidentDto incident;

    public PollAndIncidentDto() {
    }

    public PollAndIncidentDto(String pollId, IncidentDto incident) {
        this.pollId = pollId;
        this.incident = incident;
    }

    public String getPollId() {
        return pollId;
    }

    public void setPollId(String pollId) {
        this.pollId = pollId;
    }

    public IncidentDto getIncident() {
        return incident;
    }

    public void setIncident(IncidentDto incident) {
        this.incident = incident;
    }
}
