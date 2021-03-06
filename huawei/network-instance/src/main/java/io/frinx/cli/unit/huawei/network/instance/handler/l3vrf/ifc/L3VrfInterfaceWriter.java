/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.huawei.network.instance.handler.l3vrf.ifc;

import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.handlers.network.instance.L3VrfListWriter;
import io.frinx.cli.io.Cli;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L3VrfInterfaceWriter implements L3VrfListWriter<Interface, InterfaceKey> {

    private final Cli cli;

    private static final String WRITE_TEMPLATE = "system-view\n" +
            "interface %s\n" +
            "ip binding vpn-instance %s\n" +
            "commit\n" +
            "return";

    private static final String DELETE_TEMPLATE = "system-view\n" +
            "interface %s\n" +
            "undo ip binding vpn-instance %s\n" +
            "commit\n" +
            "return";

    public L3VrfInterfaceWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributesForType(InstanceIdentifier<Interface> instanceIdentifier, Interface anInterface,
                                              WriteContext writeContext) throws WriteFailedException {
        final NetworkInstance networkInstance =
                writeContext.readAfter(RWUtils.cutId(instanceIdentifier, NetworkInstance.class)).get();

        blockingWriteAndRead(cli, instanceIdentifier, anInterface,
                f(WRITE_TEMPLATE,
                        anInterface.getId(),
                        networkInstance.getConfig().getName()));
    }

    @Override public void updateCurrentAttributesForType(InstanceIdentifier<Interface> id, Interface dataBefore,
                                                         Interface dataAfter, WriteContext writeContext) throws WriteFailedException {
        deleteCurrentAttributes(id, dataBefore, writeContext);
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributesForType(InstanceIdentifier<Interface> instanceIdentifier, Interface anInterface,
                                               WriteContext writeContext) throws WriteFailedException {
        NetworkInstanceKey networkInstanceKey = instanceIdentifier.firstKeyOf(NetworkInstance.class);

        blockingDeleteAndRead(cli, instanceIdentifier,
                f(DELETE_TEMPLATE,
                        anInterface.getId(),
                        networkInstanceKey.getName()));
    }
}
