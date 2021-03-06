/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.mpls.handler;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnels_top.tunnels.TunnelKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnels_top.tunnels.tunnel.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.types.rev170824.LSPMETRICABSOLUTE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Address;

import java.util.List;
import java.util.stream.Collectors;

public class TunnelReaderTest {

    private static final String OUTPUT = "Wed Nov 29 09:38:07.070 UTC\n" +
        "tunnel-te50 is down, line protocol is down \n" +
        "tunnel-te55 is down, line protocol is down ";

    private static final String TUNNEL_OUTPUT = "Wed Nov 29 10:01:16.985 UTC\n" +
        "interface tunnel-te50\n" +
        " load-share 30\n" +
        " autoroute announce\n" +
        "  metric absolute 15\n" +
        " !\n" +
        " destination 192.168.1.50\n" +
        "!\n";

    @Test
    public void testIds() {
        List<TunnelKey> keys = TunnelReader.getTunnelKeys(OUTPUT);
        Assert.assertFalse(keys.isEmpty());
        Assert.assertEquals(Lists.newArrayList("50", "55"),
                keys.stream().map(TunnelKey::getName).collect(Collectors.toList()));
    }

    @Test
    public void testTunnelConfig() {
        ConfigBuilder cb = new ConfigBuilder();
        TunnelConfigReader.parseConfig(TUNNEL_OUTPUT, cb);
        Assert.assertEquals(LSPMETRICABSOLUTE.class, cb.getMetricType());
        Assert.assertTrue(cb.isShortcutEligible());
        Assert.assertEquals(15, cb.getMetric().intValue());

        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.cisco.rev171024.cisco.mpls.te.tunnel.top.cisco.mpls.te.extension.ConfigBuilder cb1 =
                new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.cisco.rev171024.cisco.mpls.te.tunnel.top.cisco.mpls.te.extension.ConfigBuilder();
        LoadShareConfigReader.parseConfig(TUNNEL_OUTPUT, cb1);
        Assert.assertEquals(30, cb1.getLoadShare().intValue());

        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnel.p2p_top.p2p.tunnel.attributes.ConfigBuilder cb2 =
                new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnel.p2p_top.p2p.tunnel.attributes.ConfigBuilder();

        P2pAttributesConfigReader.parseConfig(TUNNEL_OUTPUT, cb2);
        Assert.assertEquals(new Ipv4Address("192.168.1.50"), cb2.getDestination().getIpv4Address());
    }
}
