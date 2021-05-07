/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.gcard.device.ui;

import java.util.Calendar;
import java.util.Iterator;
import javafx.application.Application;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MySQLAESCrypt;
import org.rmj.appdriver.SQLUtil;
import org.rmj.gcard.service.GCLoadInfo;
import org.rmj.webcamfx.ui.ReadGCard;
import org.rmj.webcamfx.ui.Webcam;

/**
 *
 * @author sayso
 */
public class GCardDeviceQRBarcode implements GCardDevice{
    String psCardNmbr = "";
    String psMessagex = "";
    boolean pbOnlinex = false;
    GRider poGRider;
    JSONObject poDetail = null;
    JSONObject poTransx = null;
    boolean pbLoadOthers = true;

    @Override
    public boolean setCardNo(String fsCardNmbr) {
        psMessagex = "Setting of Card No for QR Code Reader User Interface is not allowed";
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
        
        String lsQRData = "";
        if (!pbLoadOthers){
            ReadGCard instance = new ReadGCard();
        
            Application.launch(instance.getClass());
        
            lsQRData = instance.getGCardInfo();
        }
        else
            lsQRData = Webcam.getQRValue();
        
        if(lsQRData.isEmpty() || lsQRData == null){
            psMessagex = "No QR code value was read.";
            return false;
        }

                
        //mac 2019.07.27
        lsQRData = MySQLAESCrypt.Decrypt(lsQRData, "20190625");
        //if there is an exception, the value is null
        if (lsQRData == null){
            psMessagex = "QR code data is null. Please re-scan.";
            return false;
        }
        
        String [] laQRData = lsQRData.split("»");

        //mac 2019.07.23
        //  replace all unwanted characters
        String lsCardNmbr = laQRData[2].replaceAll("[^0-9]", ""); //numeric only
        String lsIMEINoxx = laQRData[1].replaceAll("[^A-Za-z0-9]", ""); //alphanumeric only
        String lsUserIDxx = laQRData[3].replaceAll("[^A-Za-z0-9]", ""); //alphanumeric only
        String lsMobileNo = laQRData[4].replaceAll("[^0-9+]", ""); //numeric with plus sign 
        String lsModelCde = laQRData[7].replaceAll("[^A-Za-z0-9]", ""); //alphanumeric only
        long lnDevPoint = Double.valueOf(laQRData[6].replaceAll("[^0-9.]", "")).longValue(); //numeric with dot
        
        //check the authenticity of the QRCode...
        Calendar qrcal = Calendar.getInstance();

        qrcal.setTime(SQLUtil.toDate(laQRData[5], "yyyyMMddHHmmss")); 
        Calendar curcal = Calendar.getInstance();
        int diff = (int)java.time.Duration.between(curcal.toInstant(), qrcal.toInstant()).toMinutes();

        if(diff <= -5 || diff >= 5){
            psMessagex = "Expired QR Code information detected! Please recreate QR Code and try again...";
            return false;
        }
        
        if(!pbLoadOthers){
            poDetail = new JSONObject();
            poDetail.put("sCardNmbr", lsCardNmbr);
            poDetail.put("sIMEINoxx", lsIMEINoxx);
            poDetail.put("sUserIDxx", lsUserIDxx);
            poDetail.put("sMobileNo", lsMobileNo);
            poDetail.put("dQRDateTm", SQLUtil.dateFormat(qrcal.getTime(), SQLUtil.FORMAT_TIMESTAMP));
            poDetail.put("bIsOnline", true);
            poDetail.put("nDevPoint", lnDevPoint);
            poDetail.put("sModelCde", lsModelCde);
            if(laQRData.length == 9){
                poDetail.put("sTransNox", laQRData[8].replace("[^A-Za-z0-9]", ""));
            }
            else{
                poDetail.put("sTransNox", "");
            }
            
            return true;
        }

        //Get the GCard No
        String lsGCardNmbr = laQRData[2].replaceAll("[^\\d.]", "");
        //try to pull out data from the central server first
        JSONObject result = GCLoadInfo.GetCardInfoOnline(poGRider, lsGCardNmbr);
        String success = (String)result.get("result");
        if(success.equalsIgnoreCase("success")){
            pbOnlinex = true;
            poDetail = (JSONObject) result;
            poDetail.put("sIMEINoxx", lsIMEINoxx);
            poDetail.put("sUserIDxx", lsUserIDxx);
            poDetail.put("sMobileNo", lsMobileNo);
            poDetail.put("dQRDateTm", SQLUtil.dateFormat(qrcal.getTime(), SQLUtil.FORMAT_TIMESTAMP));
            poDetail.put("bIsOnline", true);
            poDetail.put("nDevPoint", lnDevPoint);
            poDetail.put("sModelCde", lsModelCde);
            if(laQRData.length == 9){
                poDetail.put("sTransNox", laQRData[8].replace("[^A-Za-z0-9]", ""));
            }
            else{
                poDetail.put("sTransNox", "");
            }
                
            return true;
        }
        
        //try to pull data from the local database
        result = GCLoadInfo.GetCardInfoOffline(poGRider, lsGCardNmbr);
        success = (String)result.get("result");
        if(success.equalsIgnoreCase("success")){
            pbOnlinex = false;
            poDetail = (JSONObject) result; //.get("detail")
            poDetail.put("sIMEINoxx", lsIMEINoxx);
            poDetail.put("sUserIDxx", lsUserIDxx);
            poDetail.put("sMobileNo", lsMobileNo);
            poDetail.put("dQRDateTm", SQLUtil.dateFormat(qrcal.getTime(), SQLUtil.FORMAT_TIMESTAMP));
            poDetail.put("bIsOnline", false);
            poDetail.put("nDevPoint", lnDevPoint);
            poDetail.put("sModelCde", lsModelCde);
            if(laQRData.length == 9){
                poDetail.put("sTransNox", laQRData[8].replace("[^A-Za-z0-9]", ""));
            }
            else{
                poDetail.put("sTransNox", "");
            }
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
            if (fsField.equals("nDevPoint"))
                if (String.valueOf(poDetail.get(fsField)).contains("?"))
                    return String.valueOf(poDetail.get(fsField)).replace("?", "");
                else
                    return poDetail.get(fsField);
            else
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
        return GCardDeviceFactory.DeviceType.QRCODE;
    }

    @Override
    public boolean write() {
        if(poTransx == null){
            return false;
        }

        String qrdata = (String) poTransx.get("SOURCE") 
                      + "»" + (String) poTransx.get("sOTPasswd")
                      + "»" + (String) poDetail.get("sIMEINoxx")
                      + "»" + (String) poDetail.get("sUserIDxx")
                      + "»" + (String) poDetail.get("sCardNmbr")
                      + "»" + (String) poDetail.get("sMobileNo");
        JSONArray artrans = (JSONArray) poTransx.get("DETAIL");
        Iterator<JSONObject> iterator = artrans.iterator();
        String trans = "";
        
        while (iterator.hasNext()) {
            JSONObject oJson = iterator.next();
            trans += ";" + (String) oJson.get("sTransNox")
                    + "@" + oJson.get("dTransact").toString()
                    + "@" + (String) oJson.get("sSourceNo")
                    + "@" + (String) oJson.get("sSourceCD")
                    + "@" + ((Double) oJson.get("nPointsxx")).longValue();
        }

        qrdata += "»" + trans.substring(1);
        
        System.out.println(qrdata);
        qrdata = qrdata.replaceAll("[^A-Za-z0-9+.»;@-]", "");
        qrdata = MySQLAESCrypt.Encrypt(qrdata, "20190625");
        String otp = (String) poTransx.get("sOTPasswd");
        
        String qrpin = Webcam.getPIN(qrdata);
        
        if (qrpin.equalsIgnoreCase(otp))
            return true;
        else {
            psMessagex = "QR code not matched from the transaction.";
            return false;
        }
    }

    @Override
    public void setTransData(JSONObject foJson) {
        poTransx = foJson;
    }

    @Override
    public boolean release() {
        return true;
    }
    
    @Override
    public void isLoadInfo(boolean load) {
        pbLoadOthers = load;
    }
}
