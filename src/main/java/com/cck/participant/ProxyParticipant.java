package com.cck.participant;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;
import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.XmlConfigurable;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;
import org.jpos.space.SpaceUtil;
import org.jpos.transaction.Context;
import org.jpos.transaction.TransactionParticipant;
import org.jpos.util.Log;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;

import com.cck.pass.IRouter;
import com.cck.util.GeckoLogger;
import com.cck.util.ISOContextUtil;
import com.cck.util.ISOFieldExpression;

public class ProxyParticipant implements TransactionParticipant, XmlConfigurable, Configurable, LogSource {
	public static final String E_META = "meta";

	public static final String E_OPCODE = "opcode";

	public static final String E_BLOCK = "block";

	public static final String A_TYPE = "type";

	public static final String A_TIMEOUT = "timeout";

	public static final String A_VALUE = "value";

	public static final String A_ENDPOINT = "endpoint";

	public static final String A_FIELDS = "fields";

	public static final String A_CHANNEL = "channel";

	public static final String A_SKIP_ON = "skip-on";

	public static final String A_BEFORE_SEND = "before-send";

	public static final String BOTH_DIR = "0";

	public static final String DEST_SOURCE = "1";

	public static final String SOURCE_DEST_DIR = "2";

	public static final String RSP_MGR_NAME = "rsp-mgr";

	private Map<String, Block> blockers = new HashMap<>();

	private String messageRouterName = null;

	private long holdSkipCounter = 90000L;

	private long delayBeforeDrop = 0L;

	private boolean isLog = true;

	private Log log;

	public void setConfiguration(Configuration cfg) throws ConfigurationException {
		this.messageRouterName = cfg.get("msg-router");
		this.holdSkipCounter = cfg.getLong("hold-skip", 90000L);
		this.delayBeforeDrop = cfg.getLong("drop-delay", 0L);
		this.isLog = cfg.getBoolean("is-log", true);
	}

	public void setConfiguration(Element e) throws ConfigurationException {
		List<Element> elementBlocks = e.getChildren(E_BLOCK);
		for (Element elementBlock : elementBlocks)
			constructBlock(elementBlock);
	}

	private void constructBlock(Element elementBlock) {
		String endPoint = elementBlock.getAttributeValue(A_ENDPOINT);
		if (endPoint != null && !endPoint.isEmpty()) {
			String fields = elementBlock.getAttributeValue(A_FIELDS);
			if (fields != null && !fields.isEmpty())
				try {
					Block block = new Block(endPoint, fields);
					List<Element> opCodes = elementBlock.getChildren(E_OPCODE);
					for (Element opCode : opCodes) {
						ProxyOpCode proxyOpCode = constructProxyOpCode(opCode);
						if (proxyOpCode != null)
							block.add(proxyOpCode);
					}
					this.blockers.put(endPoint, block);
				} catch (Exception e) {
					GeckoLogger.error(e, this.log, "Invalid expressing %s",  fields);
				}
		}
	}

	private ProxyOpCode constructProxyOpCode(Element element) {
		String value = element.getAttributeValue(A_VALUE);
		if (value != null && !value.isEmpty()) {
			int skipOn;
			String type = element.getAttributeValue(A_TYPE);
			String rawDelay = element.getAttributeValue(A_TIMEOUT);
			long timeout = 0L;
			try {
				timeout = Long.parseLong(rawDelay);
			} catch (Exception exception) {
			}
			if (type == null || type.isEmpty())
				type = ProxyOpCode.TYPE_DISMISS;
			ProxyOpCode proxyOpCode = new ProxyOpCode(value, type);
			try {
				skipOn = Integer.parseInt(element.getAttributeValue(A_SKIP_ON));
			} catch (Exception e) {
				skipOn = -1;
			}
			proxyOpCode.setSkipOn(skipOn);
			if ( ProxyOpCode.TYPE_DELAY.equals(proxyOpCode.getType())) {
				proxyOpCode.setDelay(timeout);
			} else if (ProxyOpCode.TYPE_DROP.equals(proxyOpCode.getType())) {
				String channel = element.getAttributeValue(A_CHANNEL);
				proxyOpCode.setSourceChannel("source".equalsIgnoreCase(channel));
				boolean beforeSend = false;
				try {
					beforeSend = Boolean.parseBoolean(element.getAttributeValue(A_BEFORE_SEND));
				} catch (Exception e) {
					beforeSend = false;
				}
				proxyOpCode.setBeforeSend(beforeSend);
			}
			return proxyOpCode;
		}
		return null;
	}

