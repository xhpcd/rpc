package com.xhpcd.rpc.client.cluster.loadBalance;

import com.xhpcd.rpc.client.cluster.LoadBalanceStrategy;
import com.xhpcd.rpc.client.provider.ServiceProvider;
import org.springframework.stereotype.Component;
import java.util.Comparator;
import java.util.List;



@Component
public class WeightRoundStrategy implements LoadBalanceStrategy {

    @Override
    public ServiceProvider select(List<ServiceProvider> list) {
        ServiceProvider serviceProvider = list.get(0);
        boolean isFirst = serviceProvider.isFirst();
        //计算总权重
        int totalWeight = list.stream().map(s->s.getWeight()).mapToInt(Integer::intValue).sum();
        //当前权重加上自身权重
        ServiceProvider taeget = null;
        if(isFirst){
            taeget = list.stream().max(Comparator.comparing(ServiceProvider::getCurrentWeight)).get();
            taeget.setCurrentWeight(taeget.getCurrentWeight() - totalWeight);
        }else {
            list.forEach(s->s.setCurrentWeight(s.getCurrentWeight()+s.getWeight()));
            taeget = list.stream().max(Comparator.comparing(ServiceProvider::getCurrentWeight)).get();
            taeget.setCurrentWeight(taeget.getCurrentWeight() - totalWeight);
        }
        return taeget;

    }
}
