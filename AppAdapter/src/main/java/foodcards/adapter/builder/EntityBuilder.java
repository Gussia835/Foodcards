package foodcards.adapter.builder;

import foodcards.adapter.models.AppAdapterIoMsgs;
import foodcards.adapter.models.AppAdapterTrans;

public class EntityBuilder {

    public AppAdapterTrans buildTrans(String requestId, String jsonMessage) {
        return AppAdapterTrans.builder()
                .systemId("GRU")
                .requestId(requestId)
                .eventType("BALANCE")
                .data(jsonMessage)
                .status("WAIT")
                .build();
    }

    public AppAdapterIoMsgs buildIoMsg(Long transId, String jsonMessage) {

        return AppAdapterIoMsgs.builder()
                .transId(transId)
                .msgType("BALANCE")
                .dir("OUT")
                .msg(jsonMessage)
                .build();
    }


}
