/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.gcard.device.ui;

import java.util.Calendar;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.agentfx.ui.ValidateOTPx;
import org.rmj.appdriver.agentfx.ui.showFXDialog;
import org.rmj.gcard.service.GCLoadInfo;
import javafx.application.Application;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agentfx.ui.ValidateOTP;

/**
 *
 * @author sayso
 */
public class GCardDeviceNone implements GCardDevice{
    String psCardNmbr = "";
    String psMessagex = "";
    boolean pbOnlinex = false;
    GRider poGRider;
    JSONObject poDetail = null;
    JSONObject poTransx = null;
    boolean pbLoadOthers = true;
    
    @Override
    public boolean setCardNo(String fsCardNmbr) {
        psCardNmbr = fsCardNmbr.replace("-", "");
        return true;
    }

    @Override
    public boolean read() {
        poDetail = null;
        pbOnlinex = false;
        
        if(poGRider == null){
            psMessagex = "Invalid GRider driver detected!";
            return false;
        }

        //try to pull out data from the central server first
        JSONObject result = GCLoadInfo.GetCardInfoOnline(poGRider, psCardNmbr);
        String success = (String)result.get("result");
        
        Calendar calendar = Calendar.getInstance();
        
        if(success.equalsIgnoreCase("success")){
            pbOnlinex = true;
            poDetail = (JSONObject) result;
            poDetail.put("sIMEINoxx", "");
            poDetail.put("sUserIDxx", "");
            poDetail.put("dQRDateTm", SQLUtil.dateFormat(calendar.getTime(), SQLUtil.FORMAT_TIMESTAMP));
            poDetail.put("bIsOnline", true);
            poDetail.put("nDevPoint", Double.valueOf(poDetail.get("nTotPoint").toString()).longValue());
            return true;
        }
        
        //try to pull data from the local database
        result = GCLoadInfo.GetCardInfoOffline(poGRider, psCardNmbr);
        success = (String)result.get("result");
        if(success.equalsIgnoreCase("success")){
            pbOnlinex = false;
            poDetail = (JSONObject) result;
            poDetail.put("sIMEINoxx", "");
            poDetail.put("sUserIDxx", "");
            poDetail.put("dQRDateTm", SQLUtil.dateFormat(calendar.getTime(), SQLUtil.FORMAT_TIMESTAMP));
            poDetail.put("bIsOnline", false);
            poDetail.put("nDevPoint", Double.valueOf(poDetail.get("nTotPoint").toString()).longValue());
            return true;
        }
        
        JSONObject err = (JSONObject)result.get("error");
        psMessagex = (String)err.get("message");
        return false;
    }

    @Override
    public void setGRider(GRider foGRider) {
        poGRider = foGRider;
    }

    @Override
    public Object getCardInfo(String fsField) {
        if(poDetail != null){
            return poDetail.get(fsField);
        }
        else{
            return null;
        }
    }
    
    //mac 2020.03.17
    @Override
    public boolean setCardInfo(String fsValue){
        try {
            JSONParser loParse = new JSONParser();
            
            poDetail = (JSONObject) loParse.parse(fsValue);
            return true;
        } catch (ParseException ex) {
            ex.printStackTrace();
            psMessagex = ex.getMessage();
            return false;
        }
    }
    //mac 2020.03.17
    @Override
    public JSONObject getCardInfo(){
        return poDetail;
    }

    @Override
    public String getMessage() {
        return psMessagex;
    }

    @Override
    public GCardDeviceFactory.DeviceType UIDeviceType() {
        return GCardDeviceFactory.DeviceType.NONE;
    }

    @Override
    public boolean write() {        
        try {
            ValidateOTP loOTP = new ValidateOTP();
            loOTP.setGRider(poGRider);
            loOTP.setTransaction(poTransx);

            javafx.application.Application.launch(loOTP.getClass());

            if (!loOTP.isOkay()){                                
                psMessagex = "OTP validation failed.";
                return false;
            }
        } catch (Exception ex) {
            if (!showFXDialog.validateOTP(poGRider, poTransx)){
                psMessagex = "OTP validation failed.";
                return false;
            }
        }
        
        return true;
    }

    @Override
    public void setTransData(JSONObject foJson) {
        poTransx = foJson;
    }

    @Override
    public boolean release() {
        //clear g-card properties
        System.setProperty("app.card.connected", "");
        System.setProperty("app.gcard.no", "");
        System.setProperty("app.gcard.holder", "");
        System.setProperty("app.card.no", "");
        System.setProperty("app.device.type", "");
        System.setProperty("app.device.data", "");
        System.setProperty("app.client.id", "");
        System.setProperty("app.gcard.online", "");
        
        return true;
    }
    
    @Override
    public void isLoadInfo(boolean load) {
        pbLoadOthers = load;
    }
}
