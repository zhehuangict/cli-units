/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.bgp.handler;

import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.handlers.bgp.BgpWriter;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.network.instance.NetworInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Global;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NeighborConfigWriter implements BgpWriter<Config> {

    private Cli cli;

    public NeighborConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributesForType(InstanceIdentifier<Config> id, Config data,
                                              WriteContext writeContext) throws WriteFailedException {
        final Global g = writeContext.readAfter(RWUtils.cutId(id, Bgp.class)).get().getGlobal();
        final String instName =
                NetworInstance.DEFAULT_NETWORK_NAME.equals(id.firstKeyOf(Protocol.class).getName()) ? "" :
                        "instance " + id.firstKeyOf(Protocol.class).getName();
        blockingWriteAndRead(cli, id, data,
                "configure terminal",
                f("router bgp %s %s", g.getConfig().getAs().getValue(), instName),
                f("neighbor %s", id.firstKeyOf(Neighbor.class).getNeighborAddress().getIpv4Address().getValue()),
                f("remote-as %s", data.getPeerAs().getValue()),
                data.isEnabled() ? "no shutdown" : "shutdown",
                data.getPeerGroup() != null ? f("use neighbor-group %s", data.getPeerGroup()) : "",
                "commit",
                "end");
    }

    @Override
    public void updateCurrentAttributesForType(InstanceIdentifier<Config> id, Config dataBefore, Config dataAfter,
                                               WriteContext writeContext) throws WriteFailedException {
        final Global g = writeContext.readAfter(RWUtils.cutId(id, Bgp.class)).get().getGlobal();
        final String instName =
                NetworInstance.DEFAULT_NETWORK_NAME.equals(id.firstKeyOf(Protocol.class).getName()) ? "" :
                        "instance " + id.firstKeyOf(Protocol.class).getName();
        blockingWriteAndRead(cli, id, dataAfter,
                "configure terminal",
                f("router bgp %s %s", g.getConfig().getAs().getValue(), instName),
                f("neighbor %s", id.firstKeyOf(Neighbor.class).getNeighborAddress().getIpv4Address().getValue()),
                f("remote-as %s", dataAfter.getPeerAs().getValue()),
                dataAfter.isEnabled() ? "no shutdown" : "shutdown",
                dataAfter.getPeerGroup() != null ? f("use neighbor-group %s", dataAfter.getPeerGroup()) : "",
                "commit",
                "end");
    }

    @Override
    public void deleteCurrentAttributesForType(InstanceIdentifier<Config> id, Config config, WriteContext writeContext)
            throws WriteFailedException {
        final Global g = writeContext.readBefore(RWUtils.cutId(id, Bgp.class)).get().getGlobal();
        final String instName =
                NetworInstance.DEFAULT_NETWORK_NAME.equals(id.firstKeyOf(Protocol.class).getName()) ? "" :
                        "instance " + id.firstKeyOf(Protocol.class).getName();
        blockingDeleteAndRead(cli, id,
                "configure terminal",
                f("router bgp %s %s", g.getConfig().getAs().getValue(), instName),
                f("no neighbor %s", id.firstKeyOf(Neighbor.class).getNeighborAddress().getIpv4Address().getValue()),
                "commit",
                "end");
    }
}
