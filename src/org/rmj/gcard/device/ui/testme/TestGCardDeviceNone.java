/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.gcard.device.ui.testme;

import java.util.Random;
import org.json.simple.JSONObject;
import org.rmj.gcard.device.ui.GCardDevice;
import org.rmj.gcard.device.ui.GCardDeviceFactory;

/**
 *
 * @author sayso
 */
public class TestGCardDeviceNone {
    public static void main(String[] args){
        GCardDevice gcdevice = GCardDeviceFactory.make(GCardDeviceFactory.DeviceType.NONE);
        gcdevice.setCardNo("0011800067804");
        if(!gcdevice.read()){
            System.out.println(gcdevice.getMessage());
        }
        
        System.out.println(getRandom(999999));
        
        JSONObject master = new JSONObject();
        master.put("result", "SUCCESS");
        
        JSONObject detail = new JSONObject();
        detail.put("name", "marlon");
        
        master.put("detail", detail);
        System.out.println(master.toJSONString());
                
        
    }
    
    public static int getRandom(int num){
        Random rand = new Random();
        return rand.nextInt(num) + 1;
    }
}

