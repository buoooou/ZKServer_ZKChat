package kafeihu.zk.zkchat.proc;

import kafeihu.zk.base.proc.BaseProc;
import kafeihu.zk.base.socket.model.ResponseData;

/**
 * Created by zhangkuo on 2016/11/27.
 */
public class OpenQRPayProtocol extends BaseProc {
    public OpenQRPayProtocol(String mModuleName, String mProcId) {
        super(mModuleName, mProcId);
    }
    @Override
    public void init() throws Exception
    {
        super.init();

    }

    @Override
    protected Object doProc(Object reqData) throws Exception {
        ResponseData responseData = new ResponseData();
        String str="zk";
        System.out.println("qingqiu");
        responseData.setData(str.getBytes());
        return responseData;
    }
}
