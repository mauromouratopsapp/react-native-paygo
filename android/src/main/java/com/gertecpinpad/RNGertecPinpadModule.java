package com.gertecpinpad;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.content.Intent;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import br.com.gertec.exception.PPCGeneralException;
import br.com.gertec.pinpad.GPINpad;
import br.com.gertec.pinpad.GPPError;

import android.hardware.usb.UsbManager;
import android.widget.Toast;

import com.lib.android.usbserial.driver.UsbSerialDriver;
import com.lib.android.usbserial.driver.UsbSerialProber;


public class RNGertecPinpadModule extends ReactContextBaseJavaModule {
  private final ReactApplicationContext reactContext;
  public String screen_first    = "                ";
  public String screen_title    = "      IVE       ";
  public String screen_subtitle = "    TOPSAPP     ";
  public String screen_last     = "                ";

  public GPINpad ppcCommand;
  private static final String TAG = "USB";
  private static final boolean D = true;

  private Button buttonConnect;
  private Button buttonDisconnect;
  private Button buttonBip;
  private Button buttonSendText;
  private Button buttonClearText;
  private Button buttonKeyboard;
  private Button buttonCard;
  private Button buttonPayment;
  private Button buttonQRCODE;

  byte bTimeout = (byte) 60;
  byte bNetwork = (byte) 0;
  byte bAppType = (byte) 99;
  String sIniAmount = "000000000000";
  String sDate;
  String sHour;
  String sTimeStamp = "1234567892";
  AtomicInteger bCardType = new AtomicInteger();
  AtomicInteger bChipLastStatus = new AtomicInteger();
  AtomicInteger bAppTypeSelected = new AtomicInteger();
  AtomicInteger bNetworkSelected = new AtomicInteger();
  AtomicInteger bAIDIdx = new AtomicInteger();
  StringBuffer sTrk1 = new StringBuffer();
  StringBuffer sTrk2 = new StringBuffer();
  StringBuffer sTrk3 = new StringBuffer();
  StringBuffer sPAN = new StringBuffer();
  AtomicInteger bPANSeq = new AtomicInteger();
  StringBuffer sAppLabel = new StringBuffer();
  AtomicInteger bServCode = new AtomicInteger();
  StringBuffer sCardHolder = new StringBuffer();
  StringBuffer sApplExpir = new StringBuffer();
  AtomicInteger sCardExtNum = new AtomicInteger();
  StringBuffer sBalance = new StringBuffer();
  AtomicLong lIssuerCode = new AtomicLong();
  StringBuffer sAcquirerData = new StringBuffer();
  StringBuffer sData = new StringBuffer();
  public static String mascaraData = "dd/MM/yyyy", mascaraHora = "hh:mm:ss";
  public static String stringDataAtual = "", stringHoraAtual = "";
  public static Date dateAtual = null;
  public static SimpleDateFormat simpleDateFormata = null;
  public static String amount = "0.00";
  String sMessage = "";

  private ProgressDialog pDialog;
  public static int contador = 60;
  public static boolean controleThreadTeclado = false;
  public static AtomicInteger aiStatus = new AtomicInteger();


  private UsbManager manager;
  private UsbSerialDriver driver;

