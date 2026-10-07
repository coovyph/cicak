package com.cck.mock;

import javax.management.ObjectName;

import org.jdom2.Element;
import org.jpos.core.ConfigurationException;
import org.jpos.core.SimpleConfiguration;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.q2.Q2;
import org.jpos.q2.QFactory;
import org.jpos.q2.iso.QMUX;

public class QMUXMock extends QMUX {

    private QFactory dummFactory;
    private boolean connected;

    public QMUXMock(boolean connected) {
        super();
        try {
            setConfiguration(new SimpleConfiguration());
        } catch (Exception e) {
        }
        try {
            dummFactory = new QFactory(new ObjectName("test"), new Q2());
        } catch (Exception e) {
        }
        this.connected = connected;
    }

    @Override
    public QFactory getFactory() {
       return dummFactory;
    }

    private Element constructTagText(String tag, String value) {
        Element element = new Element(tag);
        element.addContent(value);
        return element;
    }

    @Override
    public void initService() throws ConfigurationException {
        try {
            super.initService();
        } catch (RuntimeException cfe) {
        }

    }

    @Override
    public Element getPersist() {
        Element element = new Element("mux");

        element.addContent(constructTagText("out", "alto_pos_in"));
        element.addContent(constructTagText("in", "alto_pos_out"));
        element.addContent(constructTagText("unhandled", "posqueue"));
        element.addContent(constructTagText("ready", "alto_pos.ready"));
        return element;
    }

    @Override
    public boolean isConnected() {
        return this.connected;
    }

    @Override
    public ISOMsg request(ISOMsg m, long timeout) throws ISOException {
        ISOMsg rspMsg = (ISOMsg) m.clone();
        rspMsg.setResponseMTI();
        rspMsg.set(39, "00");

        log.info(rspMsg);
        return rspMsg;
    }
}
