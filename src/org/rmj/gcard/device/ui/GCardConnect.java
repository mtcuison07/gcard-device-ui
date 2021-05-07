/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.gcard.device.ui;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.json.simple.JSONObject;
import org.rmj.appdriver.GRider;
import org.rmj.appdriver.MiscUtil;
import org.rmj.appdriver.SQLUtil;
import org.rmj.appdriver.agentfx.ui.showFXDialog;
import org.rmj.appdriver.constants.GCDeviceType;
import org.rmj.appdriver.constants.GCardStatus;

/**
 *
 * @author Mac
 */
public class GCardConnect {
    private GRider oApp;
    private GCardDeviceFactory.DeviceType nDeviceType;
    private String sMessage;
    
    public GCardConnect(GRider foApp){
        oApp = foApp;
        nDeviceType = GCardDeviceFactory.DeviceType.NONE;
    }
    
    public void setDeviceType(GCardDeviceFactory.DeviceType fnValue){
        nDeviceType = fnValue;
    }
    
    public String getMessage(){
        return sMessage;
    }
    
    private void setMessage(String fsValue){
        sMessage = fsValue;
    }
    
    public boolean Connect(){
        switch (nDeviceType){
            case NONE:
                if (System.getProperty("app.card.no").isEmpty()){
                    setMessage("No G-Card account to connect.");
                } else{
                    GCardDeviceNone nodevice = new GCardDeviceNone();
                    nodevice.setGRider(oApp);
                    if (nodevice.setCardNo(System.getProperty("app.card.no"))){
                        if (nodevice.read()){
                            System.setProperty("app.gcard.no", (String) nodevice.getCardInfo("sGCardNox"));
                            System.setProperty("app.gcard.holder", (String) nodevice.getCardInfo("sCompnyNm"));
                            System.setProperty("app.card.no", (String) nodevice.getCardInfo("sCardNmbr"));
                            System.setProperty("app.gcard.mobile", (String) nodevice.getCardInfo("sMobileNo"));
                            System.setProperty("app.device.data", nodevice.getCardInfo().toJSONString());
                            System.setProperty("app.device.type", GCDeviceType.NONE);
                            System.out.println("Device type is set to " + GCDeviceType.NONE);
                            System.setProperty("app.card.connected", "1");
                            System.setProperty("app.gcard.online", String.valueOf(nodevice.getCardInfo("bIsOnline")));
                            System.setProperty("app.client.id", (String) nodevice.getCardInfo("sClientID"));
                            return true;
                        }
                    }
                }
                System.setProperty("app.card.connected", "0");
                return false;
            case SMARTCARD:
                GCardDeviceSmartCard smartcard = new GCardDeviceSmartCard();
                smartcard.setGRider(oApp);
                
                if (smartcard.read()){
                    System.setProperty("app.gcard.no", (String) smartcard.getCardInfo("sGCardNox"));
                    System.setProperty("app.gcard.holder", (String) smartcard.getCardInfo("sCompnyNm"));
                    System.setProperty("app.card.no", (String) smartcard.getCardInfo("sCardNmbr"));
                    System.setProperty("app.gcard.mobile", (String) smartcard.getCardInfo("sMobileNo"));
                    System.setProperty("app.device.data", smartcard.getCardInfo().toJSONString());
                    System.setProperty("app.device.type", GCDeviceType.SMARTCARD);
                    System.out.println("Device type is set to " + GCDeviceType.SMARTCARD);
                    System.setProperty("app.card.connected", "1");
                    System.setProperty("app.gcard.online", String.valueOf(smartcard.getCardInfo("bIsOnline")));
                    System.setProperty("app.client.id", (String) smartcard.getCardInfo("sClientID"));
                    return true;
                } else {
                    System.setProperty("app.card.connected", "0");
                    setMessage(smartcard.getMessage());
                    return false;
                }
                
                //System.setProperty("app.device.type", GCDeviceType.SMARTCARD);
                //System.out.println("Device type is set to " + GCDeviceType.SMARTCARD);
                //System.setProperty("app.card.connected", "1");
                //return true;
            case QRCODE:
                GCardDeviceQRCode qrcode = new GCardDeviceQRCode();
                qrcode.setGRider(oApp);
                
                if (qrcode.setCardNo(System.getProperty("app.card.no"))){
                    if (qrcode.read()){       
                        System.setProperty("app.gcard.no", (String) qrcode.getCardInfo("sGCardNox"));
                        System.setProperty("app.gcard.holder", (String) qrcode.getCardInfo("sCompnyNm"));
                        System.setProperty("app.card.no", (String) qrcode.getCardInfo("sCardNmbr"));
                        System.setProperty("app.gcard.mobile", (String) qrcode.getCardInfo("sMobileNo"));
                        System.setProperty("app.device.data", qrcode.getCardInfo().toJSONString());
                        System.setProperty("app.device.type", GCDeviceType.QRCODE);
                        System.out.println("Device type is set to " + GCDeviceType.QRCODE);
                        System.setProperty("app.card.connected", "1");
                        System.setProperty("app.gcard.online", String.valueOf(qrcode.getCardInfo("bIsOnline")));
                        System.setProperty("app.client.id", (String) qrcode.getCardInfo("sClientID"));
                        return true;
                    } else {
                        System.setProperty("app.card.connected", "0");
                        setMessage(qrcode.getMessage());
                        return false;
                    }
                }
            case QRBARCODE:
                GCardDeviceQRCode barcode = new GCardDeviceQRCode();
                barcode.setGRider(oApp);
                
                if (barcode.read()){         
                    System.setProperty("app.gcard.no", (String) barcode.getCardInfo("sGCardNox"));
                    System.setProperty("app.gcard.holder", (String) barcode.getCardInfo("sCompnyNm"));
                    System.setProperty("app.card.no", (String) barcode.getCardInfo("sCardNmbr"));
                    System.setProperty("app.gcard.mobile", (String) barcode.getCardInfo("sMobileNo"));
                    System.setProperty("app.device.data", barcode.getCardInfo().toJSONString());
                    System.setProperty("app.device.type", GCDeviceType.QRBARCODE);
                    System.out.println("Device type is set to " + GCDeviceType.QRBARCODE);
                    System.setProperty("app.card.connected", "1");
                    System.setProperty("app.gcard.online", String.valueOf(barcode.getCardInfo("bIsOnline")));
                    System.setProperty("app.client.id", (String) barcode.getCardInfo("sClientID"));
                    return true;
                } else {
                    System.setProperty("app.card.connected", "0");
                    setMessage(barcode.getMessage());
                    return false;
                }
        }
        
        System.setProperty("app.card.connected", "1");
        return true;
    }
    
