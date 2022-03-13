import org.iota.jota.IotaAPI;
import org.iota.jota.dto.response.FindTransactionResponse;
import org.iota.jota.dto.response.GetNodeInfoResponse;
import org.iota.jota.dto.response.SendTransferResponse;
import org.iota.jota.dto.response.WereAddressesSpentFromResponse;
import org.iota.jota.model.Transaction;
import org.iota.jota.model.Transfer;
import org.iota.jota.utils.IotaAPIUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.iota.jota.utils.IotaAPIUtils;

public class main {
    private static String seed0="SEED99999999999999999999999999999999999999999999999999999999999999999999999999999";
    private static String seed1="HMXZHGKGHYKSFHYCLEP9ZUAEDQQ9SJBHGYCEOAUHZJCABUKKIV9VHKLQDPUHCQ99PGZXRWFK9JJJSLGVD";

    public static void GetBalanceFromStart(IotaAPI api,String seed, int start, int num){
        for(int i=start;i<num;i++){
            String address= IotaAPIUtils.newAddress(seed, 2, i, true, api.getCurl());
            long balance=api.getBalance(100,address);
            WereAddressesSpentFromResponse spent = api.wereAddressesSpentFrom(address);
            System.out.printf("%s: %d : %s\n",address,balance, Arrays.toString(spent.getStates()));
        }
    }

    public static void SendBalance(IotaAPI api, String soureSeed, String targetSeed, int balance){
        String targetAddress = IotaAPIUtils.newAddress(targetSeed, 2, 0,  true, api.getCurl());

        List<Transfer> transfers = new ArrayList<>();
        transfers.add(new Transfer(targetAddress, balance, "TESTMESSAGE", "TESTTAG"));

        SendTransferResponse res = api.sendTransfer(soureSeed, 2, 3, 9, transfers, null, null,
                false, true, null);
        List<Transaction> restx=res.getTransactions();
        for (Transaction tx : restx) {
            //System.out.printf("Transactino %s\nAddress %s\nValue %d\n", tx.getHash(), tx.getAddress(), tx.getValue());
        }
        System.out.println("send");
    }
    public static boolean hasEnoughBalance(IotaAPI api, String seed, int balance){
        int start=0;
        long rest=balance;
        while (true){
            String address= IotaAPIUtils.newAddress(seed, 2, start++, true, api.getCurl());
            long b=api.getBalance(100,address);
            if (b > 0) {
                WereAddressesSpentFromResponse spent = api.wereAddressesSpentFrom(address);
                if(!spent.getStates()[0]){
                    rest-=b;
                    if(rest<=0){
                        return true;
                    }
                }
            }
            if(start>1){
                FindTransactionResponse tx = api.findTransactionsByAddresses(address);
                if(tx.getHashes().length == 0 ){
                    return false;
                }
            }
        }
    }
    public static boolean isNodeSynced(IotaAPI api){
        GetNodeInfoResponse info = api.getNodeInfo();
        return info.getIsNodeSynced();
/*
        int latestSolidMilestoneIndex= info.getLatestSolidSubtangleMilestoneIndex();
        int latestMilestoneIndex=info.getLatestMilestoneIndex();
        return latestSolidMilestoneIndex>=latestMilestoneIndex-1;*/
    }

    public static void balanceTest(IotaAPI api){
        try{
            while(true) {
                /*
                while(!isNodeSynced(api)){
                    System.out.printf("waiting for node Synchronized...\n");
                    Thread.sleep(1000);
                }*/
                while(!hasEnoughBalance(api,seed0,100)){
                    //GetBalanceFromStart(api,seed0,0,10);
                    System.out.printf("waiting for last tx confirmed...\n");
                    Thread.sleep(2000);
                }
                SendBalance(api, seed0, seed1, 100);
                //break;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void NewAddressTest(IotaAPI api) throws InterruptedException {
            while(true){
                for(int i=1;;i++){
                    long total=0;
                    for(int j=0;j<i;j++){
                        long start=System.nanoTime();
                        String address = IotaAPIUtils.newAddress(seed0, 2, j, true, api.getCurl());
                        total+=System.nanoTime()-start;
                        Thread.sleep(1);
                    }
                    System.out.printf("%d %d ms\n",i,(total/i)/1000000);
                }
            }
    }
    public static void main(String[] args) throws InterruptedException {
        IotaAPI api = new IotaAPI.Builder().protocol("http").host("127.0.0.1").port(14265).build();
        //IotaAPI api = new IotaAPI.Builder().protocol("http").host("8.214.74.110").port(14265).build();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            //IotaAPIUtils.GetWatch().Stop();
            //api.GetWatch().PrintCountNumberAndRoundAverage();
        }));

        //NewAddressTest(api);
       balanceTest(api);
        //GetBalanceFromStart(api,seed0,0,30);
    }
}
