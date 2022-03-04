package fish.focus.uvms.docker.validation.webgateway.dto;

import fish.focus.uvms.asset.client.model.Note;
import fish.focus.uvms.incident.model.dto.IncidentDto;

public class NoteAndIncidentDto {

    private Note note;

    private IncidentDto incident;

    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        this.note = note;
    }

    public IncidentDto getIncident() {
        return incident;
    }

    public void setIncident(IncidentDto incident) {
        this.incident = incident;
    }
}
