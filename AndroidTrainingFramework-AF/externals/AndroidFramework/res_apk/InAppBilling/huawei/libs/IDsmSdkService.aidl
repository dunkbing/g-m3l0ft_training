package com.huawei.dsm.aidl;
import com.huawei.dsm.aidl.ICallback;


interface IDsmSdkService
{
    void getUserToken(String requestParam, ICallback iCallback);
    void orderProduct(String requestParam, ICallback iCallback);
}