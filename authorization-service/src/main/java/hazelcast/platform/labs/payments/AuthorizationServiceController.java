package hazelcast.platform.labs.payments;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.UserCodeNamespaceConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.nio.serialization.genericrecord.GenericRecord;
import hazelcast.platform.labs.payments.domain.Names;
import hazelcast.platform.labs.payments.domain.Transaction;
import hazelcast.platform.labs.payments.domain.TransactionEntryProcessor;
import jakarta.annotation.PostConstruct;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthorizationServiceController {

    @PostConstruct
    public void init(){

        // -- client config coming from hazelcast-client.yaml
        //ClientConfig clientConfig = new ClientConfig();
        //clientConfig.setClusterName("hz-test");
        //clientConfig.getNetworkConfig().addAddress(new String[] {"127.0.0.1:5701"});

        // use the default instantiation process
        hz = HazelcastClient.newHazelcastClient(/*clientConfig*/);
        cardMap = hz.getMap(Names.CARD_MAP_NAME);

        // send the TransactionEntryProcessor and Transaction classes to the cluster
        UserCodeNamespaceConfig ns = new UserCodeNamespaceConfig("card-ns");
        ns.addClass(TransactionEntryProcessor.class, Transaction.class);
        hz.getConfig().getNamespacesConfig().addNamespaceConfig(ns);
    }
    private HazelcastInstance hz;
    private IMap<String, GenericRecord> cardMap;

    @PostMapping("/authorize")
    public String authorize(@RequestBody Transaction transaction){
        TransactionEntryProcessor tep = new TransactionEntryProcessor(transaction);
        return cardMap.executeOnKey(transaction.getCardNumber(), tep);
    }
}
