/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.bgp.handler.neighbor;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.bgp.BgpReader;
import io.frinx.cli.io.Cli;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.AfiSafi;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.afi.safi.StateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.state.Prefixes;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.state.PrefixesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.AFISAFITYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV4UNICAST;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PrefixesReader implements BgpReader.BgpOperReader<Prefixes, PrefixesBuilder> {

    private final String SH_IPV4 = "show bgp ipv4 unicast summary | section %s";
    private final String SH_VPNV4 = "show bgp vpnv4 unicast all summary | section %s";

    private final Cli cli;

    public PrefixesReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Prefixes> instanceIdentifier,
                                             @Nonnull PrefixesBuilder prefixesBuilder,
                                             @Nonnull ReadContext readContext) throws ReadFailedException {
        Class<? extends AFISAFITYPE> afiKey = instanceIdentifier.firstKeyOf(AfiSafi.class).getAfiSafiName();
        String neighborIp = NeighborWriter.getNeighborIp(instanceIdentifier);
        String command = afiKey.equals(IPV4UNICAST.class) ? String.format(SH_IPV4, neighborIp) : String.format(SH_VPNV4, neighborIp);
        parsePrefixes(blockingRead(command, cli, instanceIdentifier, readContext), prefixesBuilder, neighborIp);
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull Prefixes prefixes) {
        ((StateBuilder) builder).setPrefixes(prefixes);
    }

    @VisibleForTesting
    public static void parsePrefixes(String output, PrefixesBuilder builder, String neighborIp) {
        String pfx = NeighborStateReader.parseState(output, neighborIp);
        if (StringUtils.isNumeric(pfx)) {
            builder.setReceived(Long.valueOf(pfx));
        }
    }
}