    public boolean Disconnect(){
        initValues();
        return true;
    }
    
    public boolean SearchMaster(String fsIndex, Object foValue){
        if (nDeviceType != GCardDeviceFactory.DeviceType.NONE && 
            nDeviceType != GCardDeviceFactory.DeviceType.QRCODE){
            setMessage("This procedure is not suitable for the selected device type.");
            return false;
        }
                
        if (fsIndex.equalsIgnoreCase("sCardNmbr") ||
            fsIndex.equalsIgnoreCase("sClientNm") ||
            fsIndex.equalsIgnoreCase("sGCardNox")){
            
            initValues();
            return searchClient(fsIndex, (String) foValue);
        }
        
        setMessage("Invalid search index detected.");
        return false;
    }
        
    private boolean searchClient(String fsFieldNm, String fsValue){
        System.out.println("Inside searchClient");
        fsValue = fsValue.trim();
        
        if(fsValue.trim().length() == 0){
            setMessage("Nothing to process!");
            return false;
        }

        String lsSQL = getSQL_Client();
        
        if(fsFieldNm.equalsIgnoreCase("sCardNmbr")){
            lsSQL = MiscUtil.addCondition(lsSQL, "a.sCardNmbr LIKE " + SQLUtil.toSQL(fsValue));
        } else if(fsFieldNm.equalsIgnoreCase("sClientNm")){
            lsSQL = MiscUtil.addCondition(lsSQL, "CONCAT(b.sLastName, ', ', b.sFrstName, IF(IFNULL(b.sSuffixNm, '') = '', CONCAT(' ', b.sSuffixNm), ' '), b.sMiddName) LIKE " + SQLUtil.toSQL(fsValue + "%"));
        } else if(fsFieldNm.equalsIgnoreCase("sGCardNox")){
            lsSQL = MiscUtil.addCondition(lsSQL, "a.sGCardNox LIKE " + SQLUtil.toSQL(fsValue));
        }

        //Create the connection object
        Connection loCon = oApp.getConnection();

        if(loCon == null){
            setMessage("Invalid connection!");
            return false;
        }

        boolean lbHasRec = false;
        Statement loStmt = null;
        ResultSet loRS = null;

        try {
            System.out.println("Before Execute");

            loStmt = loCon.createStatement();
            loRS = loStmt.executeQuery(lsSQL);

            System.out.println(lsSQL);
            if(!loRS.next()){
                setMessage("No record found...");
                initValues();
            }else{
                JSONObject loValue = showFXDialog.jsonBrowse(oApp, loRS, 
                                                                "Name»Address»Mobile No»Card Number", 
                                                                "sClientNm»xAddressx»b.sMobileNo»a.sCardNmbr");
              
                if (loValue != null){
                    //if (!"4".equals((String) loValue.get("cCardStat"))){
                    //    setMessage("G-Card is not activated. Please activate the card to continue...");
                    //} else {
                        System.setProperty("app.gcard.no", (String) loValue.get("sGCardNox"));
                        System.setProperty("app.card.no", (String) loValue.get("sCardNmbr"));
                        System.setProperty("app.gcard.holder", (String) loValue.get("sClientNm"));
                        System.setProperty("app.client.id", (String) loValue.get("sClientID"));
                        System.setProperty("app.device.type", (String) loValue.get("cDigitalx"));
                        lbHasRec = true;
                    //}
                } else
                    setMessage("No record selected...");
            }
            System.out.println("After Execute");
        } catch (SQLException ex) {
            ex.printStackTrace();
            setMessage(ex.getMessage());
        }
        finally{
            MiscUtil.close(loRS);
            MiscUtil.close(loStmt);
        }
        System.out.println(lbHasRec);
        return lbHasRec;
    }
    
