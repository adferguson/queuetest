package edu.brown.cs.queuetest;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPacketQueue;
import org.openflow.protocol.OFPhysicalPort;
import org.openflow.protocol.OFPort;
import org.openflow.protocol.OFQueueGetConfigRequest;
import org.openflow.protocol.OFQueueGetConfigReply;
import org.openflow.protocol.OFQueueProp;
import org.openflow.protocol.OFType;
import org.openflow.util.U16;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.IOFSwitchListener;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;

import net.floodlightcontroller.core.IFloodlightProviderService;
import java.util.ArrayList;
import net.floodlightcontroller.staticflowentry.IStaticFlowEntryPusherService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueueTestController implements IOFMessageListener, IOFSwitchListener, IFloodlightModule {

    protected IFloodlightProviderService floodlightProvider;
    protected static Logger logger;
    protected IStaticFlowEntryPusherService staticFlowEntryPusher;

    @Override
    public String getName() {
        return "QueueTester";
    }

    @Override
    public boolean isCallbackOrderingPrereq(OFType type, String name) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isCallbackOrderingPostreq(OFType type, String name) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l =
                new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IFloodlightProviderService.class);
        return l;
    }

    @Override
    public void init(FloodlightModuleContext context)
            throws FloodlightModuleException {
        floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
        logger = LoggerFactory.getLogger(QueueTestController.class);
        staticFlowEntryPusher = context.getServiceImpl(IStaticFlowEntryPusherService.class);
    }

    @Override
    public void startUp(FloodlightModuleContext context) {
        floodlightProvider.addOFSwitchListener(this);
        floodlightProvider.addOFMessageListener(OFType.QUEUE_GET_CONFIG_REPLY, this);    
    }

    @Override
    public net.floodlightcontroller.core.IListener.Command receive(
            IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
        
        logger.info("this message is from switch: " + sw.getId());

        switch (msg.getType()) {
        case QUEUE_GET_CONFIG_REPLY:
            System.out.println("Got a Queue Config Reply!!");
            OFQueueGetConfigReply reply = (OFQueueGetConfigReply) msg;
            List<OFPacketQueue> queues = reply.getQueues();

            System.out.println("This is the port: " + reply.getPortNumber());
            
            System.out.println("Number of queues: " + queues.size());
            
            for (OFPacketQueue queue : queues) {
                long qid = queue.getQueueId();
                List<OFQueueProp> props = queue.getProperties();

                System.out.println("    qid = " + qid);
                System.out.println("    num props = " + props.size());
                
                for (OFQueueProp prop : props) {
                    System.out.println("      type = " + prop.getType());
                    System.out.println("      rate = " + prop.getRate());
                }
                
            }
            
            break;
        default:
            System.out.println("a message unknow" + msg.getType());

        };
        
        return Command.CONTINUE;
    }

    @Override
    public void addedSwitch(IOFSwitch sw) {
        OFQueueGetConfigRequest m = new OFQueueGetConfigRequest();
        
        Collection<OFPhysicalPort> ports = sw.getPorts();
        
        for (OFPhysicalPort port : ports) {
            if (U16.f(port.getPortNumber()) >= U16.f(OFPort.OFPP_MAX.getValue())) {
                continue;
            }
            
            System.out.println("Sending a queue get config to: " + port.getPortNumber());
            
            m.setPortNumber(port.getPortNumber());
            
            try {
                sw.write(m, null);
            } catch (IOException e) {
                logger.error("Tried to write to switch {} but got {}", sw.getId(), e.getMessage());
            } 
        }
        
        sw.flush();
    }

    @Override
    public void removedSwitch(IOFSwitch sw) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void switchPortChanged(Long switchId) {
        // TODO Auto-generated method stub
        
    }
}