/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.network.instance;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericListReader;
import io.fd.honeycomb.translate.impl.read.GenericReader;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.ios.network.instance.handler.NetworkInstanceConfigReader;
import io.frinx.cli.unit.ios.network.instance.handler.NetworkInstanceInterfaceReader;
import io.frinx.cli.unit.ios.network.instance.handler.NetworkInstanceReader;
import io.frinx.cli.unit.ios.network.instance.handler.ProtocolConfigReader;
import io.frinx.cli.unit.ios.network.instance.handler.ProtocolReader;
import io.frinx.cli.unit.ios.network.instance.handler.ProtocolStateReader;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.NetworkInstancesBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ProtocolsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class IosNetworkInstanceUnit implements TranslateUnit {
    private static final Device IOS_ALL = new DeviceIdBuilder()
            .setDeviceType("ios")
            .setDeviceVersion("*")
            .build();

    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public IosNetworkInstanceUnit(@Nonnull final TranslationUnitCollector registry) {
        this.registry = registry;
    }

    public void init() {
        reg = registry.registerTranslateUnit(IOS_ALL, this);
    }

    public void close() {
        if (reg != null) {
            reg.close();
        }
    }

    @Override
    public Set<RpcService<?, ?>> getRpcs(@Nonnull Context context) {
        return Collections.emptySet();
    }

    @Override
    public void provideHandlers(@Nonnull ModifiableReaderRegistryBuilder rRegistry,
                                @Nonnull ModifiableWriterRegistryBuilder wRegistry,
                                @Nonnull Context context) {
        Cli cli = context.getTransport();
        provideReaders(rRegistry, cli);
    }

    private void provideReaders(@Nonnull ModifiableReaderRegistryBuilder rRegistry, Cli cli) {
        // VRFs
        rRegistry.addStructuralReader(IIDs.NETWORKINSTANCES, NetworkInstancesBuilder.class);
        rRegistry.add(new GenericListReader<>(IIDs.NE_NETWORKINSTANCE, new NetworkInstanceReader(cli)));
        rRegistry.add(new GenericReader<>(IIDs.NE_NE_CONFIG, new NetworkInstanceConfigReader(cli)));
        rRegistry.addStructuralReader(IIDs.NE_NE_INTERFACES, InterfacesBuilder.class);
        rRegistry.add(new GenericListReader<>(IIDs.NE_NE_IN_INTERFACE, new NetworkInstanceInterfaceReader(cli)));

        rRegistry.addStructuralReader(IIDs.NE_NE_PROTOCOLS, ProtocolsBuilder.class);
        rRegistry.add(new GenericListReader<>(IIDs.NE_NE_PR_PROTOCOL, new ProtocolReader(cli)));
        rRegistry.add(new GenericReader<>(IIDs.NE_NE_PR_PR_CONFIG, new ProtocolConfigReader()));
        rRegistry.add(new GenericReader<>(IIDs.NE_NE_PR_PR_STATE, new ProtocolStateReader()));
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228
                .$YangModuleInfoImpl.getInstance());
    }

    @Override
    public String toString() {
        return "IOS Network Instance (Openconfig) translate unit";
    }
}
