package com.nisith.smartchat.Notification.Model;

//ServerResponse structure must be like this
//this is returned by FCM Server
//    { "multicast_id": 108,
//            "success": 1,
//            "failure": 0,
//            "results": [
//        { "message_id": "1:08" }
//  ]
//    }

import java.util.List;

public class ServerResponse {
    private long multicast_id;
    public int success;
    public int failure;
    private List<MessageId> results;
    public ServerResponse(){

    }

    public ServerResponse(long multicast_id, int success, int failure, List<MessageId> results) {
        this.multicast_id = multicast_id;
        this.success = success;
        this.failure = failure;
        this.results = results;
    }

    public long getMulticast_id() {
        return multicast_id;
    }

    public int getSuccess() {
        return success;
    }

    public int getFailure() {
        return failure;
    }

    public List<MessageId> getResults() {
        return results;
    }


}