	private IRouter getMessageRouter(String name) {
		Object obj = NameRegistrar.getIfExists(name);
		if (obj == null)
			return null;
		if (obj instanceof IRouter)
			return (IRouter) obj;
		return null;
	}

	public int prepare(long id, Serializable context) {
		if (!(context instanceof Context)) {
			this.log.info("Cannot process incoming message, incoming message is not instance of Context");
			return 1;
		}
		Context ctx = (Context) context;
		return prepareImpl(id, ctx);
	}

	protected int prepareImpl(long id, Context ctx) {
		ISOMsg msg = ISOContextUtil.getInIsomsg(ctx);
		String endPoint = ISOContextUtil.getEndpoint(ctx);
		Block block = this.blockers.get(endPoint);
		if (block == null)
			block = this.blockers.get("*");
		String formattedRouterName = ISOContextUtil.getSessionString(ctx, "interchange", "");
		if (formattedRouterName.equals("")) {
			formattedRouterName = this.messageRouterName;
		} else {
			formattedRouterName = formattedRouterName + "_router";
		}
		IRouter messageRouter = getMessageRouter(formattedRouterName);
		if (messageRouter == null) {
			GeckoLogger.info(this.log, "Cannot find message router %s", formattedRouterName);
			return 1;
		}
		String source = ISOContextUtil.getSocketName(ctx);
		if (this.isLog)
			GeckoLogger.info(this.log, "Source socket name %s", source);
		if (block != null) {
			if (this.isLog)
				GeckoLogger.info(this.log, "Receive incoming message using endpoint block : %s ", block);
			int direction = messageRouter.getSocketOrigin(ISOContextUtil.getSocketName(ctx), msg);
			String opCode = block.constructOpCode(msg);
			if (this.isLog)
				GeckoLogger.info(this.log, "Using op code %s , direction %d", opCode, direction);
			ProxyOpCode proxyOpCode = block.retrieveProxyOpCode(opCode, direction);
			if (proxyOpCode != null) {
				GeckoLogger.info(this.log, "Using proxy : %s,%s,%d ", proxyOpCode.getCode(), proxyOpCode.getType(),
						Long.valueOf(proxyOpCode.getDelay()));
				if (ProxyOpCode.TYPE_DROP.equals(proxyOpCode.getType())) {
					if (proxyOpCode.isBeforeSend()) {
						dropConnection(messageRouter, this.log, source, msg, proxyOpCode.isSourceChannel());
					} else if (messageRouter.routeToDest(this.log, msg, source)) {
						ISOUtil.sleep(this.delayBeforeDrop);
						dropConnection(messageRouter, this.log, source, msg, proxyOpCode.isSourceChannel());
					} else {
						handleInvalidRoute(messageRouter, this.log, msg, source);
					}
					return 1;
				}
				boolean process = (proxyOpCode.skipOn == -1);
				if (!process) {
					String key = endPoint + "." + direction + "." + opCode;
					int counter = getCounter(key);
					process = (counter <= proxyOpCode.skipOn);
					if (!process) {
						SpaceUtil.wipe(SpaceFactory.getSpace(), key);
						GeckoLogger.info(this.log, "Reset key %s counter to 0", key);
					}
				}
				if (process) {
					if (ProxyOpCode.TYPE_DISMISS.equals(proxyOpCode.getType())) {
						LogEvent evt = this.log.createLogEvent("info");
						evt.addMessage("Dismiss message : ");
						evt.addMessage(msg);
						Logger.log(evt);
						return 1;
					}
					if ( ProxyOpCode.TYPE_DELAY.equals(proxyOpCode.getType())) {
						GeckoLogger.info(this.log, "Delay message %d for %d ms", Long.valueOf(id),
								Long.valueOf(proxyOpCode.getDelay()));
						ISOUtil.sleep(proxyOpCode.getDelay());
					} else if (ProxyOpCode.TYPE_DUPLICATE.equals(proxyOpCode.getType())) {
						if (messageRouter.routeToDest(this.log, msg, source)) {
							GeckoLogger.info(this.log, "Duplicate message tramission for %d", Long.valueOf(id));
						} else {
							GeckoLogger.info(this.log, "Cannot send duplicate message tramission for %d",
									Long.valueOf(id));
						}
					}
				}
			}
		} else {
			GeckoLogger.info(this.log, "Cannot find block");
		}
		if (!messageRouter.routeToDest(this.log, msg, source))
			handleInvalidRoute(messageRouter, this.log, msg, source);
		return 1;
	}

