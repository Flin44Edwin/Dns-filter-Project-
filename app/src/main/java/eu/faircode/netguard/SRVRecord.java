package eu.faircode.netguard;

import java.net.InetAddress;
import java.util.List;

class SRVRecord {

    public SRVRecord(DnsName host, int port, int priority, int weight, List<InetAddress> hostAddresses) {
    }

    public <DnsName> SRVRecord(DnsName host, int port, int priority, int weight, List<InetAddress> hostAddresses) {
    }
}
