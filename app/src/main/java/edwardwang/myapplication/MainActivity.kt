package edwardwang.myapplication

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import java.io.*
import java.net.InetAddress
import java.net.Socket
import java.nio.charset.Charset
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class MainActivity : AppCompatActivity() {

    var isConnected:Boolean = false
    //val testFileKey = "000102030405060708090A0B0C0D0E0F".toByteArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //widgets
        //setUpWidgets()
        cipherTest()
    }

    fun setUpWidgets()
    {
        val ipAddress = findViewById<EditText>(R.id.ipAddressInput)
        val port = findViewById<EditText>(R.id.portInput)
        val connToServer:Button = findViewById<Button>(R.id.connToServerButton)
        connToServer.setOnClickListener {
            val serverInfo:String = "IPAddress: "+ipAddress + " \tPort:" + port
            Log.i("ConnToServer",serverInfo)
            connectToServer(ipAddress.text.toString(), port.text.toString().toInt())
            //val intent = Intent(this, StreamDisplay::class.java);
            //startActivity(intent);
        }
        val disconnectButton = findViewById<Button>(R.id.disconnectButton)
        disconnectButton.setOnClickListener {

        }
    }

    fun cipherTest()
    {
        try
        {
            val serverOutputTextView:TextView = findViewById<TextView>(R.id.serverOutput)
            //break encrypted file into 16 byte/128 bit blocks
            val inputStream:InputStream = resources.openRawResource(R.raw.secret2)
            val readInEncrypt = inputStream.readBytes(16)
            var encrypt = ByteArray(16)
            for(i in 0..15) encrypt[i] = readInEncrypt[15-i]

            //get key
            val testFileKey = ByteArray(16)

            //0c0d0e0f  08090a0b  040506070  0010203
            /*
            for(i in 12..15)testFileKey[i-12] = i.toByte()
            for(i in 8..11) testFileKey[i-4] = i.toByte()
            for(i in 4..7) testFileKey[i+4] =  i.toByte()
            for(i in 0..3)testFileKey[i+12] =  i.toByte()
            */

            //secret2
            //00000001 00000000 00000000
            testFileKey[3] = 1.toByte()


            System.out.println("Size of encrypt file: "+encrypt.size)
/*
            var encrypt1 = ByteArray(16)
            var encrypt2 = ByteArray(16)
            var encrypt3 = ByteArray(16)
            var encrypt4 = ByteArray(16)

            for(i in 0..15) encrypt1[i] = encrypt[i]
            for(i in 16..31)encrypt2[i-16] = encrypt[i]
            for(i in 32..47)encrypt3[i-32] = encrypt[i]
            for(i in 48..57)encrypt4[i-48] = encrypt[i]
*/
            //set initialization vector
            val iv = ByteArray(16)
            //AES decrypts at 16 bytes at a given time
            val decryptedMessageByteArray = decrypt(iv, testFileKey, encrypt)
            val correctedDecryptedByteArray = ByteArray(16)
            for(i in 0..15) correctedDecryptedByteArray[i] = decryptedMessageByteArray[15-i]
            //val decryptedMessage2:String = decrypt(iv, testFileKey, encrypt2).toString()
            //val decryptedMessage3:String = decrypt(iv, testFileKey, encrypt3).toString()
            //val decryptedMessage4:String = decrypt(iv, testFileKey, encrypt4).toString()

            //serverOutputTextView.text = decryptedMessage1 + decryptedMessage2 + decryptedMessage3 + decryptedMessage4

            //convert byte array to char test
            /*
            var decryptedMessage:String? = ""
            for(i in 0..15)decryptedMessage += decryptedMessageByteArray[i].toChar()
            serverOutputTextView.text = decryptedMessage
            */

            ///*
            //convert char to ascii
            val charset = Charsets.US_ASCII
            serverOutputTextView.text = correctedDecryptedByteArray.toString(charset)
            //*/
        }catch (e: IOException)
        {
            e.printStackTrace()
        }
    }

    /**
     * Decrypt AES 128-bits block in 16 bytes blocks
     *
     * test file: testEncryptAES128.jpg
     * test key: 0x000102030405060708090A0B0C0D0E0F
     *
     * @param ivBytes         : initialization vector - 16 bytes for a 128-byte block
     * @param secretKeyBytes  : secret key            - 16 bytes
     * @param encrypted       : encrypted block
     * */
    fun decrypt(ivBytes:ByteArray, secretKeyBytes:ByteArray, encrypted:ByteArray):ByteArray
    {
        val initializationVector = IvParameterSpec(ivBytes)
        val secretKeySpec = SecretKeySpec(secretKeyBytes, "AES_128")
        val cipher:Cipher = Cipher.getInstance("AES/CBC/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, initializationVector)
        try{
            return cipher.doFinal(encrypted)
        }catch (e: IllegalBlockSizeException)
        {
            System.out.println("Illegal Block Size Exception")
            e.printStackTrace()
            var newEncrypted = ByteArray(16)
            for(i in 0..encrypted.size-1) newEncrypted[i] = encrypted[i]
            if(encrypted.size < 15) for(i in encrypted.size..15) newEncrypted[i] = 0
            return cipher.doFinal(newEncrypted)
        }
    }

    fun connectToServer(ipAddress:String, port:Int)
    {
        Thread({
            val servAddress = InetAddress.getByName(ipAddress)
            val socket = Socket(servAddress, port)
            val serverOutputTextView:TextView = findViewById<TextView>(R.id.serverOutput)
            try
            {
                val input = BufferedReader(InputStreamReader(socket.getInputStream()))
                runOnUiThread({
                    isConnected = true
                    serverOutputTextView.text = "Connected To Server"
                })

                Log.d("ConnectToServer","Begin Reading")

                var txt = ""
                while(true)
                {
                    if(input.ready())
                    {
                        Log.d("ConnectToServer","Ready to read from server.")

                        //saveStream()

                        txt = input.readLine()
                        if(txt != null)
                        {
                            runOnUiThread({
                                serverOutputTextView.text = txt
                            })
                            Log.d("ConnectToServer","Txt: " + txt)
                        }else{
                            Log.d("ConnectToServer","Text is null")
                            break
                        }
                    }
                }

                Log.d("ConnectToServer","Disconnect From Server")
                socket.close()

                runOnUiThread({
                    serverOutputTextView.text = "Disconnected To Server"
                    isConnected = false
                })
            }catch (e:Exception)
            {
                e.printStackTrace()
            }
        }).start()
    }

    fun saveStream()
    {

    }

}