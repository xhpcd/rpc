package com.xhpcd.rpc.client.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceProvider implements Serializable {
    private boolean first = true;
    private String serviceName;
    private String serverIp;
    private int rcpPort;
    private int netWorkPort;
    private long timeout;
    private int weight;
    private int currentWeight;
}
