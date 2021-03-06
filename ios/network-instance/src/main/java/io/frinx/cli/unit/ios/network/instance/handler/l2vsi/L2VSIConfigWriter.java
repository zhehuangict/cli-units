/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.network.instance.handler.l2vsi;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2VSIConfigWriter implements CliWriter<Config> {

    private final Cli cli;

    public L2VSIConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(InstanceIdentifier<Config> instanceIdentifier, Config config, WriteContext writeContext)
            throws WriteFailedException.CreateFailedException {
        // NOOP at this level
    }

    @Override
    public void updateCurrentAttributes(InstanceIdentifier<Config> id, Config dataBefore, Config dataAfter, WriteContext writeContext)
            throws WriteFailedException {
        // NOOP at this level
    }

    @Override
    public void deleteCurrentAttributes(InstanceIdentifier<Config> instanceIdentifier, Config config, WriteContext writeContext)
            throws WriteFailedException.DeleteFailedException {
        // NOOP at this level
    }
}
