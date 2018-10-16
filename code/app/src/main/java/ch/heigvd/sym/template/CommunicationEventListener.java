package ch.heigvd.sym.template;

import java.util.EventListener;

public interface CommunicationEventListener extends EventListener {
    public boolean handleServerResponse(String response);
}
