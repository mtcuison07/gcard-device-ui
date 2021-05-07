/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rmj.gcard.device.ui;

import org.rmj.appdriver.constants.GCDeviceType;

/**
 *
 * @author sayso
 */
public class GCardDeviceFactory {
    public enum DeviceType {
        NONE, SMARTCARD, QRCODE, QRBARCODE;
    }       

    public static GCardDevice make(GCardDeviceFactory.DeviceType point){
        switch (point) {
        case SMARTCARD:
            return new GCardDeviceSmartCard();
        case QRCODE:            
            return new GCardDeviceQRCode();
        case QRBARCODE:
            return new GCardDeviceQRBarcode();
        default:
            return new GCardDeviceNone();
        }
    }
    
    
    public static String DeviceTypeID(GCardDeviceFactory.DeviceType fnValue){
        switch (fnValue){
            case NONE:
                return GCDeviceType.NONE;
            case SMARTCARD:
                return GCDeviceType.SMARTCARD;
            case QRCODE:
                return GCDeviceType.QRCODE;
            case QRBARCODE:
                return GCDeviceType.QRBARCODE;
            default:
                return GCDeviceType.UNKNOWN;
        }    
    }
    
    public static DeviceType DeviceTypeID(String fsValue){
        switch (fsValue){
            case GCDeviceType.NONE:
                return DeviceType.NONE;
            case GCDeviceType.SMARTCARD:
                return DeviceType.SMARTCARD;
            case GCDeviceType.QRCODE:
                return DeviceType.QRCODE;
            case GCDeviceType.QRBARCODE:
                return DeviceType.QRBARCODE;
            default:
                return null;
        }
    }
}
