package kafeihu.zk.zkchat.proc;

import kafeihu.zk.bserver.proc.base.BaseProc;
import kafeihu.zk.bserver.proc.ResponseData;

/**
 * Created by zhangkuo on 2016/11/27.
 */
public class GetUserFriends extends BaseProc {
    public GetUserFriends(String mModuleName, String mProcId) {
        super(mModuleName, mProcId);
    }
    @Override
    public void init() throws Exception{
        super.init();

    }

    @Override
    protected Object doProc(Object reqData) throws Exception {

        String sql="select user from User where userid=?";


        ResponseData responseData = new ResponseData();
        String str="zk";
        System.out.println("qingqiu");
        responseData.setData(str.getBytes());
        return responseData;
    }
}