	public void setLogger(Logger logger, String realm) {
		this.log = new Log(logger, realm);
	}

	public String getRealm() {
		return this.log.getRealm();
	}

	public Logger getLogger() {
		return this.log.getLogger();
	}

	private int getCounter(String key) {
		Space sp = SpaceFactory.getSpace();
		Object skipCounter = sp.inp(key);
		Integer counter = null;
		if (skipCounter == null) {
			counter = 1;
		} else {
			counter = (Integer) skipCounter;
			counter++;
		}
		sp.out(key, counter, this.holdSkipCounter);
		return counter.intValue();
	}

	private void dropConnection(IRouter router, Log log, String source, ISOMsg msg, boolean isSourceChannel) {
		if (isSourceChannel) {
			router.drop(log, source);
		} else {
			router.dropDest(log, source, msg);
		}
	}

	private void handleInvalidRoute(IRouter messageRouter, Log log, ISOMsg msg, String source) {
		try {
			if (msg.isRequest()) {
				ISOMsg rsp = (ISOMsg) msg.clone();
				rsp.setResponseMTI();
				rsp.set(39, "G2");
				messageRouter.route(log, rsp, source);
			} else {
				GeckoLogger.info(log, "Not a request message do not send response ");
			}
		} catch (Exception e) {
			GeckoLogger.error(e, log, "Error when handling invalidate route ");
		}
	}

	private static final class Block {
		private String endPoint;

		private ISOFieldExpression expression;

		private Map<String, ProxyParticipant.ProxyOpCode> proxyOpCodes;

		public Block(String endPoint, String exprString) throws Exception {
			this.proxyOpCodes = new HashMap<>();
			this.expression = new ISOFieldExpression(exprString);
			this.endPoint = endPoint;
		}

		public void add(ProxyParticipant.ProxyOpCode proxyOpCode) {
			this.proxyOpCodes.put(proxyOpCode.getCode(), proxyOpCode);
		}

		public String constructOpCode(ISOMsg msg) {
			return this.expression.construct(msg);
		}

		public ProxyParticipant.ProxyOpCode retrieveProxyOpCode(String opCode, int direction) {
			if (opCode != null) {
				ProxyParticipant.ProxyOpCode proxyOpCode = this.proxyOpCodes.get(direction + "." + opCode);
				if (proxyOpCode == null)
					proxyOpCode = this.proxyOpCodes.get("0." + opCode);
				return proxyOpCode;
			}
			return null;
		}

		public String getEndPoint() {
			return this.endPoint;
		}

		public void setEndPoint(String endPoint) {
			this.endPoint = endPoint;
		}
	}

	private static final class ProxyOpCode implements Serializable {
		public static final int NEVER_SKIP = -1;

		private static final long serialVersionUID = 1L;

		private String code;

		private String type;

		private long delay;

		private int skipOn = -1;

		private boolean sourceChannel;

		private boolean beforeSend = false;

		public static final String TYPE_DISMISS = "dismiss";

		public static final String TYPE_DELAY = "delay";

		public static final String TYPE_DROP = "drop";

		public static final String TYPE_DUPLICATE = "duplicate";

		public ProxyOpCode(String code, String type) {
			setCode(code);
			setType(type);
		}

		public String getCode() {
			return this.code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getType() {
			return this.type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public long getDelay() {
			return this.delay;
		}

		public void setDelay(long delay) {
			this.delay = delay;
		}

		public int getSkipOn() {
			return this.skipOn;
		}

		public void setSkipOn(int skipOn) {
			this.skipOn = skipOn;
		}

		public boolean isSourceChannel() {
			return this.sourceChannel;
		}

		public void setSourceChannel(boolean sourceChannel) {
			this.sourceChannel = sourceChannel;
		}

		public boolean isBeforeSend() {
			return this.beforeSend;
		}

		public void setBeforeSend(boolean beforeSend) {
			this.beforeSend = beforeSend;
		}

		public int hashCode() {
			int prime = 31;
			int result = 1;
			result = 31 * result + ((this.code == null) ? 0 : this.code.hashCode());
			return result;
		}

		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ProxyOpCode other = (ProxyOpCode) obj;
			if (this.code == null) {
				if (other.code != null)
					return false;
			} else if (!this.code.equals(other.code)) {
				return false;
			}
			return true;
		}
	}
}