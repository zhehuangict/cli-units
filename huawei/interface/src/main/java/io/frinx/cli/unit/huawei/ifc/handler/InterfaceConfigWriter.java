/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.huawei.ifc.handler;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.SoftwareLoopback;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class InterfaceConfigWriter implements CliWriter<Config> {

    private Cli cli;

    private static final Set<Class<? extends InterfaceType>> SUPPORTED_INTERFACE_TYPES = Sets.newHashSet();
    static {
        SUPPORTED_INTERFACE_TYPES.add(SoftwareLoopback.class);
    }

    private static final Set<Class<? extends InterfaceType>> PHYS_IFC_TYPES =
            Collections.singleton(EthernetCsmacd.class);


    public InterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                       @Nonnull Config data,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (isPhysicalInterface(data)) {
            throw new WriteFailedException.CreateFailedException(id, data,
                    new IllegalArgumentException("Cannot create physical interface"));
        }


        if (isSupportedInterface(data)) {
            validateIfcNameAgainstType(data);
            validateIfcConfiguration(data);
            writeOrUpdateInterface(id, data);
        } else {
            throw new WriteFailedException.CreateFailedException(id, data,
                    new IllegalArgumentException("Cannot create interface of type: " + data.getType()));
        }
    }

    private static boolean isSupportedInterface(Config config) {
        return SUPPORTED_INTERFACE_TYPES.contains(config.getType());
    }

    private void writeOrUpdateInterface(InstanceIdentifier<Config> id, Config data)
            throws WriteFailedException.CreateFailedException {

        blockingWriteAndRead(cli, id, data,
                "system-view",
                f("interface %s", data.getName()),
                data.getMtu() == null ? "undo mtu" : f("mtu %s", data.getMtu()),
                data.getDescription() == null ? "undo description" : f("description %s", data.getDescription()),
                data.isEnabled() != null && data.isEnabled() ? "undo shutdown" : "shutdown",
                "commit",
                "return");
    }

    private static void validateIfcConfiguration(Config data) {
        if (data.getType() == SoftwareLoopback.class) {
            checkArgument(data.getMtu() == null,
                    "Cannot configure mtu for interface %s of type SoftwareLoopback",
                    data.getName());
        }
    }

    private static final Pattern LOOPBACK_NAME_PATTERN = Pattern.compile("Loopback(?<number>[0-9]+)");

    private static void validateIfcNameAgainstType(Config data) {

        if (data.getType() == SoftwareLoopback.class) {
            Matcher matcher = LOOPBACK_NAME_PATTERN.matcher(data.getName());
            checkArgument(matcher.matches(),
                    "Loopback name must be in format: Loopback45, not: %s", data.getName());
        } else {
            throw new IllegalArgumentException("Cannot create interface of type: " + data.getType());
        }
    }

    private static boolean isPhysicalInterface(Config data) {
        return PHYS_IFC_TYPES.contains(data.getType());
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        try {
            checkArgument(dataBefore.getType().equals(dataAfter.getType()),
                    "Changing interface type is not permitted. Before: %s, After: %s",
                    dataBefore.getType(), dataAfter.getType());
        } catch (RuntimeException e) {
            throw new WriteFailedException.UpdateFailedException(id, dataBefore, dataAfter, e);
        }

        // we support update also for physical interfaces
        if (isSupportedInterface(dataAfter) || isPhysicalInterface(dataAfter)) {
            validateIfcConfiguration(dataAfter);
            writeOrUpdateInterface(id, dataAfter);
        } else {
            throw new WriteFailedException.UpdateFailedException(id, dataBefore, dataAfter,
                    new IllegalArgumentException("Unknown interface type: " + dataAfter.getType()));
        }
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (isPhysicalInterface(dataBefore)) {
            throw new WriteFailedException.DeleteFailedException(id,
                    new IllegalArgumentException("Cannot delete physical interface"));
        } else if (isSupportedInterface(dataBefore)) {
            deleteInterface(id, dataBefore);
        } else {
            throw new WriteFailedException.CreateFailedException(id, dataBefore,
                    new IllegalArgumentException("Unknown interface type: " + dataBefore.getType()));
        }
    }

    private void deleteInterface(InstanceIdentifier<Config> id, Config data)
            throws WriteFailedException.DeleteFailedException {
        blockingDeleteAndRead(cli, id,
                "system-view",
                f("undo interface %s", data.getName()),
                "commit",
                "return");
    }
}
