package fish.focus.uvms.docker.validation.webgateway.dto;

import fish.focus.schema.exchange.v1.ExchangeLogStatusType;
import fish.focus.uvms.asset.client.model.mt.MobileTerminal;
import fish.focus.uvms.docker.validation.asset.assetfilter.test.dto.SanePollDto;

public class PollInfoDto {

    SanePollDto pollInfo;

    ExchangeLogStatusType pollStatus;

    MobileTerminal mobileTerminalSnapshot;

    public PollInfoDto() {
    }

    public PollInfoDto(SanePollDto pollInfo, ExchangeLogStatusType pollStatus) {
        this.pollInfo = pollInfo;
        this.pollStatus = pollStatus;
    }

    public SanePollDto getPollInfo() {
        return pollInfo;
    }

    public void setPollInfo(SanePollDto pollInfo) {
        this.pollInfo = pollInfo;
    }

    public ExchangeLogStatusType getPollStatus() {
        return pollStatus;
    }

    public void setPollStatus(ExchangeLogStatusType pollStatus) {
        this.pollStatus = pollStatus;
    }

    public MobileTerminal getMobileTerminalSnapshot() {
        return mobileTerminalSnapshot;
    }

    public void setMobileTerminalSnapshot(MobileTerminal mobileTerminalSnapshot) {
        this.mobileTerminalSnapshot = mobileTerminalSnapshot;
    }
}
