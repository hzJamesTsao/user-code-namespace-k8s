package hazelcast.platform.labs.payments;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.config.UserCodeNamespaceConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import hazelcast.platform.labs.payments.domain.Card;
import hazelcast.platform.labs.payments.domain.Names;
import java.util.HashMap;
import java.util.Map;

/**
 * Expects the following environment variables
 * <p>
 * CARD_COUNT The number of machines credit cards to load
 *
 */
public class PatchRefdata {
 
    public static void main(String []args){


        HazelcastInstance hzClient = HazelcastClient.newHazelcastClient();

        UserCodeNamespaceConfig ns = new UserCodeNamespaceConfig("card-ns");
        hzClient.getConfig().getNamespacesConfig().addNamespaceConfig(ns);

        IMap<String, Card> cardMap = hzClient.getMap(Names.CARD_MAP_NAME);
        IMap<String, String> systemActivitiesMap = hzClient.getMap(Names.SYSTEM_ACTIVITIES_MAP_NAME);

        systemActivitiesMap.put("PATCH_STATUS","STARTED");

        Object[] keys  = cardMap.keySet().toArray();

        Map<String, Card> batch = new HashMap<>();
        for(int i=0; i < keys.length; ++i){
            
            Card c = cardMap.get(keys[i]);
//            c.setLastTransaction(null);

            batch.put(c.getCardNumber(), c);
            int BATCH_SIZE = 1000;
            if (batch.size() == BATCH_SIZE){
                cardMap.putAll(batch);
                batch.clear();
            }
        }

        if (batch.size() > 0) cardMap.putAll(batch);

        System.out.println("Patched " + keys.length + " cards ");
        
        systemActivitiesMap.put("PATCH_STATUS","FINISHED");
        hzClient.shutdown();
    }
}
