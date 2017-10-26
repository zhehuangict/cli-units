package io.frinx.cli.unit.ios.cdp.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.unit.utils.CliReader;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.lldp.rev160516.lldp._interface.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.lldp.rev160516.lldp._interface.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.lldp.rev160516.lldp._interface.top.interfaces._interface.State;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.lldp.rev160516.lldp._interface.top.interfaces._interface.StateBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceStateReader implements CliReader<State, StateBuilder> {

    // FIXME duplicate with COnfigReader

    @Nonnull
    @Override
    public StateBuilder getBuilder(@Nonnull InstanceIdentifier<State> instanceIdentifier) {
        return new StateBuilder();
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<State> instanceIdentifier, @Nonnull StateBuilder StateBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        StateBuilder.setName(instanceIdentifier.firstKeyOf(Interface.class).getName());
        StateBuilder.setEnabled(true);
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull State State) {
        ((InterfaceBuilder) builder).setState(State);
    }
}