  public RNGertecPinpadModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNGertecPinpad";
  }


  @ReactMethod
  public void config(String title, String subtitle, Callback callback){
    int ltitle = title.length();
    int lsubtitle = subtitle.length();

    if(ltitle > 0 && lsubtitle > 0){
      screen_title = title;
      screen_subtitle = subtitle;
      callback.invoke(true);
    }else{
      callback.invoke(false);
    }
  }

  @ReactMethod
  public void initialize(Callback callback){
    final Activity activity = getCurrentActivity();

    try {
      ppcCommand = new GPINpad(this.reactContext);
      callback.invoke(true);
      System.out.println(ppcCommand);
    } catch (IOException e) {
      e.printStackTrace();
      callback.invoke(false);
    }

    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
      public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
          try {
            ppcCommand.PPC_Close();
          } catch (PPCGeneralException e) {
          }
        }
      }
    };

    IntentFilter filter = new IntentFilter();;
    filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
    filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
    this.reactContext.registerReceiver(mUsbReceiver, filter);
    onNewIntent(activity.getIntent());

  }

  @ReactMethod
  public void connect(Callback callback){
    try {
      Byte bInterface = 1;
      ppcCommand.PPC_Open(bInterface);
      ppcCommand.PPC_DisplayString_4Lines(0, screen_first, screen_title, screen_subtitle, screen_last);
      callback.invoke(true);
    }catch (PPCGeneralException e) {
      e.printStackTrace();
      callback.invoke(false);
    }
  }

  protected void onNewIntent(Intent intent) {
    final Activity activity = getCurrentActivity();
    //onNewIntent(intent);
    activity.setIntent(intent);

    String action = intent.getAction();

    if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
      manager = (UsbManager) activity.getSystemService(Context.USB_SERVICE);

      driver = UsbSerialProber.acquire(manager);
      if (driver != null) {
        try {
          ppcCommand.Set_USB(driver, manager);
        } catch (PPCGeneralException e) {
          if (D) {
            Log.e(TAG, "Set_USB@Error");
          }
          e.printStackTrace();
        }

      } else {
        if (D) {
          Log.e(TAG, "onCreate@Driver_Create_Error");
        }
      }
    }
  }

  @ReactMethod
  public void clearScreen(Callback callback){
    try {
      ppcCommand.PPC_LCD_Clear();
      ppcCommand.PPC_DisplayString_4Lines(0, screen_first, screen_title, screen_subtitle, screen_last);
      callback.invoke(true);
    }catch (PPCGeneralException e) {
      e.printStackTrace(  );
      callback.invoke(false);
    }
  }


  @ReactMethod
  public void normalScreen(String line1, String line2, String line3, String line4, String line5, String line6, String line7, String line8, Callback callback){
    Byte bClearScreen = 1;
    Byte bLine = 1;
    Byte bColumn = 1;
    String text = line1 + "\t100" + line2 + "\t200" + line3 + "\t300" + line4 + "\t400" + line5 + "\t500" + line6 + "\t600" + line7 + "\t700" + line8;
    try {
      ppcCommand.PPC_DisplayFormattedText(bClearScreen, bLine, bColumn, text);
      callback.invoke(true);
    }catch (PPCGeneralException e) {
      e.printStackTrace();
      callback.invoke(false);
    }
  }

  @ReactMethod
  public void largeScreen(String line1, String line2, String line3, String line4, Callback callback){
    try {
      ppcCommand.PPC_DisplayString_4Lines(0, line1, line2, line3, line4);
      callback.invoke(true);
    }catch (PPCGeneralException e) {
      e.printStackTrace();
      callback.invoke(false);
    }
  }

  @ReactMethod
  public void processingScreen(String text, Callback callback){
    try {
      ppcCommand.PPC_SetProcessPrompt(text);
      callback.invoke(true);
    }catch (PPCGeneralException e) {
      e.printStackTrace();
      callback.invoke(false);
    }
  }

  @ReactMethod
  public void processPayment(Callback callback){
    try {
      mascaraData = "yyMMdd";
      mascaraHora = "hhmmss";
      //Gets the current date and time
      dateAtual = new Date();
      //Formats the date
      simpleDateFormata = new SimpleDateFormat(mascaraData);
      stringDataAtual = simpleDateFormata.format(dateAtual);
      //Formats the hour
      simpleDateFormata = new SimpleDateFormat(mascaraHora);
      stringHoraAtual = simpleDateFormata.format(dateAtual);

      sDate = stringDataAtual;
      sHour = stringHoraAtual;
      //Pass any value to simulate a sale
      sIniAmount = sIniAmount.substring(0, sIniAmount.length() - amount.replace(",", "").length()) + amount.replace(",", "");
      //Command to start the transaction on the card
      ppcCommand.PPC_GetEMVCard(bTimeout, bNetwork, bAppType, sIniAmount, sDate, sHour, sTimeStamp, bCardType, bChipLastStatus,
              bAppTypeSelected, bNetworkSelected, bAIDIdx, sTrk1, sTrk2, sTrk3, sPAN, bPANSeq, sAppLabel, bServCode, sCardHolder, sApplExpir,
              sCardExtNum, sBalance, lIssuerCode, sAcquirerData);

      //If no error is because he could read the card
      try {

        Byte bPINStatus;
        Byte bPINLen;
        Byte sEncDataBlk = 17;
        Byte sKeyKSN = 21;

        ppcCommand.PPC_CapturePINBlock (
                4, // Between 4
                12, // and 12 digits
                0, // Clear all data when clear is pressed
                1, // PPC may finish PIN entry process automatically 300, // 300 seconds
                8, // Eight line prompt
                4, // Password on line four
                1, // Following text on line one
                "Por gentileza,", // Text in line one
                2, // Following text on line two
                "insira a senha:", // Text in line two
                3, // Following text on line three
                "", // Text in line three
                5, // Following text on line five
                "", // Text in line five
                0, // DES Encryption
                1, // Key in index 1
                3, // See remarks above
                "0000000000000000", // Key option 3 = length 16
                0, // ANSI 0 PIN Block standard
                "F", // F padding
                0, // CN 0
                "000000000000", // To not interfere in PIN Block
                5, // 5 seconds
                bPINStatus,
                bPINLen,
                sEncDataBlk,
                sKeyKSN);

        //Command to remove the card
        ppcCommand.PPC_RemoveCard(bTimeout, sMessage);
        //Send text read successfully for the display of mobi pin
        ppcCommand.PPC_EMVDisplayString("Processado com sucesso!");
        callback.invoke(true);
      } catch (PPCGeneralException e) {
        //Sends the text with error to display the mobi pin
        ppcCommand.PPC_EMVDisplayString(" Erro - " + e.getErrorCode());
        callback.invoke(false);
        //Shows the error in the application
      }
    } catch (Exception e) {

      callback.invoke(false);
      //TODO: handle exception
    }
  }



}
