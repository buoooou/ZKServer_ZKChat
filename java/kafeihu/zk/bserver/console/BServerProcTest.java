package kafeihu.zk.bserver.console;

import kafeihu.zk.base.client.impl.BServerSocketClient;
import kafeihu.zk.base.util.IoUtil;
import kafeihu.zk.base.util.MiscUtil;
import kafeihu.zk.base.util.XmlUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhangkuo on 2016/11/27.
 */
public class BServerProcTest {
    private Map<String, String> procTestPacketMap = new ConcurrentHashMap<String, String>();

    private String serverIp = "127.0.0.1";
    private int serverPort = 8010;

    public BServerProcTest() throws Exception
    {
        init();
    }

    private void init() throws Exception
    {
        String workingDir = System.getProperty("user.dir");
        String cfgFileName = workingDir + File.separator + "ProcTestCfg.xml";

        String xmlCfg = IoUtil.readTextFileContent(cfgFileName);

        serverIp = "127.0.0.1";
        try
        {
            serverIp = XmlUtil.getXmlElement("bserverIP", xmlCfg);
            InetAddress.getByName(serverIp);
        }
        catch (Exception exp)
        {
            serverIp = InetAddress.getLocalHost().getHostAddress();
        }
        serverPort = Integer.parseInt(XmlUtil.getXmlElement("bserverPort", xmlCfg));

        List<String> procList = XmlUtil.getAllXmlElements("proc", xmlCfg);
        for (String procCfg : procList)
        {
            String id = XmlUtil.getXmlElement("id", procCfg);
            if (MiscUtil.isEmpty(id))
            {
                throw new Exception("proc/id can not be empty");
            }
            String module = XmlUtil.getXmlElement("module", procCfg, "euser");
            if (MiscUtil.isEmpty(module))
            {
                throw new Exception("proc/module can not be empty");
            }
            String reqPacket = XmlUtil.getXmlElement("reqPacket4Test", procCfg);
            if (MiscUtil.isEmpty(reqPacket))
            {
                throw new Exception("proc/reqPacket can not be empty");
            }
            procTestPacketMap.put((id + module).toLowerCase(), reqPacket);
        }
    }

    private void doTest(String prid, String module)
    {
        //System.out.println("prid:" + prid + " module:" + module);

        String reqPacket = procTestPacketMap.get((prid + module).toLowerCase());
        if (null == reqPacket)
        {
            System.out.println("reqPacket4Test missing!");
            return;
        }

        try
        {
            BServerSocketClient client = new BServerSocketClient(serverIp, serverPort, 30000);

            client.setProcId(prid);
            client.setModuleName(module);

            byte[] resp = client.sendRecv(reqPacket.getBytes());
            System.out.println("response:" + new String(resp));
        }
        catch (Exception exp)
        {
            System.out.println("exception:" + exp.getMessage());
        }
    }

    public void doTest() throws Exception
    {
        System.out.println("BServer ip:" + serverIp + " port:" + serverPort);
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (true)
        {
            System.out.println();
            System.out.println("Input prid&module to kafeihu.zk.bserver.test. 'Q' or 'q' to quit!");

            System.out.print(">prid:");
            String prid = in.readLine();
            if (prid.equalsIgnoreCase("q"))
            {
                break;
            }
            System.out.print(">module:");
            String module = in.readLine();

            doTest(prid, module);
        }
    }

    public static void main(String[] args) throws Exception
    {
        BServerProcTest bspt = new BServerProcTest();

        bspt.doTest();
    }
}
