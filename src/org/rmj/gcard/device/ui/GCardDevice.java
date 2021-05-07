/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.gcard.device.ui;

import org.json.simple.JSONObject;
import org.rmj.appdriver.GRider;
import org.rmj.gcard.device.ui.GCardDeviceFactory.DeviceType;

/**
 *
 * @author sayso
 */
public interface GCardDevice {
    public boolean read();
    public boolean write();
    public boolean release();
    public void setTransData(JSONObject foJson);
    public boolean setCardNo(String fsCardNmbr);
    public void setGRider(GRider foRider);
    public Object getCardInfo(String fsField); 
    public String getMessage();
    public DeviceType UIDeviceType();
    public void isLoadInfo(boolean load);
    
    //mac 2020.03.17
    public boolean setCardInfo(String fsValue);
    public JSONObject getCardInfo();
}
