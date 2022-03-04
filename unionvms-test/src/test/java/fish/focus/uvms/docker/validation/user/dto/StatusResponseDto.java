package fish.focus.uvms.docker.validation.user.dto;

public class StatusResponseDto {
    private int statusCode;
    private String statusMessage;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String message) {
        this.statusMessage = message;
    }
}
