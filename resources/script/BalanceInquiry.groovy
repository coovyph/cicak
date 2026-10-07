import org.jpos.core.Configuration;
import org.jpos.iso.ISOMsg;
import org.jpos.transaction.Context;
import org.jpos.util.Log;

//#ndefine
//#include payment.groovy
void exec(Log log,Configuration cfg,Map<String,Object> out,
		Context ctx,ISOMsg msg,ISOMsg rspMsg){
	rspMsg.set(39,"00");
}