    private String getSQL_Client(){
        return "SELECT" +
                    "  a.sCardNmbr" +
                    ", CONCAT(b.sLastName, ', ', b.sFrstName, ' ', IF(IFNull(b.sSuffixNm, '') = '', '', CONCAT(b.sSuffixNm, ' ')), b.sMiddName) sClientNm" +
                    ", CONCAT(b.sAddressx, ', ', c.sTownName, ' ', d.sProvName, ' ', c.sZippCode) xAddressx" +
                    ", a.dActivate dTransact" +
                    ", a.sGCardNox" +
                    ", a.cCardStat" + 
                    ", b.sMobileNo" +
                    ", a.sClientID" +
                    ", a.cDigitalx" +
                " FROM G_Card_Master a" +
                    ", Client_Master b" +
                    " LEFT JOIN TownCity c ON b.sTownIDxx = c.sTownIDxx" +
                    " LEFT JOIN Province d ON c.sProvIDxx = d.sProvIDxx" +
                " WHERE a.sClientID = b.sClientID" +
                    " AND a.cCardStat <= " + SQLUtil.toSQL(GCardStatus.ACTIVATED);
    }
    
    private void initValues(){
        System.setProperty("app.card.connected", "");
        System.setProperty("app.gcard.no", "");
        System.setProperty("app.gcard.holder", "");
        System.setProperty("app.card.no", "");
        System.setProperty("app.device.type", "");
        System.setProperty("app.device.data", "");
        System.setProperty("app.client.id", "");
        System.setProperty("app.gcard.online", "");
    }
}
