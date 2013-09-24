/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package mecolabuc.nfcrole;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * An {@link Activity} which handles a broadcast of a new tag that the device
 * just discovered.
 */
public class TagViewer extends Activity {

	static final String TAG = "ViewTag";

	/**
	 * This activity will finish itself in this amount of time if the user
	 * doesn't do anything.
	 */
	static final int ACTIVITY_TIMEOUT_MS = 1 * 1000;

	SQLhelper sdb;
	SQLiteDatabase db;
	TextView mTitle;
	TagTrigger tagtrigger;
	LinearLayout mTagContent;
	PendingIntent mPendingIntent;
	IntentFilter ndef;
	IntentFilter[] mFilters;
	String[][] mTechLists;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sdb = new SQLhelper(this.getApplicationContext());
		db = sdb.getDatabase();
		tagtrigger = TagTrigger.getTagTrigger();
		
		mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		
		ndef = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        
        try {
            ndef.addDataType("*/*");
        } catch (MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        mFilters = new IntentFilter[] {
                ndef,
        };

        //// Setup a tech list for all NfcF tags
        mTechLists = new String[][] { new String[] { MifareClassic.class.getName() } };
        Intent i = getIntent();
        resolveIntent(i);
	}

	void resolveIntent(Intent intent) {
		// Parse the intent
		String action = intent.getAction();
		if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
			Tag tfi = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			byte[] empty0 = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
			int[] empty1 = new int[4];
			String[] hexID = new String[4];
			for(int i = 0;i<empty0.length;i++){
				
					empty1[i]=empty0[i];
					if(empty1[i]<0){
						empty1[i]+=256;
					}
					
					hexID[i] = Integer.toHexString(empty1[i]);
				
			}
			String final_hexID = hexID[3].toUpperCase()+hexID[2].toUpperCase()+hexID[1].toUpperCase()+hexID[0].toUpperCase();
			
			String cardID = final_hexID;
			try {
				byte[] bytesOfMessage = cardID.getBytes("UTF-8");
				MessageDigest md = MessageDigest.getInstance("MD5");
				byte[] thedigest = md.digest(bytesOfMessage);

				
				String val = toHex(thedigest);
				ContentValues values = new ContentValues();
				values.put("ID_Tag", val);
				
				db.insert("Role", null, values);
			} catch (UnsupportedEncodingException e1) {
				Log.e("NFCROLE", e1.getMessage());
				e1.printStackTrace();
			}
			catch (NoSuchAlgorithmException e) {
				Log.e("NFCROLE", e.getMessage());
				e.printStackTrace();
			}
			
			MifareClassic mfc = MifareClassic.get(tfi);
			byte[] data;
			
			try{
				mfc.connect();
				boolean auth = false;
				int secCount = mfc.getSectorCount();
				 int bCount = 0;
		            int bIndex = 0;
		            for(int j = 0; j < secCount; j++){
		                // 6.1) authenticate the sector
		                auth = mfc.authenticateSectorWithKeyA(j, MifareClassic.KEY_DEFAULT);
		                if(auth){
		                    // 6.2) In each sector - get the block count
		                    bCount = mfc.getBlockCountInSector(j);
		                    bIndex = 0;
		                    for(int i = 0; i < bCount; i++){
		                        bIndex = mfc.sectorToBlock(j);
		                        // 6.3) Read the block
		                        data = mfc.readBlock(bIndex);    
		                        // 7) Convert the data into a string from Hex format.                
		                        data.notifyAll();
		                        bIndex++;
		                    }
		                }else{ // Authentication failed - Handle it
		                    
		                }
		            }
			}
				
			
			catch(IOException e){
				Log.e("Read Exception", e.toString());
			}
			catch( IllegalStateException f){
				Log.e("Card Exception", f.toString());
			}
			
			MediaPlayer mp = MediaPlayer.create(this,
					R.raw.discovered_tag_notification);
			mp.start();
			tagtrigger.trigger(cardID);
			finish();

		} else {
			Log.e(TAG, "Unknown intent " + intent);
			finish();
			return;
		}
	}

	@Override
	public void onNewIntent(Intent intent) {
		setIntent(intent);
		resolveIntent(intent);
	}
	
	public static String toHex(byte[] bytes) {
	    BigInteger bi = new BigInteger(1, bytes);
	    return String.format("%0" + (bytes.length << 1) + "X", bi);
	}

}
