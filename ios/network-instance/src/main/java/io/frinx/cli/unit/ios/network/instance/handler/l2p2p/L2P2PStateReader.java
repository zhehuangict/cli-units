/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.network.instance.handler.l2p2p;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.common.CompositeReader;
import io.frinx.cli.unit.utils.CliOperReader;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.StateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.L2P2P;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2P2PStateReader implements CliOperReader<State, StateBuilder>,
        CompositeReader.Child<State, StateBuilder> {

    private Cli cli;

    public L2P2PStateReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<State> instanceIdentifier,
                                      @Nonnull StateBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        if (isP2P(instanceIdentifier, readContext)) {
            configBuilder.setName(instanceIdentifier.firstKeyOf(NetworkInstance.class).getName());
            configBuilder.setType(L2P2P.class);

            // TODO set other attributes i.e. description
        }
    }

    private boolean isP2P(InstanceIdentifier<State> id, ReadContext readContext) throws ReadFailedException {
        return L2P2PReader.getAllIds(id, readContext, cli, this).contains(id.firstKeyOf(NetworkInstance.class));
    }
}
