/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.gcard.device.ui;

import java.util.Iterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.SQLUtil;
import org.rmj.gcard.base.misc.GCEncoder;
import org.rmj.gcard.service.GCLoadInfo;

/**
 *
 * @author sayso
 */
public class GCardDeviceSmartCard implements GCardDevice{
    String psCardNmbr = "";
    String psMessagex = "";
    boolean pbOnlinex = false;
    GRider poGRider;
    JSONObject poDetail = null;
    JSONObject poTransx = null;
    boolean pbLoadOthers = true;
    
    @Override
    public boolean setCardNo(String fsCardNo) {
        psMessagex = "Setting of Card No for Smart Card Reader User Interface is not allowed";
        return false;
    }

    @Override
    public boolean read() {
        poDetail = null;
        pbOnlinex = false;

        //make sure that poGRider is set if load others is true
        if(pbLoadOthers){
            if(poGRider == null){
                psMessagex = "Invalid GRider driver detected!";
                return false;
            }
        }
        
        if(!GCEncoder.init()){
            psMessagex = "read: Can't Initialize card. " + GCEncoder.getErrMessage();
            return false;
        }//end: if(!GCEncoder.init())

        if(!GCEncoder.connect()){
           psMessagex = "connectCard: Can't Connect card. " + GCEncoder.getErrMessage();
           return false;
        }//end: if(!GCEncoder.connect())

        String lsPin1 =  String.valueOf(((String) GCEncoder.read(GCEncoder.RESERVED3)).getBytes()[0]);
        String lsPin2 =  String.valueOf(((String) GCEncoder.read(GCEncoder.RESERVED5)).getBytes()[0]);

        String lsGCardNmbr = (String) GCEncoder.read(GCEncoder.CARD_NUMBER);

        if(!GCEncoder.verifyPSC(lsPin1, lsPin2)){
           psMessagex = "connectCard: Unable to verify pin number " + lsGCardNmbr + "!";
           return false;
        }//end: if(!GCEncoder.verifyPSC(lsPin1, lsPin2))

        //release device
        //GCEncoder.disconnect();
        
        long points = (long) GCEncoder.read(GCEncoder.POINTS);
        
        if(!pbLoadOthers){
            poDetail = new JSONObject();
            poDetail.put("sCardNmbr", lsGCardNmbr);
            poDetail.put("sIMEINoxx", "");
            poDetail.put("sUserIDxx", "");
            poDetail.put("sMobileNo", "");
            poDetail.put("dQRDateTm", "");
            poDetail.put("bIsOnline", true);
            poDetail.put("nDevPoint", points);
            poDetail.put("sModelCde", "");
            return true;
        }
        
        //try to pull out data from the central server first
        JSONObject result = GCLoadInfo.GetCardInfoOnline(poGRider, lsGCardNmbr);
        String success = (String)result.get("result");
        if(success.equalsIgnoreCase("success")){
            //mac 2019.07-24
            //if it is digital, this should not continue
            if ("1".equals((String) result.get("cDigitalx"))){
                psMessagex = "G-Card was converted as digital. Please use Guanzon App to load your card.";
                return false;
            }
            
            pbOnlinex = true;
            poDetail = (JSONObject) result; //.get("detail")
            poDetail.put("sIMEINoxx", "");
            poDetail.put("sUserIDxx", "");
            poDetail.put("dQRDateTm", "");
            poDetail.put("bIsOnline", true);
            poDetail.put("nDevPoint", points);
            poDetail.put("sModelCde", "");
            return true;
        }
        
        //try to pull data from the local database
        result = GCLoadInfo.GetCardInfoOffline(poGRider, lsGCardNmbr);
        success = (String) result.get("result");
        if(success.equalsIgnoreCase("success")){
            //mac 2019.07-24
            //if it is digital, this should not continue
            if ("1".equals((String) result.get("cDigitalx"))){
                psMessagex = "G-Card was converted as digital. Please use Guanzon App to load your card.";
                return false;
            }
            
            pbOnlinex = false;
            poDetail = (JSONObject) result; //.get("detail")
            poDetail.put("sIMEINoxx", "");
            poDetail.put("sUserIDxx", "");
            poDetail.put("dQRDateTm", "");
            poDetail.put("bIsOnline", false);
            poDetail.put("nDevPoint", points);
            poDetail.put("sModelCde", "");
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
        return GCardDeviceFactory.DeviceType.SMARTCARD;
    }

    @Override
    public boolean write() {
        if(poTransx == null){
            return false;
        }
        
        String source = (String) poTransx.get("SOURCE");
        
        Long pointD = (long) 0.0;
        JSONArray artrans = (JSONArray) poTransx.get("DETAIL");
        Iterator<JSONObject> iterator = artrans.iterator();
        while (iterator.hasNext()) {
            JSONObject oJson = iterator.next();
            pointD += ((Double) oJson.get("nPointsxx")).longValue();
        }       
        
        if(source.equalsIgnoreCase("redemption") || source.equalsIgnoreCase("preorder")){
            pointD *= -1;
        }
        
        long points = (long) GCEncoder.read(GCEncoder.POINTS);
        GCEncoder.write(GCEncoder.POINTS, (long)(points + pointD));
        long newpoints = (long) GCEncoder.read(GCEncoder.POINTS);

        if(points == newpoints){
           psMessagex = "GCardDeviceSmartCard.write: Card " + "<GCard Number>" + " with Transaction No:  " + "<Trans No>" + " was not updated since card points in card was not changed. \n";
           psMessagex += "Card was not updated. Please capture card and report the incidence for checking...";
           return false;
        }
        
        return true;
    }

    @Override
    public void setTransData(JSONObject foJson) {
        poTransx = foJson;
    }

    @Override
    public boolean release() {
        if(!GCEncoder.disconnect()){
            psMessagex = GCEncoder.getErrMessage();
            return false;
        }
       
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
