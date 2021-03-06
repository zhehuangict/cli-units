/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ospf.handler;

import static io.frinx.openconfig.network.instance.NetworInstance.DEFAULT_NETWORK_NAME;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.handlers.ospf.OspfWriter;
import io.frinx.cli.io.Cli;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class GlobalConfigWriter implements OspfWriter<Config> {

    private static final String TEMPLATE = "configure terminal\n" +
            "router ospf {$ospf}" + "{.if ($vrf) } vrf {$vrf}{/if}" +
            "\n" +
            "{.if ($config.router_id) } router-id {$config.router_id.value}\n{.else}no router-id\n{/if}" +
            "end";

    private Cli cli;

    public GlobalConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributesForType(InstanceIdentifier<Config> instanceIdentifier, Config config,
                                              WriteContext writeContext) throws WriteFailedException {

        String vrfName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        String protocolName = instanceIdentifier.firstKeyOf(Protocol.class).getName();

        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(TEMPLATE,
                        "ospf", protocolName,
                        "vrf", vrfName.equals(DEFAULT_NETWORK_NAME) ? null : vrfName,
                        "config", config));
    }

    @Override
    public void updateCurrentAttributesForType(InstanceIdentifier<Config> id, Config dataBefore, Config dataAfter,
                                               WriteContext writeContext) throws WriteFailedException {
        writeCurrentAttributesForType(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributesForType(InstanceIdentifier<Config> instanceIdentifier, Config config,
                                               WriteContext writeContext) throws WriteFailedException {

        String vrfName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        String protocolName = instanceIdentifier.firstKeyOf(Protocol.class).getName();

        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(TEMPLATE,
                        "ospf", protocolName,
                        "vrf", vrfName.equals(DEFAULT_NETWORK_NAME) ? null : vrfName));
    }
}
