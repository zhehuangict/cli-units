package io.frinx.cli.ospf;

import static io.frinx.cli.unit.utils.ParsingUtils.NEWLINE;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.policy.types.rev160512.OSPF;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class OspfProtocolReader implements CliListReader<Protocol, ProtocolKey, ProtocolBuilder> {

    public static final Class<OSPF> TYPE = OSPF.class;
    private Cli cli;

    public OspfProtocolReader(Cli cli) {
        this.cli = cli;
    }

    private static final Pattern OSPF_NO_VRF = Pattern.compile("\\s*router ospf (?<id>[^\\s]+)\\s*");
    private static final Pattern OSPF_VRF = Pattern.compile("\\s*router ospf (?<id>[^\\s]+) vrf (?<vrf>[^\\s]+)\\s*");

    @Nonnull
    @Override
    public List<ProtocolKey> getAllIds(@Nonnull InstanceIdentifier<Protocol> instanceIdentifier, @Nonnull ReadContext readContext) throws ReadFailedException {
        String output = blockingRead("sh run | include ospf", cli, instanceIdentifier, readContext);
        String vrfId = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();

        // FIXME FIXME FIXME Use constant from NetworkInstance, no magic string here
        if (vrfId.equals("default")) {
            return ParsingUtils.parseFields(output, 0,
                    OSPF_NO_VRF::matcher,
                    matcher -> matcher.group("id"),
                    s -> new ProtocolKey(TYPE, s));
        } else {
            return NEWLINE.splitAsStream(output)
                    .map(String::trim)
                    // Only include those from VRF
                    .filter(s -> s.contains("vrf " + vrfId))
                    .map(OSPF_VRF::matcher)
                    .filter(Matcher::matches)
                    .map(matcher -> matcher.group("id"))
                    .map(s -> new ProtocolKey(TYPE, s))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<Protocol> list) {
        // NOOP
    }

    @Nonnull
    @Override
    public ProtocolBuilder getBuilder(@Nonnull InstanceIdentifier<Protocol> instanceIdentifier) {
        // NOOP
        return null;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Protocol> instanceIdentifier,
                                      @Nonnull ProtocolBuilder protocolBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        ProtocolKey key = instanceIdentifier.firstKeyOf(Protocol.class);
        if (key.getIdentifier().equals(TYPE)) {
            protocolBuilder.setName(key.getName());
            protocolBuilder.setIdentifier(key.getIdentifier());
            // FIXME set attributes
        }
    }
}